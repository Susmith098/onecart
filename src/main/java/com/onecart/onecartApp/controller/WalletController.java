package com.onecart.onecartApp.controller;

import com.onecart.onecartApp.model.User;
import com.onecart.onecartApp.model.Wallet;
import com.onecart.onecartApp.service.UserService;
import com.onecart.onecartApp.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class WalletController {

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @GetMapping("/wallet")
    public String viewWallet(Model model, Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getName());
        Wallet wallet = walletService.getWalletByUser(user);
        model.addAttribute("wallet", wallet);
        return "wallet";
    }

//    @PostMapping("/wallet/deposit")
//    public String deposit(@RequestParam BigDecimal amount, Authentication authentication) {
//        User user = userService.getUserByEmail(authentication.getName());
//        walletService.deposit(user, amount);
//        return "redirect:/wallet";
//    }
//
//    @PostMapping("/wallet/withdraw")
//    public String withdraw(@RequestParam BigDecimal amount, Authentication authentication, Model model) {
//        User user = userService.getUserByEmail(authentication.getName());
//        boolean success = walletService.withdraw(user, amount);
//        if (success) {
//            return "redirect:/wallet";
//        } else {
//            model.addAttribute("withdrawError", "Insufficient balance");
//            return "wallet";
//        }
//    }
}

