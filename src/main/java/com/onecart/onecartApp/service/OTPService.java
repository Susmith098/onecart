package com.onecart.onecartApp.service;

import com.onecart.onecartApp.repository.OTPRepository;
import com.onecart.onecartApp.util.OTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class OTPService {

    private static final int OTP_LENGTH = 6;
    private static final long OTP_EXPIRATION_TIME_MS = 5 * 60 * 1000; // 5 minutes

    @Autowired
    private OTPRepository otpRepository;

    @Autowired
    private EmailService emailService;

    public String generateOTP() {
        int otpLength = 6;
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            int digit = (int) (Math.random() * 10);
            otp.append(digit);
        }
        return otp.toString();
    }

    public void sendOTPByEmail(String email) {
        String otp = generateOTP();
        otpRepository.saveOrUpdateOTP(email, otp);
        emailService.sendOTPEmail(email, otp);
    }

    public boolean verifyOTP(String email, String enteredOTP) {
        OTP otpRecord = otpRepository.findOTPByEmail(email);
        if (otpRecord == null || !otpRecord.getOtp().equals(enteredOTP)) {
            return false; // Invalid OTP
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - otpRecord.getCreatedAt().getTime() > OTP_EXPIRATION_TIME_MS) {
            return false; // OTP expired
        }
        otpRepository.deleteOTP(email); // Delete the OTP after successful verification
        return true;
    }
}

