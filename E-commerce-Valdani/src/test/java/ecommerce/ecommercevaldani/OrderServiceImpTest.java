package ecommerce.ecommercevaldani;

import ecommerce.ecommercevaldani.DTO.OrderDTO;
import ecommerce.ecommercevaldani.model.*;
import ecommerce.ecommercevaldani.repository.*;
import ecommerce.ecommercevaldani.request.CreateOrderRequest;
import ecommerce.ecommercevaldani.service.BranchService;
import ecommerce.ecommercevaldani.service.CartService;
import ecommerce.ecommercevaldani.service.CartServiceImp;
import ecommerce.ecommercevaldani.service.OrderServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImpTest {

    @InjectMocks
    private OrderServiceImp orderServiceImp;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BranchService branchService;

    @Mock
    private CartService cartService;

    @Mock
    private CartServiceImp cartServiceImp;

    @Mock
    private CartRepository cartRepository;

    private User user;
    private Branch branch;
    private CreateOrderRequest createOrderRequest;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setAddresses(new ArrayList<>());

        branch = new Branch();
        branch.setId(1L);
        branch.setOrders(new ArrayList<>());

        Address address = new Address();
        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setAddress(address);

        cart = new Cart();
        cart.setId(1L);
        cart.setItems(new ArrayList<>());
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        cartItem = new CartItem();
        Product product = new Product();
        product.setId(1L);
        product.setPrice(10.0);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setPriceAtTime(20.0);
        cartItem.setCart(cart);

        cart.getItems().add(cartItem);
    }

    @Test
    void testCreateOrder_Success() throws Exception {
        when(addressRepository.save(any(Address.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(branchService.findBranchById(branch.getId())).thenReturn(branch);
        when(cartService.findByUserId(user.getId())).thenReturn(cart);
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order order = orderServiceImp.createOrder(createOrderRequest, user);

        assertNotNull(order);
        assertEquals(user, order.getUser());
        assertEquals(branch, order.getBranch());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(1, order.getItems().size());
        verify(addressRepository).save(createOrderRequest.getAddress());
        verify(userRepository).save(user);
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
        verify(orderRepository).save(order);
    }

    @Test
    void testCreateOrder_AddressAddedToUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setAddresses(new ArrayList<>());

        Branch branch = new Branch();
        branch.setId(1L);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAddress(new Address());

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(branchService.findBranchById(1L)).thenReturn(branch);
        when(cartService.findByUserId(user.getId())).thenReturn(cart);
        when(addressRepository.save(any(Address.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order order = orderServiceImp.createOrder(request, user);

        assertTrue(user.getAddresses().contains(request.getAddress()));
    }



    @Test
    void testUpdateOrder_ValidStatus() throws Exception {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        OrderDTO updatedOrderDTO = orderServiceImp.updateOrder(1L, "SHIPPED");

        assertEquals("SHIPPED", updatedOrderDTO.getStatus());
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    @Test
    void testUpdateOrder_InvalidStatus_ThrowsException() {
        Order order = new Order();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Exception ex = assertThrows(Exception.class, () -> {
            orderServiceImp.updateOrder(1L, "INVALID_STATUS");
        });

        assertEquals("Invalid OrderStatus, please select a valid OrderStatus", ex.getMessage());
    }

    @Test
    void testGetUserOrders_ReturnsOrderDTOList() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setId(1L);
        List<Order> orders = Collections.singletonList(order);

        when(orderRepository.findByUserId(1L)).thenReturn(orders);

        List<OrderDTO> result = orderServiceImp.getUserOrders(1L);

        assertEquals(1, result.size());
        verify(orderRepository).findByUserId(1L);
    }

    @Test
    void testFindByOrderId_OrderExists() throws Exception {
        Order order = new Order();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order found = orderServiceImp.findByOrderId(1L);

        assertEquals(order, found);
    }

    @Test
    void testFindByOrderId_OrderNotFound_ThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(Exception.class, () -> {
            orderServiceImp.findByOrderId(1L);
        });

        assertEquals("Order not found", ex.getMessage());
    }

    @Test
    void testCancelOrder_DeletesOrder() throws Exception {
        Order order = new Order();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        doNothing().when(orderRepository).deleteById(1L);

        orderServiceImp.CancelOrder(1L);

        verify(orderRepository).deleteById(1L);
    }

    @Test
    void testPing_ReturnsTrue() {
        when(orderRepository.count()).thenReturn(5L);
        assertTrue(orderServiceImp.ping());
        verify(orderRepository).count();
    }
}