package com.onecart.onecartApp.repository;

import com.onecart.onecartApp.model.User;
import com.onecart.onecartApp.model.UserCart;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserCartRepository extends JpaRepository<UserCart, Long> {
    UserCart findByUser(User user);
}
