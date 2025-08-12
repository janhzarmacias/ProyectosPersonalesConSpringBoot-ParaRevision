package ecommerce.ecommercevaldani.service;

import ecommerce.ecommercevaldani.model.Branch;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.request.CreateBranchRequest;
import java.util.List;

public interface BranchService {
    public Branch createBranch(CreateBranchRequest req, User user );
    public Branch updateBranch(Long branchId, CreateBranchRequest updatedBranch) throws Exception;
    public void deleteBranch(Long branchId) throws Exception;
    public List<Branch> getAllBranches();
    public Branch searchBranch(String name);
    public Branch findBranchById(Long branchId) throws Exception;
    public Branch updateBranchStatus(Long branchId) throws Exception;

}
