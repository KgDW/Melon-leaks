package dev.zenhao.melon.utils.other;

import com.google.common.hash.Hashing;
import dev.zenhao.melon.Melon;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class VerifyOld {
    private String HWID_URL = "";

    public static byte[] rawHWID() throws NoSuchAlgorithmException {
        String main = System.getenv("PROCESS_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL") + System.getenv("PROCESSOR_REVISION") + System.getenv("PROCESSOR_ARCHITECTURE") + System.getenv("PROCESSOR_ARCHITEW6432") + System.getenv("NUMBER_OF_PROCESSORS") + System.getenv("COMPUTERNAME");
        byte[] bytes = main.getBytes(StandardCharsets.UTF_8);
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        return messageDigest.digest(bytes);
    }

    public static String Encrypt(String strToEncrypt, String secret) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(1, VerifyOld.getKey(secret));
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        }
        catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
            return null;
        }
    }

    public static SecretKeySpec getKey(String myKey) {
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, "AES");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getEncryptedHWID(String key) {
        try {
            String a = Hashing.sha1().hashString(new String(VerifyOld.rawHWID(), StandardCharsets.UTF_8), StandardCharsets.UTF_8).toString();
            String b = Hashing.sha256().hashString(a, StandardCharsets.UTF_8).toString();
            String c = Hashing.sha512().hashString(b, StandardCharsets.UTF_8).toString();
            String d = Hashing.sha1().hashString(c, StandardCharsets.UTF_8).toString();
            return VerifyOld.Encrypt(d, "xg42" + key);
        }
        catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }

    public List<String> getHwidAndTypeList() {
        ArrayList<String> HWIDList = new ArrayList<String>();
        try {
            String inputLine;
            URL url = new URL(this.HWID_URL);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((inputLine = in.readLine()) != null) {
                HWIDList.add(inputLine);
            }
        }
        catch (Exception e) {
            Melon.logger.error("Load HWID Failed: " + e.getMessage());
        }
        return HWIDList;
    }

    public UserType getType(String HwidAndTypeString) {
        String[] HwidAndType = HwidAndTypeString.split(":");
        if (HwidAndType[1] == "U") {
            return UserType.USER;
        }
        if (HwidAndType[1] == "P") {
            return UserType.PREMIUM;
        }
        if (HwidAndType[1] == "PV") {
            return UserType.PREMIUM_VIP;
        }
        if (HwidAndType[1] == "E") {
            return UserType.ELITE;
        }
        if (HwidAndType[1] == "EV") {
            return UserType.ELITE_VIP;
        }
        if (HwidAndType[1] == "D") {
            return UserType.DEVELOPER;
        }
        return UserType.COMMUNITY;
    }
}

