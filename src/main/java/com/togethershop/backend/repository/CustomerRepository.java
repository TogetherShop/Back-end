package com.togethershop.backend.repository;


import com.togethershop.backend.domain.Customer;
import com.togethershop.backend.dto.AccountStatus;
import com.togethershop.backend.dto.CustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNullApi;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByIdAndStatus(Long customerId, AccountStatus status);


}
