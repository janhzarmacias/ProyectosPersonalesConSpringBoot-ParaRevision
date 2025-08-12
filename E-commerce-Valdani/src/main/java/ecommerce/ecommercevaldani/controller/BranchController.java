package ecommerce.ecommercevaldani.controller;

import ecommerce.ecommercevaldani.model.Branch;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.service.BranchServiceImp;
import ecommerce.ecommercevaldani.service.UserServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branchs")
public class BranchController {

    @Autowired
    private BranchServiceImp branchService;
    @Autowired
    private UserServiceImp userService;

    @GetMapping("/search/{name}")
    public ResponseEntity<Branch> findBranchByName(
            @RequestHeader ("Authorization") String jwt,
            @PathVariable String name
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Branch branch = branchService.searchBranch(name);
        return new ResponseEntity<>(branch,HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Branch> findBranchById(
            @RequestHeader ("Authorization") String jwt,
            @PathVariable Long id
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Branch branch = branchService.findBranchById(id);
        return new ResponseEntity<>(branch,HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<Branch>> getAllBranches(
            @RequestHeader ("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        List<Branch> branches = branchService.getAllBranches();
        return new ResponseEntity<>(branches,HttpStatus.OK);
    }

}
