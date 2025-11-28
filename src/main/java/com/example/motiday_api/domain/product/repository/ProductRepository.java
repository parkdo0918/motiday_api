package com.example.motiday_api.domain.product.repository;

import com.example.motiday_api.domain.product.entity.Product;
import com.example.motiday_api.domain.routine.entity.routine.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 카테고리별 필터
    List<Product> findByCategory(Category category);

}