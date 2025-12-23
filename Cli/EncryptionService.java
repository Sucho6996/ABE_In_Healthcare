import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionService {

    private final SecretKey aesKey;
    private static final int IV_LENGTH = 12; // 96 bits (recommended for GCM)
    private static final int TAG_LENGTH = 128; // 128-bit authentication tag

    public EncryptionService(SecretKey aesKey) {
        this.aesKey = aesKey;
    }

    public String encrypt(String plainText) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);

        byte[] encrypted = cipher.doFinal(plainText.getBytes());
        byte[] encryptedWithIv = new byte[iv.length + encrypted.length];

        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
        System.arraycopy(encrypted, 0, encryptedWithIv, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    public String decrypt(String cipherText) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(cipherText);

        byte[] iv = new byte[IV_LENGTH];
        byte[] encrypted = new byte[decoded.length - IV_LENGTH];

        System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);
        System.arraycopy(decoded, IV_LENGTH, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);

        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted);
    }
}

