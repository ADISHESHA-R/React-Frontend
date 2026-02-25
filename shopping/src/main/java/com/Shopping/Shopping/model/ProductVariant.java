package com.Shopping.Shopping.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    private String variantType; // "size", "color", "storage", "material", etc.
    private String variantValue; // "XL", "Red", "256GB", "Cotton", etc.
    private Double priceModifier; // Additional price for this variant (can be negative)
    private Integer stockQuantity;
    private String sku; // SKU for this specific variant
    private Boolean isAvailable;
    
    public ProductVariant() {}
}
