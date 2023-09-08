package com.onecart.onecartApp.repository;

import com.onecart.onecartApp.model.User;
import com.onecart.onecartApp.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Wallet findByUser(User user);
}

