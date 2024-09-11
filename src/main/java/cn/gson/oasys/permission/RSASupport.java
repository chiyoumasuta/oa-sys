package cn.gson.oasys.permission;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

@Component
public class RSASupport {

    @Value("${keys.path}")
    private String outPath;

    private final String ENCRYPT_MODE = "RSA";
    private final int KEY_SIZE = 1024;

    public KeyPair genKeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(ENCRYPT_MODE);
        kpg.initialize(KEY_SIZE);
        System.out.println("--------生成密钥对--------");
        return kpg.genKeyPair();
    }

    public void saveKey(KeyPair keyPair) throws Exception {
        Base64.Encoder encoder = Base64.getEncoder();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        FileOutputStream outPub = new FileOutputStream(outPath + "/key.pub");
        outPub.write(encoder.encode(publicKey.getEncoded()));
        outPub.close();

        FileOutputStream outPvt = new FileOutputStream(outPath + "/key");
        outPvt.write(encoder.encode(privateKey.getEncoded()));
        outPvt.close();

        System.out.println("--------写入密钥--------");
        System.gc();
    }

    public String loadPublicKey() throws Exception {
        FileInputStream inStream = new FileInputStream(outPath + "/key");
        byte[] bytes = new byte[inStream.available()];
        return new String(bytes);
    }

    public String decrypt(String info) throws Exception {
        FileInputStream inStream = new FileInputStream(outPath + "/key");
        byte[] bytes = new byte[inStream.available()];
        inStream.read(bytes);
        inStream.close();
        String keyString = new String(bytes, StandardCharsets.UTF_8);
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPT_MODE);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        byte[] decode = Base64.getDecoder().decode(info);
        Cipher cipher = Cipher.getInstance(ENCRYPT_MODE);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] resultBytes = cipher.doFinal(decode);

        return new String(resultBytes);
    }

    public String encrypt(String data, String publicKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPT_MODE);
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        Cipher cipher = Cipher.getInstance(ENCRYPT_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        String encryptedString = Base64.getEncoder().encodeToString(encryptedBytes);
        return encryptedString;
    }
}