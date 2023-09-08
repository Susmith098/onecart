package com.onecart.onecartApp.service;

import com.onecart.onecartApp.model.*;
import com.onecart.onecartApp.repository.*;
import com.onecart.onecartApp.util.AuthenticationFacade;
import com.onecart.onecartApp.util.InvoiceGenerator;
import com.onecart.onecartApp.util.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.onecart.onecartApp.controller.HomeController.uploadImgDir;

@Service
public class UserService {


    @Autowired
    UserRepository userRepository;

    @Autowired
    private OTPService otpService;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserCartRepository userCartRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    ProductService productService;


    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    CouponUsageRepository couponUsageRepository;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    UserBuyRepository userBuyRepository;

    private final InvoiceGenerator invoiceGenerator;

    public List<User> getAllUser(){
        return userRepository.findAll();
    }

    public int getTotalUsers() {
        return (int) userRepository.count();
    }

//    user block and unblock

    public User blockUser(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setBlocked(true);
            return userRepository.save(user);
        }
        return null;
    }

    public User unblockUser(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setBlocked(false);
            return userRepository.save(user);
        }
        return null;
    }

    public User registerUser(User user) {
        String otp = otpService.generateOTP();
        user.setOtp(otp);

        return userRepository.save(user);
    }

    public boolean verifyOTP(String email, String enteredOTP) {
        Optional<User> userOptional = userRepository.findUserByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getOtp() != null && user.getOtp().equals(enteredOTP)) {
                // Check if OTP has expired
                LocalDateTime currentTime = LocalDateTime.now();
                LocalDateTime otpGeneratedTime = user.getOtpGeneratedTime();
                if (currentTime.minusMinutes(5).isBefore(otpGeneratedTime)) {
                    user.setOtp(null);
                    user.setOtpGeneratedTime(null);
                    userRepository.save(user);
                    return true; // OTP verification successful
                }
            }
        }
        return false; // OTP verification failed
    }

    public User getLoggedInUser() {
        String loggedInUserEmail = authenticationFacade.getAuthentication().getName();
        return userRepository.findUserByEmail(loggedInUserEmail).orElse(null);
    }

    public User updateUserProfile(User updatedUser, MultipartFile profileImage) {
        User loggedInUser = getLoggedInUser();

        if (loggedInUser != null) {
            loggedInUser.setFirstName(updatedUser.getFirstName());
            loggedInUser.setLastName(updatedUser.getLastName());
            loggedInUser.setEmail(updatedUser.getEmail());
            loggedInUser.setMobilePhoneNumber(updatedUser.getMobilePhoneNumber());
            // Update other profile fields as needed

            try {
                if (!profileImage.isEmpty()) {
                    String imageUUID = profileImage.getOriginalFilename();
                    Path fileNameAndPath = Paths.get(uploadImgDir, imageUUID);

                    try (OutputStream outputStream = Files.newOutputStream(fileNameAndPath)) {
                        outputStream.write(profileImage.getBytes());
                    }

                    loggedInUser.setProfileImageName(imageUUID); // Update the profile image name
                }

                return userRepository.save(loggedInUser);
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the error appropriately, such as logging or showing an error message
                return null;
            }
        }

        return null;
    }


    public void addAddressToUser(String email, Address address) {
        User user = userRepository.findUserByEmail(email).orElse(null);
        if (user != null) {
            address.setUser(user);
            user.getAddresses().add(address);
            userRepository.save(user);
        }
    }


    public User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public void updateUserPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }


    public UserCart getOrCreateUserCart(User user) {
        UserCart cart = userCartRepository.findByUser(user);
        if (cart == null) {
            cart = new UserCart();
            cart.setUser(user);
            userCartRepository.save(cart);
        }
        return cart;
    }

    public void addToUserCart(User user, Product product) {
        UserCart cart = getOrCreateUserCart(user);
        cart.getProducts().add(product);
        userCartRepository.save(cart);
    }

    public void removeFromUserCart(User user, Long productId) {
        UserCart cart = getOrCreateUserCart(user);
        Optional<Product> product = productService.getProductById(productId);
        product.ifPresent(cart.getProducts()::remove);
        userCartRepository.save(cart);
    }

    public Address getAddressById(Long addressId) {
        return addressRepository.findById(addressId).orElse(null);
    }

    public UserCart getUserCart(User user) {
        return getOrCreateUserCart(user);
    }

    public UserService(InvoiceGenerator invoiceGenerator) {
        this.invoiceGenerator = invoiceGenerator;
    }

    @Transactional
    public void placeOrder(Order order, UserCart userCart) {

        double shippingCharge = 40.0;
        double taxPercentage = 0.05; // 5%
        double taxAmount = userCart.getTotalCartAmount() * taxPercentage;

        Coupon appliedCoupon = userCart.getUser().getAppliedCoupon();
        double totalCartAmount = userCart.getTotalCartAmount();

        double totalAmount;

        if (appliedCoupon != null) {
            double discountAmount = totalCartAmount * (appliedCoupon.getDiscountPercentage() / 100);
            totalCartAmount -= discountAmount;

            totalAmount = totalCartAmount + shippingCharge + taxAmount;

            CouponUsage couponUsage = new CouponUsage();
            couponUsage.setUser(userCart.getUser()); // Set the user from the userCart
            couponUsage.setCoupon(appliedCoupon);
            couponUsage.setUsedAt(LocalDateTime.now());
            couponUsageRepository.save(couponUsage);
            userCart.getUser().setAppliedCouponUsage(couponUsage); // Update the user's appliedCouponUsage
            userRepository.save(userCart.getUser());
        }
        else {

            // Calculate the total amount
            totalAmount = totalCartAmount + shippingCharge + taxAmount;
        }
        // Set order details and save the order
        order.setOrderDateTime(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        orderRepository.save(order);

        for (Product product : userCart.getProducts()) {
            int orderedQuantity = 1;
            if (product.getStockQuantity() >= orderedQuantity) {
                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(product);
                orderItem.setQuantity(orderedQuantity);
                orderItem.setOrder(order);
                orderItemRepository.save(orderItem);

                // Update inventory quantity
                product.setStockQuantity(product.getStockQuantity() - orderedQuantity);
                productService.saveProduct(product);

            } else {
                // Handle out of stock scenario
                // Inform user or take appropriate action
            }
        }

        try {
            OutputStream outputStream = new FileOutputStream("invoice_" + order.getId() + ".pdf");
            invoiceGenerator.generateInvoice(order, outputStream);
            outputStream.close();
        } catch (IOException e) {
            // Handle exceptions
        }

        userCart.getProducts().clear();
        userCart.setTotalCartAmount(0.0);
        userCartRepository.save(userCart);
    }

    @Transactional
    public void placeOrderBuy(Order order) {
        double shippingCharge = 40.0;
        double taxPercentage = 0.05; // 5%

        Product product = order.getProduct();

        if (product == null) {
            return;
        }

        double productPrice = product.getPrice();
        double taxAmount = productPrice * taxPercentage;

        // Calculate the total amount
        double totalAmount = productPrice + shippingCharge + taxAmount;

        // Set order details and save the order
        order.setOrderDateTime(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(1);
        orderItem.setOrder(order);
        orderItemRepository.save(orderItem);

        // Update inventory quantity
        int orderedQuantity = 1;
        if (product.getStockQuantity() >= orderedQuantity) {
            product.setStockQuantity(product.getStockQuantity() - orderedQuantity);
            productService.saveProduct(product);
        } else {

        }

        try {
            OutputStream outputStream = new FileOutputStream("invoice_" + order.getId() + ".pdf");
            invoiceGenerator.generateInvoice(order, outputStream);
            outputStream.close();
        } catch (IOException e) {
            // Handle exceptions
        }

    }

    public Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));
    }







    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUser(user);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if ("Online Payment".equals(order.getPaymentMethod())) {
            User user = order.getUser();
            Wallet wallet = user.getWallet();

            if (wallet == null) {
                // Create a new wallet for the user if it doesn't exist
                wallet = new Wallet();
                wallet.setUser(user);
                wallet.setBalance(BigDecimal.ZERO); // Set an initial balance of zero
                walletRepository.save(wallet);
            }

            BigDecimal orderAmount = BigDecimal.valueOf(order.getTotalAmount()); // Use BigDecimal for order amount
            BigDecimal currentBalance = wallet.getBalance();
            BigDecimal newBalance = currentBalance.add(orderAmount); // Update balance

            wallet.setBalance(newBalance);
            walletRepository.save(wallet);
            order.setStatus(OrderStatus.CANCELED);
            orderRepository.save(order);
        } else {
            throw new IllegalStateException("Order is not in a cancelable state");
        }
    }





    @Transactional
    public void returnOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        order.setStatus(OrderStatus.RETURN);
        orderRepository.save(order);
    }

    public void saveUserCart(UserCart userCart) {
        userCartRepository.save(userCart);
    }





}
