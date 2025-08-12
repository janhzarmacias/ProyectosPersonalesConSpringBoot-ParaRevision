package ecommerce.ecommercevaldani.service;

import ecommerce.ecommercevaldani.model.Address;

import java.util.List;

public interface AddressService {
    public void deleteAddressById(String jwt, Long addressId) throws Exception;
    List<Address> getUserAddresses(String jwt) throws Exception;
}
