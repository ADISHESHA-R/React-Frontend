package com.Shopping.Shopping.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "product_images")
@Getter
@Setter
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "BYTEA")
    private byte[] imageData;  // Same as Product.image - stored in database
    
    private String imageName;
    private String imageType; // "front", "back", "lifestyle", "detail", etc.
    private Integer displayOrder; // For ordering images (0, 1, 2, ...)
    private Boolean isPrimary; // Primary image flag (first image is primary)
    
    public ProductImage() {}
    
    public ProductImage(Product product, byte[] imageData, String imageName, String imageType, Integer displayOrder, Boolean isPrimary) {
        this.product = product;
        this.imageData = imageData;
        this.imageName = imageName;
        this.imageType = imageType;
        this.displayOrder = displayOrder;
        this.isPrimary = isPrimary;
    }
}
