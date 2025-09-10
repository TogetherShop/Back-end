package com.togethershop.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "grouppurchase_projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupPurchaseProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(columnDefinition = "TEXT")
    private String description; // DDL: description (제목 역할)

    @Column(name = "total_quantity")
    private Integer totalQuantity; // DDL: total_quantity

    @Column(name = "target_number", nullable = false)
    private Integer targetNumber; // DDL: target_number (목표 인원)

    @Column(name = "target_money", nullable = false)
    private Long targetMoney; // DDL: target_money

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber; // DDL: account_number

    @Column(name = "account_host", nullable = false, length = 50)
    private String accountHost; // DDL: account_host

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProjectStatus status = ProjectStatus.OPEN; // DDL: status

    @Column(name = "end_date")
    private LocalDateTime endDate; // DDL: end_date

    @Column(name = "created_at")
    private LocalDateTime createdAt; // DDL: created_at

    public enum ProjectStatus {
        OPEN, CLOSED, FULFILLED, CANCELLED // DDL과 정확히 일치
    }
}