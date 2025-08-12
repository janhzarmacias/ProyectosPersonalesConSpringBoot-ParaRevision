package ecommerce.ecommercevaldani.repository;

import ecommerce.ecommercevaldani.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    //public List<Order> findByUserId(Long userId);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.items " +
            "LEFT JOIN FETCH o.branch " +
            "LEFT JOIN FETCH o.address " +
            "LEFT JOIN FETCH o.payment " +
            "WHERE o.user.id = :userId")
    List<Order> findByUserId(@Param("userId") Long userId);

    public List<Order> findByBranchId(Long branchId);

    public void deleteById(Long orderId);

    Optional<Order> findByPaymentIntentId(String paymentIntentId);



}
