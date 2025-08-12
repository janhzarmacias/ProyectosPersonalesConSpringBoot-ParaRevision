package ecommerce.ecommercevaldani.service;

import ecommerce.ecommercevaldani.model.Branch;
import ecommerce.ecommercevaldani.model.Cart;
import ecommerce.ecommercevaldani.request.AddCartItemRequest;
import ecommerce.ecommercevaldani.DTO.CartDTO;
import ecommerce.ecommercevaldani.DTO.CartItemDTO;
import ecommerce.ecommercevaldani.request.CartUpdateRequest;

import java.math.BigDecimal;


public interface CartService {
    CartItemDTO addItemToCart(AddCartItemRequest addCartItemRequest, String jwt) throws Exception;
    CartItemDTO updateCartItemQuantity(Long cartItemId, CartUpdateRequest request) throws Exception;
    CartDTO removeItemFromCart(Long cartItemId, String jwt) throws Exception;
    CartDTO findCartByIdDTO(Long cartId) throws Exception;
    CartDTO findCartByCustomerIdDTO(Long userId) throws Exception;

    Cart findByUserId(Long userId) throws Exception;

    CartDTO clearCart(Long cartId) throws Exception;
    BigDecimal calculateCartTotals(Cart cart) throws Exception;
    String getCartTotalAsString(Cart cart) throws Exception;

    void purchaseCart(Long cartId, Branch branch) throws Exception;
}

