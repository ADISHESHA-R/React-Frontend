package com.Shopping.Shopping.controller.api;

import com.Shopping.Shopping.dto.ApiResponse;
import com.Shopping.Shopping.dto.CartItemDTO;
import com.Shopping.Shopping.dto.ProductDTO;
import com.Shopping.Shopping.model.CartItem;
import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/cart")
public class ApiCartController {

    private final ProductService productService;

    public ApiCartController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cart");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }

        List<CartItemDTO> cartItemDTOs = cartItems.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        double total = cartItems.stream()
            .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
            .sum();

        CartResponse response = new CartResponse(cartItemDTOs, total);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<ApiResponse<String>> addToCart(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session) {
        try {
            Product product = productService.getProductById(productId);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Product not found"));
            }

            @SuppressWarnings("unchecked")
            List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
            if (cart == null) {
                cart = new ArrayList<>();
            }

            boolean found = false;
            for (CartItem item : cart) {
                if (item.getProduct().getId().equals(productId)) {
                    item.setQuantity(item.getQuantity() + quantity);
                    found = true;
                    break;
                }
            }

            if (!found) {
                cart.add(new CartItem(product, quantity));
            }

            session.setAttribute("cart", cart);
            return ResponseEntity.ok(ApiResponse.success("Item added to cart"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to add item: " + e.getMessage()));
        }
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<String>> removeFromCart(
            @PathVariable Long productId,
            HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null) {
            cart.removeIf(item -> item.getProduct().getId().equals(productId));
            session.setAttribute("cart", cart);
        }
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart"));
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<ApiResponse<String>> updateQuantity(
            @PathVariable Long productId,
            @RequestParam int quantity,
            HttpSession session) {
        try {
            @SuppressWarnings("unchecked")
            List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
            if (cart != null) {
                for (CartItem item : cart) {
                    if (item.getProduct().getId().equals(productId)) {
                        if (quantity <= 0) {
                            cart.remove(item);
                        } else {
                            item.setQuantity(quantity);
                        }
                        break;
                    }
                }
                session.setAttribute("cart", cart);
            }
            return ResponseEntity.ok(ApiResponse.success("Cart updated"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update cart: " + e.getMessage()));
        }
    }

    private CartItemDTO convertToDTO(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setProduct(convertProductToDTO(item.getProduct()));
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getProduct().getPrice() * item.getQuantity());
        return dto;
    }

    private ProductDTO convertProductToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategory(product.getCategory());
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
