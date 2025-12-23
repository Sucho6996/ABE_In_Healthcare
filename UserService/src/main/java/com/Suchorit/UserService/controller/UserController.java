package com.Suchorit.UserService.controller;

import com.Suchorit.UserService.model.PatientDetails;
import com.Suchorit.UserService.model.UserData;
import com.Suchorit.UserService.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Map<String,String>> signUp(@RequestBody UserData user){
        return userService.addUser(user);
    }
    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> logIn(@RequestBody UserData user){
        return userService.login(user);
    }
    @PostMapping("/logout")
    public ResponseEntity<Map<String,String>> logout
        (@RequestHeader("Authorization") String authHeader){
        return userService.logout(authHeader);
    }
    @PostMapping("/deactivate")
    public ResponseEntity<Map<String,String>> deactivate(@RequestHeader("Authorization") String authHeader){
        return userService.deactivate(authHeader);
    }
    @PostMapping("/setPublicKey")
    public ResponseEntity<Map<String,String>> setPublicKey
            (@RequestParam("adharNo") String adharNo,@RequestParam("key") String key){
        return userService.setPublicKey(adharNo,key);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String,String>> upload
            (@RequestHeader("Authorization") String authHeader,
             @RequestPart PatientDetails patientDetails, @RequestPart MultipartFile img){
        return userService.upload(authHeader,patientDetails,img);
    }
    @PostMapping("/getAllDetails")
    @Transactional
    public ResponseEntity<List<PatientDetails>> getAllDetails
            (@RequestHeader("Authorization") String authHeader){
        return userService.getAllDetails(authHeader);
    }
    @PostMapping("/getDetails")
    @Transactional
    public ResponseEntity<Map<String,String>> getDetails
            (@RequestHeader("Authorization") String authHeader,@RequestParam("id") long id){
        return userService.getDetails(authHeader,id);
    }

    @PostMapping("/getPDetailsByHos")
    @Transactional
    public ResponseEntity<List<PatientDetails>> getPDetailsByHos(@RequestParam("hosId") String hosId){
        return userService.getPDetailsyHos(hosId);
    }
    @PostMapping("/getPDetailsByProf")
    @Transactional
    public ResponseEntity<List<PatientDetails>> getPDetailsByProf
            (@RequestParam("role") String role,@RequestParam("spec") String spec){
        return userService.getPDetailsByprof(role,spec);
    }

    @PostMapping("/getPrescription")
    public ResponseEntity<Map<String,String>> getPrescription
            (@RequestParam("id") String id){
        return userService.getPerescription(id);
    }

    @PostMapping("/getKey")
    public ResponseEntity<Map<String,String>> getKey(@RequestParam("role") String role){
        return userService.getKey(role);
    }
    @PostMapping("/getPubKey")
    public ResponseEntity<Map<String,String>> getPubKey(@RequestParam("adharNo") String adharNo){
        return userService.getPubKey(adharNo);
    }
    @PostMapping("/giveKey")
    public ResponseEntity<Map<String,String>> giveKey(@RequestParam("id") String id){
        return userService.giveKey(id);
    }

    @PostMapping("/getSecretKey")
    public ResponseEntity<Map<String,String>> getSecretKey
            (@RequestHeader("Authorization") String authHeader,@RequestParam("id") String id){
        return userService.getSecretKey(authHeader,id);
    }
}
