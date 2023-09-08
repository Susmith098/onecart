package com.onecart.onecartApp.repository;

import com.onecart.onecartApp.model.Address;
import com.onecart.onecartApp.model.Order;
import com.onecart.onecartApp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findByShippingAddressString(Address addressString);

    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    Double calculateTotalSalesAmount();

}
