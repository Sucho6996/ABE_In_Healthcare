package com.Suchorit.AttributeAuthority.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.*;

@Service
public class KeyGenerationService {
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
}

