package com.example.motiday_api.domain.moti.dto;

import com.example.motiday_api.domain.moti.entity.MotiTransaction;
import com.example.motiday_api.domain.moti.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotiTransactionResponse {
    private Long transactionId;
    private TransactionType type;
    private Integer amount;
    private String description;
    private LocalDateTime createdAt;

    public static MotiTransactionResponse from(MotiTransaction transaction) {
        return MotiTransactionResponse.builder()
                .transactionId(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}