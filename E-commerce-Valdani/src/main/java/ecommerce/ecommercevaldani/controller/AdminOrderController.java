package ecommerce.ecommercevaldani.controller;

import ecommerce.ecommercevaldani.DTO.OrderDTO;
import ecommerce.ecommercevaldani.model.Order;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.service.OrderService;
import ecommerce.ecommercevaldani.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping("/branch/{id}")
    public ResponseEntity<List<OrderDTO>> getBranchOrders(
            @PathVariable long branchId,
            @RequestParam(required = false) String orderStatus,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        List<OrderDTO> orders = orderService.getBranchOrders(branchId,orderStatus);

        return new ResponseEntity<>(orders, HttpStatus.OK);

    }

    @PutMapping("/{orderId}/{orderStatus}")
    public ResponseEntity<OrderDTO> updateOrder(
            @PathVariable long orderId,
            @PathVariable String orderStatus,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        OrderDTO order = orderService.updateOrder(orderId,orderStatus);

        return new ResponseEntity<>(order, HttpStatus.OK);

    }

}
