package com.onecart.onecartApp.repository;

import com.onecart.onecartApp.model.Order;
import com.onecart.onecartApp.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    public Payment findByOrderId(String orderId);
}
