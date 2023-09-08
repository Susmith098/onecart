package com.onecart.onecartApp.util;

import java.util.Date;

public class OTP {

    private String email;
    private String otp;
    private Date createdAt;

    public OTP(String email, String otp) {
        this.email = email;
        this.otp = otp;
        this.createdAt = new Date();
    }

    public String getEmail() {
        return email;
    }

    public String getOtp() {
        return otp;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}

