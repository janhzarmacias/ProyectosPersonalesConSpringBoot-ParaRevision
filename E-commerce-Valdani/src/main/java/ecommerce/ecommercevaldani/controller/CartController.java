package ecommerce.ecommercevaldani.controller;

import ecommerce.ecommercevaldani.DTO.CartDTO;
import ecommerce.ecommercevaldani.DTO.CartItemDTO;
import ecommerce.ecommercevaldani.request.AddCartItemRequest;
import ecommerce.ecommercevaldani.request.CartUpdateRequest;
import ecommerce.ecommercevaldani.response.MessageResponse;
import ecommerce.ecommercevaldani.service.CartService;
import ecommerce.ecommercevaldani.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public ResponseEntity<?> addItemToCart(
            @RequestHeader("Authorization") String jwt,
            @RequestBody AddCartItemRequest request
    ) {
        try {
            CartItemDTO itemDTO = cartService.addItemToCart(request, jwt);
            return ResponseEntity.ok(itemDTO);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<CartItemDTO> updateCartItem(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long cartItemId,
            @RequestBody CartUpdateRequest request
    ) throws Exception {
        CartItemDTO updatedItemDTO = cartService.updateCartItemQuantity(cartItemId,request);
        return ResponseEntity.ok(updatedItemDTO);
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<CartDTO> removeItemFromCart(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long cartItemId
    ) throws Exception {
        CartDTO updatedCartDTO = cartService.removeItemFromCart(cartItemId, jwt);
        return ResponseEntity.ok(updatedCartDTO);
    }

    @GetMapping
    public ResponseEntity<CartDTO> getUserCart(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        CartDTO cartDTO = cartService.findCartByCustomerIdDTO(
                userService.findUserByjwtToken(jwt).getId()
        );
        return ResponseEntity.ok(cartDTO);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<MessageResponse> clearCart(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        cartService.clearCart(userService.findUserByjwtToken(jwt).getCart().getId());
        MessageResponse response = new MessageResponse();
        response.setMessage("Cart cleared successfully");
        return ResponseEntity.ok(response);
    }
}

