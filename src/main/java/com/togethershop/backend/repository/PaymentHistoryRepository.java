package com.togethershop.backend.repository;

import com.togethershop.backend.domain.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
    Optional<PaymentHistory> findTopByCustomerIdOrderByPaymentDateDesc(Long customerId);

    List<PaymentHistory> findTop3ByCustomerIdOrderByPaymentDateDesc(Long customerId);
}
