package com.Shopping.Shopping.controller.api;

import com.Shopping.Shopping.dto.ApiResponse;
import com.Shopping.Shopping.dto.CartItemDTO;
import com.Shopping.Shopping.dto.ProductDTO;
import com.Shopping.Shopping.model.Cart;
import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.model.User;
import com.Shopping.Shopping.repository.CartRepository;
import com.Shopping.Shopping.repository.UserRepository;
import com.Shopping.Shopping.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/cart")
public class ApiCartController {

    private static final Logger logger = LoggerFactory.getLogger(ApiCartController.class);

    private final ProductService productService;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public ApiCartController(ProductService productService, 
                           CartRepository cartRepository,
                           UserRepository userRepository) {
        this.productService = productService;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    // Helper method to get current user from JWT
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            logger.error("Authentication is null or principal is null");
            throw new RuntimeException("Authentication failed");
        }
        
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        logger.info("Getting user for username: {}", username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                logger.error("User not found for username: {}", username);
                return new RuntimeException("User not found: " + username);
            });
        
        logger.info("User found - ID: {}, Username: {}", user.getId(), user.getUsername());
        return user;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        try {
            logger.info("Getting cart for authenticated user");
            User user = getCurrentUser(authentication);
            List<Cart> cartItems = cartRepository.findByUser(user);
            logger.info("Found {} items in cart for user: {} (ID: {})", cartItems.size(), user.getUsername(), user.getId());
            
            List<CartItemDTO> cartItemDTOs = cartItems.stream()
                .map(cart -> {
                    CartItemDTO dto = new CartItemDTO();
                    dto.setProduct(convertProductToDTO(cart.getProduct()));
                    dto.setQuantity(cart.getQuantity());
                    dto.setSubtotal(cart.getProduct().getPrice() * cart.getQuantity());
                    return dto;
                })
                .collect(Collectors.toList());
            
            double total = cartItems.stream()
                .mapToDouble(cart -> cart.getProduct().getPrice() * cart.getQuantity())
                .sum();
            
            CartResponse response = new CartResponse(cartItemDTOs, total);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get cart: " + e.getMessage()));
        }
    }

    @PostMapping("/add/{productId}")
    @Transactional
    public ResponseEntity<ApiResponse<String>> addToCart(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            Authentication authentication) {
        try {
            logger.info("Adding product {} to cart with quantity {}", productId, quantity);
            User user = getCurrentUser(authentication);
            Product product = productService.getProductById(productId);
            
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Product not found"));
            }

            // Check if item already in cart
            Optional<Cart> existingCart = cartRepository.findByUserAndProductId(user, productId);
            
            if (existingCart.isPresent()) {
                Cart cart = existingCart.get();
                cart.setQuantity(cart.getQuantity() + quantity);
                cartRepository.save(cart);
                logger.info("Updated existing cart item - Product ID: {}, New Quantity: {}", productId, cart.getQuantity());
            } else {
                Cart newCart = new Cart(user, product, quantity);
                cartRepository.save(newCart);
                logger.info("Created new cart item - Product ID: {}, Quantity: {}, User ID: {}", productId, quantity, user.getId());
            }

            return ResponseEntity.ok(ApiResponse.success("Item added to cart"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to add item: " + e.getMessage()));
        }
    }

    @DeleteMapping("/remove/{productId}")
    @Transactional
    public ResponseEntity<ApiResponse<String>> removeFromCart(
            @PathVariable Long productId,
            Authentication authentication) {
        try {
            logger.info("Removing product {} from cart", productId);
            User user = getCurrentUser(authentication);
            cartRepository.deleteByUserAndProductId(user, productId);
            logger.info("Successfully removed product {} from cart for user: {} (ID: {})", productId, user.getUsername(), user.getId());
            return ResponseEntity.ok(ApiResponse.success("Item removed from cart"));
        } catch (Exception e) {
            logger.error("Failed to remove item from cart - Product ID: {}, User: {}", productId, 
                authentication != null ? ((UserDetails) authentication.getPrincipal()).getUsername() : "unknown", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to remove item: " + e.getMessage()));
        }
    }

    @PutMapping("/update/{productId}")
    @Transactional
    public ResponseEntity<ApiResponse<String>> updateQuantity(
            @PathVariable Long productId,
            @RequestParam int quantity,
            Authentication authentication) {
        try {
            logger.info("Updating cart quantity - Product ID: {}, New Quantity: {}", productId, quantity);
            User user = getCurrentUser(authentication);
            Optional<Cart> cartOpt = cartRepository.findByUserAndProductId(user, productId);
            
            if (cartOpt.isPresent()) {
                Cart cart = cartOpt.get();
                if (quantity <= 0) {
                    cartRepository.delete(cart);
                    logger.info("Removed cart item (quantity <= 0) - Product ID: {}, User ID: {}", productId, user.getId());
                    return ResponseEntity.ok(ApiResponse.success("Item removed from cart"));
                } else {
                    cart.setQuantity(quantity);
                    cartRepository.save(cart);
                    logger.info("Updated cart quantity - Product ID: {}, Quantity: {}, User ID: {}", productId, quantity, user.getId());
                    return ResponseEntity.ok(ApiResponse.success("Cart updated"));
                }
            } else {
                logger.warn("Cart item not found - Product ID: {}, User ID: {}", productId, user.getId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Item not found in cart"));
            }
        } catch (Exception e) {
            logger.error("Failed to update cart - Product ID: {}, Quantity: {}", productId, quantity, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update cart: " + e.getMessage()));
        }
    }

    private ProductDTO convertProductToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setBrandName(product.getBrandName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setSellingPrice(product.getSellingPrice() != null ? product.getSellingPrice() : product.getPrice());
        dto.setCategory(product.getCategory());
        dto.setUniqueProductId(product.getUniqueProductId());
        
        // Handle images
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            List<String> imageUrls = product.getImages().stream()
                .sorted((a, b) -> Integer.compare(
                    a.getDisplayOrder() != null ? a.getDisplayOrder() : 0,
                    b.getDisplayOrder() != null ? b.getDisplayOrder() : 0))
                .map(img -> "/product-image/" + product.getId() + "/" + img.getId())
                .collect(java.util.stream.Collectors.toList());
            dto.setImageUrls(imageUrls);
            dto.setPrimaryImageUrl(imageUrls.get(0));
            dto.setImageUrl(imageUrls.get(0));
        } else {
            dto.setImageUrl("/product-image/" + product.getId());
        }
        
        return dto;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    static class CartResponse {
        private List<CartItemDTO> items;
        private double total;
    }
}
