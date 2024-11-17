package com.sprintell.multivendor.ecommerce.service.impl;

import com.sprintell.multivendor.ecommerce.config.JwtProvider;
import com.sprintell.multivendor.ecommerce.domain.USER_ROLE;
import com.sprintell.multivendor.ecommerce.exception.OtpException;
import com.sprintell.multivendor.ecommerce.model.Cart;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Override
    public void sendSignupOtp(String email) throws Exception {
        if (userRepository.existsByEmail(email)) {  // Check if email already exists
            throw new Exception("Email already in use. Please log in.");
        }
        otpService.sendOtp(email);  // Send OTP after verifying the email is not in use
    }

    @Override
    public void sendLoginOtp(String email, String password) throws Exception {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new Exception("User not found.");
        }
        User user = userOpt.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception("Invalid email or password.");
        }

        otpService.sendOtp(email);  // Send OTP for login
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
    public String resetPassword() {
        return "";
    }

    @Override
    public String changePassword() {
        return "";
    }

    @Override
    public AuthResponse login(LoginRequest req) throws Exception {
        Authentication authentication = authenticate(req.getEmail(), req.getPassword(), req.getOtp());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtProvider.generateToken(authentication);
        AuthResponse response = new AuthResponse(token, "Login successful", USER_ROLE.ROLE_CUSTOMER);
        response.setJwt(token);
        response.setMessage("Login successful");
        response.setRole(USER_ROLE.ROLE_CUSTOMER); // Set the user's role
        return response;
    }

    private Authentication authenticate(String email, String password, String otp) throws Exception {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new BadCredentialsException("Invalid credentials.");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials.");
        }

//        boolean otpValid = otpService.validateOtp(email, otp);
//        if (!otpValid) {
//            throw new BadCredentialsException("Invalid OTP.");
//        }
        Optional<VerificationCode> verificationCode = verificationCodeRepository.findByEmail(email);
        if (verificationCode.isEmpty() || !verificationCode.get().getOtp().equals(otp) ){
            throw new BadCredentialsException("invalid otp");
        }

        Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().toString()));
        return new UsernamePasswordAuthenticationToken(user, null, authorities);
    }
}
