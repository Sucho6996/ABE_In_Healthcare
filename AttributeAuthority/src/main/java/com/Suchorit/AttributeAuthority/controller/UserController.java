package com.Suchorit.AttributeAuthority.controller;


import com.Suchorit.AttributeAuthority.model.Hospitals;
import com.Suchorit.AttributeAuthority.model.UserData;
import com.Suchorit.AttributeAuthority.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/AA")
public class UserController {
    @Autowired
    UserService userService;

    //    @PostMapping("/signup")
//    public ResponseEntity<Map<String,String>> signUp(@RequestBody UserData user){
//        return userService.addUser(user);
//    }
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> logIn(@RequestBody UserData user) {
        return userService.login(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout
            (@RequestHeader("Authorization") String authHeader) {
        return userService.logout(authHeader);
    }

    @PostMapping("/resetPass")
    public ResponseEntity<Map<String, String>> resetPass
            (@RequestHeader("Authorization") String authHeader, @RequestBody UserData user) {
        return userService.resetPass(authHeader, user);
    }

    @PostMapping("/createHospital")
    public ResponseEntity<Map<String, String>> createHos(@RequestBody Hospitals hospital) {
        return userService.createHos(hospital);
    }

    @PostMapping("/seeAllHospital")
    public ResponseEntity<List<Hospitals>> seeAllHos() {
        return userService.seeAllHos();
    }

    @PostMapping("/revokeHospital")
    public ResponseEntity<Map<String, String>> revoke(@RequestParam("id") String id) {
        return userService.revoke(id);
    }

    @PostMapping("/setPublicKey")
    public ResponseEntity<Map<String, String>> setPublicKey
            (@RequestHeader("Authorization") String authHeader, @RequestParam("key") String key) {
        return userService.setPublicKey(authHeader, key);
    }

    @PostMapping("/genKey")
    public ResponseEntity<Map<String, String>> generate(@RequestParam("role") String role) {
        return userService.generate(role);
    }

    @PostMapping("/getKey")
    public ResponseEntity<Map<String, String>> retrieve(@RequestParam("role") String role) {
        return userService.retrieve(role);
    }

    @PostMapping("/giveSecretKey")
    public ResponseEntity<Map<String,String>> giveSecretKey
            (@RequestParam("pubKey") String publicKey,
             @RequestParam("role1") String role1,
             @RequestParam("role2") String role2){
        return userService.giveSecretKey(publicKey,role1,role2);
    }
}
