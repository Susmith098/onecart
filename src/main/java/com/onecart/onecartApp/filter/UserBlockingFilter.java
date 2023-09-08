package com.onecart.onecartApp.filter;

import com.onecart.onecartApp.model.User;
import com.onecart.onecartApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class UserBlockingFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            Optional<User> user = userRepository.findUserByEmail(userEmail);

            if (user.isPresent() && user.get().isBlocked()) {
                SecurityContextHolder.clearContext();
                response.sendRedirect("/blocked"); // Customize the blocked page URL
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}