package com.sprintell.multivendor.ecommerce.service.impl;

import com.sprintell.multivendor.ecommerce.config.JwtProvider;
import com.sprintell.multivendor.ecommerce.domain.USER_ROLE;
import com.sprintell.multivendor.ecommerce.exception.DuplicateUserException;
import com.sprintell.multivendor.ecommerce.exception.OtpException;
import com.sprintell.multivendor.ecommerce.model.*;
import com.sprintell.multivendor.ecommerce.repository.CartRepository;
import com.sprintell.multivendor.ecommerce.repository.SellerRepository;
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
import org.springframework.security.core.userdetails.UserDetails;
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
    private final SellerRepository sellerRepository;
   private final CustomUserServiceImpl customUserService;

    @Override
    public void sendSignupOtp(String email) throws Exception {
            String SELLER_PREFIX = "seller_";
        try {
            // Check if a user exists with the given email
            userService.validateUniqueUser(email);

            // If no exception is thrown, it means the email is already in use
            throw new Exception("Email already in use. Please log in.");
        } catch (UsernameNotFoundException e) {
            // If no user is found, it's safe to send the OTP
           verificationCodeRepository.findByEmail(email).ifPresent(verificationCodeRepository::delete);

            otpService.sendOtp(email);
        } catch (DuplicateUserException e) {
            // Handle the case where multiple users exist with the same email
            throw new Exception("Multiple accounts found with this email. Please contact support.");
        }catch (Exception e) {
            // Catch any other unexpected errors
            throw new Exception("An error occurred while sending the signup OTP: " + e.getMessage(), e);
        }
    }
    @Override
    public void sendLoginOtp(String email, String password, USER_ROLE role) throws Exception {
        try {
            // Fetch user or seller and validate the password
            String hashedPassword = getHashedPasswordByRole(email, role);

            // Validate password
            if (!passwordEncoder.matches(password, hashedPassword)) {
                throw new Exception("Invalid credentials.");
            }

            // Delete any existing OTP for the email
            verificationCodeRepository.findByEmail(email).ifPresent(verificationCodeRepository::delete);

            // Send OTP
            otpService.sendOtp(email);
        } catch (DuplicateUserException | UsernameNotFoundException e) {
            throw new Exception("User not found: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Failed to send OTP. Reason: " + e.getMessage());
        }
    }

    private String getHashedPasswordByRole(String email, USER_ROLE role) throws Exception {
        if (role.equals(USER_ROLE.ROLE_SELLER)) {
            Seller seller = userService.validateUniqueSeller(email);
            return seller.getPassword();
        } else {
            User user = userService.validateUniqueUser(email);
            return user.getPassword();
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
        String username = req.getEmail();
        String otp = req.getOtp();
        // Authenticate user and generate token
        Authentication authentication = authenticate(req.getEmail(), req.getPassword(), req.getOtp());
        SecurityContextHolder.getContext().setAuthentication(authentication);


       String token = jwtProvider.generateToken(authentication);
       AuthResponse authResponse = new AuthResponse();
       authResponse.setJwt(token);
       authResponse.setMessage("Login success");

       Collection <? extends GrantedAuthority> authorities = authentication.getAuthorities();
       String roleName = authorities.isEmpty()?null:authorities.iterator().next().getAuthority();

        if (roleName != null && roleName.startsWith("ROLE_")) {
            // Remove "ROLE_" prefix before mapping to USER_ROLE
            roleName = roleName.substring(5); // Remove "ROLE_" part
            try {
                // Map role string to USER_ROLE enum
                authResponse.setRole(USER_ROLE.valueOf(roleName));
            } catch (IllegalArgumentException e) {
                throw new Exception("Invalid role: " + roleName);
            }
        } else {
            throw new Exception("Role not found or invalid role format.");
        }

        return authResponse;
    }

    private Authentication authenticate(String email, String password, String otp) throws Exception {
        // Determine if the user is a seller or user
        String SELLER_PREFIX = "seller_";
        UserDetails userDetails;
        if (email.startsWith(SELLER_PREFIX)) {
            // Handle seller authentication
            String actualEmail = email.substring(SELLER_PREFIX.length());
            userDetails = customUserService.loadUserByUsername(SELLER_PREFIX + actualEmail);
        } else {
            // Handle user authentication
            userDetails = customUserService.loadUserByUsername(email);
        }

        // If user details not found
        if (userDetails == null) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        // Validate password
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        Optional<VerificationCode> verificationCode = verificationCodeRepository.findByOtp(otp);
        if (verificationCode.isEmpty() || !verificationCode.get().getOtp().equals(otp)) {
            throw new Exception("Invalid OTP. Please try again.");
        }

        // Return authenticated token
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }



}