package ecommerce.ecommercevaldani.repository;

import ecommerce.ecommercevaldani.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //buscar usuario por email, el email será nuestro username para el login
    public User findByEmail(String email);
    Optional<User> findByVerificationToken(String token);

}

