package com.onecart.onecartApp.controller;

import com.onecart.onecartApp.model.User;
import com.onecart.onecartApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class OTPController {

    @Autowired
    UserRepository userRepository;

    @GetMapping("/verify-otp")
    public String verifyOTP(@RequestParam("email") String email, @RequestParam("otp") String otp, Model model) {
        Optional<User> userOptional = userRepository.findUserByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getOtp() != null && user.getOtp().equals(otp)) {
                // Mark OTP as verified and save the user
                user.setOtpVerified(true);
                userRepository.save(user);

                return "redirect:/";
            }
        }

        // OTP verification failed, redirect back to OTP verification page
        model.addAttribute("verificationFailed", true);
        model.addAttribute("email", email);
        return "otpVerification";
    }
}

