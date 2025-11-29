package com.example.motiday_api.controller;

import com.example.motiday_api.domain.moti.dto.MotiTransactionResponse;
import com.example.motiday_api.domain.moti.entity.MotiTransaction;
import com.example.motiday_api.domain.moti.service.MotiTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MotiTransactionController {

    private final MotiTransactionService motiTransactionService;

    // 모티 거래 내역 조회
    @GetMapping("/users/{userId}/moti/transactions")
    public ResponseEntity<List<MotiTransactionResponse>> getTransactions(
            @PathVariable Long userId,
            @AuthenticationPrincipal Long currentUserId
    ) {
        // 본인 확인
        if (!userId.equals(currentUserId)) {
            return ResponseEntity.status(403).build();
        }

        List<MotiTransaction> transactions = motiTransactionService.getUserTransactions(userId);
        List<MotiTransactionResponse> response = transactions.stream()
                .map(MotiTransactionResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
