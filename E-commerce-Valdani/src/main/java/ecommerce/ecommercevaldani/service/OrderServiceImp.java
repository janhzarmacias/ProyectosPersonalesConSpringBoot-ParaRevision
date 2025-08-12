package ecommerce.ecommercevaldani.service;

import ecommerce.ecommercevaldani.DTO.OrderDTO;
import ecommerce.ecommercevaldani.model.*;
import ecommerce.ecommercevaldani.repository.*;
import ecommerce.ecommercevaldani.request.CreateOrderRequest;
import ecommerce.ecommercevaldani.util.OrderMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImp implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchService branchService;

    @Autowired
    private CartServiceImp cartServiceImp;

    @Autowired
    private CartService cartService;
    @Autowired
    private CartRepository cartRepository;

    @Transactional
    @Override
    public Order createOrder(CreateOrderRequest request, User user) throws Exception {

        Address shipAddress = request.getAddress();
        Address savedAddress = addressRepository.save(shipAddress);
        if(!user.getAddresses().contains(shipAddress)) {
            user.getAddresses().add(shipAddress);
            userRepository.save(user);
        }

        Cart cart = cartService.findByUserId(user.getId());

        Branch branch = cart.getBranch();
        branch.getImages().size();

        Order order = new Order();
        order.setUser(user);
        order.setBranch(branch);
        order.setAddress(shipAddress);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);


        List<OrderItem> orderItems = new ArrayList<>();

        for(CartItem cartItem: cart.getItems()){
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setPriceAtTime(cartItem.getPriceAtTime());

            OrderItem savedOrderItem = orderItemRepository.save(orderItem);
            orderItems.add(savedOrderItem);
        }

        order.setItems(orderItems);
        order.setTotalPrice(cartService.getCartTotalAsString(cart));

        Order savedOrder = orderRepository.save(order);
        branch.getOrders().add(savedOrder);

        return order;
    }

    @Transactional
    @Override
    public List<OrderDTO> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }



    @Transactional
    @Override
    public OrderDTO updateOrder(Long orderId, String orderStatus) throws Exception {
        Order order = findByOrderId(orderId);

        if (orderStatus.equals(OrderStatus.PENDING.toString())) {
            order.setStatus(OrderStatus.PENDING);
        } else if (orderStatus.equals(OrderStatus.CANCELLED.toString())) {
            order.setStatus(OrderStatus.CANCELLED);
        } else if (orderStatus.equals(OrderStatus.SHIPPED.toString())) {
            order.setStatus(OrderStatus.SHIPPED);
        } else if (orderStatus.equals(OrderStatus.DELIVERED.toString())) {
            order.setStatus(OrderStatus.DELIVERED);
        } else if (orderStatus.equals(OrderStatus.WAITINGFORDELIVERING.toString())) {
        order.setStatus(OrderStatus.WAITINGFORDELIVERING);
        }
        else {
            throw new Exception("Invalid OrderStatus, please select a valid OrderStatus");
        }

        orderRepository.save(order);

        return OrderMapper.toDTO(order);
    }


    @Transactional
    @Override
    public List<OrderDTO> getBranchOrders(Long branchId, String orderStatus) throws Exception {
        List<Order> orders = orderRepository.findByBranchId(branchId);

        if (orderStatus != null) {
            orders = orders.stream()
                    .filter(order -> order.getStatus().toString().equalsIgnoreCase(orderStatus))
                    .collect(Collectors.toList());
        }

        return orders.stream()
                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public void CancelOrder(Long orderId) throws Exception {
        findByOrderId(orderId);
        orderRepository.deleteById(orderId);
    }

    @Transactional
    @Override
    public Order findByOrderId(Long orderId) throws Exception{
        Optional<Order> order = orderRepository.findById(orderId);
        if(order.isEmpty()){
            throw new Exception("Order not found");
        }
        return order.get();
    }


    public boolean ping() {
        orderRepository.count();
        return true;
    }
}
