package com.Shopping.Shopping.controller.api;

import com.Shopping.Shopping.dto.ApiResponse;
import com.Shopping.Shopping.dto.OrderDTO;
import com.Shopping.Shopping.model.Orders;
import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.model.User;
import com.Shopping.Shopping.repository.OrdersRepository;
import com.Shopping.Shopping.repository.ProductRepository;
import com.Shopping.Shopping.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/payment")
public class ApiPaymentController {

    private final String razorpayKey;
    private final String razorpaySecret;
    private final OrdersRepository ordersRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ApiPaymentController(@Value("${razorpay.key}") String razorpayKey,
                               @Value("${razorpay.secret}") String razorpaySecret,
                               OrdersRepository ordersRepository,
                               UserRepository userRepository,
                               ProductRepository productRepository) {
        this.razorpayKey = razorpayKey;
        this.razorpaySecret = razorpaySecret;
        this.ordersRepository = ordersRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/buy-now/{productId}")
    public ResponseEntity<ApiResponse<BuyNowResponse>> buyNow(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
            }

            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

            User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            double amount = product.getPrice() * quantity;
            boolean needsAddress = user.getAddress() == null || user.getAddress().trim().isEmpty();

            BuyNowResponse response = new BuyNowResponse(product, quantity, amount, needsAddress);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to process buy now: " + e.getMessage()));
        }
    }

    @PostMapping("/buy-now/address")
    @Transactional
    public ResponseEntity<ApiResponse<String>> saveAddress(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
            }

            User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            user.setAddress(request.get("address"));
            userRepository.save(user);

            return ResponseEntity.ok(ApiResponse.success("Address saved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to save address: " + e.getMessage()));
        }
    }

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOrder(@RequestBody Map<String, Object> data) {
        try {
            int amount = (int) data.get("amount");
            RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);
            JSONObject options = new JSONObject();
            options.put("amount", amount);
            options.put("currency", "INR");
            options.put("receipt", "txn_" + System.currentTimeMillis());

            Order order = client.orders.create(options);
            Map<String, Object> response = new HashMap<>();
            response.put("id", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("key", razorpayKey);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create order: " + e.getMessage()));
        }
    }

    @PostMapping("/success")
    @Transactional
    public ResponseEntity<ApiResponse<String>> handlePaymentSuccess(
            @RequestBody Map<String, Object> data,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
            }

            User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            double amount = 500.0;
            if (data.containsKey("isBuyNow") && Boolean.TRUE.equals(data.get("isBuyNow"))) {
                Object productIdObj = data.get("productId");
                Object quantityObj = data.get("quantity");
                if (productIdObj != null && quantityObj != null) {
                    Long productId = Long.valueOf(productIdObj.toString());
                    Integer quantity = Integer.valueOf(quantityObj.toString());
                    Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                    amount = product.getPrice() * quantity;
                }
            } else {
                if (data.containsKey("amount")) {
                    amount = Double.parseDouble(data.get("amount").toString()) / 100.0;
                }
            }

            Orders order = new Orders();
            order.setRazorpayPaymentId(data.get("razorpay_payment_id").toString());
            order.setRazorpayOrderId(data.get("razorpay_order_id").toString());
            order.setRazorpaySignature(data.get("razorpay_signature").toString());
            order.setAmount((int)(amount * 100));
            order.setOrderDate(LocalDateTime.now());
            order.setUser(user);
            order.setEmail(user.getUsername());

            ordersRepository.save(order);
            return ResponseEntity.ok(ApiResponse.success("Payment successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to process payment: " + e.getMessage()));
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
            }

            User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            List<Orders> orders = ordersRepository.findByUser(user);

            List<OrderDTO> orderDTOs = orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(orderDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch orders: " + e.getMessage()));
        }
    }

    private OrderDTO convertToDTO(Orders order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setRazorpayOrderId(order.getRazorpayOrderId());
        dto.setRazorpayPaymentId(order.getRazorpayPaymentId());
        dto.setAmount(order.getAmount() / 100.0);
        dto.setOrderDate(order.getOrderDate());
        dto.setEmail(order.getEmail());
        return dto;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    static class BuyNowResponse {
        private Product product;
        private int quantity;
        private double amount;
        private boolean needsAddress;
    }
}
