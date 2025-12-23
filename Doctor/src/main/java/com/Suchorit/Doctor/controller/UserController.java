package com.Suchorit.Doctor.controller;

import com.Suchorit.Doctor.model.PatientDetails;
import com.Suchorit.Doctor.service.LoginCreds;

import com.Suchorit.Doctor.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/staff")
public class UserController {
    @Autowired
    UserService userService;
    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> logIn(@RequestBody LoginCreds user){
        return userService.login(user);
    }
    @PostMapping("/logout")
    public ResponseEntity<Map<String,String>> logout
        (@RequestHeader("Authorization") String authHeader){
        return userService.logout(authHeader);
    }

    @PostMapping("/resetPass")
    public ResponseEntity<Map<String,String>> resetPass
            (@RequestHeader("Authorization") String authHeader, @RequestBody LoginCreds user){
        return userService.resetPass(authHeader,user);
    }
    @PostMapping("/cll")
    public ResponseEntity<Map<String,String>> checkAndSetLastLogin
            (@RequestHeader("Authorization") String authHeader){
        return userService.checkLastLogin(authHeader);
    }
    @PostMapping("/setPublicKey")
    public ResponseEntity<Map<String,String>> setPublicKey
            (@RequestHeader("Authorization") String authHeader,@RequestParam("key") String key){
        return userService.setPublicKey(authHeader,key);
    }

    @PostMapping("/getPDetailsByHos")
    @Transactional
    public ResponseEntity<List<PatientDetails>> getPDetailsByHos
            (@RequestHeader("Authorization") String authHeader){
        return userService.getPDetailsByHos(authHeader);
    }
    @PostMapping("/getPDetailsByProf")
    @Transactional
    public ResponseEntity<List<PatientDetails>> getPDetailsByProf
            (@RequestHeader("Authorization") String authHeader){
        return userService.getPDetailsByProf(authHeader);
    }
    @PostMapping("/getPrescription")
    public ResponseEntity<Map<String,String>> getPrescription
            (@RequestParam("id") String id){
        return userService.getPrescription(id);
    }
    @PostMapping("/getKey")
    public ResponseEntity<Map<String, String>> retrieve(@RequestHeader("Authorization") String authHeader){
        return userService.retrieve(authHeader);
    }
}
