package com.example.motiday_api.domain.moti.service;

import com.example.motiday_api.domain.moti.entity.MotiTransaction;
import com.example.motiday_api.domain.moti.entity.TransactionType;
import com.example.motiday_api.domain.moti.repository.MotiTransactionRepository;
import com.example.motiday_api.domain.user.entity.User;
import com.example.motiday_api.domain.user.repository.UserRepository;
import com.example.motiday_api.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MotiTransactionService {

    private final MotiTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // 사용자별 거래 내역 조회
    public List<MotiTransaction> getUserTransactions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        return transactionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // 타입별 거래 내욕 조회 추가
    public List<MotiTransaction> getUserTransactionsByType(Long userId, TransactionType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        return transactionRepository.findByUserAndTypeOrderByCreatedAtDesc(user, type);
    }
}