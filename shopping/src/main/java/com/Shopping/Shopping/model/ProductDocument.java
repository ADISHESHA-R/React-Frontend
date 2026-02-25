package com.Shopping.Shopping.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "product_documents")
@Getter
@Setter
public class ProductDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "BYTEA")
    private byte[] documentData;
    
    private String documentName;
    private String documentType; // "brand_authorization", "trademark", "bis_certificate", "fssai", etc.
    private String mimeType; // "application/pdf", "image/jpeg", etc.
    
    public ProductDocument() {}
}
