package com.onecart.onecartApp.repository;

import com.onecart.onecartApp.model.Coupon;
import com.onecart.onecartApp.model.CouponUsage;
import com.onecart.onecartApp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    boolean existsByUserAndCoupon(User user, Coupon coupon);
}
