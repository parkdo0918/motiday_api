package com.example.motiday_api.domain.product.entity;

import com.example.motiday_api.domain.common.BaseTimeEntity;
import com.example.motiday_api.domain.routine.entity.routine.Category;
import jakarta.persistence.*;
import lombok.*;

/**
 * 상품(제품) 엔티티
 * 제휴 영양제 정보 관리 - 외부 링크 연결
 */
@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;  // 제품 고유 ID

    @Column(nullable = false, length = 200)
    private String name;  // 제품명 (예: "아나로민 플러스, 2박스, 20개입")

    @Column(length = 100)
    private String brand;  // 브랜드명 (예: "센트룸")

    @Column(nullable = false)
    private Integer price;  // 판매 가격 (59,900원)

    @Column(name = "original_price")
    private Integer originalPrice;  // 원가 (150,000원)

    @Column(name = "discount_rate")
    @Builder.Default
    private Integer discountRate = 0;  // 할인율 (63%)

    @Column(length = 500)
    private String imageUrl;  // 제품 이미지 URL

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Category category;  // 카테고리 필터용 (전체보기/운동/독서/공부)

    @Column(nullable = false, length = 500)
    private String externalLink;  // 외부 구매 링크

    @Column(name = "is_special_deal", nullable = false)
    @Builder.Default
    private Boolean isSpecialDeal = false;  // 특가 할인 배지 여부 (노란 배지)

    // ========== 비즈니스 메서드 ==========

    /**
     * 할인율 자동 계산
     * 원가와 판매가로 할인율 계산
     */
    public void calculateDiscountRate() {
        if (this.originalPrice != null && this.originalPrice > 0) {
            this.discountRate = (int) (((this.originalPrice - this.price)
                    / (double) this.originalPrice) * 100);
        }
    }

    /**
     * 제품 정보 수정
     * @param name 제품명
     * @param price 가격
     * @param originalPrice 원가
     */
    public void updateInfo(String name, Integer price, Integer originalPrice) {
        this.name = name;
        this.price = price;
        this.originalPrice = originalPrice;
        calculateDiscountRate();
    }

    /**
     * 특가 할인 배지 설정
     * @param isSpecial 특가 여부
     */
    public void setSpecialDeal(boolean isSpecial) {
        this.isSpecialDeal = isSpecial;
    }
}