package com.Shopping.Shopping.controller;

import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.model.Seller;
import com.Shopping.Shopping.service.ProductService;
import com.Shopping.Shopping.repository.SellerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;
    
    @Autowired
    private SellerRepository sellerRepository;

    @GetMapping("/seller-dashboard")
    public String sellerDashboard(Model model) {
        logger.info("=== SELLER DASHBOARD REQUEST STARTED ===");
        try {
            // Get all categories for dropdown
            List<String> categories = productService.getAllCategories();
            model.addAttribute("categories", categories);
            
            logger.info("Rendering seller dashboard page with {} categories", categories.size());
            logger.info("=== SELLER DASHBOARD REQUEST COMPLETED SUCCESSFULLY ===");
            return "seller-dashboard";
        } catch (Exception e) {
            logger.error("=== ERROR IN SELLER DASHBOARD REQUEST ===", e);
            throw e;
        }
    }

    @PostMapping("/upload-product")
    public String uploadProduct(@RequestParam String productName,
                                @RequestParam String productDescription,
                                @RequestParam double productPrice,
                                @RequestParam String productCategory,
                                @RequestParam String uniqueProductId,
                                @RequestParam MultipartFile productImage) {
        logger.info("=== PRODUCT UPLOAD REQUEST STARTED ===");
        logger.info("Product details - Name: '{}', Description: '{}', Price: {}, Category: '{}', Unique ID: '{}'", 
                   productName, productDescription, productPrice, productCategory, uniqueProductId);
        logger.info("Image file - Name: '{}', Size: {} bytes, Content Type: '{}'", 
                   productImage.getOriginalFilename(), productImage.getSize(), productImage.getContentType());
        
        try {
            // Get current seller
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String sellerUsername = auth.getName();
            Optional<Seller> sellerOpt = sellerRepository.findByUsername(sellerUsername);
            
            Seller seller = null;
            if (sellerOpt.isPresent()) {
                seller = sellerOpt.get();
                logger.info("Found seller: {}", seller.getUsername());
            } else {
                logger.warn("Seller not found for username: {}, creating product without seller association", sellerUsername);
            }
            
            // Generate unique product ID if not provided
            if (uniqueProductId == null || uniqueProductId.trim().isEmpty()) {
                uniqueProductId = "PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                logger.info("Generated unique product ID: {}", uniqueProductId);
            }
            
            Product newProduct = new Product(productName, productDescription, productPrice, 
                                          productImage.getOriginalFilename(), productCategory, 
                                          uniqueProductId, seller);
            logger.info("Created new product object with ID: {}", newProduct.getId());
            
            logger.info("Saving product to database...");
            productService.saveProduct(newProduct, productImage);
            logger.info("Product saved successfully with ID: {}", newProduct.getId());
            
            logger.info("=== PRODUCT UPLOAD REQUEST COMPLETED SUCCESSFULLY ===");
            return "redirect:/seller-dashboard?success=product_uploaded";
        } catch (Exception e) {
            logger.error("=== ERROR IN PRODUCT UPLOAD REQUEST ===", e);
            logger.error("Failed to upload product: '{}'", productName, e);
            throw e;
        }
    }
    
    @GetMapping("/product/{id}")
    public String getProductDetail(@PathVariable("id") Long productId, Model model) {
        logger.info("=== PRODUCT DETAIL REQUEST STARTED ===");
        logger.info("Requested product ID: {}", productId);
        
        try {
            // Check if user is authenticated
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isUser = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");
            model.addAttribute("isUser", isUser);
            
            logger.info("Fetching product details for ID: {}", productId);
            Product product = productService.getProductById(productId);
            
            if (product != null) {
                logger.info("Product found - Name: '{}', Price: {}, Description: '{}'", 
                           product.getName(), product.getPrice(), product.getDescription());
                model.addAttribute("product", product);
                logger.info("=== PRODUCT DETAIL REQUEST COMPLETED SUCCESSFULLY ===");
                return "product-detail";
            } else {
                logger.warn("Product not found for ID: {}", productId);
                logger.info("=== PRODUCT DETAIL REQUEST COMPLETED - PRODUCT NOT FOUND ===");
                return "product-not-found";
            }
        } catch (Exception e) {
            logger.error("=== ERROR IN PRODUCT DETAIL REQUEST for ID: {} ===", productId, e);
            throw e;
        }
    }
}
