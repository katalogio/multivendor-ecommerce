package com.sprintell.multivendor.ecommerce.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data

public class ApiResponse {
    private String message;

    public ApiResponse setMessage(String message) {
        this.message = message;
        return this; // Return the instance for method chaining.
    }

    public String getMessage() {
        return message;
    }
}
