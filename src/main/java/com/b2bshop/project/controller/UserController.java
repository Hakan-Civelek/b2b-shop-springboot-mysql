package com.b2bshop.project.controller;

import com.b2bshop.project.dto.CreateUserRequest;
import com.b2bshop.project.model.User;
import com.b2bshop.project.repository.UserRepository;
import com.b2bshop.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;


    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping()
    public List<User> getAllUsers() {
        // TODO sadece ADMIN
        return userRepository.findAll();
    }

    @PostMapping()
    public User addUser(@RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId) {
        // TODO custom exception
        return userRepository.findById(userId).orElse(null);
    }

    @PutMapping("/{userId}")
    public User updateUserById(@PathVariable Long userId, @RequestBody User newUser) {
        // TODO USER kendini yapabilecek ADMIN herkesi yapabilecek
        return userService.updateUserById(userId, newUser);
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) {
        // TODO USER kendini yapabilecek ADMIN herkesi yapabilecek
        userRepository.deleteById(userId);
    }

}