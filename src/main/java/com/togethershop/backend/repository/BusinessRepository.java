package com.togethershop.backend.repository;


import com.togethershop.backend.domain.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {
    Optional<Business> findFirstByUsernameOrderByIdAsc(String username);
}
