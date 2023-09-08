package com.onecart.onecartApp.repository;

import com.onecart.onecartApp.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CategoryRepository extends JpaRepository<Category, Integer> {
    boolean existsByName(String name);
}
