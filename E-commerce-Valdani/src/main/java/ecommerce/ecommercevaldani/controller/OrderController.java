package ecommerce.ecommercevaldani.controller;


import ecommerce.ecommercevaldani.DTO.OrderDTO;
import ecommerce.ecommercevaldani.model.Brand;
import ecommerce.ecommercevaldani.model.Order;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.request.CreateBrandRequest;
import ecommerce.ecommercevaldani.request.CreateOrderRequest;
import ecommerce.ecommercevaldani.response.MessageResponse;
import ecommerce.ecommercevaldani.service.OrderService;
import ecommerce.ecommercevaldani.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;


    @PostMapping()
    public ResponseEntity<Order> createOrderRequest(
            @RequestBody CreateOrderRequest req,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Order order = orderService.createOrder(req, user);

        return new ResponseEntity<>(order, HttpStatus.CREATED);

    }


    @GetMapping("/user")
    public ResponseEntity<List<OrderDTO>> getOrderHistory(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        List<OrderDTO> orders = orderService.getUserOrders(user.getId());

        return new ResponseEntity<>(orders, HttpStatus.OK);

    }
}
