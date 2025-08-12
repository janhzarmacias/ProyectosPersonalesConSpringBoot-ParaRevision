package ecommerce.ecommercevaldani.request;

import ecommerce.ecommercevaldani.model.Address;
import ecommerce.ecommercevaldani.model.ContactInformation;
import ecommerce.ecommercevaldani.model.Order;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class CreateBranchRequest {

    private String name;
    private Address address;
    private ContactInformation contactInformation;
    private String description;
    private String openingHours;
    private List<String> images = new ArrayList<>();
    private boolean isActive;

}
