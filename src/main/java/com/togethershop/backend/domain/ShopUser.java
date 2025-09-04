package com.togethershop.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shop_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;    // 로그인용(이메일 또는 아이디)

    @Column(nullable = false)
    private String password;

    private String shopName;
}
