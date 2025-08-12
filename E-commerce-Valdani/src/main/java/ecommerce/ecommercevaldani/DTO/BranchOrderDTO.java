package ecommerce.ecommercevaldani.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchOrderDTO {
    private Long id;
    private String name;
    private AddressDTO address;
    private String description;
    private String openingHours;
}

