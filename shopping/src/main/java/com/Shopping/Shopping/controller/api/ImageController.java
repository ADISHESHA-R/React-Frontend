package com.Shopping.Shopping.controller.api;

import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.model.ProductImage;
import com.Shopping.Shopping.service.ProductService;
import com.Shopping.Shopping.repository.ProductImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ImageController {

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
    
    private final ProductService productService;
    
    @Autowired
    private ProductImageRepository productImageRepository;

    public ImageController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Get primary product image (backward compatible - same as current)
     * Endpoint: GET /product-image/{id}
     * 
     * @param id Product ID
     * @return Primary image bytes (same format as current)
     */
    @GetMapping("/product-image/{id}")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id) {
        try {
            logger.debug("Requesting product image for ID: {}", id);
            
            Product product = productService.getProductById(id);
            
            if (product == null) {
                logger.warn("Product not found for ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            // Try new multiple images first
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                ProductImage primaryImage = product.getPrimaryImage();
                if (primaryImage != null && primaryImage.getImageData() != null) {
                    return buildImageResponse(primaryImage.getImageData(), primaryImage.getImageName());
                }
            }
            
            // Fallback to legacy single image (same as current implementation)
            if (product.getImage() != null && product.getImage().length > 0) {
                return buildImageResponse(product.getImage(), product.getImageName());
            }
            
            logger.warn("Product image is null or empty for ID: {}", id);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            logger.error("Error serving product image for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get specific product image by image ID
     * Endpoint: GET /product-image/{productId}/{imageId}
     * 
     * @param productId Product ID
     * @param imageId Image ID
     * @return Image bytes (same format as current)
     */
    @GetMapping("/product-image/{productId}/{imageId}")
    public ResponseEntity<byte[]> getProductImageById(
            @PathVariable Long productId, 
            @PathVariable Long imageId) {
        try {
            logger.debug("Requesting product image - Product ID: {}, Image ID: {}", productId, imageId);
            
            Product product = productService.getProductById(productId);
            if (product == null) {
                logger.warn("Product not found for ID: {}", productId);
                return ResponseEntity.notFound().build();
            }
            
            ProductImage image = productImageRepository.findById(imageId)
                .orElse(null);
            
            if (image == null || !image.getProduct().getId().equals(productId)) {
                logger.warn("Image not found - Product ID: {}, Image ID: {}", productId, imageId);
                return ResponseEntity.notFound().build();
            }
            
            if (image.getImageData() == null || image.getImageData().length == 0) {
                logger.warn("Product image is null or empty for Image ID: {}", imageId);
                return ResponseEntity.notFound().build();
            }
            
            return buildImageResponse(image.getImageData(), image.getImageName());
            
        } catch (Exception e) {
            logger.error("Error serving product image - Product ID: {}, Image ID: {}", productId, imageId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all images for a product (for slide view)
     * Endpoint: GET /product-images/{productId}
     * 
     * @param productId Product ID
     * @return List of image info with URLs
     */
    @GetMapping("/product-images/{productId}")
    public ResponseEntity<List<ImageInfo>> getAllProductImages(@PathVariable Long productId) {
        try {
            logger.debug("Requesting all images for product ID: {}", productId);
            
            Product product = productService.getProductById(productId);
            if (product == null) {
                logger.warn("Product not found for ID: {}", productId);
                return ResponseEntity.notFound().build();
            }
            
            List<ProductImage> images = productImageRepository.findByProductOrderByDisplayOrderAsc(product);
            
            // If no images in new table, check legacy field
            if (images.isEmpty() && product.getImage() != null && product.getImage().length > 0) {
                ImageInfo legacyImage = new ImageInfo();
                legacyImage.setImageId(null);
                legacyImage.setImageUrl("/product-image/" + productId);
                legacyImage.setImageType("primary");
                legacyImage.setDisplayOrder(0);
                legacyImage.setIsPrimary(true);
                return ResponseEntity.ok(List.of(legacyImage));
            }
            
            List<ImageInfo> imageInfos = images.stream()
                .map(img -> {
                    ImageInfo info = new ImageInfo();
                    info.setImageId(img.getId());
                    info.setImageUrl("/product-image/" + productId + "/" + img.getId());
                    info.setImageType(img.getImageType());
                    info.setDisplayOrder(img.getDisplayOrder());
                    info.setIsPrimary(img.getIsPrimary());
                    return info;
                })
                .toList();
            
            return ResponseEntity.ok(imageInfos);
            
        } catch (Exception e) {
            logger.error("Error fetching all images for product ID: {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Build image response (same as current implementation)
     */
    private ResponseEntity<byte[]> buildImageResponse(byte[] imageData, String imageName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // Default to JPEG
        headers.setContentLength(imageData.length);
        headers.setCacheControl("public, max-age=3600"); // Cache for 1 hour
        
        // Detect content type from imageName if available (same as current)
        if (imageName != null) {
            String name = imageName.toLowerCase();
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (name.endsWith(".png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (name.endsWith(".gif")) {
                headers.setContentType(MediaType.IMAGE_GIF);
            }
        }
        
        logger.info("Serving product image, Size: {} bytes", imageData.length);
        return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    }

    // DTO for image info
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ImageInfo {
        private Long imageId;
        private String imageUrl;
        private String imageType;
        private Integer displayOrder;
        private Boolean isPrimary;
    }
}
