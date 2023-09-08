package com.onecart.onecartApp.controller;

import com.onecart.onecartApp.dto.ProductDTO;
import com.onecart.onecartApp.model.*;
import com.onecart.onecartApp.service.*;
import com.onecart.onecartApp.util.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    public static String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/productImages";

    @Autowired
    UserService userService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CouponService couponService;



    //    Users Manage Section Starts Here

    @GetMapping("/users")
    public String getUsers(Model model){
        model.addAttribute("users", userService.getAllUser());
        model.addAttribute("pageTitle", "Users Control | Admin");
        return "users";
    }

    @PostMapping("/block/{userId}")
    public ResponseEntity<String> blockUser(@PathVariable Integer userId) {
        User blockedUser = userService.blockUser(userId);
        if (blockedUser != null) {
            return ResponseEntity.ok("User blocked successfully");
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/unblock/{userId}")
    public ResponseEntity<String> unblockUser(@PathVariable Integer userId) {
        User unblockedUser = userService.unblockUser(userId);
        if (unblockedUser != null) {
            return ResponseEntity.ok("User unblocked successfully");
        }
        return ResponseEntity.notFound().build();
    }


    //    Users Manage Section Ends Here


    @GetMapping
    public String adminDashboard(Model model){
        int totalUsers = userService.getTotalUsers();
        model.addAttribute("totalUsers", totalUsers);
        int totalProducts = productService.getTotalProducts();
        model.addAttribute("totalProducts", totalProducts);
        int totalCategories = categoryService.getTotalCategories();
        model.addAttribute("totalCategories", totalCategories);
        int totalOrders = orderService.getTotalOrders();
        model.addAttribute("totalOrders", totalOrders);
        List<Product> lowStockProducts = productService.getProductsLowStock();
        model.addAttribute("lowStockProducts", lowStockProducts);
        List<Order> recentOrders = orderService.getRecentOrders();
        model.addAttribute("recentOrders", recentOrders);
        Double totalSalesAmount = orderService.getTotalSalesAmount();
        model.addAttribute("totalSalesAmount", totalSalesAmount);
        model.addAttribute("pageTitle", "Admin Dashboard | Admin");
        return "adminDashboard";
    }


//    Category Section Starts Here

    @GetMapping("/categories")
    public String getCategories(Model model){
        model.addAttribute("categories", categoryService.getAllCategory());
        model.addAttribute("pageTitle", "Categories | Admin");
        return "categories";
    }

    @GetMapping("/categories/add")
    public String getCategoriesAdd(Model model){
        model.addAttribute("category", new Category());
        model.addAttribute("pageTitle", "Add New Category | Admin");
        return "categoriesAdd";
    }

    @PostMapping("/categories/add")
    public String postCategoriesAdd(@ModelAttribute("category") Category category, Model model){
        if(categoryService.isCategoryNameExists(category.getName())){
            model.addAttribute("error", "Category name already exists");
            return "categoriesAdd";
        }
        categoryService.addCategory(category);

        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/addproducts")
    public String postCategoriesAddFromProducts( @ModelAttribute("newCategory") Category newCategory, Model model){
        if(categoryService.isCategoryNameExists(newCategory.getName())){
            model.addAttribute("error", "Category name already exists");
            return "categoriesAdd";
        }
        categoryService.addCategory(newCategory);

        if (newCategory.getName() != null && !newCategory.getName().isEmpty()) {
            categoryService.addCategory(newCategory);
        }

        return "redirect:/admin/products/add";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategories(@PathVariable int id){
        categoryService.removeCategoryById(id);
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/update/{id}")
    public String updateCategories(@PathVariable int id, Model model){
        Optional<Category> category = categoryService.getCategoryById(id);
        model.addAttribute("pageTitle", "Update Category | Admin");
        if(category.isPresent()){
            model.addAttribute("category", category.get());
            return "categoriesAdd";
        }
        else{
            return "404";
        }
    }

    //    Category Section Ends Here

//    Product Section Starts Here

    @GetMapping("/products")
    public String getProducts(Model model){
        model.addAttribute("products", productService.getAllProduct());
        model.addAttribute("pageTitle", "Products | Admin");
        return "products";
    }

    @GetMapping("/products/details/{productId}")
    public String getProductDetails(@PathVariable Long productId, Model model) {
        Optional<Product> product = productService.getProductById(productId);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            model.addAttribute("pageTitle", "Product Details | Admin");
            return "productDetails";
        } else {
            return "404";
        }
    }


    @GetMapping("/products/add")
    public String getProductsAdd(Model model){
        model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("newCategory", new Category());

        model.addAttribute("categories", categoryService.getAllCategory());
        model.addAttribute("pageTitle", "Add New Product | Admin");
        return "productsAdd";
    }

    @PostMapping("/products/add")
    public String postProductsAdd(@ModelAttribute("productDTO") ProductDTO productDTO,
                                  @RequestParam("productImages") List<MultipartFile> files,
                                  @RequestParam("imgNames") List<String> imgNames, Model model) throws IOException {

        Product product = new Product();
        product.setId(productDTO.getId());
        product.setName(productDTO.getName());
        product.setCategory(categoryService.getCategoryById(productDTO.getCategoryId()).get());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setWeight(productDTO.getWeight());
        product.setDescription(productDTO.getDescription());

        if(productService.isProductNameExists(product.getName())){
            model.addAttribute("error", "Product name already exists");
            return "productsAdd";
        }

        List<ProductImage> images = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String imageUUID;

            if (!file.isEmpty()) {
                imageUUID = file.getOriginalFilename();
                Path fileNameAndPath = Paths.get(uploadDir, imageUUID);
                Files.write(fileNameAndPath, file.getBytes());
            } else {
                if (i < imgNames.size()) {
                    imageUUID = imgNames.get(i);
                } else {
                    imageUUID = "images/no_image_available.png";
                }
            }

            ProductImage image = new ProductImage();
            image.setImageName(imageUUID);
            images.add(image);
        }

        for (ProductImage image : images) {
            Long imageId = productService.saveImageAndGetId(image.getImageName());
        }

        product.setImages(images);
        productService.addProduct(product);

        return "redirect:/admin/products";
    }

    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable long id){
        productService.removeProductById(id);
        return "redirect:/admin/products";
    }

    @GetMapping("/product/update/{id}")
    public String updateProduct(@PathVariable long id, Model model){
        Product product = productService.getProductById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
//        productDTO.setCategoryId(product.getCategory().getId());
        Category category = product.getCategory();
        if (category != null) {
            productDTO.setCategoryId(category.getId());
        }
        productDTO.setPrice(product.getPrice());
        productDTO.setStockQuantity(product.getStockQuantity());
        productDTO.setWeight(product.getWeight());
        productDTO.setDescription(product.getDescription());
        productDTO.setImageNames(product.getImages().stream().map(ProductImage::getImageName).collect(Collectors.toList()));

//        List<String> existingImages = productDTO.getImageNames();

        model.addAttribute("categories", categoryService.getAllCategory());
        model.addAttribute("productDTO", productDTO);
//        model.addAttribute("existingImages", existingImages);
        model.addAttribute("pageTitle", "Update Product | Admin");

        return "productsAdd";
    }

//        Orders Management

    @GetMapping("/orders")
    public String getOrders(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("pageTitle", "Orders | Admin");
        return "orders";
    }

    @GetMapping("/orders/details/{orderId}")
    public String getOrderDetails(@PathVariable Long orderId, Model model) {
        Optional<Order> order = orderService.getOrderById(orderId);
        if (order.isPresent()) {
            model.addAttribute("order", order.get());
            model.addAttribute("pageTitle", "Order Details | Admin");
            return "orderDetails";
        } else {
            return "404";
        }
    }

    @PostMapping("/orders/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId) {
        Optional<Order> order = orderService.getOrderById(orderId);
        if (order.isPresent()) {
            Order cancelOrder = order.get();
            cancelOrder.setStatus(OrderStatus.CANCELED);
            orderService.saveOrder(cancelOrder);
        }
        return "redirect:/admin/orders";
    }

//    @PostMapping("/orders/update-status")
//    public String updateOrderStatus(@RequestParam Long orderId, @RequestParam String status) {
//        Optional<Order> optionalOrder = orderService.getOrderById(orderId);
//        if (optionalOrder.isPresent()) {
//            Order order = optionalOrder.get();
//
//            try {
//                OrderStatus newStatus = OrderStatus.valueOf(status);
//                order.setStatus(newStatus);
//                orderService.saveOrder(order);
//            } catch (IllegalArgumentException e) {
//                // Handle invalid status value
//            }
//        }
//        return "redirect:/admin/orders";
//    }

    @PostMapping("/orders/update-status")
    public String updateOrderStatus(@RequestParam Long orderId, @RequestParam OrderStatus newStatus, @RequestParam String placeReached) {
        Optional<Order> optionalOrder = orderService.getOrderById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.updateOrderStatusAndPlace(newStatus, placeReached);
            orderService.saveOrder(order);
        }
        return "redirect:/admin/orders";
    }


    // Coupon or discount management

    @GetMapping("/coupons")
    public String listCoupons(Model model) {
        List<Coupon> coupons = couponService.getAllCoupons();
        model.addAttribute("coupons", coupons);
        return "couponsList";
    }

    @GetMapping("/coupons/add")
    public String showCouponForm(Model model) {
        model.addAttribute("coupon", new Coupon());
        return "couponAdd";
    }

    @PostMapping("/coupons/add")
    public String saveCoupon(@ModelAttribute Coupon coupon) {
        couponService.saveCoupon(coupon);
        return "redirect:/admin/coupons";
    }

    @GetMapping("/coupons/edit/{id}")
    public String editCouponForm(@PathVariable Long id, Model model) {
        Optional<Coupon> coupon = couponService.getCouponById(id);
        if (coupon.isPresent()) {
            model.addAttribute("coupon", coupon.get());
            return "couponEdit";
        } else {
            return "404";
        }
    }

    @PostMapping("/coupons/edit/{id}")
    public String editCoupon(@PathVariable Long id, @ModelAttribute Coupon editedCoupon) {
        Optional<Coupon> coupon = couponService.getCouponById(id);
        if (coupon.isPresent()) {
            Coupon existingCoupon = coupon.get();
            existingCoupon.setCode(editedCoupon.getCode());
            existingCoupon.setDiscountPercentage(editedCoupon.getDiscountPercentage());
            existingCoupon.setStartDate(editedCoupon.getStartDate());
            existingCoupon.setEndDate(editedCoupon.getEndDate());

            couponService.saveCoupon(existingCoupon);
            return "redirect:/admin/coupons";
        } else {
            return "404";
        }
    }


    @GetMapping("/coupons/delete/{id}")
    public String deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return "redirect:/admin/coupons";
    }


}
