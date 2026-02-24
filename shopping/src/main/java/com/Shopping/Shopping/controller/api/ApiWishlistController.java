package com.Shopping.Shopping.controller.api;

import com.Shopping.Shopping.dto.ApiResponse;
import com.Shopping.Shopping.dto.ProductDTO;
import com.Shopping.Shopping.model.Wishlist;
import com.Shopping.Shopping.model.User;
import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.repository.WishlistRepository;
import com.Shopping.Shopping.repository.UserRepository;
import com.Shopping.Shopping.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/wishlist")
public class ApiWishlistController {

    private final ProductService productService;
    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;

    public ApiWishlistController(ProductService productService,
                                WishlistRepository wishlistRepository,
                                UserRepository userRepository) {
        this.productService = productService;
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication authentication) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // GET /api/v1/wishlist - Get all wishlist items
    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistItemDTO>>> getWishlist(Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            List<Wishlist> wishlistItems = wishlistRepository.findByUser(user);
            
            List<WishlistItemDTO> response = wishlistItems.stream()
                .map(item -> {
                    WishlistItemDTO dto = new WishlistItemDTO();
                    dto.setId(item.getId());
                    dto.setProduct(convertProductToDTO(item.getProduct()));
                    return dto;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get wishlist: " + e.getMessage()));
        }
    }

    // POST /api/v1/wishlist/add/{productId} - Add to wishlist
    @PostMapping("/add/{productId}")
    public ResponseEntity<ApiResponse<String>> addToWishlist(
            @PathVariable Long productId,
            Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            Product product = productService.getProductById(productId);
            
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Product not found"));
            }

            // Check if already in wishlist
            if (wishlistRepository.existsByUserAndProductId(user, productId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Product already in wishlist"));
            }

            Wishlist wishlist = new Wishlist(user, product);
            wishlistRepository.save(wishlist);

            return ResponseEntity.ok(ApiResponse.success("Item added to wishlist"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to add item: " + e.getMessage()));
        }
    }

    // DELETE /api/v1/wishlist/remove/{productId} - Remove from wishlist
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<String>> removeFromWishlist(
            @PathVariable Long productId,
            Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            wishlistRepository.deleteByUserAndProductId(user, productId);
            return ResponseEntity.ok(ApiResponse.success("Item removed from wishlist"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to remove item: " + e.getMessage()));
        }
    }

    // GET /api/v1/wishlist/check/{productId} - Check if product is in wishlist
    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> checkWishlist(
            @PathVariable Long productId,
            Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            boolean exists = wishlistRepository.existsByUserAndProductId(user, productId);
            return ResponseEntity.ok(ApiResponse.success(exists));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to check wishlist: " + e.getMessage()));
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
    static class WishlistItemDTO {
        private Long id;
        private ProductDTO product;
    }
}
