package com.Suchorit.Hospital.service;


import com.Suchorit.Hospital.model.Hospitals;
import com.Suchorit.Hospital.model.Staff;
import com.Suchorit.Hospital.repo.HospitalRepo;
import com.Suchorit.Hospital.repo.StaffRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

@Service
public class UserService {
    @Autowired
    HospitalRepo userRepo;
    @Autowired
    StaffRepo staffRepo;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtService jwtService;
    NoSuchFieldException noSuchFieldException;

    private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(12);

    public ResponseEntity<Map<String, String>> login(Hospitals user) {
        Map<String,String> response=new HashMap<>();
        try {
            Authentication auth= authenticationManager.
                    authenticate(new UsernamePasswordAuthenticationToken(user.getId(),user.getPass()));
            if(auth.isAuthenticated()){
                //String p= user.getId();
                Hospitals u=userRepo.findById(user.getId()).orElseThrow();
                String logtime=u.getLastLoginTime();
                u.setLastLoginTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                //userService.addUser(user);
                userRepo.save(u);
                response.put("message", jwtService.generateToken(user.getId()));
                response.put("logtime",logtime);
                //feign.loadData(p);
                return ResponseEntity.ok(response);
            }
        }
        catch (BadCredentialsException e){
            response.put("message","Invalid Credential");
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
        catch (Exception e){
            response.put("message",e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.UNAUTHORIZED);
        }
        response.put("message","Authentication Failed!!!");
        return new ResponseEntity<>(response,HttpStatus.UNAUTHORIZED);
    }


    public ResponseEntity<Map<String, String>> logout(String authHeader) {
        Map<String, String> response = new HashMap<>();
        String token = authHeader.substring(7);
        if (jwtService.invalidateToken(token)) {
            response.put("message", "Logout successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Invalid Token you hakor");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<Map<String, String>> resetPass(String authHeader, Hospitals user) {
        Map<String,String> response=new HashMap<>();
        String token= authHeader.substring(7);
        String id= jwtService.extractUserName(token);
        try {
            Hospitals userData=userRepo.findById(id).get();
            if(userData.equals(null)){
                response.put("message", "no account exist");
                System.out.println("no account exist");
            }
            else{
                userData.setPass(encoder.encode(user.getPass()));
                userRepo.save(userData);
                response.put("message", "Password changed successfully");
            }
        }catch (Exception e) {
            response.put("message", e.getMessage());
            System.out.println("Exception: "+e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, String>> addStaff(String authHeader, Staff staff) {
        Map<String,String> response=new HashMap<>();
        try{
            String token=authHeader.substring(7);
            String hosId= jwtService.extractUserName(token);
            staff.setHospitalId(hosId);
            staff.setPass(encoder.encode(staff.getPass()));
            staffRepo.save(staff);
            response.put("message","Staff added successfully");
        }catch (Exception e){
            response.put("message",e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, String>> removeStaff(String authHeader, String regNo) throws NoSuchFieldException {
        Map<String,String> response=new HashMap<>();
        try {
            String token=authHeader.substring(7);
            String hosId= jwtService.extractUserName(token);
            Staff staff=staffRepo.findById(regNo).orElseThrow();
            if(!staff.getHospitalId().equals(hosId)) throw noSuchFieldException;
            staffRepo.deleteById(regNo);
            response.put("message","Account revoked successfully");
        }catch (Exception e){
            response.put("message",e.getMessage());
        }
        finally {
            return ResponseEntity.ok(response);
        }
    }

    public ResponseEntity<List<Staff>> seeAll(String authHeader) {
        List<Staff> staffs=new ArrayList<>();
        String token=authHeader.substring(7);
        String hosId=jwtService.extractUserName(token);
        try {
            staffs=staffRepo.findAllByHospitalId(hosId);
            for (Staff staff:staffs) staff.setPass(null);
        }catch (Exception e){
            staffs=null;
        }
        finally {
            return ResponseEntity.ok(staffs);
        }
    }

    public ResponseEntity<Map<String, String>> setPublicKey(String authHeader, String key) {
        Map<String,String>response=new HashMap<>();
        try {
            String token=authHeader.substring(7);
            String id= jwtService.extractUserName(token);
            Hospitals hospital=userRepo.findById(id).get();
            hospital.setPublicKey(key);
            userRepo.save(hospital);
            response.put("publicKey",key);
        }catch (Exception e){
            response.put("message",e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}
