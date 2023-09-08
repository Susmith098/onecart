package com.onecart.onecartApp.controller;

import com.onecart.onecartApp.model.*;
import com.onecart.onecartApp.repository.CountryRepository;
import com.onecart.onecartApp.repository.OrderRepository;
import com.onecart.onecartApp.repository.PaymentRepository;
import com.onecart.onecartApp.service.*;
import com.onecart.onecartApp.util.AddressConverter;
import com.onecart.onecartApp.util.InvoiceGenerator;
import com.onecart.onecartApp.util.OrderStatus;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.razorpay.RazorpayClient;

@Controller
public class HomeController {

    public static String uploadImgDir = System.getProperty("user.dir") + "/src/main/resources/static/profileImages";

    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AddressService addressService;

    @Autowired
    OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceGenerator invoiceGenerator;

    @GetMapping({"/", "/home"})
    public String home(Model model, Principal principal) {
        model.addAttribute("pageTitle", "Home Page");
        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            UserCart userCart = userService.getUserCart(user);
            model.addAttribute("cartCount", userCart.getProducts().size());
        }
        model.addAttribute("products", productService.getAllProduct());
        return "index";
    }

    @GetMapping("/shop")
    public String shop(Model model, Principal principal,
                       @RequestParam(name = "q", required = false) String query,
                       @RequestParam(name = "sort", required = false) String sort) {
        model.addAttribute("categories", categoryService.getAllCategory());

        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            UserCart userCart = userService.getUserCart(user);
            model.addAttribute("cartCount", userCart.getProducts().size());
        }

        List<Product> products;
        if (query != null) {
            products = productService.searchProducts(query);
        } else {
            products = productService.getAllProduct();
        }

        model.addAttribute("products", products);
        model.addAttribute("pageTitle", "Shop Page");
        model.addAttribute("searchQuery", query);

        return "shop";
    }


    @GetMapping("/shop/category/{id}")
    public String shopByCategory(Model model, @PathVariable int id, Principal principal) {
        model.addAttribute("categories", categoryService.getAllCategory());
        model.addAttribute("products", productService.getAllProductsByCategoryId(id));
        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            UserCart userCart = userService.getUserCart(user);
            model.addAttribute("cartCount", userCart.getProducts().size());
        }
        model.addAttribute("pageTitle", "Shop Page");
        return "shop";
    }

    @GetMapping("/shop/viewproduct/{id}")
    public String viewProduct(Model model, @PathVariable int id, Principal principal) {
        Optional<Product> optionalProduct = productService.getProductById((long) id);

        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();

            if (principal != null) {
                User user = userService.getUserByEmail(principal.getName());
                UserCart userCart = userService.getUserCart(user);
                model.addAttribute("cartCount", userCart.getProducts().size());
            }

            List<String> imageNames = product.getImages().stream()
                    .map(ProductImage::getImageName)
                    .collect(Collectors.toList());

            List<Review> reviews = reviewService.getReviewsByProduct(product);
            model.addAttribute("reviews", reviews);

            model.addAttribute("product", product);
            model.addAttribute("imageNames", imageNames);
            model.addAttribute("pageTitle", "Shop Page");
            return "viewProduct";
        } else {
            return "productNotFound";
        }
    }

    @GetMapping("/product/addReview/{productId}")
    public String addReviewPage(@PathVariable Long productId, Model model) {
        model.addAttribute("productId", productId);
        return "reviewAdd";
    }


    @PostMapping("/product/addReview")
    public String addReview(@RequestParam Long productId, @RequestParam String comment,
                            @RequestParam int rating, @RequestParam LocalDate reviewDate, @RequestParam String detailedReview, Authentication authentication) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        User user = userService.getUserByEmail(authentication.getName());
        reviewService.addReview(product, user, comment, rating, reviewDate, detailedReview);
        return "redirect:/shop/viewproduct/" + product.getId();
    }



