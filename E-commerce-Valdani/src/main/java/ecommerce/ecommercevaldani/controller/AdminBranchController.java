package ecommerce.ecommercevaldani.controller;


import ecommerce.ecommercevaldani.model.Branch;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.request.CreateBranchRequest;
import ecommerce.ecommercevaldani.response.MessageResponse;
import ecommerce.ecommercevaldani.service.BranchServiceImp;
import ecommerce.ecommercevaldani.service.UserServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/branchs")
public class AdminBranchController {

    @Autowired
    private BranchServiceImp branchService;

    @Autowired
    private UserServiceImp userService;

    @PostMapping
    public ResponseEntity<Branch> createBranchRequest(
        @RequestBody CreateBranchRequest req,
        @RequestHeader ("Authorization") String jwt
        ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Branch branch = branchService.createBranch(req, user);

        return new ResponseEntity<>(branch,HttpStatus.CREATED);

    }

    @PutMapping("/{id}")
    public ResponseEntity<Branch> updateBranchRequest(
            @RequestBody CreateBranchRequest req,
            @RequestHeader ("Authorization") String jwt,
            @PathVariable long id
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Branch branch = branchService.updateBranch(id,req);

        return new ResponseEntity<>(branch,HttpStatus.CREATED);

    }
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteBranchRequest(
            @RequestHeader ("Authorization") String jwt,
            @PathVariable long id
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        branchService.deleteBranch(id);

        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessage("Branch successfully deleted ");

        return new ResponseEntity<>(messageResponse,HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Branch> updateBranchStatusRequest(
            @RequestHeader ("Authorization") String jwt,
            @PathVariable long id
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Branch branch = branchService.updateBranchStatus(id);

        return new ResponseEntity<>(branch,HttpStatus.OK);
    }

    @GetMapping("/search/{name}")
    public ResponseEntity<Branch> findBranchByName(
            @RequestHeader ("Authorization") String jwt,
            @PathVariable String name
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Branch branch = branchService.searchBranch(name);
        return new ResponseEntity<>(branch,HttpStatus.OK);
    }

}
