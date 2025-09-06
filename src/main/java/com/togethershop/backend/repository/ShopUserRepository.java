package com.togethershop.backend.repository;

import com.togethershop.backend.domain.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopUserRepository extends JpaRepository<Business, Long> {
    Optional<Business> findByUsername(String username);
}