//    User details Management User-side

    @GetMapping("/profile")
    public String userProfile(Model model) {
        User loggedInUser = userService.getLoggedInUser();
        model.addAttribute("user", loggedInUser);
        return "profile";
    }

    @GetMapping("/editprofile")
    public String editProfile(Model model) {
        User loggedInUser = userService.getLoggedInUser();
        model.addAttribute("user", loggedInUser);
        return "profileEdit";
    }

    @PostMapping("/editprofile")
    public String saveEditedProfile(@ModelAttribute User user,
                                    @RequestParam("profileImage") MultipartFile profileImage,
                                    @RequestParam("imgName") String imgName,
                                    Authentication authentication) {
        User loggedInUser = userService.getUserByEmail(authentication.getName());
        user.setId(loggedInUser.getId());

        try {
            userService.updateUserProfile(user, profileImage);
            return "redirect:/profile";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }


//    Address Management User-side

    @GetMapping("/add-address")
    public String showAddAddressForm(Model model,
                                     @RequestParam(value = "fromCheckout", required = false) boolean fromCheckout) {
        List<Country> countries = countryRepository.findAll();
        model.addAttribute("countries", countries);
        model.addAttribute("address", new Address());

        if (fromCheckout) {
            model.addAttribute("fromCheckout", true);
        }

        return "addressAdd";
    }

    @PostMapping("/add-address")
    public String addAddress(@ModelAttribute Address address, Principal principal,
                             @RequestParam(value = "fromCheckout", required = false) boolean fromCheckout) {
        userService.addAddressToUser(principal.getName(), address);

        if (fromCheckout) {
            return "redirect:/checkout";
        } else {
            return "redirect:/profile";
        }
    }

    @GetMapping("/addresses")
    public String showAddresses(Model model) {
        User loggedInUser = userService.getLoggedInUser();
        List<Address> addresses = loggedInUser.getAddresses();
        model.addAttribute("addresses", addresses);
        return "addressList";
    }

    @GetMapping("/edit-address/{id}")
    public String editAddress(@PathVariable Long id, Model model) {
        Address address = addressService.getAddressById(id);
        List<Country> countries = countryRepository.findAll();
        model.addAttribute("address", address);
        model.addAttribute("countries", countries);
        return "addressEdit";
    }

    @PostMapping("/edit-address/{id}")
    public String editAddress(@PathVariable Long id, @ModelAttribute Address address, Authentication authentication) {
        User loggedInUser = userService.getUserByEmail(authentication.getName());
        address.setId(id);
        address.setUser(loggedInUser);
        addressService.editAddress(address);
        return "redirect:/addresses";
    }

    @GetMapping("/delete-address/{id}")
    public String deleteAddress(@PathVariable long id) {
        addressService.deleteAddressById(id);
        return "redirect:/addresses";
    }

//    Change Password
@PostMapping("/change-password")
public String changePassword(@RequestParam("currentPassword") String currentPassword,
                             @RequestParam("newPassword") String newPassword,
                             @RequestParam("confirmPassword") String confirmPassword,
                             Model model, Principal principal) {
    User loggedInUser = userService.getLoggedInUser();
    if (!passwordEncoder.matches(currentPassword, loggedInUser.getPassword())) {
        // Current password is incorrect, display an error message
        model.addAttribute("passwordError", "Incorrect current password");
    } else if (!newPassword.equals(confirmPassword)) {
        // New password and confirmation do not match, display an error message
        model.addAttribute("passwordError", "New password and confirmation do not match");
    } else {
        // Update the user's password
        userService.updateUserPassword(loggedInUser, newPassword);
        model.addAttribute("passwordSuccess", "Password updated successfully");
    }

    model.addAttribute("user", loggedInUser);
    return "profile";
}

// checkout address add and select

    @PostMapping("/select-address")
    public String selectAddress(@RequestParam Long selectedAddressId, Model model) {
        Address selectedAddress = addressService.getAddressById(selectedAddressId);

        if (selectedAddress != null) {
            model.addAttribute("selectedAddress", selectedAddress);
        }

        return "checkout";
    }

    @GetMapping("/checkout/{productId}")
    public String goToCheckout(@PathVariable Long productId, Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // Create a cart with the selected product only
        UserCart userCart = new UserCart();
        userCart.setUser(user);
        userCart.getProducts().add(product); // Add the selected product directly

        List<Address> addresses = user.getAddresses();

        List<Product> cartProducts = userCart.getProducts();

        double totalCartAmount = userCart.getTotalCartAmount();

//        Coupon appliedCoupon = user.getAppliedCoupon();

        double shippingCharge = 40.0;


        double taxPercentage = 0.05; // 5%
        double taxAmount = userCart.getTotalCartAmount() * taxPercentage;

        double totalAmount;

            // Calculate the total amount
            totalAmount = userCart.getTotalCartAmount() + shippingCharge + taxAmount;

        model.addAttribute("userCart", userCart);
        model.addAttribute("addresses", addresses);
        model.addAttribute("pageTitle", "Checkout");
        model.addAttribute("cartProducts", cartProducts);
        model.addAttribute("cartAmount", totalCartAmount);
        model.addAttribute("shippingCharge", shippingCharge);
        model.addAttribute("taxAmount", taxAmount);
        model.addAttribute("total", totalAmount);
        return "checkoutBuy";
    }


    @PostMapping("/placeOrderBuy")
    public String placeOrderBuy(@RequestParam("paymentMethod") String paymentMethod,
                                @RequestParam("selectedAddressId") Long selectedAddressId,
                                @RequestParam("productId") Long productId,
                                Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        double shippingCharge = 40.0;
        double taxPercentage = 0.05; // 5%

        // Retrieve the product by its ID
        Optional<Product> product = productService.getProductById(productId);

        if (!product.isPresent()) {
            return "redirect:/checkoutBuy";
        }

        double productPrice = product.get().getPrice();
        double taxAmount = productPrice * taxPercentage;

        // Calculate the total amount
        double totalAmount = productPrice + shippingCharge + taxAmount;

        Order order = new Order();
        order.setUser(user);
        order.setOrderDateTime(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(OrderStatus.PENDING);
        order.setProduct(product.get());

        Address selectedAddress = addressService.getAddressById(selectedAddressId);

        // Convert the address to a formatted string
        String selectedAddressString = AddressConverter.convertAddressToString(selectedAddress);

        if (selectedAddress == null) {
            return "redirect:/checkout";
        }

        order.setShippingAddressString(selectedAddressString);

        // Set the product directly in the order
        order.setProduct(product.get());

        // Save the order
        userService.placeOrderBuy(order);

        model.addAttribute("order", order);
        return "orderSuccessful";
    }






    @PostMapping("/placeOrder")
    public String placeOrder(@RequestParam("paymentMethod") String paymentMethod,
                             @RequestParam("selectedAddressId") Long selectedAddressId,
                             Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        UserCart userCart = userService.getUserCart(user);

        double shippingCharge = 40.0;
        double taxPercentage = 0.05; // 5%
        double taxAmount = userCart.getTotalCartAmount() * taxPercentage;

        // Calculate the total amount
        double totalAmount = userCart.getTotalCartAmount() + shippingCharge + taxAmount;



        Order order = new Order();
        order.setUser(user);
        order.setOrderDateTime(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(OrderStatus.PENDING);

        Address selectedAddress = addressService.getAddressById(selectedAddressId);

        // Convert the address to a formatted string
        String selectedAddressString = AddressConverter.convertAddressToString(selectedAddress);


        if (selectedAddress == null) {
            return "redirect:/checkout";
        }

        // Set the formatted address string to the order's shipping address string
        order.setShippingAddressString(selectedAddressString);

        userService.placeOrder(order, userCart);

        model.addAttribute("order", order);
        return "redirect:/orderSuccessful";
    }

    @PostMapping("/create_order")
    @ResponseBody
    public String createOrder(@RequestBody Map<String, Object> data, Principal principal) throws Exception {
        System.out.println(data);

        double amt = Double.parseDouble(data.get("amount").toString());
        var client = new RazorpayClient("rzp_test_ALJMt7PoSbKEF5", "78TrYnRnqzbmhbSSYm6CXyQ0");

        JSONObject ob = new JSONObject();
        ob.put("amount", amt*100);
        ob.put("currency", "INR");
        ob.put("receipt", "txn_123435");

        //create new order
        com.razorpay.Order order = client.orders.create(ob);
        System.out.println(order);

        //save the order in database
        Payment myOrder = new Payment();
        myOrder.setAmount(order.get("amount")+ "");
        myOrder.setOrderId(order.get("id"));
        myOrder.setPaymentId(null);
        myOrder.setPaymentStatus("created");
        myOrder.setUser(userService.getUserByEmail(principal.getName()));
        myOrder.setReceipt(order.get("receipt"));

        this.paymentRepository.save(myOrder);

        return order.toString();
    }

    @PostMapping("/update_order")
    public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data){

        Payment myorder = this.paymentRepository.findByOrderId(data.get("order_id").toString());
        myorder.setPaymentId(data.get("payment_id").toString());
        myorder.setPaymentStatus(data.get("status").toString());
        this.paymentRepository.save(myorder);

        System.out.println(data);
        return ResponseEntity.ok(Map.of("msg", "updated"));
    }



    @GetMapping("/orderSuccessful")
    public String getOrderSuccessful(){
        return "orderSuccessful";
    }

    @GetMapping("/orders")
    public String showUserOrders(Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        List<Order> orders = userService.getUserOrders(user);

        model.addAttribute("orders", orders);
        return "userOrders";
    }

    @GetMapping("/orders/details/{orderId}")
    public String getOrderDetails(@PathVariable Long orderId, Model model) {
        Optional<Order> order = orderService.getOrderById(orderId);
        if (order.isPresent()) {
            model.addAttribute("order", order.get());
            model.addAttribute("pageTitle", "Order Details | OneCart");
            return "userOrderDetails";
        } else {
            return "404";
        }
    }

    @GetMapping("/cancelOrder/{orderId}")
    public String cancelOrder(@PathVariable Long orderId) {
        userService.cancelOrder(orderId);
        return "redirect:/orders";
    }

    @GetMapping("/returnOrder/{orderId}")
    public String returnOrder(@PathVariable Long orderId) {
        userService.returnOrder(orderId);
        return "redirect:/orders";
    }

//    Wishlist items

    @GetMapping("/wishlist")
    public String viewWishlist(Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        List<WishlistItem> wishlistItems = wishlistService.getUserWishlist(user);
        model.addAttribute("wishlistItems", wishlistItems);
        return "wishlist";
    }

    @PostMapping("/wishlist/add")
    public String addToWishlist(@RequestParam("productId") Long productId, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        Product product = productService.getProductById(productId).orElseThrow(() -> new EntityNotFoundException("Product not found"));
        wishlistService.addToWishlist(user, product);
        return "redirect:/wishlist";
    }

    @PostMapping("/wishlist/remove")
    public String removeFromWishlist(@RequestParam("wishlistItemId") Long wishlistItemId) {
        wishlistService.removeFromWishlist(wishlistItemId);
        return "redirect:/wishlist";
    }

    @GetMapping("/orders/invoice/{orderId}")
    public void generateInvoice(@PathVariable Long orderId, HttpServletResponse response) {
        try {
            Order order = userService.findOrderById(orderId);
            if (order != null) {
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=invoice_" + orderId + ".pdf");
                invoiceGenerator.generateInvoice(order, response.getOutputStream());
            }
        } catch (IOException e) {
            // Handle exceptions
        }
    }


}
