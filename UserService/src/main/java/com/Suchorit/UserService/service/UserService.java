package com.Suchorit.UserService.service;

import com.Suchorit.UserService.controller.AAFeign;
import com.Suchorit.UserService.model.PatientDetails;
import com.Suchorit.UserService.model.UserData;
import com.Suchorit.UserService.repo.PatientRepo;
import com.Suchorit.UserService.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UserService {
    @Autowired
    UserRepo userRepo;
    @Autowired
    PatientRepo patientRepo;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtService jwtService;
    @Autowired
    AAFeign aaFeign;

    private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(12);
    public ResponseEntity<Map<String, String>> addUser(UserData user) {
        Map<String,String> response=new HashMap<>();
        try {
            user.setPassword(encoder.encode(user.getPassword()));
            if(!userRepo.existsById(user.getAdharNo())){
                userRepo.save(user);
                response.put("message","Succesfully Account created");
                return ResponseEntity.ok(response);//return new ResponseEntity<>(response, HttpStatus.OK);
            }
            else{
                response.put("message","Account already been registered");
                return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
            }
        }
        catch (Exception e){
            response.put("message",e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.OK);
        }
    }

    public ResponseEntity<Map<String, String>> login(UserData user) {
        Map<String,String> response=new HashMap<>();
        try {
            Authentication auth= authenticationManager.
                    authenticate(new UsernamePasswordAuthenticationToken(user.getAdharNo(),user.getPassword()));
            if(auth.isAuthenticated()){
                String p= user.getPhNo();
                UserData u=userRepo.findByadharNo(user.getAdharNo());
                //u.setLastLoginTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                //userService.addUser(user);
                userRepo.save(u);
                response.put("message", jwtService.generateToken(user.getAdharNo()));
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

    public ResponseEntity<Map<String, String>> deactivate(String authHeader) {
        Map<String,String> response=new HashMap<>();
        try {
            String token=authHeader.substring(7);
            String adharNo=jwtService.extractUserName(token);
            userRepo.deleteById(adharNo);
            response.put("message","Deleted successfully");
        }catch (Exception e){
            response.put("message",e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, String>> setPublicKey(String adharNo, String key) {
        Map<String,String>response=new HashMap<>();
        try {
            UserData user=userRepo.findById(adharNo).get();
            user.setPublicKey(key);
            userRepo.save(user);
            response.put("publicKey",key);
        }catch (Exception e){
            response.put("message",e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, String>> upload
            (String authHeader, PatientDetails patientDetails, MultipartFile img) {
        Map<String,String> response=new HashMap<>();
        String token=authHeader.substring(7);
        String adharNo= jwtService.extractUserName(token);
        patientDetails.setAdharNo(adharNo);
        patientDetails.setImageName(img.getOriginalFilename());
        patientDetails.setImageType(img.getContentType());
        patientDetails.setUploadTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        try {
            patientDetails.setImage(img.getBytes());
        }catch (IOException e){
            response.put("message",e.getMessage());
            return ResponseEntity.ok(response);
        }
        patientRepo.save(patientDetails);
        response.put("message","successfully added");
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<List<PatientDetails>> getAllDetails(String authHeader) {
        String token=authHeader.substring(7);
        String adharNo=jwtService.extractUserName(token);
        List<PatientDetails> patientDetails=patientRepo.findAllByadharNo(adharNo);
        if(!patientDetails.isEmpty()) return ResponseEntity.ok(patientDetails);
        else return new ResponseEntity<>(patientDetails,HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<Map<String,String>> getDetails(String authHeader, long id) {
        List<PatientDetails> patientDetails=getAllDetails(authHeader).getBody();
        Map<String,String> response=new HashMap<>();
        for (PatientDetails patientDetail:patientDetails)
            if(patientDetail.getId()==id){
                response.put("image", Base64.getEncoder().encodeToString(patientDetail.getImage()));
                return ResponseEntity.ok(response);
            }
        response.put("image","Not available");
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    public ResponseEntity<List<PatientDetails>> getPDetailsyHos(String hosId) {
        List<PatientDetails> list=patientRepo.findAllByhosId(hosId);
        return ResponseEntity.ok(list);
    }

    public ResponseEntity<List<PatientDetails>> getPDetailsByprof(String role, String spec) {
        List<PatientDetails> list=patientRepo.findAllByAllowedRoleAndAllowedSpecialization(role,spec);
        return ResponseEntity.ok(list);
    }

    public ResponseEntity<Map<String, String>> getPerescription(String id) {
        Map<String ,String> map=new HashMap<>();
        PatientDetails patientDetail=patientRepo.findById(id).orElse(new PatientDetails());
        if(patientDetail==null)map.put("Image:","Not found");
        else{
            map.put("image",Base64.getEncoder().encodeToString(patientDetail.getImage()));
            map.put("key", userRepo.findByadharNo(patientDetail.getAdharNo()).getPublicKey());
            map.put("spec",patientDetail.getAllowedSpecialization());
        }
        return ResponseEntity.ok(map);
    }

    public ResponseEntity<Map<String, String>> getKey(String role) {
        Map<String ,String> response=new HashMap<>();
        try{
            String key=aaFeign.retrieve(role).getBody().get("public");
            response.put("message",key);
        }catch (Exception e){
            response.put("message",e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, String>> getPubKey(String adharNo) {
        Map<String, String> response=new HashMap<>();
        try {
            String pubKey=userRepo.findByadharNo(adharNo).getPublicKey();
            response.put("message",pubKey);
        }catch (Exception e){
            response.put("message",e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String,String>> giveKey(String id) {
        PatientDetails patientDetails=patientRepo.findById(id).orElse(new PatientDetails());
        patientDetails.setImage(null);
        return aaFeign.giveKey(patientDetails);
    }

    public ResponseEntity<Map<String, String>> getSecretKey(String authHeader,String id) {
        Map<String, String> response=new HashMap<>();
        String token=authHeader.substring(7);
        String adharNo= jwtService.extractUserName(token);
        if(!adharNo.equals(patientRepo.findById(id).get().getAdharNo())){
            response.put("message","Unauthorized");
            return ResponseEntity.ok(response);
        }
        String publicKey=userRepo.findByadharNo(adharNo).getPublicKey();
        String role1=patientRepo.findById(id).get().getAllowedRole();
        String role2=patientRepo.findById(id).get().getAllowedSpecialization();
        return aaFeign.giveSecretKey(publicKey,role1.toLowerCase(),role2.toLowerCase());
    }
}
