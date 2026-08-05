package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String registerUser(@Valid @RequestBody User user) {
        return userService.createUser(user);
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable String id) throws Exception {
        return userService.getUser(id);
    }
    @PutMapping("/users/{id}/weights")
    public String updateWeights(
            @PathVariable String id,
            @RequestBody User user
    ) throws InterruptedException, ExecutionException{
        return userService.updateWeights(id, user);
    }
}