package com.Shopping.Shopping.service;

import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
    }

    @Test
    void testSearchProducts() {
        // Fix: Mock the correct method that ProductService actually uses
        when(productRepository.searchProducts("test"))
                .thenReturn(Arrays.asList(product));

        List<Product> products = productService.searchProducts("test");

        assertThat(products).isNotEmpty();
        assertThat(products.get(0).getName()).isEqualTo("Test Product");
        verify(productRepository, times(1))
                .searchProducts("test");
    }

    @Test
    void testSaveProductSuccessfully() throws IOException {
        // Fix: Set uploadDir using reflection since @Value doesn't work in unit tests
        try {
            java.lang.reflect.Field uploadDirField = ProductService.class.getDeclaredField("uploadDir");
            uploadDirField.setAccessible(true);
            uploadDirField.set(productService, System.getProperty("java.io.tmpdir"));
        } catch (Exception e) {
            // If reflection fails, continue - the test will still verify save is called
        }
        
        // Use JPG format as per new validation requirements
        when(multipartFile.getOriginalFilename()).thenReturn("image.jpg");
        when(multipartFile.getBytes()).thenReturn("dummy".getBytes());
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L); // 1KB - under 10MB limit
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        
        // Mock the repository.save() to return the product (fixes NullPointerException)
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            return p; // Return the same product that was passed in
        });

        productService.saveProduct(product, multipartFile);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(1)).save(captor.capture());

        Product savedProduct = captor.getValue();
        assertThat(savedProduct.getImageName()).isNotNull();
        assertThat(savedProduct.getImageName()).endsWith(".jpg");
    }
    
    @Test
    void testSaveProductWithPNGShouldFail() throws IOException {
        // Test that PNG files are rejected
        when(multipartFile.getOriginalFilename()).thenReturn("image.png");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        
        try {
            productService.saveProduct(product, multipartFile);
            // Should not reach here
            assertThat(false).as("Should have thrown IllegalArgumentException for PNG file").isTrue();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("Only JPG and JPEG image formats are allowed");
        }
    }
    
    @Test
    void testSaveProductWithLargeFileShouldFail() throws IOException {
        // Test that files > 10MB are rejected
        when(multipartFile.getOriginalFilename()).thenReturn("large-image.jpg");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(11 * 1024 * 1024L); // 11MB - exceeds limit
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        
        try {
            productService.saveProduct(product, multipartFile);
            // Should not reach here
            assertThat(false).as("Should have thrown IllegalArgumentException for large file").isTrue();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("Image size exceeds 10MB limit");
        }
    }
    
    @Test
    void testSaveProductWithoutImage() {
        // Test that products can be saved without images (backward compatibility)
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            return p;
        });
        
        productService.saveProduct(product, null);
        
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(1)).save(captor.capture());
        
        Product savedProduct = captor.getValue();
        assertThat(savedProduct.getImage()).isNull();
        assertThat(savedProduct.getImageName()).isNull();
    }

    @Test
    void testGetAllProducts() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));

        List<Product> products = productService.getAllProducts();

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Test Product");
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetProductByIdFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Product");
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductByIdNotFound() {
        when(productRepository.findById(2L)).thenReturn(Optional.empty());

        Product result = productService.getProductById(2L);

        assertThat(result).isNull();
        verify(productRepository, times(1)).findById(2L);
    }
}
