package com.Suchorit.Doctor.controller;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("AttributeAuthority")
public interface AAFeign {
    @PostMapping("/AA/getKey")
    public ResponseEntity<Map<String,String>> retrieve(@RequestParam("role") String role);
}
