package com.Shopping.Shopping.repository;

import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.model.ProductSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSpecificationRepository extends JpaRepository<ProductSpecification, Long> {
    List<ProductSpecification> findByProductOrderByDisplayOrderAsc(Product product);
    List<ProductSpecification> findByProductAndSpecGroup(Product product, String specGroup);
    void deleteByProduct(Product product);
}
