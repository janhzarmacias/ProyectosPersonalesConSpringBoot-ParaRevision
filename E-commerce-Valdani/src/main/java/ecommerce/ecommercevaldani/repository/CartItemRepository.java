package ecommerce.ecommercevaldani.repository;

import ecommerce.ecommercevaldani.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    public Optional<CartItem> findById(Long id);

}
