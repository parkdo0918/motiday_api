package com.example.motiday_api.controller;

import com.example.motiday_api.domain.product.dto.ProductResponse;
import com.example.motiday_api.domain.product.entity.Product;
import com.example.motiday_api.domain.product.service.ProductService;
import com.example.motiday_api.domain.routine.entity.routine.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 전체 상품 조회
    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) Category category
    ) {
        List<Product> products;

        if (category != null) {
            products = productService.getProductsByCategory(category);
        } else {
            products = productService.getAllProducts();
        }

        List<ProductResponse> response = products.stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

}