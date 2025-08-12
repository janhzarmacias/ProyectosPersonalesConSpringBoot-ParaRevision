package ecommerce.ecommercevaldani.service;

import ecommerce.ecommercevaldani.model.USER_ROLE;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        USER_ROLE role = user.getRole(); // ahora ya no lanza error si los getters funcionan
        if (role == null) role = USER_ROLE.CUSTOMER;

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role.name()));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                authorities
        );

    }

    public boolean ping() {
        userRepository.count();
        return true;
    }
}
