import org.json.JSONObject;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

import java.math.BigInteger;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


public class DoctorCli {

    private static final String BASE_URL = "http://localhost:8082/staff";
    private static String regno;
    private static String jwtToken = null;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice;

        while (true) {
            System.out.println("\n=== Staff Service CLI ===");
//            System.out.println("1. Signup");
           if(jwtToken==null) System.out.println("1. Login");
           else {
               System.out.println("2. Logout");
               System.out.println("3. Reset Password");
               System.out.println("4. Get all records of your hospital");
               System.out.println("5. Get all records available for you");
           }
            System.out.println("8. Exit");
            System.out.print("Enter choice: ");

            choice = sc.nextInt();
            sc.nextLine(); // consume newline
//                case 1 -> signup(sc);
                if(choice==1) login(sc);
                else if(choice==2) logout();
                else if(jwtToken!=null) {
                    if(choice==3) resetPass(sc);
                    else if(choice==4) getPDetailsByHos();
                    else if(choice==5) getPDetailsByProf();
//                    else if(choice==8) getOrKey();
                }
                else if(choice==8) {
                    jwtToken=null;
                    System.out.println("Exiting CLI...");
                    return;
                }
                else System.out.println("Invalid choice");
        }
    }

    private static void login(Scanner sc) {
        System.out.print("Enter Registration Number: ");
        String regNo = sc.nextLine();
        regno=regNo;
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        JSONObject obj = new JSONObject();
        obj.put("regNo", regNo);
        obj.put("pass", password);

        String json = obj.toString();

        String response = sendPost("/login", json, null);

        if (response.isEmpty()) {
            System.out.println("❌ Server did not respond.");
            return;
        }
        //System.out.println("RAW RESPONSE = " + response);
        try {
            JSONObject resObj = new JSONObject(response);

            if (resObj.has("error")) {
                System.out.println("❌ " + resObj.getString("error"));
                return;
            }

            if (resObj.toString().contains("Invalid Credential")) {
                System.out.println("Invalid Credential");
                return;
            }

            jwtToken = resObj.getString("message");
            System.out.println("\n✅ Login successful!\n");
            //System.out.println(resObj.get("logtime"));
            if(resObj.get("logtime").equals(null)){
                resetPass(regNo,sc);
            }

        } catch (Exception e) {
            System.out.println("❌ Invalid JSON response from server."+"->>"+e.getMessage());
            System.out.println("Server sent: " + response);
        }
    }


    private static void logout() {
        if (jwtToken == null) {
            System.out.println("⚠ You must log in first");
            return;
        }
        String res=sendPost("/logout", "", jwtToken);
        if(res.toString().contains("Logout successfully")){
            jwtToken=null;
            System.out.println("Successfully Logout");
        }
        else if (res.toString().contains("hakor")) System.out.println("You're not logged in");
//        System.out.println(res);
    }

    private static void resetPass(Scanner sc) {
        System.out.println("Enter New Password: ");
        String pass=sc.nextLine();
        JSONObject obj=new JSONObject();
        obj.put("pass", pass);
        String res=sendPost("/resetPass", obj.toString(), jwtToken);
        if(res.toString().contains("Password changed successfully")){
            System.out.println("Password changed successfully");
            Map<String,String> map=KeyGeneration.getKey();
            sendPublicKey(map.get("public"));
        }
        else System.out.println(res.toString().substring(7));
        logout();
        //System.out.println(res);
    }
    private static void resetPass(String id,Scanner sc) {
        System.out.println("Enter New Password: ");
        String pass=sc.nextLine();
        JSONObject obj=new JSONObject();
        obj.put("pass", pass);
        String res=sendPost("/resetPass", obj.toString(), jwtToken);
        if(res.toString().contains("Password changed successfully")){
            System.out.println("Password changed successfully");
            Map<String,String> map=KeyGeneration.getKey();
            sendPublicKey(map.get("public"));
            storePrivateKey(id,pass,map.get("private"));
        }
        else System.out.println(res.toString().substring(7));
        logout();
        //System.out.println(res);
    }

    private static void sendPublicKey(String key){
        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
        String endpoint="/setPublicKey?key="+encodedKey;
        String response=sendPost(endpoint,"",jwtToken);
        System.out.println(response);
    }

    private static void storePrivateKey(String id,String pass,String key){
        try{
            KeyGeneration.saveKey(key,id);
        }catch (Exception e){
            System.out.println("Exception at storePrivateKey: "+e.getMessage());
        }
    }

    private static void getPDetailsByHos(){
        List<String> response=new ArrayList<>
                (Arrays.asList(sendPost("/getPDetailsByHos","",jwtToken).split("}")));
        if(response.isEmpty()){
            System.out.println("No data of this Hospital is available");
            return;
        }
        for (String str:response){
            String[] s=str.split(",");
            for (int i=0;i< s.length-1;i++) System.out.println(s[i]);
            System.out.println("}");
        }
        getDetails();
    }
    private static void getPDetailsByProf(){
        List<String> response=new ArrayList<>
                (Arrays.asList(sendPost("/getPDetailsByProf","",jwtToken).split("}")));
        if(response.isEmpty()){
            System.out.println("No data is available");
            return;
        }
        for (String str:response){
            String[] s=str.split(",");
            for (int i=0;i< s.length-1;i++) System.out.println(s[i]);
            System.out.println("}");
        }
        getDetails();
    }

    private static void getDetails() {
        Scanner sc = new Scanner(System.in);
        System.out.print("which prescription you want to see?(Give the id): ");
        String id = sc.nextLine();

        String response = sendPost("/getPrescription?id=" + id, "", jwtToken);
        JSONObject obj = new JSONObject(response);

        if (!obj.has("image")) {
            System.out.println("No image found");
            return;
        }
        //if(obj access policy is OR) call OR decryption from here
        if(obj.has("policyType") && obj.getString("policyType").equals("or")) {
            System.out.println("access policy is OR");
            orDesc(obj);
            return;
        }

        try {
            // ================= ENCRYPTED IMAGE =================
            byte[] encryptedImg = Base64.getDecoder()
                    .decode(obj.getString("image").replaceAll("\\s", ""));

            // ================= PATIENT PUBLIC KEY =================
            PublicKey patientPubKey =
                    UserCli.loadECPublicKey(obj.getString("key"));

            // ================= DOCTOR KEYS =================
            JSONObject keyObj = new JSONObject(
                    sendPost("/getKey?id=" + id, "", jwtToken)
            );
            String pubKey=keyObj.getString("pubKey");
            PublicKey publicKey=EcKeyUtil.loadPublicKeyFromBase64(pubKey);
            PrivateKey privateKey=null;
            try{
                privateKey = EcKeyUtil.loadPrivateKeyFromBase64
                        (EcKeyUtil.readStringFromFile(regno + ".txt"));
            }catch (Exception e){
                System.out.println(e.getMessage()+"--> No file found for private key");
            }
            File tempFile=null;
            SecretKey secretKey=EcKeyUtil.deriveECDHKey(privateKey,publicKey);
            String roleKey=keyObj.getString("role");
            byte[] role=Base64.getDecoder().decode(keyObj.getString("role"));
            byte[] roleSecret=AESGCM.decrypt(role,secretKey);
            PrivateKey rolePrivateKey=EcKeyUtil.loadPrivateKeyFromBase64(Base64.getEncoder().encodeToString(roleSecret));
            SecretKey roleSecretKey=EcKeyUtil.deriveECDHKey(rolePrivateKey,patientPubKey);
            byte[] decryptedImg = encryptedImg;
            decryptedImg = AESGCM.decrypt(decryptedImg, roleSecretKey);
            try {
                if (obj.has("spec") && !obj.getString("spec").equals("N/A")) {
                    if (!keyObj.has("spec")) throw new SecurityException("Access denied: specialization required");
                    byte[] spec=Base64.getDecoder().decode(keyObj.getString("spec"));
                    byte[] specSecret=AESGCM.decrypt(spec,secretKey);
                    PrivateKey specPrivateKey=EcKeyUtil.loadPrivateKeyFromBase64
                            (Base64.getEncoder().encodeToString(specSecret));
                    SecretKey specSecretKey=EcKeyUtil.deriveECDHKey(specPrivateKey,patientPubKey);
                    decryptedImg = AESGCM.decrypt(decryptedImg, specSecretKey);
                }
                String extension = UserCli.detectImageType(decryptedImg);
                tempFile = File.createTempFile("prescription_", "." + extension);
                tempFile.deleteOnExit();
            }catch (Exception e){
                System.out.println(e.getMessage());
                return;
            }

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(decryptedImg);
            }

            Desktop.getDesktop().open(tempFile);

            System.out.println("Image opened successfully");
            System.out.println("Decrypted length = " + decryptedImg.length);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 32 && i < decryptedImg.length; i++) {
                sb.append(String.format("%02X ", decryptedImg[i]));
            }
            System.out.println("First 32 bytes (hex) = " + sb);

        } catch (Exception e) {
            System.out.println("Error opening image: " + e.getMessage());
            return;
        }
    }

    private static String getOrKey(){
        String response=sendPost("/genStaffAttrKey","",jwtToken);
        System.out.println(response);
        return response;
    }

    private static SecretKeySpec deriveAESKey(ECPoint skPoint) throws Exception {
        BigInteger kx = skPoint.getAffineXCoord().toBigInteger();
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        return new SecretKeySpec(sha256.digest(kx.toByteArray()), "AES");
    }
    private static void orDesc(JSONObject obj) {
        try {
            // 1. Fetch Staff Attribute Keys (Di) and UID from Backend
            String keyResponseStr = getOrKey();
            if (keyResponseStr == null || keyResponseStr.isEmpty()) {
                System.out.println("Failed to retrieve attribute keys.");
                return;
            }
            JSONObject keyResponse = new JSONObject(keyResponseStr);
            String uid = keyResponse.getString("uid");
            JSONObject staffKeys = keyResponse.getJSONObject("keys"); // Map of attribute -> Di (Base64)

            // 2. Parse the Access Tree String (e.g., "[[doctor, cardiology], [nurse]]")
            String treeStr = obj.getString("accessTree");
            List<List<String>> parsedTree = new ArrayList<>();
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\[(.*?)\\]").matcher(treeStr);
            while (m.find()) {
                String inside = m.group(1);
                List<String> branch = Arrays.stream(inside.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                if (!branch.isEmpty()) {
                    parsedTree.add(branch);
                }
            }

            // 3. Find a Satisfying Branch
            List<String> satisfyingBranch = null;
            for (List<String> branch : parsedTree) {
                boolean canSatisfy = true;
                for (String attr : branch) {
                    if (!staffKeys.has(attr)) {
                        canSatisfy = false;
                        break;
                    }
                }
                if (canSatisfy) {
                    satisfyingBranch = branch;
                    break;
                }
            }

            if (satisfyingBranch == null) {
                System.out.println("Access Denied: You do not possess the required attributes for this file.");
                return;
            }
            System.out.println("Access Granted. Satisfying branch: " + satisfyingBranch);

            // 4. Setup Curve and Hashing Parameters
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
            BigInteger q = ecSpec.getN();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] uidHashBytes = digest.digest(uid.getBytes(StandardCharsets.UTF_8));
            BigInteger hashUid = new BigInteger(1, uidHashBytes);
            BigInteger hashUidInv = hashUid.modInverse(q); // H^-1 mod q

            JSONObject cipherKeyMap = new JSONObject(obj.getString("cipherKey"));
            ECPoint pCombined = null;

            // 5. Recover the Point (k * H * PMK) based on Threshold
            if (satisfyingBranch.size() == 1) {
                // OR Branch (Threshold 1)
                String attr = satisfyingBranch.get(0);

                BigInteger d_i = new BigInteger(1, Base64.getDecoder().decode(staffKeys.getString(attr)));
                byte[] c_iBytes = Base64.getDecoder().decode(cipherKeyMap.getString(attr));
                ECPoint c_i = ecSpec.getCurve().decodePoint(c_iBytes);

                pCombined = c_i.multiply(d_i).normalize();

            } else if (satisfyingBranch.size() == 2) {
                // AND Branch (Threshold 2) -> Lagrange Interpolation
                String attr1 = satisfyingBranch.get(0); // share 1 (x=1)
                String attr2 = satisfyingBranch.get(1); // share 2 (x=2)

                BigInteger d_1 = new BigInteger(1, Base64.getDecoder().decode(staffKeys.getString(attr1)));
                byte[] c_1Bytes = Base64.getDecoder().decode(cipherKeyMap.getString(attr1));
                ECPoint c_1 = ecSpec.getCurve().decodePoint(c_1Bytes);

                BigInteger d_2 = new BigInteger(1, Base64.getDecoder().decode(staffKeys.getString(attr2)));
                byte[] c_2Bytes = Base64.getDecoder().decode(cipherKeyMap.getString(attr2));
                ECPoint c_2 = ecSpec.getCurve().decodePoint(c_2Bytes);

                ECPoint p_1 = c_1.multiply(d_1).normalize();
                ECPoint p_2 = c_2.multiply(d_2).normalize();

                // L1(0) = 2, L2(0) = -1 = (q - 1)
                BigInteger L1 = BigInteger.TWO;
                BigInteger L2 = q.subtract(BigInteger.ONE);

                pCombined = p_1.multiply(L1).add(p_2.multiply(L2)).normalize();
            }

            // 6. Multiply by H^-1 to get the final Session Key (SK)
            ECPoint SK = pCombined.multiply(hashUidInv).normalize();

            // 7. Derive AES Key and Decrypt
            SecretKeySpec aesKey = deriveAESKey(SK);
            byte[] encryptedImg = Base64.getDecoder().decode(obj.getString("image").replaceAll("\\s", ""));
            byte[] decryptedImg = AESGCM.decrypt(encryptedImg, aesKey);

            // 8. Save and Open File
            String extension = UserCli.detectImageType(decryptedImg);
            File tempFile = File.createTempFile("prescription_abe_", "." + extension);
            tempFile.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(decryptedImg);
            }

            Desktop.getDesktop().open(tempFile);
            System.out.println("ABE Decrypted Image opened successfully. Size: " + decryptedImg.length + " bytes.");

        } catch (Exception e) {
            System.out.println("ABE Decryption Error: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private static String sendPost(String endpoint, String json, String token) {
        try {
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            if (token != null) {
                con.setRequestProperty("Authorization", "Bearer " + token);
            }

            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes());
                os.flush();
            }
            int status = con.getResponseCode();
            InputStream is = (status >= 200 && status < 300)
                    ? con.getInputStream()
                    : con.getErrorStream();

            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //System.out.println("\nResponse: " + response + "\n");
            return response.toString();

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());
            return "";
        }
    }
}



