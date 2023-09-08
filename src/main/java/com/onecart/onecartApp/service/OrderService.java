package com.onecart.onecartApp.service;

import com.onecart.onecartApp.model.Address;
import com.onecart.onecartApp.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<Order> getAllOrders();
    Optional<Order> getOrderById(Long orderId);

    List<Order> getOrdersByShippingAddress(Address address);
    void saveOrder(Order order);

    int getTotalOrders();

    List<Order> getRecentOrders();

    double getTotalSalesAmount();

}
