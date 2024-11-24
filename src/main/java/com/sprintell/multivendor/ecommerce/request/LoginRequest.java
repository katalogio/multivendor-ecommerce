package com.sprintell.multivendor.ecommerce.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sprintell.multivendor.ecommerce.domain.USER_ROLE;
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
public class LoginRequest {

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    private String email;


    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters.")
    private String password;

    @NotBlank(message = "OTP is required.")
    @Size(min = 6, max = 6, message = "OTP must be 6 digits.")
    private String otp;




}
