package com.togethershop.backend.domain;


import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "customer_refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private Date expiryDate;

    private boolean revoked = false;
}

