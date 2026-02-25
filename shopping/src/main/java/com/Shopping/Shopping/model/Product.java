package com.Shopping.Shopping.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Basic Product Details
    private String name;
    private String brandName;
    private String category;
    private String subCategory;
    private String description; // Short description
    @Column(columnDefinition = "TEXT")
    private String longDescription; // Long description
    @Column(columnDefinition = "TEXT")
    private String keyFeatures; // Key features/highlights
    
    // Pricing Details
    private Double mrp; // Maximum Retail Price
    private Double sellingPrice; // Offer price
    private double price; // Legacy field (maps to sellingPrice)
    private Double discountPercent;
    private Boolean gstIncluded; // true if GST included in price
    private Integer minimumOrderQuantity;
    
    // Inventory & Stock
    private Integer availableQuantity;
    private String skuId; // Internal tracking code
    private String stockAvailability; // "ready", "made-to-order", "pre-order"
    
    // Product Specifications (stored as JSON or separate table)
    @Column(columnDefinition = "TEXT")
    private String specifications; // JSON string for flexible specs
    
    // Shipping Details
    private Double packageWeight; // in kg
    private Double packageLength; // in cm
    private Double packageWidth; // in cm
    private Double packageHeight; // in cm
    private String pickupAddress; // Warehouse location
    private String deliveryMethod; // "fulfilled", "self-ship"
    
    // Tax & Compliance
    private String gstNumber; // Seller's GST number
    private String hsnCode; // HSN code for tax
    private Boolean invoiceRequired;
    
    // Legal & Brand Info
    private Boolean brandAuthorized; // Has brand authorization
    private Boolean trademarkVerified;
    @Column(columnDefinition = "TEXT")
    private String complianceCertificates; // JSON array of certificate types
    
    // Seller Preferences
    @Column(columnDefinition = "TEXT")
    private String returnPolicy; // Return policy description
    private Boolean replacementAvailable;
    @Column(columnDefinition = "TEXT")
    private String warrantyDetails; // Warranty description
    
    // Legacy fields (keep for backward compatibility)
    private String imageName; // Primary image name
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "BYTEA")
    private byte[] image; // Primary image (legacy)
    private String uniqueProductId;
    
    // Relationships
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = true)
    private Seller seller;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductImage> images = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductSpecification> specificationsList = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductVariant> variants = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductDocument> documents = new ArrayList<>();

    public Product() {}

    public Product(String name, String description, double price, String imageName, String category, String uniqueProductId, Seller seller) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageName = imageName;
        this.category = category;
        this.uniqueProductId = uniqueProductId;
        this.seller = seller;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getUniqueProductId() { return uniqueProductId; }
    public void setUniqueProductId(String uniqueProductId) { this.uniqueProductId = uniqueProductId; }

    public Seller getSeller() { return seller; }
    public void setSeller(Seller seller) { this.seller = seller; }

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }

    public String getImageBase64() {
        if (this.image != null) {
            return Base64.getEncoder().encodeToString(this.image);
        }
        ProductImage primary = getPrimaryImage();
        if (primary != null && primary.getImageData() != null) {
            return Base64.getEncoder().encodeToString(primary.getImageData());
        }
        return null;
    }
    
    // Helper method to get primary image
    public ProductImage getPrimaryImage() {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
            .filter(img -> img.getIsPrimary() != null && img.getIsPrimary())
            .findFirst()
            .orElse(images.get(0)); // Fallback to first image
    }
    
    // Getters and setters for new fields
    public List<ProductImage> getImages() {
        return images;
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
    }
    
    public List<ProductSpecification> getSpecificationsList() {
        return specificationsList;
    }
    
    public void setSpecificationsList(List<ProductSpecification> specificationsList) {
        this.specificationsList = specificationsList;
    }
    
    public List<ProductVariant> getVariants() {
        return variants;
    }
    
    public void setVariants(List<ProductVariant> variants) {
        this.variants = variants;
    }
    
    public List<ProductDocument> getDocuments() {
        return documents;
    }
    
    public void setDocuments(List<ProductDocument> documents) {
        this.documents = documents;
    }
    
    // Getters and setters for new fields
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    
    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }
    
    public String getLongDescription() { return longDescription; }
    public void setLongDescription(String longDescription) { this.longDescription = longDescription; }
    
    public String getKeyFeatures() { return keyFeatures; }
    public void setKeyFeatures(String keyFeatures) { this.keyFeatures = keyFeatures; }
    
    public Double getMrp() { return mrp; }
    public void setMrp(Double mrp) { this.mrp = mrp; }
    
    public Double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(Double sellingPrice) { 
        this.sellingPrice = sellingPrice;
        if (sellingPrice != null) {
            this.price = sellingPrice; // Map to legacy field
        }
    }
    
    public Double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Double discountPercent) { this.discountPercent = discountPercent; }
    
    public Boolean getGstIncluded() { return gstIncluded; }
    public void setGstIncluded(Boolean gstIncluded) { this.gstIncluded = gstIncluded; }
    
    public Integer getMinimumOrderQuantity() { return minimumOrderQuantity; }
    public void setMinimumOrderQuantity(Integer minimumOrderQuantity) { this.minimumOrderQuantity = minimumOrderQuantity; }
    
    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }
    
    public String getSkuId() { return skuId; }
    public void setSkuId(String skuId) { this.skuId = skuId; }
    
    public String getStockAvailability() { return stockAvailability; }
    public void setStockAvailability(String stockAvailability) { this.stockAvailability = stockAvailability; }
    
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    
    public Double getPackageWeight() { return packageWeight; }
    public void setPackageWeight(Double packageWeight) { this.packageWeight = packageWeight; }
    
    public Double getPackageLength() { return packageLength; }
    public void setPackageLength(Double packageLength) { this.packageLength = packageLength; }
    
    public Double getPackageWidth() { return packageWidth; }
    public void setPackageWidth(Double packageWidth) { this.packageWidth = packageWidth; }
    
    public Double getPackageHeight() { return packageHeight; }
    public void setPackageHeight(Double packageHeight) { this.packageHeight = packageHeight; }
    
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    
    public String getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }
    
    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }
    
    public String getHsnCode() { return hsnCode; }
    public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }
    
    public Boolean getInvoiceRequired() { return invoiceRequired; }
    public void setInvoiceRequired(Boolean invoiceRequired) { this.invoiceRequired = invoiceRequired; }
    
    public Boolean getBrandAuthorized() { return brandAuthorized; }
    public void setBrandAuthorized(Boolean brandAuthorized) { this.brandAuthorized = brandAuthorized; }
    
    public Boolean getTrademarkVerified() { return trademarkVerified; }
    public void setTrademarkVerified(Boolean trademarkVerified) { this.trademarkVerified = trademarkVerified; }
    
    public String getComplianceCertificates() { return complianceCertificates; }
    public void setComplianceCertificates(String complianceCertificates) { this.complianceCertificates = complianceCertificates; }
    
    public String getReturnPolicy() { return returnPolicy; }
    public void setReturnPolicy(String returnPolicy) { this.returnPolicy = returnPolicy; }
    
    public Boolean getReplacementAvailable() { return replacementAvailable; }
    public void setReplacementAvailable(Boolean replacementAvailable) { this.replacementAvailable = replacementAvailable; }
    
    public String getWarrantyDetails() { return warrantyDetails; }
    public void setWarrantyDetails(String warrantyDetails) { this.warrantyDetails = warrantyDetails; }
}

