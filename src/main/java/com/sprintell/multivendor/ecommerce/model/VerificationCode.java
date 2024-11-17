package com.sprintell.multivendor.ecommerce.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Data

public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String otp;

    private String email;


    private LocalDateTime expiresAt;
    private int attemptCount; // Number of failed OTP attempts

    public VerificationCode() {
        this.attemptCount = 0; // Initialize attempt count
    }


    @OneToOne
    private User user;

    @OneToOne
    private Seller seller;

    public boolean isExpired() {
        return this.expiresAt.isBefore(LocalDateTime.now());
    }
}
