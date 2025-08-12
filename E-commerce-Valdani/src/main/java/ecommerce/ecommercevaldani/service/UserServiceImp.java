package ecommerce.ecommercevaldani.service;

import ecommerce.ecommercevaldani.config.JwtProvider;
import ecommerce.ecommercevaldani.model.Address;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.repository.AddressRepository;
import ecommerce.ecommercevaldani.repository.UserRepository;
import ecommerce.ecommercevaldani.request.CreateAddressRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImp implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private AddressRepository addressRepository;

    @Override
    @Transactional
    public User findUserByjwtToken(String jwt) throws Exception {
        String email = jwtProvider.getEmailFromJwtToken(jwt);
        User user = userRepository.findByEmail(email);
        user.getAddresses().size();
        return user;
    }

    @Override
    public User findUserByEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null){
            throw new Exception("User not found");
        }
        return user;
    }

    @Override
    @Transactional
    public Address addAddressToUser(String jwt, CreateAddressRequest request) throws Exception {
        User user = findUserByjwtToken(jwt);

        Address address = new Address();
        address.setUser(user);
        address.setAddressLine(request.getAddressLine());
        address.setCity(request.getCity());
        address.setZipcode(request.getZipcode());
        address.setCountry(request.getCountry());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setPrimary(false);
        address.setCreatedAt(LocalDateTime.now());

        // Guardar dirección para que se genere el id
        address = addressRepository.save(address);

        // Asociar dirección al usuario y guardar usuario
        user.getAddresses().add(address);
        userRepository.save(user);

        return address;
    }

    public boolean ping() {
        userRepository.count();
        return true;
    }


}
