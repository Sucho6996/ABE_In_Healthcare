package com.Suchorit.Doctor.controller;


import com.Suchorit.Doctor.model.PatientDetails;
import jakarta.transaction.Transactional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient("USERSERVICE")
public interface UserFeign {
    @PostMapping("/user/getPDetailsByHos")
    @Transactional
    public ResponseEntity<List<PatientDetails>> getPDetailsByHos(@RequestParam("hosId") String hosId);
    @PostMapping("/user/getPDetailsByProf")
    @Transactional
    public ResponseEntity<List<PatientDetails>> getPDetailsByProf
            (@RequestParam("role") String role,@RequestParam("spec") String spec);
    @PostMapping("/user/getPrescription")
    public ResponseEntity<Map<String,String>> getPrescription
            (@RequestParam("id") String id);
}
