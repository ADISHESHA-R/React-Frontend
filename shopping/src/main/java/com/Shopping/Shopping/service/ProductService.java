package com.Shopping.Shopping.service;

import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.model.ProductImage;
import com.Shopping.Shopping.model.ProductSpecification;
import com.Shopping.Shopping.model.ProductVariant;
import com.Shopping.Shopping.model.ProductDocument;
import com.Shopping.Shopping.repository.ProductRepository;
import com.Shopping.Shopping.repository.ProductImageRepository;
import com.Shopping.Shopping.repository.ProductSpecificationRepository;
import com.Shopping.Shopping.repository.ProductVariantRepository;
import com.Shopping.Shopping.repository.ProductDocumentRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    @Autowired
    private ProductSpecificationRepository productSpecificationRepository;
    
    @Autowired
    private ProductVariantRepository productVariantRepository;
    
    @Autowired
    private ProductDocumentRepository productDocumentRepository;

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
            
            // Also save to ProductImage table for new functionality (backward compatibility)
            if (productImage != null && !productImage.isEmpty() && savedProduct.getImage() != null) {
                try {
                    ProductImage productImageEntity = new ProductImage();
                    productImageEntity.setProduct(savedProduct);
                    productImageEntity.setImageData(savedProduct.getImage());
                    productImageEntity.setImageName(savedProduct.getImageName());
                    productImageEntity.setImageType("primary");
                    productImageEntity.setDisplayOrder(0);
                    productImageEntity.setIsPrimary(true);
                    productImageRepository.save(productImageEntity);
                    logger.info("Image also saved to ProductImage table for backward compatibility");
                } catch (Exception e) {
                    logger.warn("Failed to save to ProductImage table, continuing with legacy storage", e);
                }
            }
            
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
                
                // Initialize lazy collections to avoid LazyInitializationException
                if (product.getImages() != null) {
                    product.getImages().size(); // Force fetch
                }
                if (product.getSpecificationsList() != null) {
                    product.getSpecificationsList().size(); // Force fetch
                }
                if (product.getVariants() != null) {
                    product.getVariants().size(); // Force fetch
                }
                if (product.getDocuments() != null) {
                    product.getDocuments().size(); // Force fetch
                }
                
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
     * Save multiple product images (same approach as current saveProduct)
     */
    public void saveProductImages(Product product, List<MultipartFile> imageFiles, List<String> imageTypes) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            logger.info("No images provided for product: {}", product.getId());
            return;
        }
        
        logger.info("Saving {} images for product ID: {}", imageFiles.size(), product.getId());
        
        // imageTypes is already a List<String> from the controller
        List<String> types = (imageTypes != null && !imageTypes.isEmpty()) ? imageTypes : new ArrayList<>();
        
        // Save each image (same validation and storage as current implementation)
        for (int i = 0; i < imageFiles.size(); i++) {
            MultipartFile imageFile = imageFiles.get(i);
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    // Validate image (same as current validateImageFile)
                    validateImageFile(imageFile);
                    
                    // Create ProductImage entity
                    ProductImage productImage = new ProductImage();
                    productImage.setProduct(product);
                    
                    // Save image data to database (same as current: product.setImage())
                    productImage.setImageData(imageFile.getBytes());
                    logger.info("Image data saved to database ({} bytes)", imageFile.getBytes().length);
                    
                    // Keep imageName (same as current implementation)
                    String imageExtension = getFileExtension(imageFile.getOriginalFilename());
                    if (!imageExtension.equalsIgnoreCase(".jpg") && !imageExtension.equalsIgnoreCase(".jpeg")) {
                        imageExtension = ".jpg"; // Default to .jpg (same as current)
                    }
                    String imageName = UUID.randomUUID().toString() + imageExtension;
                    productImage.setImageName(imageName);
                    
                    productImage.setImageType(i < types.size() ? types.get(i).trim() : "general");
                    productImage.setDisplayOrder(i);
                    productImage.setIsPrimary(i == 0); // First image is primary
                    
                    // Save to database
                    productImageRepository.save(productImage);
                    logger.info("Saved image {} for product ID: {} - Name: {}", i + 1, product.getId(), imageName);
                    
                } catch (IOException e) {
                    logger.error("Failed to save image {} for product ID: {}", i + 1, product.getId(), e);
                    throw new RuntimeException("Failed to process image file: " + e.getMessage(), e);
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid image {} for product ID: {} - {}", i + 1, product.getId(), e.getMessage());
                    throw e; // Re-throw validation errors
                }
            }
        }
        
        logger.info("All {} images saved successfully for product ID: {}", imageFiles.size(), product.getId());
    }
    
    /**
     * Save product specifications
     */
    public void saveProductSpecifications(Product product, String specificationsJson) {
        if (specificationsJson == null || specificationsJson.trim().isEmpty()) {
            return;
        }
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> specs = mapper.readValue(specificationsJson, new TypeReference<Map<String, String>>() {});
            
            int order = 0;
            for (Map.Entry<String, String> entry : specs.entrySet()) {
                ProductSpecification spec = new ProductSpecification();
                spec.setProduct(product);
                spec.setSpecKey(entry.getKey());
                spec.setSpecValue(entry.getValue());
                spec.setSpecGroup("General");
                spec.setDisplayOrder(order++);
                productSpecificationRepository.save(spec);
            }
            
            logger.info("Saved {} specifications for product ID: {}", specs.size(), product.getId());
        } catch (Exception e) {
            logger.error("Failed to parse specifications for product ID: {}", product.getId(), e);
            // Don't throw - continue without specs
        }
    }
    
    /**
     * Save product variants
     */
    public void saveProductVariants(Product product, String variantsJson) {
        if (variantsJson == null || variantsJson.trim().isEmpty()) {
            return;
        }
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> variants = mapper.readValue(variantsJson, new TypeReference<List<Map<String, Object>>>() {});
            
            for (Map<String, Object> variantData : variants) {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(product);
                variant.setVariantType((String) variantData.get("type"));
                variant.setVariantValue((String) variantData.get("value"));
                
                Object priceMod = variantData.get("priceModifier");
                variant.setPriceModifier(priceMod != null ? ((Number) priceMod).doubleValue() : 0.0);
                
                Object stock = variantData.get("stock");
                variant.setStockQuantity(stock != null ? ((Number) stock).intValue() : 0);
                
                variant.setSku((String) variantData.getOrDefault("sku", ""));
                
                Object isAvail = variantData.get("isAvailable");
                variant.setIsAvailable(isAvail != null ? (Boolean) isAvail : true);
                
                productVariantRepository.save(variant);
            }
            
            logger.info("Saved {} variants for product ID: {}", variants.size(), product.getId());
        } catch (Exception e) {
            logger.error("Failed to parse variants for product ID: {}", product.getId(), e);
            // Don't throw - continue without variants
        }
    }
    
    /**
     * Save product documents
     */
    public void saveProductDocuments(Product product, List<MultipartFile> documentFiles, List<String> documentTypes) {
        if (documentFiles == null || documentFiles.isEmpty()) {
            return;
        }
        
        // documentTypes is already a List<String> from the controller
        List<String> types = (documentTypes != null && !documentTypes.isEmpty()) ? documentTypes : new ArrayList<>();
        
        for (int i = 0; i < documentFiles.size(); i++) {
            MultipartFile docFile = documentFiles.get(i);
            if (docFile != null && !docFile.isEmpty()) {
                try {
                    ProductDocument document = new ProductDocument();
                    document.setProduct(product);
                    document.setDocumentData(docFile.getBytes());
                    document.setDocumentName(docFile.getOriginalFilename());
                    document.setDocumentType(i < types.size() ? types.get(i).trim() : "other");
                    document.setMimeType(docFile.getContentType());
                    productDocumentRepository.save(document);
                } catch (IOException e) {
                    logger.error("Failed to save document: {}", docFile.getOriginalFilename(), e);
                }
            }
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

    public void deleteProductImages(Product product) {
        productImageRepository.deleteByProduct(product);
        logger.info("Deleted all images for product ID: {}", product.getId());
    }

    public void deleteProductSpecifications(Product product) {
        productSpecificationRepository.deleteByProduct(product);
        logger.info("Deleted all specifications for product ID: {}", product.getId());
    }

    public void deleteProductVariants(Product product) {
        productVariantRepository.deleteByProduct(product);
        logger.info("Deleted all variants for product ID: {}", product.getId());
    }

    public void deleteProductDocuments(Product product) {
        productDocumentRepository.deleteByProduct(product);
        logger.info("Deleted all documents for product ID: {}", product.getId());
    }
}
