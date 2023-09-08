package com.onecart.onecartApp.repository;

import com.onecart.onecartApp.model.User;
import com.onecart.onecartApp.model.UserBuy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBuyRepository extends JpaRepository<UserBuy, Long> {
    UserBuy findByUser(User user);
}