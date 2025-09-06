package com.togethershop.backend.repository;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.domain.BusinessRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<BusinessRefreshToken, Long> {
    Optional<BusinessRefreshToken> findByToken(String token);

    List<BusinessRefreshToken> findAllByUser(Business user);

    void deleteByUser(Business user);
}