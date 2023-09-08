package com.onecart.onecartApp.repository;

import com.onecart.onecartApp.util.OTP;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class OTPRepository {

    private Map<String, OTP> otpMap = new HashMap<>();

    public void saveOrUpdateOTP(String email, String otp) {
        OTP otpRecord = new OTP(email, otp);
        otpMap.put(email, otpRecord);
    }

    public OTP findOTPByEmail(String email) {
        return otpMap.get(email);
    }

    public void deleteOTP(String email) {
        otpMap.remove(email);
    }
}
