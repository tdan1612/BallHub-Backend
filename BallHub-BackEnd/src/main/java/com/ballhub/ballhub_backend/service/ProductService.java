package com.ballhub.ballhub_backend.service;

import com.ballhub.ballhub_backend.dto.request.product.*;
import com.ballhub.ballhub_backend.dto.reponse.product.*;
import com.ballhub.ballhub_backend.exception.BadRequestException;
import com.ballhub.ballhub_backend.exception.ResourceNotFoundException;
import com.ballhub.ballhub_backend.entity.*;
import com.ballhub.ballhub_backend.repository.*;
import com.ballhub.ballhub_backend.repository.spec.ProductSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private ProductImageRepository imageRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findByStatusTrue(pageable)
                .map(this::mapToListResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(ProductFilterRequest filter, Pageable pageable) {
        return productRepository.searchProducts(
                filter.getKeyword(),
                filter.getCategoryId(),
                filter.getBrandId(),
                pageable
        ).map(this::mapToListResponse);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(Integer id) {
        Product product = productRepository.findByProductIdAndStatusTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));
        return mapToDetailResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> filterProducts(
            List<String> categories,
            List<String> teams,
            List<String> sizes,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sort,
            Pageable pageable
    ) {

        categories = (categories == null || categories.isEmpty()) ? null : categories;
        teams = (teams == null || teams.isEmpty()) ? null : teams;
        sizes = (sizes == null || sizes.isEmpty()) ? null : sizes;

        Page<Product> pageData = productRepository.filterNativeShop(
                categories, teams, sizes,
                minPrice, maxPrice,
                sort, pageable
        );

        List<ProductResponse> list = pageData.getContent().stream()
                .map(this::mapToListResponse)
                .toList();

        return new org.springframework.data.domain.PageImpl<>(
                list,
                pageable,
                pageData.getTotalElements()
        );
    }



    public ProductDetailResponse createProduct(CreateProductRequest request) {
        // Validate category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));

        // Validate brand
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Thương hiệu không tồn tại"));

        // Create product
        Product product = Product.builder()
                .productName(request.getProductName())
                .description(request.getDescription())
                .category(category)
                .brand(brand)
                .status(true)
                .build();

        Product savedProduct = productRepository.save(product);

        // Create variants
        for (CreateVariantRequest variantReq : request.getVariants()) {
            createVariant(savedProduct, variantReq);
        }

        return mapToDetailResponse(savedProduct);
    }

    public ProductDetailResponse updateProduct(Integer id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        // Validate category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));

        // Validate brand
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Thương hiệu không tồn tại"));

        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setBrand(brand);

        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        Product updated = productRepository.save(product);
        return mapToDetailResponse(updated);
    }

    public void deleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        // Soft delete
        product.setStatus(false);
        productRepository.save(product);
    }

    // Variant methods
    private void createVariant(Product product, CreateVariantRequest request) {
        // Validate size
        Size size = sizeRepository.findById(request.getSizeId())
                .orElseThrow(() -> new ResourceNotFoundException("Size không tồn tại"));

        // Validate color
        Color color = colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new ResourceNotFoundException("Color không tồn tại"));

        // Check duplicate variant
        variantRepository.findByProductAndSizeAndColor(
                product.getProductId(), size.getSizeId(), color.getColorId()
        ).ifPresent(v -> {
            throw new BadRequestException("Variant này đã tồn tại");
        });

        String sku = request.getSku();
        if (sku == null || sku.trim().isEmpty()) {
            sku = generateSKU(product, size, color);
        }

        // Check SKU uniqueness
        if (variantRepository.existsBySku(sku)) {
            throw new BadRequestException("SKU đã tồn tại: " + sku);
        }

        if (request.getSku() != null && variantRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("SKU đã tồn tại");
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .size(size)
                .color(color)
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .sku(request.getSku())
                .status(true)
                .build();

        variantRepository.save(variant);
    }

    /**
     * Generate SKU tự động
     * Format: {BrandCode}-{ProductID}-{SizeCode}-{ColorCode}
     * Example: ADIDAS-1-M-RED
     */
    private String generateSKU(Product product, Size size, Color color) {
        String brandCode = product.getBrand() != null
                ? product.getBrand().getBrandName().toUpperCase().replaceAll("\\s+", "")
                : "BRAND";

        String productId = product.getProductId().toString();

        String sizeCode = size.getSizeName().toUpperCase();

        String colorCode = color.getColorName().toUpperCase().replaceAll("\\s+", "");

        return String.format("%s-%s-%s-%s", brandCode, productId, sizeCode, colorCode);
    }

    public VariantResponse updateVariant(Integer variantId, UpdateVariantRequest request) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant không tồn tại"));

        variant.setPrice(request.getPrice());
        variant.setDiscountPrice(request.getDiscountPrice());
        variant.setStockQuantity(request.getStockQuantity());

        if (request.getStatus() != null) {
            variant.setStatus(request.getStatus());
        }

        ProductVariant updated = variantRepository.save(variant);
        return mapToVariantResponse(updated);
    }

    public void deleteVariant(Integer variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant không tồn tại"));

        // Soft delete
        variant.setStatus(false);
        variantRepository.save(variant);
    }

    // Mapping methods
    private ProductResponse mapToListResponse(Product product) {

        List<ProductVariant> variants = product.getVariants().stream()
                .filter(v -> Boolean.TRUE.equals(v.getStatus()))
                .filter(v -> v.getStockQuantity() != null && v.getStockQuantity() > 0)
                .toList();

        BigDecimal minPrice = variants.stream()
                .map(ProductVariant::getFinalPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal maxPrice = variants.stream()
                .map(ProductVariant::getFinalPrice)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        String mainImage = product.getImages().stream()
                .filter(ProductImage::getIsMain)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null);

        return ProductResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getBrandId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getBrandName() : null)
                .mainImage(mainImage)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .build();
    }

    private ProductDetailResponse mapToDetailResponse(Product product) {
        List<VariantResponse> variants = product.getVariants().stream()
                .map(this::mapToVariantResponse)
                .collect(Collectors.toList());

        List<ProductImageResponse> images = product.getImages().stream()
                .map(this::mapToImageResponse)
                .collect(Collectors.toList());

        return ProductDetailResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getBrandId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getBrandName() : null)
                .variants(variants)
                .images(images)
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .build();
    }

    private VariantResponse mapToVariantResponse(ProductVariant variant) {
        return VariantResponse.builder()
                .variantId(variant.getVariantId())
                .productId(variant.getProduct().getProductId())
                .sizeId(variant.getSize().getSizeId())
                .sizeName(variant.getSize().getSizeName())
                .colorId(variant.getColor().getColorId())
                .colorName(variant.getColor().getColorName())
                .price(variant.getPrice())
                .discountPrice(variant.getDiscountPrice())
                .finalPrice(variant.getFinalPrice())
                .stockQuantity(variant.getStockQuantity())
                .sku(variant.getSku())
                .status(variant.getStatus())
                .build();
    }

    private ProductImageResponse mapToImageResponse(ProductImage image) {
        return ProductImageResponse.builder()
                .imageId(image.getImageId())
                .productId(image.getProduct().getProductId())
                .variantId(image.getVariant() != null ? image.getVariant().getVariantId() : null)
                .imageUrl(image.getImageUrl())
                .isMain(image.getIsMain())
                .build();
    }
}
