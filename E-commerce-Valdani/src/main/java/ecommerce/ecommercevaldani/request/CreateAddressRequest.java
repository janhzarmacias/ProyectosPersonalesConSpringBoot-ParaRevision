package ecommerce.ecommercevaldani.request;

import lombok.Data;

@Data
public class CreateAddressRequest {
    private String addressLine;
    private String city;
    private String zipcode;
    private String country;
    private String phoneNumber;
}