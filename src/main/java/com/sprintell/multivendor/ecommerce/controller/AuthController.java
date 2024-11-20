package com.sprintell.multivendor.ecommerce.controller;

import com.sprintell.multivendor.ecommerce.domain.USER_ROLE;
import com.sprintell.multivendor.ecommerce.exception.OtpException;
import com.sprintell.multivendor.ecommerce.model.User;
import com.sprintell.multivendor.ecommerce.request.PasswordResetConfirmationRequest;
import com.sprintell.multivendor.ecommerce.request.PasswordResetRequest;
import com.sprintell.multivendor.ecommerce.response.ApiResponse;
import com.sprintell.multivendor.ecommerce.response.AuthResponse;
import com.sprintell.multivendor.ecommerce.request.SignupRequest;
import com.sprintell.multivendor.ecommerce.request.LoginRequest;
import com.sprintell.multivendor.ecommerce.service.AuthService;
import com.sprintell.multivendor.ecommerce.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")

public class AuthController {

    private final AuthService authService;

    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/signup/send-otp")
    public ResponseEntity<ApiResponse> sendSignupOtp(@RequestBody String email) {
        try {
            authService.sendSignupOtp(email);
            return ResponseEntity.ok(new ApiResponse().setMessage("Signup OTP sent successfully."));
        } catch (OtpException.OtpAlreadySentException e) { // Handle the right custom exception
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse().setMessage(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse().setMessage("Something went wrong. Please try again."));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandler(@RequestBody SignupRequest req) throws Exception {
        String jwt = authService.createUser(req);
        AuthResponse res = new AuthResponse(jwt, "register success", USER_ROLE.ROLE_CUSTOMER);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> loginHandler(@RequestBody LoginRequest req) throws Exception {
        AuthResponse res = authService.login(req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/send/login-otp") // Changed to match the service method name
    public ResponseEntity<ApiResponse> sendLoginOtpHandler(@RequestBody LoginRequest req) throws Exception { // Expecting email and password
        authService.sendLoginOtp(req.getEmail(), req.getPassword());  // Pass both email and password
        return ResponseEntity.ok(new ApiResponse().setMessage("Otp sent successfully"));
    }





    // Request password reset
    @PostMapping("/password-reset")
    public ResponseEntity<String> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        passwordResetService.sendResetLink(request.getEmail());
        return ResponseEntity.ok("Password reset link sent successfully.");
    }

    // Confirm password reset
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<String> confirmPasswordReset(@RequestBody PasswordResetConfirmationRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successfully.");
    }
}
