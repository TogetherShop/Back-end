package com.togethershop.backend.repository;

import com.togethershop.backend.domain.Customer;
import com.togethershop.backend.domain.CustomerRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRefreshTokenRepository extends JpaRepository<CustomerRefreshToken, Long> {

    Optional<CustomerRefreshToken> findByToken(String token);

    List<CustomerRefreshToken> findAllByCustomer(Customer customer);

    void deleteByCustomer(Customer customer);
}
