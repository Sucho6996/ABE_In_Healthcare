package com.Suchorit.Hospital.controller;

import com.Suchorit.Hospital.model.Hospitals;
import com.Suchorit.Hospital.model.Staff;
import com.Suchorit.Hospital.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hospital")
public class UserController {
    @Autowired
    UserService userService;
    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> logIn(@RequestBody Hospitals user){
        return userService.login(user);
    }
    @PostMapping("/logout")
    public ResponseEntity<Map<String,String>> logout
        (@RequestHeader("Authorization") String authHeader){
        return userService.logout(authHeader);
    }

    @PostMapping("/resetPass")
    public ResponseEntity<Map<String,String>> resetPass(@RequestHeader("Authorization") String authHeader,@RequestBody Hospitals user){
        return userService.resetPass(authHeader,user);
    }

    @PostMapping("/addStaff")
    public ResponseEntity<Map<String,String>> addStaff
            (@RequestHeader("Authorization") String authHeader, @RequestBody Staff staff){
        return userService.addStaff(authHeader,staff);
    }
    @PostMapping("/seeAllStaff")
    public ResponseEntity<List<Staff>> seeAll(@RequestHeader("Authorization") String authHeader){
        return userService.seeAll(authHeader);
    }
    @PostMapping("/removeStaff")
    public ResponseEntity<Map<String,String>> removeStaff
            (@RequestHeader("Authorization") String authHeader,@RequestParam("regNo") String regNo) throws NoSuchFieldException {
        return userService.removeStaff(authHeader,regNo);
    }

    @PostMapping("/setPublicKey")
    public ResponseEntity<Map<String,String>> setPublicKey
            (@RequestHeader("Authorization") String authHeader,@RequestParam("key") String key){
        return userService.setPublicKey(authHeader,key);
    }
}
