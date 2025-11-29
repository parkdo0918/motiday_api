package com.example.motiday_api.domain.product.dto;

import com.example.motiday_api.domain.product.entity.Product;
import com.example.motiday_api.domain.routine.entity.routine.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long productId;
    private String name;
    private String brand;
    private Integer price;
    private Integer originalPrice;
    private Integer discountRate;
    private String imageUrl;
    private Category category;
    private String externalLink;
    private Boolean isSpecialDeal;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .productId(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .discountRate(product.getDiscountRate())
                .imageUrl(product.getImageUrl())
                .category(product.getCategory())
                .externalLink(product.getExternalLink())
                .isSpecialDeal(product.getIsSpecialDeal())
                .build();
    }
}