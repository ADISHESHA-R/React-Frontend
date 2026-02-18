package com.Shopping.Shopping.service;

import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    public List<Product> searchProducts(String keyword) {
        logger.info("=== SEARCH PRODUCTS METHOD STARTED ===");
        logger.info("Search keyword: '{}'", keyword);
        
        try {
            List<Product> results = productRepository.searchProducts(keyword);
            logger.info("Search completed. Found {} products for keyword: '{}'", results.size(), keyword);
            logger.info("=== SEARCH PRODUCTS METHOD COMPLETED SUCCESSFULLY ===");
            return results;
        } catch (Exception e) {
            logger.error("=== ERROR IN SEARCH PRODUCTS METHOD for keyword: '{}' ===", keyword, e);
            throw e;
        }
    }

    public List<String> getAllCategories() {
        logger.info("=== GET ALL CATEGORIES METHOD STARTED ===");
        try {
            List<String> categories = productRepository.findDistinctCategories();
            logger.info("Found {} distinct categories", categories.size());
            logger.info("=== GET ALL CATEGORIES METHOD COMPLETED SUCCESSFULLY ===");
            return categories;
        } catch (Exception e) {
            logger.error("=== ERROR IN GET ALL CATEGORIES METHOD ===", e);
            throw e;
        }
    }

    public List<Product> getProductsByCategory(String category) {
        logger.info("=== GET PRODUCTS BY CATEGORY METHOD STARTED ===");
        logger.info("Category: '{}'", category);
        try {
            List<Product> products = productRepository.findByCategoryContainingIgnoreCase(category);
            logger.info("Found {} products in category: '{}'", products.size(), category);
            logger.info("=== GET PRODUCTS BY CATEGORY METHOD COMPLETED SUCCESSFULLY ===");
            return products;
        } catch (Exception e) {
            logger.error("=== ERROR IN GET PRODUCTS BY CATEGORY METHOD for category: '{}' ===", category, e);
            throw e;
        }
    }

    @org.springframework.beans.factory.annotation.Value("${app.upload.dir}")
    private String uploadDir;

    public void saveProduct(Product product, MultipartFile productImage) {
        logger.info("=== SAVE PRODUCT METHOD STARTED ===");
        logger.info("Product details - Name: '{}', Description: '{}', Price: {}", 
                   product.getName(), product.getDescription(), product.getPrice());
        
        try {
            // Store image in database instead of filesystem (persists across restarts)
            if (productImage != null && !productImage.isEmpty()) {
                logger.info("Processing image file - Name: '{}', Size: {} bytes, Content Type: '{}'", 
                           productImage.getOriginalFilename(), productImage.getSize(), productImage.getContentType());
                
                // Validate image format and size
                validateImageFile(productImage);
                
                try {
                    // Save image data to database
                    product.setImage(productImage.getBytes());
                    logger.info("Image data saved to database ({} bytes)", productImage.getBytes().length);
                    
                    // Keep imageName for backward compatibility (ensure .jpg extension)
                    String imageExtension = getFileExtension(productImage.getOriginalFilename());
                    // Ensure extension is .jpg or .jpeg
                    if (!imageExtension.equalsIgnoreCase(".jpg") && !imageExtension.equalsIgnoreCase(".jpeg")) {
                        imageExtension = ".jpg"; // Default to .jpg
                    }
                    String imageName = UUID.randomUUID().toString() + imageExtension;
                    product.setImageName(imageName);
                    logger.info("Image name set to: {}", imageName);
                } catch (IOException e) {
                    logger.error("Failed to process image file: '{}'", productImage.getOriginalFilename(), e);
                    throw new RuntimeException("Failed to process image file: " + e.getMessage(), e);
                }
            } else {
                logger.info("No image file provided or file is empty");
                product.setImage(null);
                product.setImageName(null);
            }

            logger.info("Saving product to database...");
            Product savedProduct = productRepository.save(product);
            logger.info("Product saved successfully with ID: {}", savedProduct.getId());
            
            logger.info("=== SAVE PRODUCT METHOD COMPLETED SUCCESSFULLY ===");
        } catch (Exception e) {
            logger.error("=== ERROR IN SAVE PRODUCT METHOD ===", e);
            logger.error("Failed to save product: '{}'", product.getName(), e);
            throw e;
        }
    }

    private String getFileExtension(String fileName) {
        logger.debug("Getting file extension for: '{}'", fileName);
        
        if (fileName == null || !fileName.contains(".")) {
            logger.debug("No extension found, returning empty string");
            return "";
        }
        
        String extension = fileName.substring(fileName.lastIndexOf("."));
        logger.debug("File extension: '{}'", extension);
        return extension;
    }

    public List<Product> getAllProducts() {
        logger.info("=== GET ALL PRODUCTS METHOD STARTED ===");
        
        try {
            List<Product> products = productRepository.findAll();
            logger.info("Successfully retrieved {} products from database", products.size());
            logger.info("=== GET ALL PRODUCTS METHOD COMPLETED SUCCESSFULLY ===");
            return products;
        } catch (Exception e) {
            logger.error("=== ERROR IN GET ALL PRODUCTS METHOD ===", e);
            throw e;
        }
    }

    public Product getProductById(Long productId) {
        logger.info("=== GET PRODUCT BY ID METHOD STARTED ===");
        logger.info("Requested product ID: {}", productId);
        
        if (productId == null) {
            logger.warn("Product ID is null");
            return null;
        }
        
        try {
            Optional<Product> productOpt = productRepository.findById(productId);
            
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                logger.info("Product found - ID: {}, Name: '{}', Price: {}", 
                           product.getId(), product.getName(), product.getPrice());
                logger.info("=== GET PRODUCT BY ID METHOD COMPLETED SUCCESSFULLY ===");
                return product;
            } else {
                logger.warn("Product not found for ID: {}", productId);
                logger.info("=== GET PRODUCT BY ID METHOD COMPLETED - PRODUCT NOT FOUND ===");
                return null;
            }
        } catch (Exception e) {
            logger.error("=== ERROR IN GET PRODUCT BY ID METHOD for ID: {} ===", productId, e);
            throw e;
        }
    }
    
    /**
     * Validate image file format and size
     * @param file MultipartFile to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Image filename is required");
        }
        
        // Check file extension (JPG/JPEG only)
        String fileNameLower = fileName.toLowerCase();
        if (!fileNameLower.endsWith(".jpg") && !fileNameLower.endsWith(".jpeg")) {
            throw new IllegalArgumentException("Only JPG and JPEG image formats are allowed. Provided: " + fileName);
        }
        
        // Check file size (10MB limit)
        long maxSize = 10 * 1024 * 1024; // 10MB in bytes
        if (file.getSize() > maxSize) {
            double sizeInMB = file.getSize() / (1024.0 * 1024.0);
            throw new IllegalArgumentException(
                String.format("Image size exceeds 10MB limit. Size: %.2f MB", sizeInMB));
        }
        
        // Validate content type if available
        String contentType = file.getContentType();
        if (contentType != null && 
            !contentType.equalsIgnoreCase("image/jpeg") && 
            !contentType.equalsIgnoreCase("image/jpg")) {
            logger.warn("Content type mismatch. Expected image/jpeg, got: {}", contentType);
        }
    }
}
