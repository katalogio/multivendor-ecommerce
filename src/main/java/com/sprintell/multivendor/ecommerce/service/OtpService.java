package com.sprintell.multivendor.ecommerce.service;

import com.sprintell.multivendor.ecommerce.exception.OtpException;
import com.sprintell.multivendor.ecommerce.model.VerificationCode;
import com.sprintell.multivendor.ecommerce.repository.VerificationCodeRepository;
import com.sprintell.multivendor.ecommerce.utils.OtpUtils;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private EmailService emailService;

    private static final int OTP_EXPIRATION_TIME_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS = 3;
    private static final int OTP_RESEND_INTERVAL_SECONDS = 60;

    public void sendOtp(String email) throws OtpException.OtpAlreadySentException {
        Optional<VerificationCode> existingCodeOpt = verificationCodeRepository.findByEmail(email);
        if (existingCodeOpt.isPresent()) {
            VerificationCode existingCode = existingCodeOpt.get();

        }

        // Generate new OTP
        String otp = OtpUtils.generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_TIME_MINUTES);

        // Save new OTP
        VerificationCode newCode = new VerificationCode();
        newCode.setEmail(email);
        newCode.setOtp(otp);
        newCode.setExpiresAt(expiresAt);
        newCode.setAttemptCount(0);
        verificationCodeRepository.save(newCode);

        // Send OTP via email
        String subject = "Login/Signup OTP";
        String text = "Your OTP is: " + otp;
        emailService.sendVerificationOtpEmail(email, otp, subject, text);
    }

    @Transactional

    public boolean validateOtp(String email, String otp) throws OtpException.OtpExpiredException,
            OtpException.OtpInvalidException, OtpException.MaxOtpAttemptsExceededException {

        Optional<VerificationCode> codeOpt = verificationCodeRepository.findByOtp(otp);
        if (codeOpt.isEmpty()) {
            System.out.println("No OTP found for email: " + email);
            throw new OtpException.OtpInvalidException("Invalid OTP or no OTP found for email.");
        }

        VerificationCode code = codeOpt.get();

        if (code.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OtpException.OtpExpiredException("OTP has expired.");
        }

        if (!code.getOtp().equals(otp)) {
            code.setAttemptCount(code.getAttemptCount() + 1);
            verificationCodeRepository.save(code);
            int remainingAttempts = MAX_OTP_ATTEMPTS - code.getAttemptCount();
            if (remainingAttempts <= 0) {
                throw new OtpException.MaxOtpAttemptsExceededException("Too many failed attempts. Try again later.");
            }
            throw new OtpException.OtpInvalidException("Invalid OTP. " + remainingAttempts + " attempts remaining.");
        }

        verificationCodeRepository.delete(code);  // Invalidate OTP after successful validation
        return true;
    }


    public void resendOtp(String email) throws OtpException.OtpAlreadySentException {
        Optional<VerificationCode> existingCodeOpt = verificationCodeRepository.findByEmail(email);
        if (existingCodeOpt.isPresent()) {
            VerificationCode existingCode = existingCodeOpt.get();
            if (existingCode.getExpiresAt().isAfter(LocalDateTime.now().minusSeconds(OTP_RESEND_INTERVAL_SECONDS))) {
                throw new OtpException.OtpAlreadySentException("Please wait before requesting a new OTP.");
            }
        }

        sendOtp(email);
    }
}
