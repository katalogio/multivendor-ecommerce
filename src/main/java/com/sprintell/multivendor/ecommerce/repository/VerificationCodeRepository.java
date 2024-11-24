package com.sprintell.multivendor.ecommerce.repository;

import com.sprintell.multivendor.ecommerce.model.VerificationCode;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository <VerificationCode, Long > {
    @QueryHints(value = @QueryHint(name = "org.hibernate.cacheable", value = "false"))
    Optional<VerificationCode> findByEmail (String email);
    Optional<VerificationCode> findByOtp(String otp);

    void delete(VerificationCode isExist);
}
