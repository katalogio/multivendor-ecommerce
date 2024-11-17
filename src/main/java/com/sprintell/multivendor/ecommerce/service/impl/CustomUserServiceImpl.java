package com.sprintell.multivendor.ecommerce.service.impl;

import com.sprintell.multivendor.ecommerce.domain.USER_ROLE;
import com.sprintell.multivendor.ecommerce.model.Seller;
import com.sprintell.multivendor.ecommerce.model.User;
import com.sprintell.multivendor.ecommerce.repository.SellerRepository;
import com.sprintell.multivendor.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserServiceImpl implements UserDetailsService {

   private final UserRepository userRepository;
   private final SellerRepository sellerRepository;
   private static final String SELLER_PREFIX = "seller_";

    public CustomUserServiceImpl(UserRepository userRepository, SellerRepository sellerRepository) {
        this.userRepository = userRepository;
        this.sellerRepository = sellerRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username.startsWith(SELLER_PREFIX)) {
            String actualUsername = username.substring(SELLER_PREFIX.length());
            Seller seller = sellerRepository.findByEmail(actualUsername)
                    .orElseThrow(() -> new UsernameNotFoundException("Seller not found with email - " + actualUsername));
            return buildUserDetails(seller.getEmail(), seller.getPassword(), seller.getRole());
        } else {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email - " + username));
            return buildUserDetails(user.getEmail(), user.getPassword(), user.getRole());
        }
    }


    private UserDetails buildUserDetails(String email, String password, USER_ROLE role) {

      if(role == null) role = USER_ROLE.ROLE_CUSTOMER;

      List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority("ROLE_" + role));

        return new org.springframework.security.core.userdetails.User(
                email,
                password,
                authorityList
        );
    }
}
