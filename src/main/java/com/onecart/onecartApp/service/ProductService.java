package com.onecart.onecartApp.service;

import com.onecart.onecartApp.model.Category;
import com.onecart.onecartApp.model.Product;
import com.onecart.onecartApp.model.ProductImage;
import com.onecart.onecartApp.repository.CategoryRepository;
import com.onecart.onecartApp.repository.ProductImageRepository;
import com.onecart.onecartApp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductImageRepository productImageRepository;

    @Autowired
    CategoryRepository categoryRepository;

    public List<Product> getAllProduct(){
        return productRepository.findAll();
    }

    public void addProduct(Product product) {
        for (ProductImage image : product.getImages()) {
            image.setProduct(product);
        }
        productRepository.save(product);
    }

    public void removeProductById(Long id){
        productRepository.deleteById(id);
    }

    public Optional<Product> getProductById(Long id){
        return productRepository.findById(id);
    }

    public List<Product> getAllProductsByCategoryId(int id){
        return productRepository.findAllByCategory_Id(id);
    }

//    public Long saveImageAndGetId(ProductImage image) {
//        ProductImage savedImage = productImageRepository.save(image);
//        return savedImage.getId();
//    }

    public Long saveImageAndGetId(String imageName) {
        ProductImage image = new ProductImage();
        image.setImageName(imageName);
        ProductImage savedImage = productImageRepository.save(image);

        return savedImage.getId();
    }

    public List<Product> searchProducts(String query) {
        return productRepository.search(query);
    }

    public void saveProduct(Product product){
        productRepository.save(product);
    }

    @Transactional
    public void restockProduct(Long productId, int quantity) {
        Product product = getProductById(productId).orElseThrow(() -> new EntityNotFoundException("Product not found"));
        product.setStockQuantity(product.getStockQuantity() + quantity);
        saveProduct(product);
    }

    public boolean isProductNameExists(String productName){
        return productRepository.existsByName(productName);
    }

    public void addCategory(Category category) {
        categoryRepository.save(category);
    }

    public List<Product> getProductsLowStock() {
        return productRepository.findLowStockProducts();
    }

    public List<Product> getProductsByIds(List<Long> productIds) {
        return productRepository.findAllById(productIds);
    }

    public int getTotalProducts() {
        return (int) productRepository.count();
    }



}
