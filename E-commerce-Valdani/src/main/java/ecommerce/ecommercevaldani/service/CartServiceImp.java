package ecommerce.ecommercevaldani.service;

import ecommerce.ecommercevaldani.DTO.CartDTO;
import ecommerce.ecommercevaldani.model.*;
import ecommerce.ecommercevaldani.repository.CartItemRepository;
import ecommerce.ecommercevaldani.repository.CartRepository;
import ecommerce.ecommercevaldani.repository.ProductBranchStockRepository;
import ecommerce.ecommercevaldani.DTO.CartItemDTO;
import ecommerce.ecommercevaldani.repository.UserRepository;
import ecommerce.ecommercevaldani.request.AddCartItemRequest;
import ecommerce.ecommercevaldani.request.CartUpdateRequest;
import ecommerce.ecommercevaldani.util.CartMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class CartServiceImp implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductBranchStockRepository productBranchStockRepository;

    @Transactional
    @Override
    public CartItemDTO addItemToCart(AddCartItemRequest addCartItemRequest, String jwt) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Product product = productService.getProductById(addCartItemRequest.getProductId());
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new Exception("Cart not found for user"));


        ProductBranchStock productBranchStock = productBranchStockRepository
                .findByProductIdAndBranchId(product.getId(), addCartItemRequest.getBranchId())
                .orElseThrow(() -> new Exception("No stock info for this product in selected branch"));


        if (productBranchStock.getStockQuantity() < addCartItemRequest.getQuantity()) {
            throw new Exception("No hay suficiente stock disponible para este producto en la sucursal seleccionada");
        }


        if (cart.getBranch() == null) {
            cart.setBranch(productBranchStock.getBranch());
            cartRepository.save(cart);
        } else if (!cart.getBranch().getId().equals(productBranchStock.getBranch().getId())) {
            throw new Exception("El carrito contiene productos de otra sucursal. Por favor vacíalo antes de agregar productos de esta.");
        }


        for (CartItem cartItem : cart.getItems()) {
            if (cartItem.getProduct().equals(product) &&
                    cartItem.getProductBranchStock().getBranch().getId().equals(addCartItemRequest.getBranchId())) {

                int newQuantity = cartItem.getQuantity() + addCartItemRequest.getQuantity();


                if (productBranchStock.getStockQuantity() < newQuantity) {
                    throw new Exception("Stock insuficiente para la nueva cantidad");
                }

                cartItem.setQuantity(newQuantity);
                cartItem.setPriceAtTime(product.getPrice() * newQuantity);
                cartItemRepository.save(cartItem);
                return CartMapper.toDTO(cartItem);
            }
        }

        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(addCartItemRequest.getQuantity());
        newCartItem.setProductBranchStock(productBranchStock);
        newCartItem.setPriceAtTime(product.getPrice() * addCartItemRequest.getQuantity());
        newCartItem.setCreatedAt(LocalDateTime.now());

        CartItem savedCartItem = cartItemRepository.save(newCartItem);
        cart.getItems().add(savedCartItem);

        return CartMapper.toDTO(savedCartItem);
    }


    @Override
    public CartItemDTO updateCartItemQuantity(Long cartItemId, CartUpdateRequest request) throws Exception {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new Exception("CartItem not found"));

        Product product = cartItem.getProduct();

        ProductBranchStock stock = productBranchStockRepository
                .findByProductIdAndBranchId(product.getId(), request.getBranchId())
                .orElseThrow(() -> new Exception("No stock info for this product in selected branch"));

        if (stock.getStockQuantity() < request.getQuantity()) {
            throw new Exception("Not enough stock for this product in the selected branch");
        }

        cartItem.setQuantity(request.getQuantity());
        BigDecimal newTotal = BigDecimal.valueOf(product.getPrice())
                .multiply(BigDecimal.valueOf(request.getQuantity()));
        cartItem.setPriceAtTime(newTotal.doubleValue());

        return CartMapper.toDTO(cartItemRepository.save(cartItem));
    }


    @Transactional
    @Override
    public CartDTO removeItemFromCart(Long cartItemId, String jwt) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new Exception("Cart not found for user"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new Exception("CartItem not found"));

        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        return CartMapper.toDTO(cartRepository.save(cart));
    }

    @Override
    public BigDecimal calculateCartTotals(Cart cart) throws Exception {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new Exception("Cart is empty or invalid");
        }

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            BigDecimal price = BigDecimal.valueOf(item.getProduct().getPrice());
            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        BigDecimal delivery = BigDecimal.valueOf(Taxes.DELIVERY.getValue());
        BigDecimal platform = BigDecimal.valueOf(Taxes.PLATFORM.getValue());
        BigDecimal iva = subtotal.multiply(BigDecimal.valueOf(Taxes.IVA.getValue()))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = subtotal.add(delivery).add(platform).add(iva);

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getCartTotalAsString(Cart cart) throws Exception {
        return calculateCartTotals(cart).toPlainString(); // Ej: "79.99"
    }

    @Override
    public CartDTO findCartByIdDTO(Long cartId) throws Exception {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new Exception("Cart not found with id: " + cartId));
        return CartMapper.toDTO(cart);
    }


    @Transactional
    @Override
    public CartDTO findCartByCustomerIdDTO(Long userId) throws Exception {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new Exception("Cart not found"));
        return CartMapper.toDTO(cart);

    }

    @Override
    public Cart findByUserId(Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new Exception("Cart not found"));
        return cart;
    }

    public Cart findCartById(Long cartId) throws Exception {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new Exception("Cart not found with id: " + cartId));
        return cart;
    }

    @Transactional
    @Override
    public CartDTO clearCart(Long cartId) throws Exception {
        Cart cart = findCartById(cartId);
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cart.setBranch(null);
        return CartMapper.toDTO(cartRepository.save(cart));
    }

    @Transactional
    @Override
    public void purchaseCart(Long cartId, Branch branch) throws Exception {
        Cart cart = findCartById(cartId);

        if (cart.getItems().isEmpty()) {
            throw new Exception("Cannot purchase an empty cart.");
        }

        // Descontar stock de cada producto por sucursal
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            Long branchId = branch.getId(); // Asegúrate de que este campo esté en CartItem

            if (branchId == null) {
                throw new Exception("Missing branch information for product in cart: " + product.getName());
            }

            ProductBranchStock stock = productBranchStockRepository
                    .findByProductIdAndBranchId(product.getId(), branchId)
                    .orElseThrow(() -> new Exception("Stock not found for product '" + product.getName() + "' in branch ID: " + branchId));

            if (stock.getStockQuantity() < item.getQuantity()) {
                throw new Exception("Not enough stock for product: " + product.getName() + " in branch ID: " + branchId);
            }

            // Descontar el stock
            int newStock = stock.getStockQuantity() - item.getQuantity();
            productService.assignProductToBranch(product.getId(), branchId, newStock);
            System.out.println(stock.getStockQuantity());
        }

        // Limpiar el carrito después de validar y descontar stock
        clearCart(cartId);
    }


    public boolean ping() {
        try {
            cartRepository.count(); // solo verifica que accede a la BD
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
