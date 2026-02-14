package com.Shopping.Shopping.controller;

import com.Shopping.Shopping.model.User;
import com.Shopping.Shopping.model.Seller;
import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.repository.UserRepository;
import com.Shopping.Shopping.repository.SellerRepository;
import com.Shopping.Shopping.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Admin Dashboard - View all data
     * Accessible at: https://hskshopping.onrender.com/admin
     */
    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        log.info("=== ADMIN DASHBOARD REQUEST ===");
        
        try {
            // Get all users
            List<User> users = userRepository.findAll();
            log.info("Found {} users", users.size());
            
            // Get all sellers
            List<Seller> sellers = sellerRepository.findAll();
            log.info("Found {} sellers", sellers.size());
            
            // Get all products
            List<Product> products = productRepository.findAll();
            log.info("Found {} products", products.size());
            
            // Add to model
            model.addAttribute("users", users);
            model.addAttribute("sellers", sellers);
            model.addAttribute("products", products);
            model.addAttribute("userCount", users.size());
            model.addAttribute("sellerCount", sellers.size());
            model.addAttribute("productCount", products.size());
            
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                model.addAttribute("currentUser", auth.getName());
            }
            
            log.info("=== ADMIN DASHBOARD REQUEST COMPLETED ===");
            return "admin-dashboard";
        } catch (Exception e) {
            log.error("Error loading admin dashboard", e);
            model.addAttribute("error", "Failed to load admin data: " + e.getMessage());
            return "admin-dashboard";
        }
    }

    /**
     * View only users
     */
    @GetMapping("/admin/users")
    public String viewUsers(Model model) {
        log.info("=== ADMIN VIEW USERS REQUEST ===");
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("userCount", users.size());
        return "admin-users";
    }

    /**
     * View only sellers
     */
    @GetMapping("/admin/sellers")
    public String viewSellers(Model model) {
        log.info("=== ADMIN VIEW SELLERS REQUEST ===");
        List<Seller> sellers = sellerRepository.findAll();
        model.addAttribute("sellers", sellers);
        model.addAttribute("sellerCount", sellers.size());
        return "admin-sellers";
    }

    /**
     * View only products
     */
    @GetMapping("/admin/products")
    public String viewProducts(Model model) {
        log.info("=== ADMIN VIEW PRODUCTS REQUEST ===");
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        model.addAttribute("productCount", products.size());
        return "admin-products";
    }
}
