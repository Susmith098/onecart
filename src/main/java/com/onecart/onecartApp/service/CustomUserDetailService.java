package com.onecart.onecartApp.service;

import com.onecart.onecartApp.model.CustomUserDetail;
import com.onecart.onecartApp.model.User;
import com.onecart.onecartApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        Optional<User> user = userRepository.findUserByEmail(email);
//        user.orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
//        return user.map(CustomUserDetail:: new).get();
//    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findUserByEmail(email);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        User user = userOptional.get();

        if (user.isBlocked()) {
            throw new DisabledException("User is blocked");
        }

        // Check if user is OTP verified
        boolean isOtpVerified = user.getOtp() == null || user.isOtpVerified();

        CustomUserDetail customUserDetail = new CustomUserDetail(user);
        customUserDetail.setOtpVerified(isOtpVerified);

        return customUserDetail;
    }
    public boolean isUserOtpVerified(String email) {
        Optional<User> userOptional = userRepository.findUserByEmail(email);
        return userOptional.map(user -> user.getOtp() != null && user.isOtpVerified()).orElse(false);
    }
}
