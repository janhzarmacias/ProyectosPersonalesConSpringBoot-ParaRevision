package ecommerce.ecommercevaldani.util;

import ecommerce.ecommercevaldani.DTO.CartDTO;
import ecommerce.ecommercevaldani.DTO.CartItemDTO;
import ecommerce.ecommercevaldani.model.Cart;
import ecommerce.ecommercevaldani.model.CartItem;
import ecommerce.ecommercevaldani.model.Taxes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

public class CartMapper {

    public static CartItemDTO toDTO(CartItem item) {
        BigDecimal unitPrice = BigDecimal.valueOf(item.getProduct().getPrice());
        BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
        BigDecimal totalPrice = unitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);

        return new CartItemDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                unitPrice.doubleValue(),
                item.getQuantity(),
                totalPrice.doubleValue(),
                item.getProductBranchStock().getBranch().getId(),
                item.getProductBranchStock().getBranch().getName()
        );
    }

    public static CartDTO toDTO(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
                .map(CartMapper::toDTO)
                .collect(Collectors.toList());

        // Subtotal
        BigDecimal subtotal = itemDTOs.stream()
                .map(i -> BigDecimal.valueOf(i.getTotalPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // Taxes
        BigDecimal delivery = BigDecimal.valueOf(Taxes.DELIVERY.getValue()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal platform = BigDecimal.valueOf(Taxes.PLATFORM.getValue()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal iva = subtotal.multiply(BigDecimal.valueOf(Taxes.IVA.getValue()))
                .setScale(2, RoundingMode.HALF_UP);


        // Total = subtotal + delivery + platform + iva
        BigDecimal total = subtotal.add(delivery).add(platform).add(iva).setScale(2, RoundingMode.HALF_UP);

        return new CartDTO(
                cart.getId(),
                cart.getUser().getId(),
                cart.getCreatedAt(),
                cart.getUpdatedAt(),
                itemDTOs,
                subtotal.toPlainString(),
                delivery.toPlainString(),
                platform.toPlainString(),
                iva.toPlainString(),
                total.toPlainString()
        );
    }
}
