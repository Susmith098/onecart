package com.onecart.onecartApp.repository;

import com.onecart.onecartApp.model.Address;
import com.onecart.onecartApp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findAddressesByUser(User user);
}
