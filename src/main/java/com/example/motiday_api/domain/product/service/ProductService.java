package com.example.motiday_api.domain.product.service;

import com.example.motiday_api.domain.product.entity.Product;
import com.example.motiday_api.domain.product.repository.ProductRepository;
import com.example.motiday_api.domain.routine.entity.routine.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    // 전체 상품 조회
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 카테고리별 상품 조회
    public List<Product> getProductsByCategory(Category category) {
        return productRepository.findByCategory(category);
    }

}