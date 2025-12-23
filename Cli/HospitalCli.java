import org.json.JSONObject;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;

public class HospitalCli {

    private static final String BASE_URL = "http://localhost:8081/hospital";
    private static String jwtToken = null;



    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice;

        while (true) {
            System.out.println("\n=== Hospital Service CLI ===");
//            System.out.println("1. Signup");
            if (jwtToken==null) System.out.println("1. Login");
            else {
                System.out.println("2. Logout");
                System.out.println("3. Reset Password");
                System.out.println("4. Add Staff");
                System.out.println("5. See all Staff");
                System.out.println("6. Remove Staff");
            }
            System.out.println("7. Exit");
            System.out.print("Enter choice: ");

            choice = sc.nextInt();
            sc.nextLine(); // consume newline

            if(choice==1) login(sc);
            else if(jwtToken!=null) {
                if(choice==2) logout();
                else if (choice==3) resetPass(sc);
                else if (choice==4) addStaff(sc);
                else if (choice==5) seeAll();
                else if (choice==6) deleteStaff(sc);
            }
            else if(choice==7) {
                jwtToken=null;
                System.out.println("Exiting CLI...");
                return;
            }
            else System.out.println("Invalid choice");

        }
    }

    private static void login(Scanner sc) {
        System.out.print("Enter Id: ");
        String id = sc.nextLine();

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("name","naam sey kya matlab");
        obj.put("pass", password);

        String json = obj.toString();

        String response = sendPost("/login", json, null);

        if (response.isEmpty()) {
            System.out.println("❌ Server did not respond.");
            return;
        }

        System.out.println("RAW RESPONSE = " + response);

        try {
            JSONObject resObj = new JSONObject(response);

            if (resObj.has("error")) {
                System.out.println("❌ " + resObj.getString("error"));
                return;
            }

            if (resObj.toString().contains("Invalid Credential") || resObj.toString().contains("No value present")) {
                System.out.println("Invalid Credential");
                return;
            }

            jwtToken = resObj.getString("message");
            System.out.println("\n✅ Login successful!\n");
            if(resObj.get("logtime").equals(null)){
                resetPass(id,sc);
            }

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
        if(res.toString().contains("Logout successfully")){
            System.out.println("Successfully Logout");
            jwtToken=null;
        }
        else if (res.toString().contains("hakor")) System.out.println("You're not logged in");
//        System.out.println(res);
    }

    private static void resetPass(Scanner sc) {
        System.out.println("Enter New Password: ");
        String pass=sc.nextLine();
        JSONObject obj=new JSONObject();
        obj.put("id","");
        obj.put("name","");
        obj.put("pass", pass);
        String res=sendPost("/resetPass", obj.toString(), jwtToken);
        if(res.toString().contains("Password changed successfully")) System.out.println("Password changed successfully");
        else System.out.println(res.toString().substring(7));
        logout();
        //System.out.println(res);
    }

    private static void resetPass(String id,Scanner sc) {
        System.out.println("Enter New Password: ");
        String pass=sc.nextLine();
        JSONObject obj=new JSONObject();
        obj.put("id","");
        obj.put("name","");
        obj.put("pass", pass);
        String res=sendPost("/resetPass", obj.toString(), jwtToken);
        if(res.toString().contains("Password changed successfully")) {
            System.out.println("Password changed successfully");
            Map<String,String> map=KeyGeneration.getKey();
            sendPublicKey(map.get("public"));
            storePrivateKey(id,pass,map.get("private"));
        }
        else System.out.println(res.toString().substring(7));
        logout();
        //System.out.println(res);
    }

    private static void addStaff(Scanner sc){
        System.out.println("Enter Registration Number: ");
        String regNo=sc.nextLine();
        System.out.println("Enter Name: ");
        String name=sc.nextLine();
        System.out.println("Enter Designation (Doctor or nurse or management): ");
        String designation=sc.nextLine();
        System.out.println("Enter Registration Specialization (If any otherwise N/A): ");
        String specialization=sc.nextLine();
        System.out.println("Enter Password: ");
        String pass=sc.nextLine();
        JSONObject obj=new JSONObject();
        obj.put("regNo",regNo);
        obj.put("name",name);
        obj.put("designation",designation);
        obj.put("specialization",specialization);
        obj.put("pass",pass);
        String res=sendPost("/addStaff",obj.toString(),jwtToken);
        if (res.isEmpty()) {
            System.out.println("❌ Something is wrong");
            return;
        } else if (res.contains("successfully")) {
            System.out.println("Staff added successfully");
        }
    }

    private static void seeAll(){
        String[] res=sendPost("/seeAllStaff","",jwtToken).split("}");
        for(String str:res)
            System.out.println(str+"}");
    }

    private static void deleteStaff(Scanner sc){
        System.out.println("Enter the staff Registration number");
        String regNo=sc.nextLine();
        String endpoint="/removeStaff?regNo="+regNo;
        String res=sendPost(endpoint,"",jwtToken);
        if (res.contains("No value")) System.out.println("No Such Staff present");
        else {
            System.out.println("Staff has been revoked");
            seeAll();
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

    private static void sendPublicKey(String key){
        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
        String endpoint="/setPublicKey?key="+encodedKey;
        String response=sendPost(endpoint,"",jwtToken);
        System.out.println(response);
    }
    private static void storePrivateKey(String id,String pass,String key){
        final byte[] GLOBAL_SALT = Base64.getDecoder()
                .decode(pass);
        try{
//            SecretKey secretKey=KeyGeneration.getAESKeyFromPassword(key,GLOBAL_SALT);
//            String privateKeyEnc=KeyGeneration.encryptPrivateKey(key,secretKey.toString());
            KeyGeneration.saveKey(key,id);
        }catch (Exception e){
            System.out.println("Exception at storePrivateKey: "+e.getMessage());
        }
    }
}





