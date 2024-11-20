package com.sprintell.multivendor.ecommerce.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    private final JavaMailSender javaMailSender;


    public void sendVerificationOtpEmail(String userEmail, String otp, String subject, String text) {
        try {
            // Log the raw email input
            System.out.println("Raw email input: " + userEmail);

            // Parse JSON if userEmail is wrapped in JSON format
            if (userEmail.startsWith("{")) {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> emailData = objectMapper.readValue(userEmail, Map.class);
                userEmail = emailData.get("email");
            }

            // Trim and sanitize
            userEmail = userEmail.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();

            // Validate email format
            InternetAddress emailAddr = new InternetAddress(userEmail);
            emailAddr.validate();

            // Create and send the email
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");

            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(text, true);  // true = HTML content
            mimeMessageHelper.setTo(userEmail);

            javaMailSender.send(mimeMessage);

            System.out.println("Email sent successfully to: " + userEmail);

        } catch (MailException e) {
            System.err.println("MailException: " + e.getMessage());
            throw new MailSendException("Failed to send email due to MailException", e);
        } catch (MessagingException e) {
            System.err.println("MessagingException: " + e.getMessage());
            throw new RuntimeException("Failed to send email due to MessagingException", e);
        } catch (Exception e) {
            System.err.println("Unexpected Exception: " + e.getMessage());
            throw new RuntimeException("Failed to send email due to unexpected exception", e);
        }
    }

    public void sendTokenEmail (String to, String subject, String body){
        try {
            System.out.println("Sending token email to: " + to);

//            trim and validate email format
            to = to.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
            InternetAddress emailAddr = new InternetAddress(to);
            emailAddr.validate();

//            create and send the email
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body, true); //true for HTML content
            mimeMessageHelper.setTo(to);

            javaMailSender.send(mimeMessage);
            System.out.println("Token email sent successfully to" + to);

        } catch (AddressException e) {
            throw new RuntimeException(e);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
