package com.Shopping.Shopping.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Base64;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private String description;
    
    private double price;
    
    private String imageName;
    
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "BYTEA")
    private byte[] image;
    
    private String category;
    
    private String uniqueProductId;
    
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = true)
    private Seller seller;

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
        return this.image != null ? Base64.getEncoder().encodeToString(this.image) : null;
    }
}

