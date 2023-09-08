package com.onecart.onecartApp.controller;

import com.onecart.onecartApp.model.Role;
import com.onecart.onecartApp.model.User;
import com.onecart.onecartApp.repository.RoleRepository;
import com.onecart.onecartApp.repository.UserRepository;
import com.onecart.onecartApp.service.EmailService;
import com.onecart.onecartApp.service.OTPService;
import com.onecart.onecartApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class LoginController {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    OTPService otpService;

    @GetMapping("/login")
    public String login(Model model){
        model.addAttribute("pageTitle", "Login Page");
        return "login";
    }


    @GetMapping("/register")
    public String registerGet(Model model){
        model.addAttribute("pageTitle", "Registration Page");
        return "register";
    }

    @PostMapping("/register")
    public String registerPost(@ModelAttribute("user") User user, HttpServletRequest request, Model model) throws ServletException {
        String password = user.getPassword();
        user.setPassword(bCryptPasswordEncoder.encode(password));
        List<Role> roles = new ArrayList<>();
        roles.add(roleRepository.findById(2).get());
        user.setRoles(roles);
        userRepository.save(user);
        request.login(user.getEmail(), password);

        // Generate and send OTP email
        String otp = otpService.generateOTP();
        user.setOtp(otp);
        emailService.sendOTPEmail(user.getEmail(), otp);

        user.setActive(true);
        userRepository.save(user);

        model.addAttribute("email", user.getEmail());

        return "otpVerification";
    }
}
