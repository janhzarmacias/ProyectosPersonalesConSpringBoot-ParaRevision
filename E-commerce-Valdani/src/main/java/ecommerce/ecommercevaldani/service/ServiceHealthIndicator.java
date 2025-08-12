package ecommerce.ecommercevaldani.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ServiceHealthIndicator implements HealthIndicator {

    @Autowired private CartServiceImp cartService;
    @Autowired private ProductServiceImp productService;
    @Autowired private UserServiceImp userService;
    @Autowired private BranchServiceImp branchService;
    @Autowired private CategoryServiceImp categoryService;
    @Autowired private StripeServiceImp stripeService;
    @Autowired private CustomerUserDetailService customerUserDetailService;
    @Autowired private OrderServiceImp orderService;

    @Override
    public Health health() {
        boolean allHealthy =
                isCartServiceHealthy() &&
                        isProductServiceHealthy() &&
                        isUserServiceHealthy() &&
                        isBranchServiceHealthy() &&
                        isCategoryServiceHealthy() &&
                        isStripeServiceHealthy() &&
                        isCustomerDetailServiceHealthy() &&
                        isOrderServiceHealthy();

        Health.Builder status = allHealthy ? Health.up() : Health.down();

        return status
                .withDetail("CartService", isCartServiceHealthy() ? "UP" : "DOWN")
                .withDetail("ProductService", isProductServiceHealthy() ? "UP" : "DOWN")
                .withDetail("UserService", isUserServiceHealthy() ? "UP" : "DOWN")
                .withDetail("BranchService", isBranchServiceHealthy() ? "UP" : "DOWN")
                .withDetail("CategoryService", isCategoryServiceHealthy() ? "UP" : "DOWN")
                .withDetail("StripeService", isStripeServiceHealthy() ? "UP" : "DOWN")
                .withDetail("CustomerDetailService", isCustomerDetailServiceHealthy() ? "UP" : "DOWN")
                .withDetail("OrderService", isOrderServiceHealthy() ? "UP" : "DOWN")
                .build();
    }

    private boolean isCartServiceHealthy() {
        try {
            return cartService.ping();
        } catch (Exception e) { return false; }
    }

    private boolean isProductServiceHealthy() {
        try {
            return productService.ping();
        } catch (Exception e) { return false; }
    }

    private boolean isUserServiceHealthy() {
        try {
            return userService.ping();
        } catch (Exception e) { return false; }
    }

    private boolean isBranchServiceHealthy() {
        try {
            return branchService.ping();
        } catch (Exception e) { return false; }
    }

    private boolean isCategoryServiceHealthy() {
        try {
            return categoryService.ping();
        } catch (Exception e) { return false; }
    }

    private boolean isStripeServiceHealthy() {
        try {
            return stripeService.ping();

        } catch (Exception e) { return false; }
    }

    private boolean isCustomerDetailServiceHealthy() {
        try {
            return customerUserDetailService.ping();
        } catch (Exception e) { return false; }
    }

    private boolean isOrderServiceHealthy() {
        try {
            return orderService.ping();
        } catch (Exception e) { return false; }
    }
}
