package com.togethershop.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private Long customerId;

    private Long businessId;

    private String externalPaymentId;

    private String paymentMethod;

    private BigDecimal amount;

    private LocalDateTime paymentDate;

    @Lob
    private String rawData;

    private LocalDateTime processedAt;
}