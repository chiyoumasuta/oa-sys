package cn.gson.oasys.permission;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;

public class RsaHelp {

    private static PublicKey pubKey;
    private static PrivateKey privateKey;

    static {
        String key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDI3fzepshAcGvYKYKfGY8T7GOet3hKCcQ6a3v6dDOFMCAM9YfTA3kuO3H1Rt83d7ga/SrW4rju09tNaAQE5bQDjDpwR+BdPTH6x+DxR7fpFH2fN1aL96VitoYFpXas9/NS+PVOpwtyPHgTKsTmhyUj5xeH5ysqQsQZcFLr0Q5XowIDAQAB";
        byte[] publicBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            pubKey = keyFactory.generatePublic(keySpec);

            byte[] privateBytes = Base64.getDecoder().decode("MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMjd/N6myEBwa9gpgp8ZjxPsY563eEoJxDpre/p0M4UwIAz1h9MDeS47cfVG3zd3uBr9KtbiuO7T201oBATltAOMOnBH4F09MfrH4PFHt+kUfZ83Vov3pWK2hgWldqz381L49U6nC3I8eBMqxOaHJSPnF4fnKypCxBlwUuvRDlejAgMBAAECgYAImnF1O+GB2Q7VcFm25MpbCRnvN16AlVCBTA9AV+/I+Zm11GJf6FstXsBvx/xRjOAmz8cg8w8Gs65F73mUmB4QoWOKv5Jlylb3uiNmuHPn8hp9SszMUESh4gZlmYaL5POFEj+KleO7tSp0wjHHJ2vjB07jKiO7K8Try+1E41+tQQJBAO25nOABEbuH7HfkW+XRNC7+ptxK4KAi37mscYSu0tPfjgdpCA4dDF5HDEfWwarAwXv0KiYlDN9XwYQJ750G7IMCQQDYTwSNbv53ShtYLtkRClRnyCsJaa/N+/oge9vRmvFxYKY66JX+aI1d8tXC3rMGP9z5azBsw5nsNzaOtRMgjT5hAkEArrUOmYGvqoaGPsZQ02EgXLlBn/xXgNigWzBkbQKeZp+RHdkO1nB6un60g8dMpVTr3VDf+RCZmTpBOeyjcF+6SQJAPH/NTCmRLpghkcZ4m9WByg0oTFes/jXNvawmzTdC9G0N0UggO6nOcEptEzXqaIMQMQqT3rfOo0zGC6AFm4MlwQJBAOU7GXx/T6yScxSnuPpZt+r0eR5FQF3jjBxlmSvYBuvD7z3x96cPj7W4K0uXi+r21GhpQ9UrnVoVf5xws+DMzig=");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateBytes);
            KeyFactory privateKeyFactory = KeyFactory.getInstance("RSA");
            privateKey = privateKeyFactory.generatePrivate(privateKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String decryptByPublicKeyKey2(String input) {
        return RSACrypt.decryptByPublicKeyKey2(input, pubKey);
    }

    private static final String transformation = "RSA";
    private static final int ENCRYPT_MAX_SIZE = 117;

    public static String encryptByPrivateKey2(String input) {
        byte[] byteArray = input.getBytes(Charset.forName("UTF-8"));
        byte[] temp;
        int offset = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }
}