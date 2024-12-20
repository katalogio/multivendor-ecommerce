package com.sprintell.multivendor.ecommerce.service;

import com.sprintell.multivendor.ecommerce.domain.USER_ROLE;
import com.sprintell.multivendor.ecommerce.request.LoginRequest;
import com.sprintell.multivendor.ecommerce.request.SignupRequest;
import com.sprintell.multivendor.ecommerce.response.AuthResponse;

public interface AuthService {

    void sendSignupOtp(String email) throws Exception;


    void sendLoginOtp(String email, String password, USER_ROLE role) throws Exception;

    String createUser(SignupRequest req) throws Exception;



    AuthResponse login(LoginRequest req) throws Exception;
}
