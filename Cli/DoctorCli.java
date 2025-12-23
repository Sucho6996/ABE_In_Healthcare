import org.json.JSONObject;

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


public class DoctorCli {

    private static final String BASE_URL = "http://localhost:8082/staff";
    private static String regno;
    private static String jwtToken = null;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice;

        while (true) {
            System.out.println("\n=== Doctor Service CLI ===");
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
            SecretKey secretKey=EcKeyUtil.deriveECDHKey(privateKey,publicKey);
            String roleKey=keyObj.getString("role");
            byte[] role=Base64.getDecoder().decode(keyObj.getString("role"));
            byte[] roleSecret=AESGCM.decrypt(role,secretKey);
            PrivateKey rolePrivateKey=EcKeyUtil.loadPrivateKeyFromBase64
                    (Base64.getEncoder().encodeToString(roleSecret));
            SecretKey roleSecretKey=EcKeyUtil.deriveECDHKey(rolePrivateKey,patientPubKey);
            byte[] decryptedImg = encryptedImg;
//            PrivateKey rolePrivKey =
//                    EcKeyUtil.loadPrivateKeyFromBase64(keyObj.getString("role"));
//
//            SecretKey roleSharedKey =
//                    EcKeyUtil.deriveECDHKey(rolePrivKey, patientPubKey);

            // IMPORTANT: Uses SAME AESGCM.decrypt() as sender encrypt()
            decryptedImg = AESGCM.decrypt(decryptedImg, roleSecretKey);
            //decryptedImg = Arrays.copyOfRange(decryptedImg, 16, decryptedImg.length);
//            System.out.println("RoleKey: " +
//                    Base64.getEncoder().encodeToString(roleSecretKey.getEncoded()));

            //decryptedImg = Arrays.copyOfRange(decryptedImg, 32, decryptedImg.length);
            File tempFile=null;
            try {
                if (obj.has("spec") && !obj.getString("spec").equals("N/A")) {

                    if (!keyObj.has("spec")) {
                        throw new SecurityException("Access denied: specialization required");
                    }
                    byte[] spec=Base64.getDecoder().decode(keyObj.getString("spec"));
                    byte[] specSecret=AESGCM.decrypt(spec,secretKey);
                    PrivateKey specPrivateKey=EcKeyUtil.loadPrivateKeyFromBase64
                            (Base64.getEncoder().encodeToString(specSecret));
                    SecretKey specSecretKey=EcKeyUtil.deriveECDHKey(specPrivateKey,patientPubKey);
                    // IMPORTANT: decrypt AFTER role, SAME AESGCM format
                    decryptedImg = AESGCM.decrypt(decryptedImg, specSecretKey);

                    System.out.println("SpecKey: " +
                            Base64.getEncoder().encodeToString(specSecretKey.getEncoded()));
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



