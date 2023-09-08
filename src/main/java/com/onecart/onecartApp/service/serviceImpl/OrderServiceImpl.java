package com.onecart.onecartApp.service.serviceImpl;

import com.onecart.onecartApp.model.Address;
import com.onecart.onecartApp.model.Order;
import com.onecart.onecartApp.repository.OrderRepository;
import com.onecart.onecartApp.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private final OrderRepository orderRepository;

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }
    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    @Override
    public List<Order> getOrdersByShippingAddress(Address address) {
         return orderRepository.findByShippingAddressString(address);
    }

    public int getTotalOrders() {
        return (int) orderRepository.count();
    }

    @Override
    public List<Order> getRecentOrders() {
        // Create a Pageable object to fetch the top 5 recent orders sorted by orderDateTime
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "orderDateTime"));

        // Fetch the recent orders
        return orderRepository.findAll(pageable).getContent();
    }

    public double getTotalSalesAmount() {
        return orderRepository.calculateTotalSalesAmount();
    }



}
