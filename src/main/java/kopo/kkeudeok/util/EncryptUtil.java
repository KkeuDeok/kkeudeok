package kopo.kkeudeok.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class EncryptUtil {
    private static final String addMessage = "PolyDataAnalysis*";

    private static final byte[] ivBytes = new byte[16];
    private static final String key = "PolyTechnic12345";

    public static String encHashSHA256(String str) {
        String result;
        String plainText = addMessage + str;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(plainText.getBytes());
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            result = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            result = "";
        }
        return result;
    }

    public static String encAES128CBC(String str)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        byte[] textBytes = str.getBytes(StandardCharsets.UTF_8);

        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSPec = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivSPec);

        byte[] encrypted = cipher.doFinal(textBytes);
        return Base64.getEncoder().encodeToString(encrypted);
    }
    public static String decAES128CBC(String str)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException{
        byte[] encryptedBytes = Base64.getDecoder().decode(str);
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8),"AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec,ivSpec);

        byte[] decrypted = cipher.doFinal(encryptedBytes);

        return new String(decrypted, StandardCharsets.UTF_8);

    }

}
