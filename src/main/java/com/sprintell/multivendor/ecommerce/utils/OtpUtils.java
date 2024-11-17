package com.sprintell.multivendor.ecommerce.utils;

import java.util.Random;

public class OtpUtils {

    public static String generateOtp(){
        //    create a 6 digit otp
//    define otp length
        int otpLength = 6;

//    Otp should be generated at random
        Random random = new Random();
        StringBuilder otp = new StringBuilder(otpLength);

        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }




}
