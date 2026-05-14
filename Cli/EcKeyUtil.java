import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
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
        byte[] shared = ka.generateSecret();
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] hash = sha256.digest(shared);
        return new SecretKeySpec(Arrays.copyOf(hash, 16), "AES");
    }

    /** P-256 field element as 32-byte unsigned big-endian (matches OR-encrypt / OR-decrypt in CLIs). */
    private static byte[] unsignedFixedWidth256(BigInteger x) {
        byte[] raw = x.toByteArray();
        if (raw.length > 0 && raw[0] == 0) {
            raw = Arrays.copyOfRange(raw, 1, raw.length);
        }
        if (raw.length > 32) {
            throw new IllegalArgumentException("affine x too large for P-256");
        }
        byte[] out = new byte[32];
        System.arraycopy(raw, 0, out, 32 - raw.length, raw.length);
        return out;
    }

    /** Session AES key = SHA-256(affine X) with X in 32-byte BE; use for both UserCli OR encrypt and DoctorCli orDesc. */
    public static byte[] sha256FromP256AffineX(BigInteger affineX) throws Exception {
        return MessageDigest.getInstance("SHA-256").digest(unsignedFixedWidth256(affineX));
    }

}
