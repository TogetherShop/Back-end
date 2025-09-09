package com.togethershop.backend.service;

import com.togethershop.backend.dto.AccountStatus;
import com.togethershop.backend.dto.CustomerProfileDTO;
import com.togethershop.backend.dto.CustomerStatus;
import com.togethershop.backend.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomerProfileService {
    private final CustomerRepository customerRepository;

    public CustomerProfileDTO getCustomerProfile(Long customerId) {
        return customerRepository.findByIdAndStatus(customerId, AccountStatus.ACTIVE)
                .map(customer -> CustomerProfileDTO.builder()
                        .name(customer.getName())
                        .address("서울시 강남구 삼성동")
                        .email(customer.getEmail())
                        .build())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found or inactive"));
    }
}
