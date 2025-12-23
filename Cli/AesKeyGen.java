import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AesKeyGen{
    public static SecretKey generateAESKey(int keySize) throws Exception {
        // keySize = 128 / 192 / 256
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(keySize);
        return keyGen.generateKey();
    }
}
