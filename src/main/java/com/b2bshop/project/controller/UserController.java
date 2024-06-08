package com.b2bshop.project.controller;

import com.b2bshop.project.model.User;
import com.b2bshop.project.repository.UserRepository;
import com.b2bshop.project.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping()
    public List<User> getAllUsers(HttpServletRequest request) {
        return userService.getAllUsers(request);
    }

    @GetMapping("/me")
    public User getMe(HttpServletRequest request) {
        return userService.getMe(request);
    }

//    @PostMapping()
//    public List<User> addUser(@RequestBody List<CreateUserRequest> requests) {
//        List<User> createdUsers = new ArrayList<>();
//        for (CreateUserRequest request : requests) {
//            createdUsers.add(userService.createUser(request));
//        }
//        return createdUsers;
//    }

    @PostMapping()
    public User addUser(HttpServletRequest request, @RequestBody JsonNode json) {
        return userService.createUser(request, json);
    }

    @PostMapping("/createSystemOwners")
    public Map<String, String>  createOwners(HttpServletRequest request, @RequestBody JsonNode json) {
        return userService.createSystemOwners(request, json);
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId) {
        return userService.findUserById(userId);
    }

    @PutMapping("/{userId}")
    public User updateUserById(@PathVariable Long userId, @RequestBody User newUser) {
        return userService.updateUserById(userId, newUser);
    }

    @PutMapping("/updatePassword/{userId}")
    public Map<String, String> updatePassword(@PathVariable Long userId, @RequestBody JsonNode json) {
        return userService.updatePassword(userId, json);
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) {
        userRepository.deleteById(userId);
    }

}