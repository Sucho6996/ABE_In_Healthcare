import org.json.JSONObject;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;


import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
//import java.security.spec.ECParameterSpec;
//import java.security.spec.ECPoint;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.List;

public class UserCli {

    private static final String BASE_URL = "http://localhost:8090/user";
    /**
     * PMK (= PK = compressed a·G), same entry as AA {@code abe.master.public.key}.
     * SMK is AA {@code abe.master.secret.key} — never used client-side for OR encrypt; keep it off the CLI in production.
     */
    private static final String PROP_MASTER_PUBLIC = "abe.master.public.key";
    private static final String ABE_APPLICATION_PROPERTIES = "application.properties";

    /** Last-resort fallback; mirrors AttributeAuthority default {@code abe.master.public.key}. */
    private static final String DEFAULT_OR_MASTER_PK = "BBwlqaf2AsufDn9kGHNRrYV1YmiPUvfNkZH2qU5vIu+o+3OWeDnOkJ5PKjEViPmv5lISSbN9WPWn7yORMMSsC0M=";
    /** Same semantics as AA property {@code #PROP_MASTER_PUBLIC} (often copy of PMK Base64 only). */
    private static final String ENV_OR_MASTER_PK = "ABE_OR_MASTER_PK";
    private static final String OR_MASTER_PK_FILE = "abe_or_master_pk.base64";
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
                System.out.println("5. Upload Prescription (AND access Only)");
                System.out.println("6. Upload Prescription (OR-AND access)");
                System.out.println("7. See all details");
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
                else if(choice==5) uploadImageAnd(sc);
                else if(choice==6) uploadImageOr(sc);
                else if(choice==7) geMyDeta();
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
            }catch (Exception e){
                System.out.println("Error opening image: " + e.getMessage());
                return;
            }
            System.out.println("Image opened");
        }catch (Exception e) {
            //sc.nextLine();
            System.out.println("Error opening image: " + e.getMessage());
        }
    }

    private static void sendPublicKey(String adharNo,String key){
        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
        String endpoint="/setPublicKey?adharNo="+adharNo+"&key="+encodedKey;//adharNo=123456789012&key=YOUR_PUBLIC_KEY
        String response=sendPostForSignup(endpoint,"");
        System.out.println(response);
    }

    private static void uploadImageAnd(Scanner sc) {
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

            //System.out.println("RoleKey:" + Base64.getEncoder().encodeToString(roleKey.getEncoded()));
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
        obj.put("policyType","and");

        // Send multipart form-data
        String response = sendMultipart
                ("/upload", jwtToken, obj, "img", encryptedFile, "image/jpeg");
        System.out.println("\nServer Response: " + response);
    }

    private static String resolveOrMasterPublicKeyBase64() {
        try {
            String env = System.getenv(ENV_OR_MASTER_PK);
            if (env != null && !env.isBlank()) {
                System.out.println("(OR) PMK (" + PROP_MASTER_PUBLIC + "): env " + ENV_OR_MASTER_PK);
                return env.trim();
            }
            Path pkFile = Paths.get(OR_MASTER_PK_FILE);
            if (Files.exists(pkFile)) {
                String s = Files.readString(pkFile).trim();
                int nl = s.indexOf('\n');
                if (nl >= 0) {
                    s = s.substring(0, nl).trim();
                }
                if (!s.isEmpty() && !s.startsWith("#")) {
                    System.out.println("(OR) PMK: raw file " + pkFile.toAbsolutePath());
                    return s;
                }
            }
            Path appPropsPath = Paths.get(ABE_APPLICATION_PROPERTIES);
            if (Files.exists(appPropsPath)) {
                Properties props = new Properties();
                try (InputStream is = Files.newInputStream(appPropsPath)) {
                    props.load(is);
                }
                String pmk = props.getProperty(PROP_MASTER_PUBLIC);
                if (pmk != null && !pmk.isBlank()) {
                    System.out.println("(OR) PMK: " + appPropsPath.toAbsolutePath() + " → " + PROP_MASTER_PUBLIC);
                    return pmk.trim();
                }
            }
        } catch (Exception e) {
            System.err.println("(OR) Master PK resolve warning: " + e.getMessage());
        }
        System.out.println("(OR) PMK: built-in default (" + PROP_MASTER_PUBLIC
                + " — copy AA application.properties section if mismatched).");
        return DEFAULT_OR_MASTER_PK;
    }

    private static void uploadImageOr(Scanner sc) {
        List<List<String>> accessTree = new ArrayList<>();
        List<String> list = new ArrayList<>();

        System.out.println("Enter Hospital Id:");
        String hosid = sc.nextLine();

        System.out.print("Enter filePath: ");
        String filePath = sc.nextLine();

        char choice = 'y';
        while (choice == 'y' || choice == 'Y') {
            System.out.print("Enter Allowed Role (Doctor/Nurse): ");
            String allowedRole = sc.nextLine().toLowerCase();
            System.out.print("Enter Allowed Specialization (N/A if none): ");
            String specialization = sc.nextLine().toLowerCase();

            if (allowedRole.isEmpty()) {
                System.out.println("No role Specified.");
            } else {
                if (!specialization.equals("na") && !specialization.equals("n/a")) {
                    list.add(allowedRole);
                    list.add(specialization);
                    accessTree.add(new ArrayList<>(List.of(allowedRole, specialization)));
                } else {
                    list.add(allowedRole);
                    accessTree.add(new ArrayList<>(List.of(allowedRole)));
                }
            }
            System.out.println("Press Y to add another Role\nPress other character to exit");
            System.out.print("Your Choice:");
            choice = sc.next().charAt(0);
            sc.nextLine();
        }

        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File not found!");
            return;
        }

        try {
            // 1. Fetch File and Keys
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String queryString = "roles=" + String.join(",", list);
            String response = sendGet("/getPubKeys?" + queryString, jwtToken);
            JSONObject pubKeysJson = new JSONObject(response);

            // 2. Setup Bouncy Castle Curve Parameters
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
            BigInteger q = ecSpec.getN();
            SecureRandom random = new SecureRandom();

            // 3. Algorithm: Choose random k from Zq*
            BigInteger k = new BigInteger(q.bitLength(), random).mod(q);

            // 4. PK = AA PMK (abe.master.public.key); scalar a stays on AA as SMK (abe.master.secret.key).
            String masterPkB64 = resolveOrMasterPublicKeyBase64();
            byte[] masterPkBytes = Base64.getDecoder().decode(masterPkB64);

            // --- THE CRITICAL FIX FOR STEP 4 ---
            ECPoint PK;
            try {
                // Try to parse standard Java X.509 format first
                org.bouncycastle.asn1.x509.SubjectPublicKeyInfo spki =
                        org.bouncycastle.asn1.x509.SubjectPublicKeyInfo.getInstance(masterPkBytes);
                PK = ecSpec.getCurve().decodePoint(spki.getPublicKeyData().getBytes());
            } catch (Exception e) {
                // Fallback if it's already a raw EC point
                PK = ecSpec.getCurve().decodePoint(masterPkBytes);
            }
            // ------------------------------------

            ECPoint SK = PK.multiply(k).normalize();

            // 5. Algorithm: Hash SK's X-coordinate to get AES_KEY
            BigInteger kx = SK.getAffineXCoord().toBigInteger();
            byte[] aesKeyBytes = EcKeyUtil.sha256FromP256AffineX(kx); // Using your custom hasher
            System.out.println("Derived AES Key (SHA-256 of SK.x): " + Base64.getEncoder().encodeToString(aesKeyBytes));
            SecretKeySpec aesKey = new SecretKeySpec(aesKeyBytes, "AES");

            // 6. Algorithm: Encrypt Message (Cm)
            byte[] encFileBytes = AESGCM.encrypt(fileBytes, aesKey); // Your existing AES util
            File encryptedFile = new File(file.getParent(), "enc_" + file.getName());
            try (FileOutputStream fos = new FileOutputStream(encryptedFile)) {
                fos.write(encFileBytes);
            }

            // 7. Algorithm: Generate Attribute Ciphertexts (Ci) for the Access Tree
            JSONObject ciMap = new JSONObject();

            for (List<String> branch : accessTree) {
                if (branch.size() == 1) {
                    // OR Branch with single attribute (Threshold 1) -> share is just k
                    String attr = branch.get(0);

                    // Extract raw point from X.509 encoding
                    byte[] attrBytes = Base64.getDecoder().decode(pubKeysJson.getString(attr));
                    org.bouncycastle.asn1.x509.SubjectPublicKeyInfo spki =
                            org.bouncycastle.asn1.x509.SubjectPublicKeyInfo.getInstance(attrBytes);
                    ECPoint pk_i = ecSpec.getCurve().decodePoint(spki.getPublicKeyData().getBytes());

                    ECPoint c_i = pk_i.multiply(k).normalize();
                    ciMap.put(attr, Base64.getEncoder().encodeToString(c_i.getEncoded(true)));

                } else if (branch.size() == 2) {
                    // AND Branch with two attributes (Threshold 2) -> Split k using 1-degree polynomial: f(x) = k + r*x
                    String attr1 = branch.get(0);
                    String attr2 = branch.get(1);

                    BigInteger r = new BigInteger(q.bitLength(), random).mod(q);
                    BigInteger share1 = k.add(r).mod(q);                            // f(1) = k + r(1)
                    BigInteger share2 = k.add(r.multiply(BigInteger.TWO)).mod(q);  // f(2) = k + r(2)

                    // Calculate Ci for attr1
                    byte[] attr1Bytes = Base64.getDecoder().decode(pubKeysJson.getString(attr1));
                    org.bouncycastle.asn1.x509.SubjectPublicKeyInfo spki1 =
                            org.bouncycastle.asn1.x509.SubjectPublicKeyInfo.getInstance(attr1Bytes);
                    ECPoint pk_1 = ecSpec.getCurve().decodePoint(spki1.getPublicKeyData().getBytes());
                    ECPoint c_1 = pk_1.multiply(share1).normalize();
                    ciMap.put(attr1, Base64.getEncoder().encodeToString(c_1.getEncoded(true)));

                    // Calculate Ci for attr2
                    byte[] attr2Bytes = Base64.getDecoder().decode(pubKeysJson.getString(attr2));
                    org.bouncycastle.asn1.x509.SubjectPublicKeyInfo spki2 =
                            org.bouncycastle.asn1.x509.SubjectPublicKeyInfo.getInstance(attr2Bytes);
                    ECPoint pk_2 = ecSpec.getCurve().decodePoint(spki2.getPublicKeyData().getBytes());
                    ECPoint c_2 = pk_2.multiply(share2).normalize();
                    ciMap.put(attr2, Base64.getEncoder().encodeToString(c_2.getEncoded(true)));
                }
            }

            // 8. Prepare JSON payload for the Backend
            JSONObject obj = new JSONObject();
            obj.put("hosId", hosid);
            obj.put("allowedRole", "N/A"); // Legacy fields, keeping them safe
            obj.put("allowedSpecialization", "N/A");
            obj.put("policyType", "or");

            // This is CT = (T, Ci) from your algorithm
            obj.put("accessTree", accessTree.toString()); // Stores as "[[doctor, cardiology], [nurse]]"
            obj.put("cipherKey", ciMap.toString());       // Stores the encrypted pieces

            // 9. Send multipart form-data to your User Service
            String uploadResponse = sendMultipart("/upload", jwtToken, obj, "img", encryptedFile, "image/jpeg");
            System.out.println("\nServer Response: " + uploadResponse);

        } catch (Exception e) {
            System.out.println("Encryption Error: " + e.getMessage());
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

    private static String sendGet(String endpoint, String token) {
        try {
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");

            if (token != null) {
                con.setRequestProperty("Authorization", "Bearer " + token);
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


