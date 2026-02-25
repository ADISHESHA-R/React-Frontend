package com.Shopping.Shopping.repository;

import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProduct(Product product);
    List<ProductVariant> findByProductAndVariantType(Product product, String variantType);
    void deleteByProduct(Product product);
}
