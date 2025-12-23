package com.Suchorit.AttributeAuthority.service;


import com.Suchorit.AttributeAuthority.controller.UserFeign;
import com.Suchorit.AttributeAuthority.model.Hospitals;
import com.Suchorit.AttributeAuthority.model.Key;
import com.Suchorit.AttributeAuthority.model.UserData;
import com.Suchorit.AttributeAuthority.repo.HospitalRepo;
import com.Suchorit.AttributeAuthority.repo.KeyRepo;
import com.Suchorit.AttributeAuthority.repo.StaffRepo;
import com.Suchorit.AttributeAuthority.repo.UserRepo;
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
    KeyRepo keyRepo;
    @Autowired
    HospitalRepo hospitalRepo;
    @Autowired
    StaffRepo staffRepo;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtService jwtService;
    @Autowired
    EncryptionService encryptionService;
    @Autowired
    UserFeign userFeign;

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
                String logtime=u.getLastLoginTime();
                u.setLastLoginTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                //userService.addUser(user);
                userRepo.save(u);
                response.put("message", jwtService.generateToken(user.getAdharNo()));
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

    public ResponseEntity<Map<String, String>> resetPass(String authHeader, UserData user) {
        Map<String,String> response=new HashMap<>();
        String token=authHeader.substring(7);
        String adharNo= jwtService.extractUserName(token);
        try {
            UserData userData=userRepo.findByadharNo(adharNo);
            if(userData.equals(null)){
                response.put("message", "no account exist");
                System.out.println("no account exist");
            }
            else{
                userData.setPassword(encoder.encode(user.getPassword()));
                userData.setLastLoginTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                userRepo.save(userData);
                response.put("message", "Password changed successfully");
            }
        }catch (Exception e) {
            response.put("message", e.getMessage());
            System.out.println("Exception: "+e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, String>> createHos(Hospitals hospital) {
        Map<String,String> response=new HashMap<>();
        try {
            hospital.setPass(encoder.encode(hospital.getPass()));
            hospitalRepo.save(hospital);
            response.put("message","Hospital added successfully");
        }catch (Exception e){
            response.put("message",e.getMessage());
        }
        finally {
            return ResponseEntity.ok(response);
        }
    }

    public ResponseEntity<List<Hospitals>> seeAllHos() {
        List<Hospitals> hospitals=hospitalRepo.findAll();
        for(Hospitals hospital:hospitals) hospital.setPass(null);
        return ResponseEntity.ok(hospitals);
    }

    public ResponseEntity<Map<String, String>> revoke(String id) {
        Map<String,String> response= new HashMap<>();
        try {
            Hospitals hospital=hospitalRepo.findById(id).orElseThrow();
            staffRepo.deleteAllByHospitalId(id);
            hospitalRepo.deleteById(id);
            response.put("message","Revoke successfully");
        }catch (Exception e){
            response.put("message",e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, String>> setPublicKey(String authHeader, String key) {
        Map<String,String>response=new HashMap<>();
        try {
            String token=authHeader.substring(7);
            String adharNo= jwtService.extractUserName(token);
            UserData user=userRepo.findById(adharNo).get();
            user.setPublicKey(key);
            userRepo.save(user);
            response.put("publicKey",key);
        }catch (Exception e){
            response.put("message",e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, String>> generate(String role) {
        Map<String,String> response=new HashMap<>();
        Key key=new Key();
        try {
            Map<String,String> keys=KeyGenerationService.getKey();
            if(!keyRepo.existsById(role)){
                key.setRole(role);
                key.setPubKey(keys.get("public"));
                try {
                    key.setPriKey(encryptionService.encrypt(keys.get("private")));
                    keyRepo.save(key);
                }catch (Exception e){
                    response.put("message",e.getMessage());
                }
                response.put("message","Role details created");
                return ResponseEntity.ok(response);
            }
            else{
                response.put("message","Role already exist");
            }
        }catch (Exception e){
            response.put("message",e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, String>> retrieve(String role) {
        Map<String,String> response=new HashMap<>();
        try {
            Key key=keyRepo.findById(role.toLowerCase()).orElseThrow();
            response.put("public",key.getPubKey());
            response.put("private",encryptionService.decrypt(key.getPriKey()));
        }catch (Exception e){
            response.put("message",e.getMessage());
        }
        return ResponseEntity.ok(response);
    }



    public ResponseEntity<Map<String, String>> giveSecretKey
            (String publicKey, String role1, String role2) {
        Map<String,String> response=new HashMap<>();
        try{
            PublicKey userPubKey=loadECPublicKey(publicKey);
            PrivateKey role1PriKey=loadPrivateKeyFromBase64(encryptionService.decrypt(keyRepo.findById(role1.toLowerCase()).get().getPriKey()));
            SecretKey secretKey=deriveECDHKey(role1PriKey,userPubKey);
            byte[] role1Key=AESGCM.encrypt(secretKey.getEncoded(),secretKey);
            response.put("pubKey",keyRepo.findById(role1.toLowerCase()).get().getPubKey());
            System.out.println("Role->"+keyRepo.findById(role1.toLowerCase()).get().getPubKey());
            response.put("role",Base64.getEncoder().encodeToString(role1Key));
            System.out.println("Role->"+Base64.getEncoder().encodeToString(role1Key));
            if(!role2.equals("N/A")){
                PrivateKey role2PriKey=loadPrivateKeyFromBase64
                        (encryptionService.decrypt(keyRepo.findById(role2.toLowerCase()).get().getPriKey()));
                SecretKey specSecret=deriveECDHKey(role2PriKey,userPubKey);
                byte[] role2Key=AESGCM.encrypt(specSecret.getEncoded(),secretKey);
                System.out.println("spec->"+Base64.getEncoder().encodeToString(role2Key));
                response.put("spec",Base64.getEncoder().encodeToString(role2Key));
            }
        }catch (Exception e){
            response.put("error",e.getMessage());
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
