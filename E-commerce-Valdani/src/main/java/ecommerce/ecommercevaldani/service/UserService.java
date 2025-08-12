package ecommerce.ecommercevaldani.service;

import ecommerce.ecommercevaldani.model.Address;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.request.CreateAddressRequest;

public interface UserService {

    public User findUserByjwtToken(String jwt) throws Exception;
    public User findUserByEmail(String email) throws Exception;

    Address addAddressToUser(String jwt, CreateAddressRequest request) throws Exception;

}
