import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class EcKeyUtil {

    // Read the file content as String (your private key string)
    public static String readStringFromFile(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path))).trim();
    }

    public static PrivateKey loadPrivateKeyFromBase64(String base64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePrivate(spec);
    }

    public static PublicKey loadPublicKeyFromBase64(String base64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePublic(spec);
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

}
