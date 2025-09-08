package com.togethershop.backend.repository;

import com.togethershop.backend.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
