package com.Shopping.Shopping.repository;

import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.model.ProductDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDocumentRepository extends JpaRepository<ProductDocument, Long> {
    List<ProductDocument> findByProduct(Product product);
    List<ProductDocument> findByProductAndDocumentType(Product product, String documentType);
    void deleteByProduct(Product product);
}
