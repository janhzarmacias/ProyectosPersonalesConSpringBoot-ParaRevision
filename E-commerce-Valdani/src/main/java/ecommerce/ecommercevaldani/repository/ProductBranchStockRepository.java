package ecommerce.ecommercevaldani.repository;

import ecommerce.ecommercevaldani.model.Branch;
import ecommerce.ecommercevaldani.model.Product;
import ecommerce.ecommercevaldani.model.ProductBranchKey;
import ecommerce.ecommercevaldani.model.ProductBranchStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductBranchStockRepository extends JpaRepository<ProductBranchStock, ProductBranchKey> {

    public Optional<ProductBranchStock> findFirstByProduct(Product product);
    public Optional<ProductBranchStock> findByProductIdAndBranchId(Long productId, Long branchId);
    public List<ProductBranchStock> findByBranchId(Long branchId);

    void deleteAllByBranch(Branch branch);
}
