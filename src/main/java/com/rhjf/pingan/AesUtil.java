package com.rhjf.pingan;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

public class AesUtil {
    
    private static final String AES_ALGORITHM = "AES";
    private static final String CHARSET_UTF8 = "UTF-8";
    /**
     * 
     * @param aesKey aesKey
     * @param content 加密内容
     * @return
     */
    public static String AESEncode(String aesKey, String content) {
        try {
            // 1.构造密钥生成器，指定为AES算法,不区分大小写
            KeyGenerator keygen = KeyGenerator.getInstance(AES_ALGORITHM);
            
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG","SUN");
            secureRandom.setSeed(aesKey.getBytes());
            // 2.根据ecnodeRules规则初始化密钥生成器
            // 生成一个128位的随机源,根据传入的字节数组
//            keygen.init(128, new SecureRandom(aesKey.getBytes()));
            keygen.init(128, secureRandom);
            // 3.产生原始对称密钥
            SecretKey original_key = keygen.generateKey();
            // 4.获得原始对称密钥的字节数组
            byte[] raw = original_key.getEncoded();
            // 5.根据字节数组生成AES密钥
            SecretKey key = new SecretKeySpec(raw, AES_ALGORITHM);
            // 6.根据指定算法AES自成密码器
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            // 7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密解密(Decrypt_mode)操作，第二个参数为使用的KEY
            cipher.init(Cipher.ENCRYPT_MODE, key);
            // 8.获取加密内容的字节数组(这里要设置为utf-8)不然内容中如果有中文和英文混合中文就会解密为乱码
            byte[] byte_encode = content.getBytes(CHARSET_UTF8);
            // 9.根据密码器的初始化方式--加密：将数据加密
            byte[] byte_AES = cipher.doFinal(byte_encode);
            // 10.将加密后的数据转换为字符串
            String AES_encode = new String(Base64.encodeBase64(byte_AES));
            // 11.将字符串返回
            return AES_encode;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 如果有错就返加nulll
        return null;
    }

    /**
     * 
     * @param aesKey aesKey
     * @param content 待解密内容
     * @return
     */
    public static String AESDncode(String aesKey, String content) {
        try {
            // 1.构造密钥生成器，指定为AES算法,不区分大小写
            KeyGenerator keygen = KeyGenerator.getInstance(AES_ALGORITHM);
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG","SUN");
            secureRandom.setSeed(aesKey.getBytes());
            // 2.根据ecnodeRules规则初始化密钥生成器
            // 生成一个128位的随机源,根据传入的字节数组
            keygen.init(128, secureRandom);
            // 3.产生原始对称密钥
            SecretKey original_key = keygen.generateKey();
            // 4.获得原始对称密钥的字节数组
            byte[] raw = original_key.getEncoded();
            // 5.根据字节数组生成AES密钥
            SecretKey key = new SecretKeySpec(raw, AES_ALGORITHM);
            // 6.根据指定算法AES自成密码器
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            // 7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密(Decrypt_mode)操作，第二个参数为使用的KEY
            cipher.init(Cipher.DECRYPT_MODE, key);
            // 8.将加密并编码后的内容解码成字节数组
            byte[] byte_content = Base64.decodeBase64(content);
            /*
             * 解密
             */
            byte[] byte_decode = cipher.doFinal(byte_content);
            String AES_decode = new String(byte_decode, CHARSET_UTF8);
            return AES_decode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 如果有错就返加nulll
        return null;
    }
    
    public static String signRequest(Map<String, String[]> params,String aesKey) throws IOException {
		// 第一步：检查参数是否已经排序
		String[] keys = params.keySet().toArray(new String[0]);
		Arrays.sort(keys);

		// 第二步：把所有参数名和参数值串在一起
		StringBuilder query = new StringBuilder();
		boolean hasParam = false;
		
		for (String key : keys) {
			String[] value = params.get(key);
			// 除sign、signType字段
			if (isNotEmpty(key)&&value.length>=0&& !key.equals("sign")) {
				if (hasParam) {
					query.append("&");
				} else {
					hasParam = true;
				}
				query.append(key).append("=").append(value[0]);
			}
		}

		// 生成签名串
		String signstr="";
		try {
			signstr =AESEncode( aesKey, query.toString());
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		System.out.println("签名原串：" + query.toString());
		System.out.println("签名串：" + signstr);

		return signstr;
	}
	/**
	 * 检查是否不为空
	 */
	public static boolean isNotEmpty(String value) {
		int strLen;
		if (value == null || (strLen = value.length()) == 0) {
			return false;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(value.charAt(i)) == false)) {
				return true;
			}
		}
		return false;
	}

	public static boolean compareString(String sign,String signRequest){
		boolean flag=false;
		if (sign==null||signRequest==null) {
			return flag;
		}
		if(sign.equals(signRequest)){
			flag=true;
		}
		return flag;
	}
	
	

	/**
	 * AES加密
	 *
	 * @param key
	 *            密钥信息
	 * @param content
	 *            待加密信息
	 */
	public static byte[] encryptAES(byte[] key, byte[] content) throws Exception {
		// 不是16的倍数的，补足
		int base = 16;
		if (key.length % base != 0) {
			int groups = key.length / base + (key.length % base != 0 ? 1 : 0);
			byte[] temp = new byte[groups * base];
			Arrays.fill(temp, (byte) 0);
			System.arraycopy(key, 0, temp, 0, key.length);
			key = temp;
		}

		SecretKey secretKey = new SecretKeySpec(key, "AES");
//		IvParameterSpec iv = new IvParameterSpec(new byte[] { 0, 0, 0, 0, 0, 0,
//				0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//		cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] tgtBytes = cipher.doFinal(content);
		return tgtBytes;
	}
	
	
	/**
	 * AES解密
	 *
	 * @param key
	 *            密钥信息
	 * @param content
	 *            待加密信息
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptAES(byte[] key, byte[] content) throws Exception {
		// 不是16的倍数的，补足
		int base = 16;
		if (key.length % base != 0) {
			int groups = key.length / base + (key.length % base != 0 ? 1 : 0);
			byte[] temp = new byte[groups * base];
			Arrays.fill(temp, (byte) 0);
			System.arraycopy(key, 0, temp, 0, key.length);
			key = temp;
		}
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		SecretKey secretKey = new SecretKeySpec(key, "AES");
//		IvParameterSpec iv = new IvParameterSpec(new byte[] { 0, 0, 0, 0, 0, 0,
//				0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
//		cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] tgtBytes = cipher.doFinal(content);
		return tgtBytes;
	}
	
    public static void main(String[] args) {
        // AES se = new AES();
        // Scanner scanner = new Scanner(System.in);
        // /*
        // * 加密
        // */
        // System.out.println("使用AES对称加密，请输入加密的规则");
        // String encodeRules = scanner.next();
        // System.out.println("请输入要加密的内容:");
        // String content = scanner.next();
        // System.out.println("根据输入的规则" + encodeRules + "加密后的密文是:" +
        // se.AESEncode(encodeRules, content));
        //
        // /*
        // * 解密
        // */
        // System.out.println("使用AES对称解密，请输入加密的规则：(须与加密相同)");
        // encodeRules = scanner.next();
        // System.out.println("请输入要解密的内容（密文）:");
        // content = scanner.next();
        // System.out.println("根据输入的规则" + encodeRules + "解密后的明文是:" +
        // se.AESDncode(encodeRules, content));
//        String secretKey = "qqqq";
//        String content = "customerNo=12345678965896455&requestId=CNY&accNo=201609185876755000348101447824660&bankNo=25245&bankName=0.01&bankCode=ABC&payName=job&accType=0&phone=18786751372&txnAmt=1.11&backUrl=www.baidu.com&remark=交易&usage=购买服务器";
//        System.out.println("加密原文：" + content);
//        String encodeContent = AESEncode(secretKey, content);
//        System.out.println("加密内容：" + encodeContent);
//        String decodeContent = AESDncode(secretKey, encodeContent);
//        System.out.println("解密内容：" + decodeContent);
//        String sign="Kqr5pzSuQiBdZVNLIt7NwHYGX+vo847N1n003QY2xTKlfGjbBbVbDcNXeIecEcwS2pc6z/+va1Na7Eak9+FEBsyiStiiaEbA/qzt0xE6SxSis+AZ0TdFprFqhVfkZFcmfkrWyBTaFEj076jXvAVHcCFCOkjVLTKtUoVQZwzPIHs/BAJdP/3jWVXj4w8nfk+aacHGo1A8KW6XdmRnfSd/dyLNf3CksRQR3fxh4sjrO5MS/AlzKxoXRMUiFB0OXK1wuRKNw8PDNDpAaXjG8irjfKcc/Du8WUVYE9JlYUAZS688U8Q90Zs/0PZ57mDWgSpe";
//        String signRequset="Kqr5pzSuQiBdZVNLIt7NwHYGX+vo847N1n003QY2xTKlfGjbBbVbDcNXeIecEcwS2pc6z/+va1Na7Eak9+FEBsyiStiiaEbA/qzt0xE6SxSis+AZ0TdFprFqhVfkZFcmfkrWyBTaFEj076jXvAVHcCFCOkjVLTKtUoVQZwzPIHs/BAJdP/3jWVXj4w8nfk+aacHGo1A8KW6XdmRnfSd/dyLNf3CksRQR3fxh4sjrO5MS/AlzKxoXRMUiFB0OXK1wuRKNw8PDNDpAaXjG8irjfKcc/Du8WUVYE9JlYUAZS688U8Q90Zs/0PZ57mDWgSpe";
//        System.out.println(compareString(sign,signRequset));
        
        
    }
}
