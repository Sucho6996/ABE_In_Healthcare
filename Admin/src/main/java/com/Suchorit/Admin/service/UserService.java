package com.Suchorit.Admin.service;


import com.Suchorit.Admin.model.AdminBody;
import com.Suchorit.Admin.model.UserData;
import com.Suchorit.Admin.repo.AdminRepo;
import com.Suchorit.Admin.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;

import java.util.Map;

@Service
public class UserService {
    @Autowired
    UserRepo userRepo;
    @Autowired
    AdminRepo adminRepo;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtService jwtService;

    private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(12);
    public ResponseEntity<Map<String, String>> addUser(AdminBody user) {

        Map<String,String> response=new HashMap<>();
        try {
            user.setPassword(encoder.encode(user.getPassword()));
            if(!adminRepo.existsById(user.getAdharNo())){
                adminRepo.save(user);
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

    public ResponseEntity<Map<String, String>> login(AdminBody user) {
        Map<String,String> response=new HashMap<>();
        try {
            Authentication auth= authenticationManager.
                    authenticate(new UsernamePasswordAuthenticationToken(user.getAdharNo(),user.getPassword()));
            if(auth.isAuthenticated()){
                //String p= user.getPhNo();
                AdminBody u=adminRepo.findByadharNo(user.getAdharNo());
                //u.setLastLoginTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                //userService.addUser(user);
                adminRepo.save(u);
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

    public ResponseEntity<Map<String, String>> addAA(UserData userData) {
        Map<String,String> response=new HashMap<>();
        try {
            userData.setPassword(encoder.encode(userData.getPassword()));
            if(!userRepo.existsById(userData.getAdharNo())){
                userRepo.save(userData);
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
}
