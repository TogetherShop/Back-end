package com.togethershop.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "current_quantity")
    private Integer currentQuantity;

    @Column(name = "max_use_per_customer")
    private Integer maxUsePerCustomer;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "business_id")
    private Long businessId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @Column(name = "accepted_by_requester")
    private Boolean acceptedByRequester;

    @Column(name = "accepted_by_recipient")
    private Boolean acceptedByRecipient;

}
