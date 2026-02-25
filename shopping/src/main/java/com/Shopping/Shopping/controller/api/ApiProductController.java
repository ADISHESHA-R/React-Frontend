package com.Shopping.Shopping.controller.api;

import com.Shopping.Shopping.dto.ApiResponse;
import com.Shopping.Shopping.dto.ProductDTO;
import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
public class ApiProductController {

    private final ProductService productService;

    public ApiProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(productDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch products: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Product not found"));
            }
            return ResponseEntity.ok(ApiResponse.success(convertToDTO(product)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch product: " + e.getMessage()));
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByCategory(@PathVariable String category) {
        try {
            List<Product> products = productService.getProductsByCategory(category);
            List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(productDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch products: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(@RequestParam String query) {
        try {
            List<Product> products = productService.searchProducts(query);
            List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(productDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to search products: " + e.getMessage()));
        }
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setBrandName(product.getBrandName());
        dto.setCategory(product.getCategory());
        dto.setSubCategory(product.getSubCategory());
        dto.setDescription(product.getDescription());
        dto.setLongDescription(product.getLongDescription());
        dto.setKeyFeatures(product.getKeyFeatures());
        dto.setPrice(product.getPrice());
        dto.setSellingPrice(product.getSellingPrice() != null ? product.getSellingPrice() : product.getPrice());
        dto.setMrp(product.getMrp());
        dto.setDiscountPercent(product.getDiscountPercent());
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
            dto.setPrimaryImageUrl("/product-image/" + product.getId());
        }
        
        return dto;
    }
}
