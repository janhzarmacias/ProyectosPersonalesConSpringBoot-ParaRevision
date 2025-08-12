package ecommerce.ecommercevaldani.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private Long id;
    private String addressLine;
    private String city;
    private String zipcode;
    private String country;
    private String phoneNumber;
}