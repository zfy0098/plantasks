package com.rhjf.jifushangwu;

/**
 * Created by hadoop on 2018/3/16.
 *
 * @author hadoop
 */
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import java.security.SecureRandom;

/**
 * AES/CBC/PKCS5Padding 对称加密
 * @author jia
 *
 */
public class AES_CBC {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    /**
     * 数据加密
     * @param srcData
     * @param key
     * @param iv
     * @return
     */
    public static String encrypt(String srcData,byte[] key,byte[] iv)
    {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        Cipher cipher;
        String encodeBase64String = null;
        try {
            cipher = Cipher.getInstance(ALGORITHM);
            SecureRandom rnd = new SecureRandom();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, keySpec , ivParameterSpec);
            byte[] encData = cipher.doFinal(srcData.getBytes());
            return  Base64.encodeBase64String(encData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 数据解密
     * @param encDataStr
     * @param key
     * @param iv
     * @return
     */
    public static String decrypt(String encDataStr,byte[] key,byte[] iv)
    {
        byte[] encData = Base64.decodeBase64(encDataStr);
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        Cipher cipher;
        byte[] decbbdt = null;
        try {
            cipher = Cipher.getInstance(ALGORITHM);
            SecureRandom rnd = new SecureRandom();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec  , ivParameterSpec);
            decbbdt = cipher.doFinal(encData);
            return new String(decbbdt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }





    public static void main(String[] args) throws Exception {
        String str = "0000000000000000";
        String s2 = "中华人民共和国";
        String iv = "0000000000000000";
        System.out.println("加密前： "+s2);
        String encrypt = AES_CBC.encrypt(s2, str.getBytes(), iv.getBytes());
        System.out.println("加密后： "+new String(encrypt));


        encrypt = "zM4H2W1kYvmw9i5IrM6Yf81FRA3pCDE33uTgxKABBz/1jWp89CtS2MuItT7xgX9gj/LmvW6RXF3eQxjhkXPGpVmGrSL/HHB2LqHsE786k2Wr5NQH3lsEPIKiJ9OBPLo6dhNp5gPyq+1xb24BLhkWfR0jZdUurzm/nh3lFShoXx6ThkxJkDTi9AD8EerWAVYyOaW4QYJl93YolMUgZAgIfySF22qoiB5FSCWfu8n+T6knt4G9puaXGUDI7WYqdclC4zyyj5HDqY6qM8HTB0khFR46Y9t6n29bSwXuzoaJFL1BIBkC9gkZZVjd3S5M9T4JccNWuP9yNfthrwiqqf6f9GQA71tGyRGo8kcMIhFo5HXdDUQmBjaoMQuQhIca/lr7BAly4e+gUgaW+3Nc+tqmF3/wKhnxxaHiaPIcPeEuXrw86MabOdq3p8K5/OgJ7tLUppSW2XdX+QuJ4fH+s+dfLg==";

        String decrypt = AES_CBC.decrypt(encrypt, str.getBytes(), iv.getBytes());
        System.out.println("解密后： "+decrypt);
    }
}
