package com.ballhub.ballhub_backend.service;

import com.ballhub.ballhub_backend.dto.reponse.cart.CartItemResponse;
import com.ballhub.ballhub_backend.dto.reponse.cart.CartResponse;
import com.ballhub.ballhub_backend.dto.request.cart.AddToCartRequest;
import com.ballhub.ballhub_backend.dto.request.cart.UpdateCartItemRequest;
import com.ballhub.ballhub_backend.entity.Cart;
import com.ballhub.ballhub_backend.entity.CartItem;
import com.ballhub.ballhub_backend.entity.ProductVariant;
import com.ballhub.ballhub_backend.entity.User;
import com.ballhub.ballhub_backend.exception.BadRequestException;
import com.ballhub.ballhub_backend.exception.ResourceNotFoundException;
import com.ballhub.ballhub_backend.repository.CartItemRepository;
import com.ballhub.ballhub_backend.repository.CartRepository;
import com.ballhub.ballhub_backend.repository.ProductVariantRepository;
import com.ballhub.ballhub_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public CartResponse getCart(Integer userId) {
        Cart cart = getOrCreateCart(userId);
        return mapToResponse(cart);
    }

    public CartResponse addToCart(Integer userId, AddToCartRequest request) {
        // Get or create cart
        Cart cart = getOrCreateCart(userId);

        // Get variant
        ProductVariant variant = variantRepository.findByVariantIdAndStatusTrue(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        // Check stock
        if (!variant.hasStock(request.getQuantity())) {
            throw new BadRequestException("Không đủ tồn kho. Còn lại: " + variant.getStockQuantity());
        }

        // Add to cart
        cart.addItem(variant, request.getQuantity());
        Cart savedCart = cartRepository.save(cart);

        return mapToResponse(savedCart);
    }

    public CartResponse updateCartItem(Integer userId, Integer cartItemId, UpdateCartItemRequest request) {
        Cart cart = cartRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng không tồn tại"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item không tồn tại"));

        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {
            throw new BadRequestException("Cart item không thuộc về giỏ hàng của bạn");
        }

        // Check stock
        if (!cartItem.getVariant().hasStock(request.getQuantity())) {
            throw new BadRequestException("Không đủ tồn kho. Còn lại: " + cartItem.getVariant().getStockQuantity());
        }

        // Update quantity
        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        return mapToResponse(cart);
    }

    public CartResponse removeFromCart(Integer userId, Integer cartItemId) {
        Cart cart = cartRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng không tồn tại"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item không tồn tại"));

        // Verify ownership
        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {
            throw new BadRequestException("Cart item không thuộc về giỏ hàng của bạn");
        }

        // Remove item
        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        return mapToResponse(cart);
    }

    public void clearCart(Integer userId) {
        Cart cart = cartRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng không tồn tại"));

        cart.clearCart();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(Integer userId) {
        return cartRepository.findByUserUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();

                    return cartRepository.save(newCart);
                });
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItem> items = cart.getItems();
        if (items == null) {
            items = new java.util.ArrayList<>();
        }

        List<CartItemResponse> itemResponses = items.stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUser().getUserId())
                .items(itemResponses)
                .totalItems(cart.getTotalItems())
                .totalAmount(cart.getTotalAmount())
                .build();
    }

    private CartItemResponse mapToItemResponse(CartItem item) {
        ProductVariant variant = item.getVariant();
        String imageUrl = null;

        if (variant.getProduct() != null && variant.getProduct().getImages() != null) {
            imageUrl = variant.getProduct().getImages().stream()
                    .filter(img -> img.getIsMain() != null && img.getIsMain())
                    .findFirst()
                    .map(img -> img.getImageUrl())
                    .orElse(null);
        }

        return CartItemResponse.builder()
                .cartItemId(item.getCartItemId())
                .variantId(variant.getVariantId())
                .productName(variant.getProduct() != null ? variant.getProduct().getProductName() : null)
                .sizeName(variant.getSize() != null ? variant.getSize().getSizeName() : null)
                .colorName(variant.getColor() != null ? variant.getColor().getColorName() : null)
                .price(variant.getPrice())
                .finalPrice(variant.getFinalPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .imageUrl(imageUrl)
                .stockQuantity(variant.getStockQuantity())
                .build();
    }
}
