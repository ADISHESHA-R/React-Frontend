package com.Shopping.Shopping.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ProductDTO {
    // Basic fields
    private Long id;
    private String name;
    private String brandName;
    private String category;
    private String subCategory;
    private String description;
    private String longDescription;
    private String keyFeatures;
    
    // Pricing
    private Double mrp;
    private Double sellingPrice;
    private double price; // Legacy field
    private Double discountPercent;
    private Boolean gstIncluded;
    private Integer minimumOrderQuantity;
    
    // Inventory
    private Integer availableQuantity;
    private String skuId;
    private String stockAvailability;
    private String uniqueProductId;
    
    // Shipping
    private Double packageWeight;
    private Double packageLength;
    private Double packageWidth;
    private Double packageHeight;
    private String pickupAddress;
    private String deliveryMethod;
    
    // Tax & Compliance
    private String gstNumber;
    private String hsnCode;
    private Boolean invoiceRequired;
    
    // Legal
    private Boolean brandAuthorized;
    private Boolean trademarkVerified;
    private List<String> complianceCertificates;
    
    // Seller Preferences
    private String returnPolicy;
    private Boolean replacementAvailable;
    private String warrantyDetails;
    
    // Images
    private String imageUrl; // Legacy single image (backward compatible)
    private List<String> imageUrls; // Multiple images for slide view
    private String primaryImageUrl; // Primary image URL
    
    // Specifications
    private Map<String, String> specifications;
    
    // Variants
    private List<Map<String, Object>> variants;
}
