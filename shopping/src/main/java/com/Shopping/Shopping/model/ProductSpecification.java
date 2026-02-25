package com.Shopping.Shopping.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_specifications")
@Getter
@Setter
public class ProductSpecification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    private String specKey; // e.g., "Model Number", "Warranty", "Color"
    private String specValue; // e.g., "ABC123", "1 Year", "Red"
    private String specGroup; // e.g., "Technical", "Physical", "Warranty"
    private Integer displayOrder;
    
    public ProductSpecification() {}
}
