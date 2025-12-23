import org.json.JSONObject;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.List;

public class UserCli {

    private static final String BASE_URL = "http://localhost:8090/user";
    private static String jwtToken = null;
    private static String adhar;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice;

        while (true) {
            System.out.println("\n=== User Service CLI ===");
            if (jwtToken==null){
                System.out.println("1. Signup");
                System.out.println("2. Login");
            }
            else {
                System.out.println("3. Logout");
                System.out.println("4. Deactivate Account");
                System.out.println("5. Upload Prescription");
                System.out.println("6. See all details");
            }
            System.out.println("8. Exit");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();
            sc.nextLine(); // consume newline

            if(choice==1) signup(sc);
            else if(choice==2) login(sc);
            else if(jwtToken!=null) {
                if (choice==3) logout();
                else if (choice==4) deactivate();
                else if(choice==5) uploadImage(sc);
                else if(choice==6) geMyDeta();
            }
            else if(choice==8) {
                jwtToken=null;
                System.out.println("Exiting CLI...");
                return;
            }
            else System.out.println("Invalid choice");
        }
    }

    private static void signup(Scanner sc) {
        System.out.print("Enter Adhar Number: ");
        String adharNo = sc.nextLine();

        System.out.print("Enter Phone number: ");
        String phNo = sc.nextLine();

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        JSONObject obj = new JSONObject();
        obj.put("adharNo", adharNo);
        obj.put("phNo", phNo);
        obj.put("password", password);
        String json = obj.toString();
        /* "adharNo": "1234567891",
            "phNo": "9230128898",
            "password": "suchorit" */

        String res=sendPostForSignup("/signup", json);
        if(res.contains("Succesfully")) {
            System.out.println("Successfully created");
            Map<String,String> map=KeyGeneration.getKey();
            sendPublicKey(adharNo,map.get("public"));
            storePrivateKey(adharNo,password,map.get("private"));
        }
        else System.out.println("Already exist");
    }

    private static void login(Scanner sc) {
        System.out.print("Enter Adhar Number: ");
        String adharNo = sc.nextLine();
        adhar=adharNo;
//        System.out.print("Enter Phone number: ");
        String phNo = "";

        System.out.print("Enter password: ");
        String password = sc.nextLine();
        JSONObject obj = new JSONObject();
        obj.put("adharNo", adharNo);
        obj.put("phNo", phNo);
        obj.put("password", password);
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

        } catch (Exception e) {
            System.out.println("❌ Invalid JSON response from server.");
            System.out.println("Server sent: " + response);
        }
    }

    private static void logout() {
        if (jwtToken == null) {
            System.out.println("⚠ You must log in first");
            return;
        }
        String res=sendPost("/logout", "", jwtToken);
        if(res.contains("Logout successfully")){
            jwtToken=null;
            System.out.println("Successfully Logout");
        }
        else if (res.contains("hakor")) System.out.println("You're not logged in");
//        System.out.println(res);
    }

    private static void deactivate() {
        String res=sendPost("/deactivate", "", jwtToken);
        if(res.contains("Deleted successfully")) {
            jwtToken=null;
            System.out.println("Account Deleted");
        }
        else System.out.println("No such account exist!!!");
        //System.out.println(res);
    }

    private static void geMyDeta(){
        List<String> response=new ArrayList<>
                (Arrays.asList(sendPost("/getAllDetails","",jwtToken).split("}")));
        if(response.isEmpty()){
            System.out.println("No data of this user is available");
            return;
        }
        for (String str:response){
            String[] s=str.split(",");
            for (int i=0;i< s.length-1;i++) System.out.println(s[i]);
            System.out.println("}");
        }
        getDetails();
    }

    private static void getDetails(){
        Scanner sc=new Scanner(System.in);
        System.out.print("which prescription you want to see?(Give the id): ");
        String id=sc.nextLine();
        String endpoint="/getDetails?id="+id;
        String response=sendPost(endpoint,"",jwtToken);
        JSONObject object=new JSONObject(response);
        //System.out.println(object);
//        String endpoint2="/giveKey?id="+id;
//        String res=sendPost(endpoint2,"",jwtToken);
//        JSONObject jsonObject=new JSONObject(res);
//        String key=jsonObject.getString("message");
//        byte[] sKey=Base64.getDecoder().decode(key);
//        X509EncodedKeySpec secret=new X509EncodedKeySpec(sKey);

        if(!object.has("image")){
            System.out.println("No Image found");
            return;
        }
        try {
            String img=object.getString("image");
            img=img.replaceAll("\\s","");
            byte[] decodedImg=Base64.getDecoder().decode(img);

            endpoint="/getSecretKey?id="+id;
            String res=sendPost(endpoint,"",jwtToken);
            JSONObject jsonObject=new JSONObject(res);
            String pubKey=jsonObject.getString("pubKey");
            PublicKey publicKey=loadECPublicKey(pubKey);
            PrivateKey privateKey = EcKeyUtil.loadPrivateKeyFromBase64
                    (EcKeyUtil.readStringFromFile(adhar + ".txt"));
            SecretKey secretKey=EcKeyUtil.deriveECDHKey(privateKey,publicKey);

            byte[] roleKey=Base64.getDecoder().decode(jsonObject.getString("role"));
            byte[] roleSecret=AESGCM.decrypt(roleKey,secretKey);
            SecretKey roleSecretKey=new SecretKeySpec(roleSecret,"AES");
            decodedImg=AESGCM.decrypt(decodedImg,roleSecretKey);
            if(jsonObject.has("spec")){
                byte[] specKey=Base64.getDecoder().decode(jsonObject.getString("spec"));
                byte[] specSecret=AESGCM.decrypt(specKey,secretKey);
                SecretKey specSecretKey = new SecretKeySpec(specSecret, "AES");
                decodedImg=AESGCM.decrypt(decodedImg,specSecretKey);
            }
            String extension = detectImageType(decodedImg); // png or jpg
            File tempFile = File.createTempFile("prescription_", "." + extension);
            tempFile.deleteOnExit();
            Desktop.getDesktop().open(tempFile);
            try(FileOutputStream fos=new FileOutputStream(tempFile)){
                fos.write(decodedImg);
            }
            System.out.println("Image opened");
        }catch (Exception e) {
            System.out.println("Error opening image: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private static void sendPublicKey(String adharNo,String key){
        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
        String endpoint="/setPublicKey?adharNo="+adharNo+"&key="+encodedKey;//adharNo=123456789012&key=YOUR_PUBLIC_KEY
        String response=sendPostForSignup(endpoint,"");
        System.out.println(response);
    }

    private static void uploadImage(Scanner sc) {
        PublicKey accessKey1, accessKey2 = null;
        PrivateKey privateKey = null;
        SecretKey roleKey, specKey;
        byte[] wrappedAESKey = null;
        if (jwtToken == null) {
            System.out.println("Please login first.");
            return;
        }

        System.out.println("Enter Hospital Id:");
        String hosid = sc.nextLine();

        System.out.print("Enter filePath: ");
        String filePath = sc.nextLine();

        System.out.print("Enter Allowed Role (Doctor/Nurse): ");
        String allowedRole = sc.nextLine();
        String response1 = sendPost("/getKey?role=" + allowedRole, "", jwtToken);
        JSONObject res1 = new JSONObject(response1);
        if (res1.get("message").equals(null)) {
            System.out.println("No such role exist");
            return;
        }
        try {
            accessKey1 = loadECPublicKey(res1.get("message").toString());
        } catch (Exception e) {
            System.out.println(e.getMessage() + " from access 1");
            return;
        }
        System.out.print("Enter Allowed Specialization (N/A if none): ");
        String specialization = sc.nextLine();

        JSONObject res2 = null;
        if (!specialization.isEmpty() && !specialization.equals("N/A")) {
            String response2 = sendPost("/getKey?role=" + specialization, "", jwtToken);
            res2 = new JSONObject(response2);
            if (res2.get("message").equals(null)) {
                System.out.println("No such specialization exist");
                return;
            }
            try {
                accessKey2 = loadECPublicKey(res2.get("message").toString());
            } catch (Exception e) {
                System.out.println(e.getMessage() + " from access 2");
                return;
            }
        }
        try {
            privateKey = EcKeyUtil.loadPrivateKeyFromBase64
                    (EcKeyUtil.readStringFromFile(adhar + ".txt"));
        } catch (Exception e) {
            System.out.println(e.getMessage() + " from access 2");
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File not found!");
            return;
        }
        File encryptedFile = null;
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            if (!specialization.isEmpty() && !specialization.contains("N/A")) {
                specKey = EcKeyUtil.deriveECDHKey(privateKey, accessKey2);
                fileBytes = AESGCM.encrypt(fileBytes, specKey);
            }
            roleKey = EcKeyUtil.deriveECDHKey(privateKey, accessKey1);
            byte[] encFileBytes = AESGCM.encrypt(fileBytes, roleKey);

            System.out.println("RoleKey:" + Base64.getEncoder().encodeToString(roleKey.getEncoded()));
            encryptedFile = new File(
                    file.getParent(),
                    "enc_" + file.getName()
            );

            try (FileOutputStream fos = new FileOutputStream(encryptedFile)) {
                fos.write(encFileBytes);
                fos.flush();
            } catch (Exception e) {
                System.out.println(e.getMessage() + "1");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage() + "2");
        }

        // Prepare JSON part
        JSONObject obj = new JSONObject();
        obj.put("hosId", hosid);
        obj.put("allowedRole", allowedRole);
        obj.put("allowedSpecialization", specialization);

        // Send multipart form-data
        String response = sendMultipart
                ("/upload", jwtToken, obj, "img", encryptedFile, "image/jpeg");
        System.out.println("\nServer Response: " + response);
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

    private static String sendPostForSignup(String endpoint, String json) {
        try {
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
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

    private static String sendMultipart(String endpoint, String token,
                                        JSONObject jsonPart, String fileFieldName,
                                        File file, String mimeType) {
        try {
            String boundary = "----Boundary" + System.currentTimeMillis();

            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            if (token != null) con.setRequestProperty("Authorization", "Bearer " + token);
            con.setDoOutput(true);

            OutputStream output = con.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true);

            // JSON PART
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"patientDetails\"\r\n");
            writer.append("Content-Type: application/json\r\n\r\n");
            writer.append(jsonPart.toString()).append("\r\n");
            writer.flush();

            // FILE PART
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"")
                    .append(fileFieldName).append("\"; filename=\"")
                    .append(file.getName()).append("\"\r\n");
            writer.append("Content-Type: ").append(mimeType).append("\r\n\r\n");
            writer.flush();

            // WRITE BINARY FILE DATA
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
            fis.close();

            writer.append("\r\n");
            writer.append("--").append(boundary).append("--\r\n");
            writer.close();

            // Read response
            int status = con.getResponseCode();
            InputStream is = (status >= 200 && status < 300)
                    ? con.getInputStream()
                    : con.getErrorStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null)
                response.append(line);

            return response.toString();

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }



    private static void storePrivateKey(String id,String pass,String key){
//        final byte[] GLOBAL_SALT = Base64.getDecoder()
//                .decode(pass);
        try{
//            SecretKey secretKey=KeyGeneration.getAESKeyFromPassword(key,GLOBAL_SALT);
//            String privateKeyEnc=KeyGeneration.encryptPrivateKey(key,secretKey);
            KeyGeneration.saveKey(key,id);
        }catch (Exception e){
            System.out.println("Exception at storePrivateKey: "+e.getMessage());
        }
    }

    public static String detectImageType(byte[] data) {
        // PNG signature
        if (data[0] == (byte)0x89 && data[1] == 0x50 && data[2] == 0x4E) {
            return "png";
        }
        // JPEG signature
        if (data[0] == (byte)0xFF && data[1] == (byte)0xD8) {
            return "jpg";
        }
        return "png"; // fallback
    }

    public static PublicKey loadECPublicKey(String base64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePublic(spec);
    }



}


