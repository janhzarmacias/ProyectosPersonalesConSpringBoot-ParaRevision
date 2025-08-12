package ecommerce.ecommercevaldani.service;

import ecommerce.ecommercevaldani.model.Address;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.repository.AddressRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImp implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserService userService;

    @Transactional
    @Override
    public void deleteAddressById(String jwt, Long addressId) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new Exception("Address not found"));
        if (address.getUser() == null || !address.getUser().getId().equals(user.getId())) {
            throw new Exception("No permission to delete this address");
        }
        user.getAddresses().remove(address);
        addressRepository.delete(address);
    }

    @Override
    public List<Address> getUserAddresses(String jwt) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        return user.getAddresses(); // Ya está cargado por el método findUserByjwtToken con user.getAddresses().size()
    }

}
