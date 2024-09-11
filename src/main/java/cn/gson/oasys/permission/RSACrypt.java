package cn.gson.oasys.permission;

import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;

/**
 * 非对称加密RSA加密和解密
 */
public class RSACrypt {
    private static final String transformation = "RSA";
    private static final int ENCRYPT_MAX_SIZE = 117; // 加密：每次最大加密长度117个字节
    private static final int DECRYPT_MAX_SIZE = 128; // 解密：每次最大解密长度128个字节

    /**
     * 私钥加密
     */
    public static String encryptByPrivateKey(String input, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] encrypt = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(encrypt);
    }

    /**
     * 公钥解密
     */
    public static String decryptByPublicKey(String input, PublicKey publicKey) throws Exception {
        byte[] decode = Base64.getDecoder().decode(input);
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] encrypt = cipher.doFinal(decode);
        return new String(encrypt);
    }

    /**
     * 公钥加密
     */
    public static String encryptByPublicKey(String input, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encrypt = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(encrypt);
    }

    /**
     * 私钥解密
     */
    public static String decryptByPrivateKey(String input, PrivateKey privateKey) throws Exception {
        byte[] decode = Base64.getDecoder().decode(input);
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] encrypt = cipher.doFinal(decode);
        return new String(encrypt);
    }

    /**
     * 私钥分段加密
     */
    public static String encryptByPrivateKey2(String input, PrivateKey privateKey) throws Exception {
        byte[] byteArray = input.getBytes();
        byte[] temp;
        int offset = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        while (byteArray.length - offset > 0) {
            if (byteArray.length - offset >= ENCRYPT_MAX_SIZE) {
                temp = cipher.doFinal(byteArray, offset, ENCRYPT_MAX_SIZE);
                offset += ENCRYPT_MAX_SIZE;
            } else {
                temp = cipher.doFinal(byteArray, offset, byteArray.length - offset);
                offset = byteArray.length;
            }
            bos.write(temp);
        }
        bos.close();

        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }

    /**
     * 公钥分段解密
     */
    public static String decryptByPublicKeyKey2(String input, PublicKey publicKey) {
        try {
            byte[] byteArray = Base64.getDecoder().decode(input);
            byte[] temp;
            int offset = 0;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, publicKey);

            while (byteArray.length - offset > 0) {
                if (byteArray.length - offset >= DECRYPT_MAX_SIZE) {
                    temp = cipher.doFinal(byteArray, offset, DECRYPT_MAX_SIZE);
                    offset += DECRYPT_MAX_SIZE;
                } else {
                    temp = cipher.doFinal(byteArray, offset, byteArray.length - offset);
                    offset = byteArray.length;
                }
                bos.write(temp);
            }
            bos.close();

            return new String(bos.toByteArray());
        } catch (Throwable err) {
            return null;
        }
    }

    /**
     * 公钥分段加密
     */
    public static String encryptByPublicKey2(String input, PublicKey publicKey) throws Exception {
        byte[] byteArray = input.getBytes();
        byte[] temp;
        int offset = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        while (byteArray.length - offset > 0) {
            if (byteArray.length - offset >= ENCRYPT_MAX_SIZE) {
                temp = cipher.doFinal(byteArray, offset, ENCRYPT_MAX_SIZE);
                offset += ENCRYPT_MAX_SIZE;
            } else {
                temp = cipher.doFinal(byteArray, offset, byteArray.length - offset);
                offset = byteArray.length;
            }
            bos.write(temp);
        }
        bos.close();

        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }

    /**
     * 私钥分段解密
     */
    public static String decryptByPrivateKey2(String input, PrivateKey privateKey) throws Exception {
        byte[] byteArray = Base64.getDecoder().decode(input);
        byte[] temp;
        int offset = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        while (byteArray.length - offset > 0) {
            if (byteArray.length - offset >= DECRYPT_MAX_SIZE) {
                temp = cipher.doFinal(byteArray, offset, DECRYPT_MAX_SIZE);
                offset += DECRYPT_MAX_SIZE;
            } else {
                temp = cipher.doFinal(byteArray, offset, byteArray.length - offset);
                offset = byteArray.length;
            }
            bos.write(temp);
        }
        bos.close();

        return new String(bos.toByteArray());
    }
}