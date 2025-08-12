package ecommerce.ecommercevaldani.repository;

import ecommerce.ecommercevaldani.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BranchRepository extends JpaRepository<Branch, Long> {

    public Branch findByName(String name);

}
