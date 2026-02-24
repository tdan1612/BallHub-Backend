package com.ballhub.ballhub_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ZaloPayService {

    @Value("${zalopay.app_id}")
    private String appId;

    @Value("${zalopay.key1}")
    private String key1;

    @Value("${zalopay.endpoint}")
    private String endpoint;

    public String createOrder(Integer orderId, Double amount) {
        try {
            // 1. Tạo mã đơn hàng duy nhất của BallHub (Định dạng: YYMMDD_OrderID_Timestamp)
            String appTransId = getCurrentTimeString("yyMMdd") + "_" + orderId + "_" + System.currentTimeMillis();

            // 2. Gom dữ liệu để gửi sang ZaloPay
            Map<String, Object> order = new HashMap<>();
            order.put("app_id", Integer.parseInt(appId));
            order.put("app_trans_id", appTransId);
            order.put("app_time", System.currentTimeMillis());
            order.put("app_user", "BallHub User");
            order.put("amount", amount.longValue()); // Zalo yêu cầu số nguyên (VD: 696000)
            order.put("description", "Thanh toán đơn hàng #" + orderId + " tại BallHub");
            order.put("bank_code", ""); // Để trống để hiện danh sách chọn ngân hàng/ví
            order.put("item", "[]");
            order.put("embed_data", "{\"order_id\":\"" + orderId + "\"}");

            // 3. Tạo chữ ký bảo mật (MAC) theo chuẩn ZaloPay
            String data = order.get("app_id") + "|" + order.get("app_trans_id") + "|" + order.get("app_user") + "|"
                    + order.get("amount") + "|" + order.get("app_time") + "|" + order.get("embed_data") + "|"
                    + order.get("item");
            order.put("mac", createHmacSHA256(data, key1));

            // 4. Gọi API của ZaloPay
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(order, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, request, Map.class);

            // 5. Rút trích link thanh toán từ ZaloPay trả về
            Map<String, Object> result = response.getBody();
            if (result != null && (Integer) result.get("return_code") == 1) {
                return (String) result.get("order_url"); // Đây chính là link để văng ra màn hình quét QR
            }

            throw new RuntimeException("Lỗi từ ZaloPay: " + result.get("return_message"));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Hệ thống ZaloPay đang bảo trì, vui lòng thử lại sau!");
        }
    }

    // Hàm băm dữ liệu HMAC SHA256
    private String createHmacSHA256(String data, String key) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        hmacSHA256.init(secretKey);
        byte[] hash = hmacSHA256.doFinal(data.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // Hàm lấy thời gian chuẩn
    private String getCurrentTimeString(String format) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }
}