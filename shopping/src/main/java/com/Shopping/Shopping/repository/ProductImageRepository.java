package com.Shopping.Shopping.repository;

import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductOrderByDisplayOrderAsc(Product product);
    List<ProductImage> findByProductAndIsPrimary(Product product, Boolean isPrimary);
    ProductImage findFirstByProductAndIsPrimary(Product product, Boolean isPrimary);
    void deleteByProduct(Product product);
}
