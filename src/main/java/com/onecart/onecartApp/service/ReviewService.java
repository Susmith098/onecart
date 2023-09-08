package com.onecart.onecartApp.service;

import com.onecart.onecartApp.model.Product;
import com.onecart.onecartApp.model.Review;
import com.onecart.onecartApp.model.User;
import com.onecart.onecartApp.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> getReviewsByProduct(Product product){
        return reviewRepository.findByProduct(product);
    }

    public void addReview(Product product, User user, String comment, int rating, LocalDate reviewDate, String detailedReview){
        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setComments(comment);
        review.setRating(rating);
        review.setReviewDate(reviewDate);
        review.setDetailedReview(detailedReview);
        reviewRepository.save(review);
    }
}
