package com.togethershop.backend.repository;

import com.togethershop.backend.domain.RefreshToken;
import com.togethershop.backend.domain.ShopUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUser(ShopUser user);

    void deleteByUser(ShopUser user);
}