package ecommerce.ecommercevaldani.controller;

import ecommerce.ecommercevaldani.model.Address;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.request.CreateAddressRequest;
import ecommerce.ecommercevaldani.service.AddressService;
import ecommerce.ecommercevaldani.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private AddressService addressServiceImp;

    @GetMapping("/profile")
    public ResponseEntity<User> findUserByjwtToken(@RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        return new ResponseEntity<>(user,HttpStatus.OK);

    }

    @PostMapping("/address")
    public ResponseEntity<Address> createAddress(
            @RequestBody CreateAddressRequest request,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        Address newAddress = userService.addAddressToUser(jwt, request);
        return new ResponseEntity<>(newAddress, HttpStatus.CREATED);
    }

    @DeleteMapping("/address/{id}")
    public ResponseEntity<?> deleteAddress(
            @RequestHeader("Authorization") String jwt,
            @PathVariable("id") Long id) {

        try {
            addressServiceImp.deleteAddressById(jwt, id);
            return ResponseEntity.ok("Address deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @GetMapping("/address")
    public ResponseEntity<List<Address>> getUserAddresses(@RequestHeader("Authorization") String jwt) throws Exception {
        List<Address> addresses = addressServiceImp.getUserAddresses(jwt);
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

}
