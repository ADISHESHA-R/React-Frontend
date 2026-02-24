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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/cart")
public class ApiCartController {

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
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            List<Cart> cartItems = cartRepository.findByUser(user);
            
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
    public ResponseEntity<ApiResponse<String>> addToCart(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            Authentication authentication) {
        try {
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
            } else {
                Cart newCart = new Cart(user, product, quantity);
                cartRepository.save(newCart);
            }

            return ResponseEntity.ok(ApiResponse.success("Item added to cart"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to add item: " + e.getMessage()));
        }
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<String>> removeFromCart(
            @PathVariable Long productId,
            Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            cartRepository.deleteByUserAndProductId(user, productId);
            return ResponseEntity.ok(ApiResponse.success("Item removed from cart"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to remove item: " + e.getMessage()));
        }
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<ApiResponse<String>> updateQuantity(
            @PathVariable Long productId,
            @RequestParam int quantity,
            Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            Optional<Cart> cartOpt = cartRepository.findByUserAndProductId(user, productId);
            
            if (cartOpt.isPresent()) {
                Cart cart = cartOpt.get();
                if (quantity <= 0) {
                    cartRepository.delete(cart);
                    return ResponseEntity.ok(ApiResponse.success("Item removed from cart"));
                } else {
                    cart.setQuantity(quantity);
                    cartRepository.save(cart);
                    return ResponseEntity.ok(ApiResponse.success("Cart updated"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Item not found in cart"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update cart: " + e.getMessage()));
        }
    }

    private ProductDTO convertProductToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategory(product.getCategory());
        dto.setUniqueProductId(product.getUniqueProductId());
        dto.setImageUrl("/product-image/" + product.getId());
        return dto;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    static class CartResponse {
        private List<CartItemDTO> items;
        private double total;
    }
}
