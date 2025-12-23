package com.Suchorit.Doctor.service;

import com.Suchorit.Doctor.controller.AAFeign;
import com.Suchorit.Doctor.controller.UserFeign;
import com.Suchorit.Doctor.model.PatientDetails;
import com.Suchorit.Doctor.model.Staff;
import com.Suchorit.Doctor.repo.KeyRepo;
import com.Suchorit.Doctor.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UserService {
    @Autowired
    UserRepo userRepo;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtService jwtService;
    @Autowired
    UserFeign userFeign;
    @Autowired
    AAFeign aaFeign;
    @Autowired
    KeyRepo keyRepo;
    @Autowired
    EncryptionService encryptionService;

    private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(12);

    public ResponseEntity<Map<String, String>> login(LoginCreds user) {
        Map<String,String> response=new HashMap<>();
        try {
            Authentication auth= authenticationManager.
                    authenticate(new UsernamePasswordAuthenticationToken(user.getRegNo(),user.getPass()));
            if(auth.isAuthenticated()){
                Staff u=userRepo.findByregNo(user.getRegNo());
                //String token=jwtService.generateToken(user.getRegNo());
                String logtime=u.getLastLoginTime();
                u.setLastLoginTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                //userService.addUser(user);
                userRepo.save(u);
                response.put("message", jwtService.generateToken(user.getRegNo()));
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


    public ResponseEntity<Map<String, String>> resetPass(String authHeader, LoginCreds user) {
        Map<String,String> response=new HashMap<>();
        String token= authHeader.substring(7);
        String regNo= jwtService.extractUserName(token);
        try {
            Staff userData=userRepo.findByregNo(regNo);
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

    public ResponseEntity<Map<String, String>> checkLastLogin(String authHeader) {
        String token= authHeader.substring(7);
        String regNo= jwtService.extractUserName(token);
        Staff staff=userRepo.findByregNo(regNo);
        String logTime=staff.getLastLoginTime();
        Map<String,String> response=new HashMap<>();
        response.put("message",logTime);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, String>> setPublicKey(String authHeader, String key) {
        Map<String,String>response=new HashMap<>();
        try {
            String token=authHeader.substring(7);
            String regNo= jwtService.extractUserName(token);
            Staff staff=userRepo.findByregNo(regNo);
            staff.setPublicKey(key);
            userRepo.save(staff);
            response.put("publicKey",key);
        }catch (Exception e){
            response.put("message",e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<List<PatientDetails>> getPDetailsByHos(String authHeader) {
        String token=authHeader.substring(7);
        String regNo= jwtService.extractUserName(token);
        String hosId=userRepo.findByregNo(regNo).getHospitalId();
        List<PatientDetails> patientDetails=userFeign.getPDetailsByHos(hosId).getBody();
        return ResponseEntity.ok(patientDetails);
    }

    public ResponseEntity<List<PatientDetails>> getPDetailsByProf(String authHeader) {
        String token=authHeader.substring(7);
        String regNo= jwtService.extractUserName(token);
        Staff staff=userRepo.findByregNo(regNo);
        System.out.println("Role: "+staff.getDesignation()+"\nDesc: "+staff.getSpecialization());
        List<PatientDetails> patientDetails=userFeign.getPDetailsByProf(staff.getDesignation(),staff.getSpecialization()).getBody();
        return ResponseEntity.ok(patientDetails);
    }

    public ResponseEntity<Map<String, String>> getPrescription(String id) {
        return userFeign.getPrescription(id);
    }

    public ResponseEntity<Map<String, String>> retrieve(String authHeader) {
        Map<String, String> response=new HashMap<>();
        PrivateKey rolePrivateKey=null,specPrivateKey=null;
        SecretKey secretKey=null;
        String token=authHeader.substring(7);
        String regNo= jwtService.extractUserName(token);
        try {
            String role1=userRepo.findByregNo(regNo).getDesignation().toLowerCase();
            String role2=userRepo.findByregNo(regNo).getSpecialization().toLowerCase();
            PublicKey pubKey=loadECPublicKey(userRepo.findByregNo(regNo).getPublicKey());
            response.put("pubKey",keyRepo.findById(role1.toLowerCase()).get().getPubKey());
            if(!role1.isEmpty()){
                String rolePri=encryptionService.decrypt
                        (keyRepo.findById(role1.toLowerCase()).get().getPriKey());
                rolePrivateKey=loadPrivateKeyFromBase64(rolePri);
                secretKey=deriveECDHKey(rolePrivateKey,pubKey);
                byte[] rolePriKey=AESGCM.encrypt(rolePrivateKey.getEncoded(),secretKey);
                response.put("role",Base64.getEncoder().encodeToString(rolePriKey));
            }
            if(!role2.isEmpty() && !role2.equals("N/A")){
                String specPri=encryptionService.decrypt
                        (keyRepo.findById(role2.toLowerCase()).get().getPriKey());
                specPrivateKey=loadPrivateKeyFromBase64(specPri);
                byte[] specPrikey=AESGCM.encrypt(specPrivateKey.getEncoded(),secretKey);
                response.put("spec",Base64.getEncoder().encodeToString(specPrikey));
            }
            else response.put("spec",null);
        }catch (Exception e){
            System.out.println(e.getMessage());
            response.put("message",e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    public static SecretKey deriveECDHKey(PrivateKey userPrivate, PublicKey rolePublic) throws Exception {
        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(userPrivate);
        ka.doPhase(rolePublic, true);
        byte[] shared = ka.generateSecret();    // Raw shared secret

        // Derive AES wrap key from shared secret
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] hash = sha256.digest(shared);

        // Use 16 bytes (128-bit) or 32 bytes (256-bit)
        return new SecretKeySpec(Arrays.copyOf(hash, 16), "AES");
    }

    public static PublicKey loadECPublicKey(String base64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePublic(spec);
    }
    public static PrivateKey loadPrivateKeyFromBase64(String base64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePrivate(spec);
    }
}
