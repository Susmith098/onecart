package com.onecart.onecartApp.repository;

import com.onecart.onecartApp.model.Address;
import com.onecart.onecartApp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findUserByEmail(String email);


}
