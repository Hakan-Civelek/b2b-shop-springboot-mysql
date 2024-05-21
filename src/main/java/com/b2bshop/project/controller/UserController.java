package com.b2bshop.project.controller;

import com.b2bshop.project.dto.CreateUserRequest;
import com.b2bshop.project.model.User;
import com.b2bshop.project.repository.UserRepository;
import com.b2bshop.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/me")
    public User getMe(HttpServletRequest request) {
        return userService.getMe(request);
    }

    @PostMapping()
    public List<User> addUser(@RequestBody List<CreateUserRequest> requests) {
        List<User> createdUsers = new ArrayList<>();
        for (CreateUserRequest request : requests) {
            createdUsers.add(userService.createUser(request));
        }
        return createdUsers;
    }

    @PostMapping("/createSystemOwners")
    public List<User> createOwners(@RequestBody List<CreateUserRequest> requests) {
        List<User> createdUsers = new ArrayList<>();
        for (CreateUserRequest request : requests) {
            createdUsers.add(userService.createUser(request));
        }
        return createdUsers;
    }

//    public User addUser(@RequestBody CreateUserRequest request) {
//        return userService.createUser(request);
//    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @PutMapping("/{userId}")
    public User updateUserById(@PathVariable Long userId, @RequestBody User newUser) {
        return userService.updateUserById(userId, newUser);
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) {
        userRepository.deleteById(userId);
    }

}