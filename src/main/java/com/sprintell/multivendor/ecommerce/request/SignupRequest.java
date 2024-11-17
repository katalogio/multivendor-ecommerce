package com.sprintell.multivendor.ecommerce.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignupRequest {
    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    private String email;

    @NotBlank(message = "Full name is required.")
    @Size(min = 2, max = 50, message = "Full name must be between 2 and 50 characters.")
    private String fullName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters.")
    private String password;


    private String otp;
}
