package com.onecart.onecartApp.service;

import com.onecart.onecartApp.model.Coupon;
import com.onecart.onecartApp.model.CouponUsage;
import com.onecart.onecartApp.model.User;
import com.onecart.onecartApp.repository.CouponRepository;
import com.onecart.onecartApp.repository.CouponUsageRepository;
import com.onecart.onecartApp.util.CouponAlreadyUsedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CouponService {
    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUsageRepository couponUsageRepository;

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public void saveCoupon(Coupon coupon) {
        couponRepository.save(coupon);
    }

    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
    }

    public Optional<Coupon> getCouponById(Long id) {
        return couponRepository.findById(id);
    }

    public Coupon getCouponByCode(String couponCode) {
        return couponRepository.findByCode(couponCode);
    }

    public boolean isCouponAlreadyUsedByUser(User user, Coupon coupon) {
        return couponUsageRepository.existsByUserAndCoupon(user, coupon);
    }

    public void applyCouponToUser(User user, Coupon coupon) {
        if (isCouponAlreadyUsedByUser(user, coupon)) {
            throw new CouponAlreadyUsedException("This coupon has already been used by the user.");
        }
        CouponUsage couponUsage = new CouponUsage();
        couponUsage.setUser(user);
        couponUsage.setCoupon(coupon);
        couponUsageRepository.save(couponUsage);
    }

}

