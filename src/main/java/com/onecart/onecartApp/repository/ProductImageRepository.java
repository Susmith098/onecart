package com.onecart.onecartApp.repository;

import com.onecart.onecartApp.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}
