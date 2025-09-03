package com.togethershop.backend.repository;


import com.togethershop.backend.domain.Businesses;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessRepository extends JpaRepository<Businesses, Long> {
}
