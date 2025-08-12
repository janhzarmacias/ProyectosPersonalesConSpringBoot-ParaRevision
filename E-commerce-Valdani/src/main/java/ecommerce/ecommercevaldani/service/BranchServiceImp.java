package ecommerce.ecommercevaldani.service;

import ecommerce.ecommercevaldani.model.Branch;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.repository.AddressRepository;
import ecommerce.ecommercevaldani.repository.BranchRepository;
import ecommerce.ecommercevaldani.repository.ProductBranchStockRepository;
import ecommerce.ecommercevaldani.request.CreateBranchRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BranchServiceImp implements BranchService {

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ProductBranchStockRepository productBranchStockRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserService userService;

    public Branch createBranch(CreateBranchRequest req, User user) {


        Branch branch = new Branch();
        branch.setAddress(req.getAddress());
        branch.setContactInformation(req.getContactInformation());
        branch.setName(req.getName());
        branch.setDescription(req.getDescription());
        branch.setImages(req.getImages());
        branch.setOpeningHours(req.getOpeningHours());
        branch.setCreatedAt(LocalDateTime.now());
        branch.setActive(true);

        return branchRepository.save(branch);
    }

    @Override
    public Branch updateBranch(Long branchId, CreateBranchRequest updatedBranch) throws Exception {

        Branch branch = findBranchById(branchId);

        if (updatedBranch.getName() != null) {
            branch.setName(updatedBranch.getName());
        }
        if (updatedBranch.getDescription() != null) {
            branch.setDescription(updatedBranch.getDescription());
        }
        if (updatedBranch.getOpeningHours() != null) {
            branch.setOpeningHours(updatedBranch.getOpeningHours());
        }
        if (updatedBranch.getContactInformation() != null) {
            branch.setContactInformation(updatedBranch.getContactInformation());
        }
        if (updatedBranch.getImages() != null && !updatedBranch.getImages().isEmpty()) {
            branch.setImages(updatedBranch.getImages());
        }
        if (updatedBranch.getAddress() != null) {
            branch.setAddress(updatedBranch.getAddress());
        }
        branch.setActive(updatedBranch.isActive());

        return branchRepository.save(branch);
    }

    @Override
    @Transactional
    public void deleteBranch(Long branchId) throws Exception {
        Branch branch = findBranchById(branchId);

        productBranchStockRepository.deleteAllByBranch(branch); // <- eliminar asociaciones

        branchRepository.delete(branch);

    }

    @Transactional
    @Override
    public List<Branch> getAllBranches() {
        List<Branch> branches = branchRepository.findAll();

        // Forzar carga de images dentro de la transacción
        branches.forEach(branch -> branch.getImages().size());

        return branches;
    }

    @Override
    public Branch searchBranch(String name) {
        return branchRepository.findByName(name);
    }

    @Transactional
    @Override
    public Branch findBranchById(Long branchId) throws Exception {
        Optional<Branch> branch = branchRepository.findById(branchId);
        if (branch.isEmpty()) {
            throw new RuntimeException("Branch not found with ID: " + branchId);
        }

        branch.get().getImages().size();

        return branch.get();
    }

    @Override
    public Branch updateBranchStatus(Long branchId) throws Exception {
        Branch branch = findBranchById(branchId);
        branch.setActive(!branch.isActive());
        return branchRepository.save(branch);
    }

    public boolean ping() {
        branchRepository.count();
        return true;
    }

}
