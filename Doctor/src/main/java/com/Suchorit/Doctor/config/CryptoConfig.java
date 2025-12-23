package com.Suchorit.Doctor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class CryptoConfig {
    @Value("${aes.key}")
    private String aesKeyBase64;
    @Bean
    public SecretKey aesSecretKey() {
        byte[] decodedKey = Base64.getDecoder().decode(aesKeyBase64);
        return new SecretKeySpec(decodedKey, "AES");
    }
}

