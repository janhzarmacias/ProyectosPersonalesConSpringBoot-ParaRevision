package ecommerce.ecommercevaldani.service;

import ecommerce.ecommercevaldani.DTO.OrderDTO;
import ecommerce.ecommercevaldani.model.Order;
import ecommerce.ecommercevaldani.model.OrderStatus;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.request.CreateOrderRequest;

import java.util.List;

public interface OrderService {
    public Order createOrder(CreateOrderRequest request, User user) throws Exception;
    public List<OrderDTO> getUserOrders(Long userId) throws Exception;
    public OrderDTO updateOrder(Long orderId, String orderStatus) throws Exception;
    public List<OrderDTO> getBranchOrders(Long branchId, String orderStatus) throws Exception;
    public void CancelOrder(Long orderId) throws Exception;

    Order findByOrderId(Long orderId) throws Exception;
}
