package com.sprintell.multivendor.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne // one user only has one cart
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> cartItems = new HashSet<>();

    private double totalSellingPrice = 0.0;  // default values
    private int totalItem = 0;
    private int totalMrpPrice = 0;
    private int discount = 0;
    private String couponCode = null;

    // Constructor that accepts only a User
    public Cart(User user) {
        this.user = user;
        this.cartItems = new HashSet<>();  // Initialize empty set
        this.totalSellingPrice = 0.0; // Initialize with default values
        this.totalItem = 0;
        this.totalMrpPrice = 0;
        this.discount = 0;
        this.couponCode = null;
    }
}
