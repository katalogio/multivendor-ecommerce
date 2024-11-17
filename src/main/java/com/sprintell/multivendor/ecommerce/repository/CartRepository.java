package com.sprintell.multivendor.ecommerce.repository;

import com.sprintell.multivendor.ecommerce.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository <Cart, Long> {
}
