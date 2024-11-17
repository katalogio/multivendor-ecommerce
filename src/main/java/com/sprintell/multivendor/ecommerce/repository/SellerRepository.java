package com.sprintell.multivendor.ecommerce.repository;

import java.util.Optional;

import com.sprintell.multivendor.ecommerce.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<Seller, Long> {
    Optional<Seller> findByEmail(String email);  // Change return type to Optional<Seller>
}
