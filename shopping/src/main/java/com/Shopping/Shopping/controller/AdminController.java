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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

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

    // ========== DELETE OPERATIONS ==========

    @PostMapping("/admin/users/delete/{id}")
    @Transactional
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userRepository.deleteById(id);
            log.info("User deleted successfully: ID {}", id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete user: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/sellers/delete/{id}")
    @Transactional
    public String deleteSeller(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            sellerRepository.deleteById(id);
            log.info("Seller deleted successfully: ID {}", id);
            redirectAttributes.addFlashAttribute("success", "Seller deleted successfully!");
        } catch (Exception e) {
            log.error("Error deleting seller: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete seller: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/products/delete/{id}")
    @Transactional
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productRepository.deleteById(id);
            log.info("Product deleted successfully: ID {}", id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        } catch (Exception e) {
            log.error("Error deleting product: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete product: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    // ========== UPDATE OPERATIONS ==========

    @PostMapping("/admin/users/update/{id}")
    @Transactional
    public String updateUser(@PathVariable Long id,
                            @RequestParam("username") String username,
                            @RequestParam("phoneNumber") String phoneNumber,
                            @RequestParam("alternateNumber") String alternateNumber,
                            @RequestParam("address") String address,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setUsername(username);
            user.setPhoneNumber(phoneNumber);
            user.setAlternateNumber(alternateNumber);
            user.setAddress(address);
            userRepository.saveAndFlush(user);
            log.info("User updated successfully: ID {}", id);
            redirectAttributes.addFlashAttribute("success", "User updated successfully!");
        } catch (Exception e) {
            log.error("Error updating user: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to update user: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/sellers/update/{id}")
    @Transactional
    public String updateSeller(@PathVariable Long id,
                              @RequestParam("username") String username,
                              @RequestParam("email") String email,
                              @RequestParam("whatsappNumber") String whatsappNumber,
                              @RequestParam("businessEmail") String businessEmail,
                              @RequestParam("gstNumber") String gstNumber,
                              RedirectAttributes redirectAttributes) {
        try {
            Seller seller = sellerRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Seller not found"));
            seller.setUsername(username);
            seller.setEmail(email);
            seller.setWhatsappNumber(whatsappNumber);
            seller.setBusinessEmail(businessEmail);
            seller.setGstNumber(gstNumber);
            sellerRepository.saveAndFlush(seller);
            log.info("Seller updated successfully: ID {}", id);
            redirectAttributes.addFlashAttribute("success", "Seller updated successfully!");
        } catch (Exception e) {
            log.error("Error updating seller: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to update seller: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/products/update/{id}")
    @Transactional
    public String updateProduct(@PathVariable Long id,
                               @RequestParam("name") String name,
                               @RequestParam("description") String description,
                               @RequestParam("price") double price,
                               @RequestParam("category") String category,
                               @RequestParam(value = "uniqueProductId", required = false) String uniqueProductId,
                               RedirectAttributes redirectAttributes) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setCategory(category);
            if (uniqueProductId != null && !uniqueProductId.trim().isEmpty()) {
                product.setUniqueProductId(uniqueProductId);
            }
            productRepository.saveAndFlush(product);
            log.info("Product updated successfully: ID {}", id);
            redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
        } catch (Exception e) {
            log.error("Error updating product: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to update product: " + e.getMessage());
        }
        return "redirect:/admin";
    }
}
