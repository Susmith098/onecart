package com.onecart.onecartApp.service;

import com.onecart.onecartApp.model.User;
import com.onecart.onecartApp.model.Wallet;
import com.onecart.onecartApp.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    public Wallet getWalletByUser(User user) {
        return walletRepository.findByUser(user);
    }

    public void deposit(User user, BigDecimal amount) {
        Wallet wallet = getWalletByUser(user);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    public boolean withdraw(User user, BigDecimal amount) {
        Wallet wallet = getWalletByUser(user);
        BigDecimal newBalance = wallet.getBalance().subtract(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            return false; // Insufficient balance
        }

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
        return true;
    }
}
