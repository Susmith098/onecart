package com.onecart.onecartApp.service;

import com.onecart.onecartApp.model.Product;
import com.onecart.onecartApp.model.User;
import com.onecart.onecartApp.model.WishlistItem;
import com.onecart.onecartApp.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    public List<WishlistItem> getUserWishlist(User user) {
        return wishlistRepository.findByUser(user);
    }

    public void addToWishlist(User user, Product product) {
        WishlistItem wishlistItem = new WishlistItem();
        wishlistItem.setUser(user);
        wishlistItem.setProduct(product);
        wishlistRepository.save(wishlistItem);
    }

    public void removeFromWishlist(Long wishlistItemId) {
        wishlistRepository.deleteById(wishlistItemId);
    }
}

