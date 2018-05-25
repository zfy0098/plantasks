package com.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import org.apache.commons.codec.digest.DigestUtils;

/** 
* 功能：MD5签名
* 版本：3.3
* 修改日期：2012-08-17
* */
public class MD5 {

    
   
    /**
     * 签名字符串
     * @param text 需要签名的字符串
     * @param key 密钥
     * @param input_charset 编码格式
     * @return 签名结果
     */
    public static String sign(String text , String input_charset) {
    	System.out.println(text);
        return DigestUtils.md5Hex(getContentBytes(text, input_charset));
    }
    

    /**
     * @param content
     * @param charset
     * @return
     * @throws SignatureException
     * @throws UnsupportedEncodingException 
     */
    private static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }
    
    /**
	 * MD5加密
	 * 
	 * @param string 待加密字符串
	 * @param encoding 编码格式
	 * @return 加密结果
	 */
    public static String md5(String string, String encoding) {
        if (string == null) {
            return null;
        }

        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(string.getBytes(encoding));
        } catch (NoSuchAlgorithmException e) {
        	System.out.println(e);
            return string;
        } catch (UnsupportedEncodingException e) {
        	System.out.println(e);
            return string;
        }

        byte[] byteArray = messageDigest.digest();
        
        return toHexString(byteArray);//
    }
    
    /**
     * 转换为十六进制字符串
     * 
     * @param byteArray 字节数组
     * @return 十六进制字符串
     */
    public static String toHexString(byte[] byteArray) {
        StringBuilder strBuilder = new StringBuilder();

//        for (int i = 0; i < byteArray.length; i++) {
//            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
//            	strBuilder.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
//            else
//            	strBuilder.append(Integer.toHexString(0xFF & byteArray[i]));
//        }
 	   int i;
 	   for (int offset = 0; offset < byteArray.length; offset++) {
 	    i = byteArray[offset];
 	    if (i < 0)
 	     i += 256;
 	    if (i < 16)
 	    	strBuilder.append("0");
 	   strBuilder.append(Integer.toHexString(i));
 	  }

        return strBuilder.toString();
    }
    
    
	/**
	 * 为数字字符串左填充0
	 * 
	 * @param str 原字符串
	 * @param strLength 需要填充的总长度
	 * @return
	 */
	public static String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        StringBuffer sb = null;
        while (strLen < strLength) {
              sb = new StringBuffer();
              sb.append("0").append(str);
              str = sb.toString();
              strLen = str.length();
        }
        return str.substring(str.length() - 4);
    }
	
	public static String appendLeft(String str, String addStr, int strLength) {
        int strLen = str.length();
        StringBuffer sb = null;
        while (strLen < strLength) {
              sb = new StringBuffer();
              sb.append(addStr).append(str);
              str = sb.toString();
              strLen = str.length();
        }
        return str;
	}

}