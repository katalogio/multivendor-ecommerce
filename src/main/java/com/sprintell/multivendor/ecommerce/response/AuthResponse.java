package com.sprintell.multivendor.ecommerce.response;

import com.sprintell.multivendor.ecommerce.domain.USER_ROLE;
import lombok.Data;

@Data
public class AuthResponse {

    private String jwt;
    private String message;
    private USER_ROLE role;

    // Constructor to initialize fields
    public AuthResponse(String jwt, String message, USER_ROLE role) {
        this.jwt = jwt;
        this.message = message;
        this.role = role;
    }
}
