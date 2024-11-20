package com.sprintell.multivendor.ecommerce.service.impl;

import com.sprintell.multivendor.ecommerce.config.JwtProvider;
import com.sprintell.multivendor.ecommerce.domain.USER_ROLE;
import com.sprintell.multivendor.ecommerce.exception.DuplicateUserException;
import com.sprintell.multivendor.ecommerce.exception.OtpException;
import com.sprintell.multivendor.ecommerce.model.Cart;
import com.sprintell.multivendor.ecommerce.model.PasswordResetToken;
import com.sprintell.multivendor.ecommerce.model.User;
import com.sprintell.multivendor.ecommerce.model.VerificationCode;
import com.sprintell.multivendor.ecommerce.repository.CartRepository;
import com.sprintell.multivendor.ecommerce.repository.UserRepository;
import com.sprintell.multivendor.ecommerce.repository.VerificationCodeRepository;
import com.sprintell.multivendor.ecommerce.request.LoginRequest;
import com.sprintell.multivendor.ecommerce.request.SignupRequest;
import com.sprintell.multivendor.ecommerce.response.AuthResponse;
import com.sprintell.multivendor.ecommerce.service.AuthService;
import com.sprintell.multivendor.ecommerce.service.EmailService;
import com.sprintell.multivendor.ecommerce.service.OtpService;
import com.sprintell.multivendor.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final JwtProvider jwtProvider;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private final OtpService otpService;
    private final UserService userService;

    @Override
    public void sendSignupOtp(String email) throws Exception {
        try {
            // Check if a user exists with the given email
            userService.validateUniqueUser(email);

            // If no exception is thrown, it means the email is already in use
            throw new Exception("Email already in use. Please log in.");
        } catch (UsernameNotFoundException e) {
            // If no user is found, it's safe to send the OTP
            otpService.sendOtp(email);
        } catch (DuplicateUserException e) {
            // Handle the case where multiple users exist with the same email
            throw new Exception("Multiple accounts found with this email. Please contact support.");
        }
    }
    @Override
    public void sendLoginOtp(String email, String password) throws Exception {
        try {
            // Validate user and retrieve user details
            User user = userService.validateUniqueUser(email);

            // Check if password matches
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new Exception("Invalid email or password.");
            }


            // Send OTP
            otpService.sendOtp(email);

        } catch (UsernameNotFoundException e) {
            throw new Exception("User not found with email: " + email);
        } catch (DuplicateUserException e) {
            throw new Exception("Multiple accounts found with this email. Please contact support.");
        } catch (Exception e) {
            throw new Exception("Failed to send OTP. Reason: " + e.getMessage());
        }
    }


    @Override
    public String createUser(SignupRequest req) throws Exception {
        try {
            // Validate OTP
            otpService.validateOtp(req.getEmail(), req.getOtp());
        } catch (OtpException.OtpExpiredException e) {
            throw new BadCredentialsException("OTP has expired. Please request a new one.");
        } catch (OtpException.OtpInvalidException e) {
            throw new BadCredentialsException("Invalid OTP. Please try again.");
        } catch (OtpException.MaxOtpAttemptsExceededException e) {
            throw new BadCredentialsException("Too many failed OTP attempts. Please try again later.");
        }

        // Check if the email is already registered
        Optional<User> existingUserOpt = userRepository.findByEmail(req.getEmail());
        if (existingUserOpt.isPresent()) {
            throw new Exception("Email already registered.");
        }

        // Create a new user
        User newUser = new User();
        newUser.setFullName(req.getFullName());
        newUser.setEmail(req.getEmail());
        newUser.setRole(USER_ROLE.ROLE_CUSTOMER);
        newUser.setPassword(passwordEncoder.encode(req.getPassword()));

        userRepository.save(newUser);
        cartRepository.save(new Cart(newUser)); // Create a cart for the new user

        // Generate JWT token for the new user
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                newUser.getEmail(), null, List.of(new SimpleGrantedAuthority(USER_ROLE.ROLE_CUSTOMER.toString()))
        );
        return jwtProvider.generateToken(authentication);
    }




    @Override
    public AuthResponse login(LoginRequest req) throws Exception {
        // Validate user credentials
        User user = userService.validateUniqueUser(req.getEmail());
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        // Validate OTP
        try {
            otpService.validateOtp(req.getEmail(), req.getOtp());
        } catch (OtpException.OtpExpiredException e) {
            throw new BadCredentialsException("OTP has expired. Please request a new one.");
        } catch (OtpException.OtpInvalidException e) {
            throw new BadCredentialsException("Invalid OTP. Please try again.");
        } catch (OtpException.MaxOtpAttemptsExceededException e) {
            throw new BadCredentialsException("Too many failed OTP attempts. Please try again later.");
        }

        // Authenticate user and generate token
        Authentication authentication = createAuthenticationToken(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtProvider.generateToken(authentication);
        return new AuthResponse(token, "Login successful", USER_ROLE.ROLE_CUSTOMER);
    }

    private Authentication createAuthenticationToken(User user) {
        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(user.getRole().toString())
        );
        return new UsernamePasswordAuthenticationToken(user, null, authorities);
    }


}