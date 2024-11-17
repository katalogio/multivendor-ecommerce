package com.sprintell.multivendor.ecommerce.exception;

public class OtpException {
    // OtpAlreadySentException.java
    public static class OtpAlreadySentException extends RuntimeException {
        public OtpAlreadySentException(String message) {
            super(message);
        }
    }

    // OtpExpiredException.java
    public static class OtpExpiredException extends RuntimeException {
        public OtpExpiredException(String message) {
            super(message);
        }
    }

    // OtpInvalidException.java
    public static class OtpInvalidException extends RuntimeException {
        public OtpInvalidException(String message) {
            super(message);
        }
    }

    // MaxOtpAttemptsExceededException.java
    public static class MaxOtpAttemptsExceededException extends RuntimeException {
        public MaxOtpAttemptsExceededException(String message) {
            super(message);
        }
    }

}
