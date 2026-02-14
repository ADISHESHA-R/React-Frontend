package com.Shopping.Shopping.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.Shopping.Shopping.model.Orders;
import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.model.User;
import com.Shopping.Shopping.repository.OrdersRepository;
import com.Shopping.Shopping.repository.ProductRepository;
import com.Shopping.Shopping.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    
    private final String razorpayKey;
    private final String razorpaySecret;
    private final OrdersRepository ordersRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public PaymentController(@Value("${razorpay.key}") String razorpayKey,
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

    // 🟢 Buy Now - Flipkart-like flow: Check address, then proceed to payment
    // POST mapping for Buy Now functionality
    @PostMapping("/buy-now/{productId}")
    public String buyNow(@PathVariable("productId") Long productId,
                         @RequestParam(defaultValue = "1") int quantity,
                         Principal principal,
                         HttpSession session,
                         Model model) {
        log.info("=== BUY NOW REQUEST STARTED ===");
        log.info("Product ID: {}, Quantity: {}", productId, quantity);
        
        try {
            if (principal == null) {
                log.warn("Buy now attempt without authentication");
                log.info("=== BUY NOW REQUEST FAILED - NOT AUTHENTICATED ===");
                return "redirect:/login";
            }

            log.info("User authenticated: {}", principal.getName());
            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Fetch product
            log.info("Fetching product details for ID: {}", productId);
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            log.info("Product found - Name: '{}', Price: {}", product.getName(), product.getPrice());
            
            // Store buy-now product in session for later use
            session.setAttribute("buyNowProductId", productId);
            session.setAttribute("buyNowQuantity", quantity);
            
            // Check if user has address (Flipkart-like behavior)
            if (user.getAddress() == null || user.getAddress().trim().isEmpty()) {
                log.info("User does not have address, redirecting to address form");
                model.addAttribute("product", product);
                model.addAttribute("quantity", quantity);
                model.addAttribute("user", user);
                return "buy-now-address"; // Show address form
            }
            
            // User has address - show payment page (order will be created via AJAX like cart checkout)
            log.info("User has address: {}", user.getAddress());
            
            // Calculate amount for display and AJAX call
            int amountInPaise = (int) (product.getPrice() * quantity * 100);
            
            model.addAttribute("key", razorpayKey);
            model.addAttribute("amount", amountInPaise); // Amount for AJAX call
            model.addAttribute("product", product);
            model.addAttribute("quantity", quantity);
            model.addAttribute("user", user);
            model.addAttribute("isBuyNow", true);

            log.info("=== BUY NOW REQUEST COMPLETED - SHOWING PAYMENT PAGE ===");
            return "buy-now-payment";
            
        } catch (Exception e) {
            log.error("=== ERROR IN BUY NOW REQUEST for Product ID: {} ===", productId, e);
            return "redirect:/";
        }
    }
    
    // Handle address submission for Buy Now
    @PostMapping("/buy-now/address")
    @Transactional
    public String saveBuyNowAddress(@RequestParam("address") String address,
                                     Principal principal,
                                     HttpSession session,
                                     Model model) {
        log.info("=== BUY NOW ADDRESS SUBMISSION ===");
        
        try {
            if (principal == null) {
                return "redirect:/login";
            }
            
            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Save address to user profile
            user.setAddress(address);
            userRepository.save(user);
            log.info("Address saved for user: {}", principal.getName());
            
            // Get product and quantity from session
            Object productIdObj = session.getAttribute("buyNowProductId");
            Object quantityObj = session.getAttribute("buyNowQuantity");
            
            if (productIdObj == null || quantityObj == null) {
                log.error("Buy Now session data missing");
                return "redirect:/";
            }
            
            Long productId = Long.valueOf(productIdObj.toString());
            Integer quantity = Integer.valueOf(quantityObj.toString());
            
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            // Clear session attributes
            session.removeAttribute("buyNowProductId");
            session.removeAttribute("buyNowQuantity");
            
            // Calculate amount for display and AJAX call
            int amountInPaise = (int) (product.getPrice() * quantity * 100);
            
            // Show payment page (order will be created via AJAX)
            model.addAttribute("key", razorpayKey);
            model.addAttribute("amount", amountInPaise);
            model.addAttribute("product", product);
            model.addAttribute("quantity", quantity);
            model.addAttribute("user", user);
            model.addAttribute("isBuyNow", true);
            
            return "buy-now-payment";
            
        } catch (Exception e) {
            log.error("=== ERROR IN BUY NOW ADDRESS SUBMISSION ===", e);
            return "redirect:/";
        }
    }

    // 🟢 AJAX: Create Razorpay Order from Total Amount
    @PostMapping("/create-order")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> data) throws RazorpayException {
        log.info("=== CREATE ORDER REQUEST STARTED ===");
        log.info("Request data: {}", data);
        
        try {
            int amount = (int) data.get("amount");
            log.info("Creating order for amount: {} paise (₹{})", amount, amount / 100);

            RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);
            JSONObject options = new JSONObject();
            options.put("amount", amount);
            options.put("currency", "INR");
            options.put("receipt", "txn_" + System.currentTimeMillis());

            Order order = client.orders.create(options);
            log.info("Razorpay order created successfully - Order ID: {}", order.get("id").toString());

            Map<String, Object> response = new HashMap<>();
            response.put("id", order.get("id"));
            response.put("amount", order.get("amount"));
            
            log.info("=== CREATE ORDER REQUEST COMPLETED SUCCESSFULLY ===");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("=== ERROR IN CREATE ORDER REQUEST ===", e);
            throw e;
        }
    }

    // 🟢 Handle Payment Success
    @PostMapping("/payment-success")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> handlePayment(@RequestBody Map<String, Object> data, Principal principal) {
        log.info("=== PAYMENT SUCCESS REQUEST STARTED ===");
        log.info("Payment data: {}", data);
        
        try {
            if (principal == null) {
                log.warn("Payment attempt without authentication");
                log.info("=== PAYMENT SUCCESS REQUEST FAILED - NOT AUTHENTICATED ===");
                return ResponseEntity.status(401).build();
            }

            log.info("Processing payment for user: {}", principal.getName());
            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Calculate amount - check if it's Buy Now or Cart checkout
            double amount = 500.0; // Default
            if (data.containsKey("isBuyNow") && Boolean.TRUE.equals(data.get("isBuyNow"))) {
                // Buy Now - get amount from product
                Object productIdObj = data.get("productId");
                Object quantityObj = data.get("quantity");
                if (productIdObj != null && quantityObj != null) {
                    Long productId = Long.valueOf(productIdObj.toString());
                    Integer quantity = Integer.valueOf(quantityObj.toString());
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Product not found"));
                    amount = product.getPrice() * quantity;
                    log.info("Buy Now order - Product: {}, Quantity: {}, Amount: {}", product.getName(), quantity, amount);
                }
            } else {
                // Cart checkout - amount already calculated
                if (data.containsKey("amount")) {
                    amount = Double.parseDouble(data.get("amount").toString()) / 100.0;
                }
            }

            log.info("Creating order record...");
            Orders order = new Orders();
            order.setRazorpayPaymentId(data.get("razorpay_payment_id").toString());
            order.setRazorpayOrderId(data.get("razorpay_order_id").toString());
            order.setRazorpaySignature(data.get("razorpay_signature").toString());
            order.setAmount((int)(amount * 100)); // Store in paise
            order.setOrderDate(LocalDateTime.now());
            order.setUser(user);
            order.setEmail(user.getUsername());

            ordersRepository.save(order);
            log.info("Order saved successfully for user: {}, Order ID: {}, Payment ID: {}, Amount: ₹{}", 
                    principal.getName(), order.getRazorpayOrderId(), order.getRazorpayPaymentId(), amount);
            
            log.info("=== PAYMENT SUCCESS REQUEST COMPLETED SUCCESSFULLY ===");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("=== ERROR IN PAYMENT SUCCESS REQUEST ===", e);
            throw e;
        }
    }

    // 🟢 Payment Success Page
    @GetMapping("/payment-success-page")
    public String paymentSuccessPage() {
        log.info("=== PAYMENT SUCCESS PAGE REQUEST STARTED ===");
        try {
            log.info("Rendering payment success page");
            log.info("=== PAYMENT SUCCESS PAGE REQUEST COMPLETED SUCCESSFULLY ===");
            return "payment_success";
        } catch (Exception e) {
            log.error("=== ERROR IN PAYMENT SUCCESS PAGE REQUEST ===", e);
            throw e;
        }
    }
}
