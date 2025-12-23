package com.Suchorit.UserService.controller;

import com.Suchorit.UserService.model.PatientDetails;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("AttributeAuthority")
public interface AAFeign {
    @PostMapping("AA/getKey")
    public ResponseEntity<Map<String, String>> retrieve(@RequestParam("role") String role);

    @PostMapping("AA/giveKey")
    public ResponseEntity<Map<String, String>> giveKey
            (@RequestBody PatientDetails patientDetails);

    @PostMapping("AA/giveSecretKey")
    public ResponseEntity<Map<String, String>> giveSecretKey
            (@RequestParam("pubKey") String publicKey,
             @RequestParam("role1") String role1,
             @RequestParam("role2") String role2);

}