package com.example.motiday_api.domain.moti.repository;

import com.example.motiday_api.domain.moti.entity.MotiTransaction;
import com.example.motiday_api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MotiTransactionRepository extends JpaRepository<MotiTransaction, Long> {

    // 사용자별 거래 내역 (최신순)
    List<MotiTransaction> findByUserOrderByCreatedAtDesc(User user);
}