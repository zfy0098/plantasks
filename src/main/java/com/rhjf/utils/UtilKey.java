package com.rhjf.utils;

import java.io.UnsupportedEncodingException;
import java.security.Key; 
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;

public class UtilKey {

	
	/** 字符串默认键�?    */
	private String strDefaultKey = "bingoDownloadKey";

	/** 加密工具     */
	private Cipher encryptCipher = null;

	/** 解密工具     */
	private Cipher decryptCipher = null;
	/** 静�?对象 */
	static private UtilKey instance;

	/**
	 * 建构函数私有以防止其它对象创建本类实�?
	 * @throws Exception 
	 */
	private UtilKey() throws Exception {
		init();
	}

	/**
	 * 创建唯一实例
	 * 
	 * @return
	 * @throws Exception 
	 */
	static public UtilKey getInstance() throws Exception {
		if (instance == null) {
			instance = new UtilKey();
		}
		return instance;
	}

	/**
	 * 初始化构�?
	 * @throws Exception
	 */
	private void init() throws Exception {
		Key key = getKey(strDefaultKey.getBytes());
		encryptCipher = Cipher.getInstance("DES");
		encryptCipher.init(Cipher.ENCRYPT_MODE, key);
		decryptCipher = Cipher.getInstance("DES");
		decryptCipher.init(Cipher.DECRYPT_MODE, key);
	}

	/**  
	* 从指定字符串生成密钥，密钥所�?��字节数组长度�?�?不足8位时后面�?，超�?位只取前8�? 
	*   
	* @param arrBTmp  
	*            构成该字符串的字节数�? 
	* @return 生成的密�? 
	* @throws java.lang.Exception  
	*/
	private Key getKey(byte[] arrBTmp) throws Exception {
		// 创建�?��空的8位字节数组（默认值为0�?  
		byte[] arrB = new byte[8];
		// 将原始字节数组转换为8�?  
		for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
			arrB[i] = arrBTmp[i];
		}
		// 生成密钥   
		Key key = new javax.crypto.spec.SecretKeySpec(arrB, "DES");
		return key;
	}

	/**
	 * 加密
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public String encryption(String value) throws Exception {
		return byteArr2HexStr(encrypt(value.getBytes()));
	}

	/**
	 * 解密
	 * @param value
	 * @return
	 * @throws Exception 
	 */
	public String decryption(String value) throws Exception {
		return new String(decrypt(hexStr2ByteArr(value)));
	}

	/**  
	* 加密字节数组  
	*   
	* @param arrB  
	*            �?��密的字节数组  
	* @return 加密后的字节数组  
	* @throws Exception  
	*/
	private byte[] encrypt(byte[] arrB) throws Exception {
		return encryptCipher.doFinal(arrB);
	}

	/**  
	   * 解密字节数组  
	   *   
	   * @param arrB  
	   *            �?��密的字节数组  
	   * @return 解密后的字节数组  
	   * @throws Exception  
	   */
	private byte[] decrypt(byte[] arrB) throws Exception {
		return decryptCipher.doFinal(arrB);
	}

	/**  
	* 将byte数组转换为表�?6进制值的字符串， 如：byte[]{8,18}转换为：0813�?和public static byte[]  
	* hexStr2ByteArr(String strIn) 互为可�?的转换过�? 
	*   
	* @param arrB  
	*            �?��转换的byte数组  
	* @return 转换后的字符�? 
	* @throws Exception
	*/
	private String byteArr2HexStr(byte[] arrB) throws Exception {
		int iLen = arrB.length;
		StringBuffer sb = new StringBuffer(iLen * 2);
		for (int i = 0; i < iLen; i++) {
			int intTmp = arrB[i];
			while (intTmp < 0) {
				intTmp = intTmp + 256;
			}
			if (intTmp < 16) {
				sb.append("0");
			}
			sb.append(Integer.toString(intTmp, 16));
		}
		return sb.toString();
	}

	/**  
	* 将表�?6进制值的字符串转换为byte数组�?和public static String byteArr2HexStr(byte[] arrB)  
	* 互为可�?的转换过�? 
	*   
	* @param strIn  
	*            �?��转换的字符串  
	* @return 转换后的byte数组  
	* @throws Exception  
	*             本方法不处理任何异常，所有异常全部抛�? 
	* @author
	*/
	private byte[] hexStr2ByteArr(String strIn) throws Exception {
		byte[] arrB = strIn.getBytes();
		int iLen = arrB.length;

		// 两个字符表示�?��字节，所以字节数组长度是字符串长度除�?   
		byte[] arrOut = new byte[iLen / 2];
		for (int i = 0; i < iLen; i = i + 2) {
			String strTmp = new String(arrB, i, 2);
			arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
		}
		return arrOut;
	}
	

	/**
	 *   MD5加密
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static String MD5(String input) {
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		try {
			MessageDigest md = MessageDigest.getInstance(System.getProperty("MD5.algorithm", "MD5"));
			
			StringBuffer sb = new StringBuffer();
			int t;
			for (int i = 0; i < 16; i++) {
				t = md.digest(input.getBytes("utf-8"))[i];
				if (t < 0){
					t += 256;
				}
				sb.append(hexDigits[(t >>> 4)]);
				sb.append(hexDigits[(t % 16)]);
			}
			return sb.toString().toLowerCase();
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
}
