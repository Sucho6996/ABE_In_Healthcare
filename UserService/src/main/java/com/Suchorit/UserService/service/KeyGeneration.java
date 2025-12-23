package com.Suchorit.UserService.service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class KeyGeneration {
    public static Map<String,String> getKey() {
        Map<String,String> keys=new HashMap<>();
        try {
            KeyPairGenerator kpg=KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecSpec=new ECGenParameterSpec("secp256r1");
            kpg.initialize(ecSpec);
            KeyPair keyPair=kpg.generateKeyPair();
            PublicKey publicKey= keyPair.getPublic();
            PrivateKey privateKey=keyPair.getPrivate();
            String publicPem = toPem("PUBLIC KEY", publicKey.getEncoded());
            String privatePem = toPem("PRIVATE KEY", privateKey.getEncoded());
            keys.put("public",publicPem);
            keys.put("private",privatePem);


        }catch (Exception e){
            keys.put("Exception",e.getMessage());
        }
        return keys;
    }
    private static String toPem(String title, byte[] derBytes) {
        String b64 = Base64.getEncoder().encodeToString(derBytes);
        String chunked = chunkString(b64, 64);
        return chunked;
    }
    private static String chunkString(String str, int chunkSize) {
        if (str == null || str.length() == 0) return "";
        int parts = (str.length() + chunkSize - 1) / chunkSize;
        StringBuilder sb = new StringBuilder(parts * (chunkSize + 1));
        for (int i = 0; i < str.length(); i += chunkSize) {
            int end = Math.min(str.length(), i + chunkSize);
            sb.append(str, i, end);
//            sb.append('\n');
        }
        return sb.toString().trim();
    }

    public static String encryptPrivateKey(String privateKey, String password) throws Exception {
        // STEP 1: Extract PKCS#8 bytes
        byte[] pkcs8 = privateKey.getBytes();

        // STEP 2: Create salt
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        // STEP 3: Derive AES key from password
        SecretKey aesKey = getAESKeyFromPassword(password, salt);

        // STEP 4: AES encryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(iv));

        byte[] encrypted = cipher.doFinal(pkcs8);

        // STEP 5: Combine salt + iv + ciphertext
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(salt);
        output.write(iv);
        output.write(encrypted);

        return Base64.getEncoder().encodeToString(output.toByteArray());
    }
    public static SecretKey getAESKeyFromPassword(String password, byte[] salt) throws Exception {
        int iterations = 65536;
        int keyLength = 256;
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();

        return new SecretKeySpec(keyBytes, "AES");
    }

    public static PrivateKey decryptPrivateKey(String encryptedData, String password) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedData);

        // Extract fields
        byte[] salt = Arrays.copyOfRange(combined, 0, 16);
        byte[] iv = Arrays.copyOfRange(combined, 16, 32);
        byte[] ciphertext = Arrays.copyOfRange(combined, 32, combined.length);

        // Derive same AES key from password + salt
        SecretKey aesKey = getAESKeyFromPassword(password, salt);

        // Decrypt
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));
        byte[] pkcs8 = cipher.doFinal(ciphertext);

        // Convert back to PrivateKey
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(pkcs8);
        KeyFactory kf = KeyFactory.getInstance("EC");

        return kf.generatePrivate(spec);
    }


    public static void saveKey(String key, String name){
        try {
            File file=new File(name+".txt");
            if(file.createNewFile()){
                FileWriter myWriter = new FileWriter(name+".txt");
                myWriter.write(key);
                myWriter.close();
                System.out.println("private key saved");
            }
        } catch (Exception e) {
            System.out.println("Exception: "+e.getMessage());
        }
    }
    public static void main(String[] args) {
//        Map<String,String> map=getKey();
//        String encodedKey = URLEncoder.encode(map.get("public"), StandardCharsets.UTF_8);
//        System.out.println("public: "+encodedKey+"\nprivate: "+map.get("private"));
        try{
            byte[] keyBytes = Base64.getDecoder().decode("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEl92s7tneFfRft/jULgEUWpcQtl43sazm/vAOAbICFRfJP90a7aLTYJ2ICJHxvfLc2Zs7frwDEX/9gLeigHyY6g==");
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            System.out.println("VALID EC KEY: " + publicKey.getAlgorithm());
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
}
