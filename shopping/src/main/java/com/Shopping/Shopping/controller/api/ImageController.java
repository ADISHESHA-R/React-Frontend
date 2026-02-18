package com.Shopping.Shopping.controller.api;

import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageController {

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
    
    private final ProductService productService;

    public ImageController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Serve product image from Render's PostgreSQL database
     * Endpoint: GET /product-image/{id}
     * 
     * @param id Product ID
     * @return Image bytes with appropriate headers
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
            
            if (product.getImage() == null || product.getImage().length == 0) {
                logger.warn("Product image is null or empty for ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            // Set headers for image response
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // Default to JPEG
            headers.setContentLength(product.getImage().length);
            headers.setCacheControl("public, max-age=3600"); // Cache for 1 hour
            
            // Detect content type from imageName if available
            if (product.getImageName() != null) {
                String imageName = product.getImageName().toLowerCase();
                if (imageName.endsWith(".jpg") || imageName.endsWith(".jpeg")) {
                    headers.setContentType(MediaType.IMAGE_JPEG);
                } else if (imageName.endsWith(".png")) {
                    headers.setContentType(MediaType.IMAGE_PNG);
                } else if (imageName.endsWith(".gif")) {
                    headers.setContentType(MediaType.IMAGE_GIF);
                }
            }
            
            logger.info("Serving product image for ID: {}, Size: {} bytes", id, product.getImage().length);
            return new ResponseEntity<>(product.getImage(), headers, HttpStatus.OK);
            
        } catch (Exception e) {
            logger.error("Error serving product image for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
