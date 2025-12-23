package com.Suchorit.Admin.controller;

import com.Suchorit.Admin.model.AdminBody;
import com.Suchorit.Admin.model.UserData;
import com.Suchorit.Admin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

@RestController
@RequestMapping("/admin")
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Map<String,String>> signUp(@RequestBody AdminBody user){
        return userService.addUser(user);
    }
    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> logIn(@RequestBody AdminBody user){
        return userService.login(user);
    }
    @PostMapping("/logout")
    public ResponseEntity<Map<String,String>> logout
        (@RequestHeader("Authorization") String authHeader){
        return userService.logout(authHeader);
    }

    @PostMapping("/addAA")
    public ResponseEntity<Map<String,String>> addAA(@RequestBody UserData userData){
        return userService.addAA(userData);
    }
}
