package com.togethershop.backend.domain;

import com.togethershop.backend.dto.AccountStatus;
import com.togethershop.backend.dto.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "business")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_id", nullable = false)
    private Long id;

    private String username;
    private String email;

    @Column(name = "password_hash")
    private String password;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "business_registration_number")
    private String businessRegistrationNumber;

    @Column(name = "representative_name")
    private String representativeName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "business_type")
    private String businessType;

    @Column(name = "business_category")
    private String businessCategory;

    private String address;

    private Double latitude;
    private Double longitude;

    @Column(name = "business_hours")
    private String businessHours;

    private String description;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "collaboration_category")
    private String collaborationCategory;
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "together_index", precision = 4, scale = 1)
    private BigDecimal togetherIndex;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.ACTIVE;

    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL)
    private List<Partnership> sentPartnerships;

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL)
    private List<Partnership> receivedPartnerships;
}
