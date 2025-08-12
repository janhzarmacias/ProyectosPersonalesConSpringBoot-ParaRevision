package ecommerce.ecommercevaldani;

import ecommerce.ecommercevaldani.DTO.CartDTO;
import ecommerce.ecommercevaldani.DTO.CartItemDTO;
import ecommerce.ecommercevaldani.model.*;
import ecommerce.ecommercevaldani.repository.*;
import ecommerce.ecommercevaldani.request.*;
import ecommerce.ecommercevaldani.service.CartServiceImp;
import ecommerce.ecommercevaldani.service.ProductService;
import ecommerce.ecommercevaldani.service.UserService;
import ecommerce.ecommercevaldani.util.CartMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceImpTest {

    @InjectMocks
    private CartServiceImp cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductBranchStockRepository productBranchStockRepository;

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Test para addItemToCart
    @Test
    void addItemToCart_addsNewItemSuccessfully() throws Exception {
        String jwt = "token";
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(1L);
        request.setBranchId(10L);
        request.setQuantity(3);

        User user = new User();
        user.setId(100L);

        Product product = new Product();
        product.setId(1L);
        product.setPrice(20.00);

        Cart cart = new Cart();
        cart.setId(200L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        ProductBranchStock stock = new ProductBranchStock();
        stock.setProduct(product);
        stock.setBranch(new Branch());
        stock.setStockQuantity(10);

        when(userService.findUserByjwtToken(jwt)).thenReturn(user);
        when(productService.getProductById(1L)).thenReturn(product);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productBranchStockRepository.findByProductIdAndBranchId(1L, 10L)).thenReturn(Optional.of(stock));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.save(cart)).thenReturn(cart);

        CartItemDTO result = cartService.addItemToCart(request, jwt);

        assertNotNull(result);
        assertEquals(3, result.getQuantity());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    // Test para updateCartItemQuantity con stock suficiente
    @Test
    void updateCartItemQuantity_updatesQuantity() throws Exception {
        Long cartItemId = 1L;
        CartUpdateRequest request = new CartUpdateRequest();
        request.setQuantity(5);
        request.setBranchId(10L);

        Product product = new Product();
        product.setId(1L);
        product.setPrice(15.00);

        CartItem cartItem = new CartItem();
        cartItem.setId(cartItemId);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        ProductBranchStock stock = new ProductBranchStock();
        stock.setStockQuantity(10);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));
        when(productBranchStockRepository.findByProductIdAndBranchId(1L, 10L)).thenReturn(Optional.of(stock));
        when(cartItemRepository.save(cartItem)).thenAnswer(invocation -> invocation.getArgument(0));

        CartItemDTO dto = cartService.updateCartItemQuantity(cartItemId, request);

        assertEquals(5, dto.getQuantity());
        verify(cartItemRepository).save(cartItem);
    }

    // Test para updateCartItemQuantity con stock insuficiente -> lanza excepción
    @Test
    void updateCartItemQuantity_throwsWhenNotEnoughStock() {
        Long cartItemId = 1L;
        CartUpdateRequest request = new CartUpdateRequest();
        request.setQuantity(20); // mayor que stock
        request.setBranchId(10L);

        Product product = new Product();
        product.setId(1L);
        product.setPrice(15.00);

        CartItem cartItem = new CartItem();
        cartItem.setId(cartItemId);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        ProductBranchStock stock = new ProductBranchStock();
        stock.setStockQuantity(10); // insuficiente para 20

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));
        when(productBranchStockRepository.findByProductIdAndBranchId(1L, 10L)).thenReturn(Optional.of(stock));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            cartService.updateCartItemQuantity(cartItemId, request);
        });

        assertEquals("400 BAD_REQUEST \"Not enough stock for this product in the selected branch\"", ex.getMessage());
    }

    // Test removeItemFromCart
    @Test
    void removeItemFromCart_removesSuccessfully() throws Exception {
        String jwt = "token";
        Long cartItemId = 1L;

        User user = new User();
        user.setId(100L);

        Cart cart = new Cart();
        cart.setId(200L);
        cart.setUser(user);
        CartItem item = new CartItem();
        item.setId(cartItemId);
        cart.setItems(new ArrayList<>(List.of(item)));

        when(userService.findUserByjwtToken(jwt)).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(item));
        when(cartRepository.save(cart)).thenReturn(cart);

        CartDTO result = cartService.removeItemFromCart(cartItemId, jwt);

        assertNotNull(result);
        assertTrue(cart.getItems().isEmpty());
        verify(cartItemRepository).delete(item);
    }

    // Test calculateCartTotals
    @Test
    void calculateCartTotals_returnsCorrectTotal() throws Exception {
        Cart cart = new Cart();
        Product p1 = new Product();
        p1.setPrice(10.00);
        CartItem i1 = new CartItem();
        i1.setProduct(p1);
        i1.setQuantity(2);

        Product p2 = new Product();
        p2.setPrice(5.50);
        CartItem i2 = new CartItem();
        i2.setProduct(p2);
        i2.setQuantity(3);

        cart.setItems(List.of(i1, i2));

        BigDecimal total = cartService.calculateCartTotals(cart);

        assertEquals(new BigDecimal("36.50").setScale(2), total);
    }

}
