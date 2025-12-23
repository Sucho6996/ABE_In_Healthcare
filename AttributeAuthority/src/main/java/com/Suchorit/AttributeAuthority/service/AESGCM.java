package com.Suchorit.AttributeAuthority.service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

public class AESGCM {

    private static final int IV_LEN = 12;
    private static final int TAG_LEN = 128;

    /* =========================
       ENCRYPT (BOX FORMAT)
       [ IV_LEN | IV | CIPHERTEXT+TAG ]
       ========================= */
    public static byte[] encrypt(byte[] plaintext, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_LEN];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LEN, iv));

        byte[] ciphertext = cipher.doFinal(plaintext);

        ByteBuffer bb = ByteBuffer.allocate(4 + iv.length + ciphertext.length);
        bb.putInt(iv.length);
        bb.put(iv);
        bb.put(ciphertext);
        return bb.array();
    }
    public static byte[] encrypt(byte[] plaintext, SecretKey specKey,SecretKey roleKey) throws Exception {
        byte[] iv = new byte[IV_LEN];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, specKey, new GCMParameterSpec(TAG_LEN, iv));
        cipher.init(Cipher.ENCRYPT_MODE, roleKey, new GCMParameterSpec(TAG_LEN, iv));

        byte[] ciphertext = cipher.doFinal(plaintext);

        ByteBuffer bb = ByteBuffer.allocate(4 + iv.length + ciphertext.length);
        bb.putInt(iv.length);
        bb.put(iv);
        bb.put(ciphertext);
        return bb.array();
    }

    /* =========================
       DECRYPT (BOX FORMAT)
       ========================= */
    public static byte[] decrypt(byte[] data, SecretKey key) throws Exception {
        ByteBuffer bb = ByteBuffer.wrap(data);

        int ivLen = bb.getInt();
        if (ivLen != IV_LEN) {
            throw new IllegalArgumentException("Invalid IV length: " + ivLen);
        }

        byte[] iv = new byte[ivLen];
        bb.get(iv);

        byte[] ciphertext = new byte[bb.remaining()];
        bb.get(ciphertext);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LEN, iv));

        return cipher.doFinal(ciphertext); // verifies tag
    }
    public static byte[] decrypt(byte[] data, SecretKey roleKey,SecretKey specKey) throws Exception {
        ByteBuffer bb = ByteBuffer.wrap(data);

        int ivLen = bb.getInt();
        if (ivLen != IV_LEN) {
            throw new IllegalArgumentException("Invalid IV length: " + ivLen);
        }

        byte[] iv = new byte[ivLen];
        bb.get(iv);

        byte[] ciphertext = new byte[bb.remaining()];
        bb.get(ciphertext);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, roleKey, new GCMParameterSpec(TAG_LEN, iv));
        cipher.init(Cipher.DECRYPT_MODE, specKey, new GCMParameterSpec(TAG_LEN, iv));

        return cipher.doFinal(ciphertext); // verifies tag
    }
}
