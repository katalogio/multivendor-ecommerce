package com.sprintell.multivendor.ecommerce.repository;

import com.sprintell.multivendor.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    // Add this method to retrieve a User by email
    Optional<User> findByEmail(String email);
}
