package com.arunya.smartstock.category.repository;

import com.arunya.smartstock.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByCategoryCode(String categoryCode);
}