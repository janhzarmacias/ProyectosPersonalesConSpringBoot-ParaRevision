package ecommerce.ecommercevaldani.util;

import ecommerce.ecommercevaldani.DTO.*;
import ecommerce.ecommercevaldani.model.*;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderDTO toDTO(Order order) {
        if (order == null) return null;

        return OrderDTO.builder()
                .id(order.getId())
                .status(order.getStatus().name()) // asumiendo que es enum
                .paymentIntentId(order.getPaymentIntentId())
                .createdAt(order.getCreatedAt())
                .user(toUserDTO(order.getUser()))
                .address(toAddressDTO(order.getAddress()))
                .branch(toBranchDTO(order.getBranch()))
                .items(toOrderItemDTOList(order.getItems()))
                .payment(toPaymentDTO(order.getPayment()))
                .build();
    }

    private static UserDTO toUserDTO(User user) {
        if (user == null) return null;

        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    private static AddressDTO toAddressDTO(Address address) {
        if (address == null) return null;

        return AddressDTO.builder()
                .id(address.getId())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .zipcode(address.getZipcode())
                .country(address.getCountry())
                .phoneNumber(address.getPhoneNumber())
                .build();
    }

    private static BranchOrderDTO toBranchDTO(Branch branch) {
        if (branch == null) return null;

        return BranchOrderDTO.builder()
                .id(branch.getId())
                .name(branch.getName())
                .description(branch.getDescription())
                .openingHours(branch.getOpeningHours())
                .address(toAddressDTO(branch.getAddress()))
                .build();
    }

    private static List<OrderItemDTO> toOrderItemDTOList(List<OrderItem> items) {
        if (items == null) return null;

        return items.stream().map(OrderMapper::toOrderItemDTO).collect(Collectors.toList());
    }

    private static OrderItemDTO toOrderItemDTO(OrderItem item) {
        if (item == null) return null;

        return OrderItemDTO.builder()
                .id(item.getId())
                .quantity(item.getQuantity())
                .priceAtTime(item.getPriceAtTime())
                .product(toProductDTO(item.getProduct()))
                .build();
    }

    private static ProductDTO toProductDTO(Product product) {
        if (product == null) return null;

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .category(toCategoryDTO(product.getCategory()))
                .brand(toBrandDTO(product.getBrand()))
                .build();
    }

    private static CategoryDTO toCategoryDTO(Category category) {
        if (category == null) return null;

        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    private static BrandDTO toBrandDTO(Brand brand) {
        if (brand == null) return null;

        return BrandDTO.builder()
                .id(brand.getId())
                .name(brand.getName())
                .build();
    }

    private static PaymentDTO toPaymentDTO(Payment payment) {
        if (payment == null) return null;

        Double amount = payment.getAmount();
        // Evita usar amount.doubleValue() si puede ser null, usa directamente el objeto Double y que el DTO acepte null

        return PaymentDTO.builder()
                .id(payment.getId())
                .amount(amount)
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .paymentMethodLast4(payment.getPaymentMethodLast4())
                .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                .stripePaymentId(payment.getStripePaymentId())
                .transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate())
                .build();
    }
}

