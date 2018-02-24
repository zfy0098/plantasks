package com.rhjf.utils;


import java.awt.image.BufferedImage;  
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

public class URLImage64Bit {


/**
 *   根据图片的ULR地址获取该图片的base64编码
 *   并把图片保存到本地
 * @author hadoop
 *
 */

	private HttpURLConnection httpUrl = null;
	
	public static void main(String[] args) throws Exception {
		URLImage64Bit c = new URLImage64Bit();
		String url = "http://img05.tooopen.com/images/20150202/sy_80219211654.jpg";
		String path = "D:/0000.JPG";
//		InputStream in = c.saveToFile(url);	//从URL读取图片
//		String str = c.GetImageStrByInPut(in);	//读取输入流,转换为Base64字符
//		System.out.println(str);
//		generateImage(str, path);			//将Base64字符转换为图片
//		c.closeHttpConn();
		
		String imgstr = encodeImgageToBase64(new URL(url));
		System.out.println(imgstr);
		generateImage(imgstr, path);		
		
	}

	
	public void closeHttpConn(){
		httpUrl.disconnect();
	}
	
	
	
	
	/**
	 * // 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
	 * @param imageUrl
	 * @return
	 */
	public static String encodeImgageToBase64(URL imageUrl) {
		ByteArrayOutputStream outputStream = null;
		try {
			BufferedImage bufferedImage = ImageIO.read(imageUrl);
			outputStream = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "jpg", outputStream);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 对字节数组Base64编码
		return new String(Base64.encodeBase64(outputStream.toByteArray()));
	}
	
	
	
	
	/**
	 * 从URL中读取图片,转换成流形式.
	 * @param destUrl
	 * @return
	 */
	public InputStream saveToFile(String destUrl){
		
		URL url = null;
		InputStream in = null; 
		try{
			url = new URL(destUrl);
			httpUrl = (HttpURLConnection) url.openConnection();
			httpUrl.connect();
			httpUrl.getInputStream();
			in = httpUrl.getInputStream();			
			return in;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 读取输入流,转换为Base64字符串
	 * @param input
	 * @return
	 */
	public String GetImageStrByInPut(InputStream input) {
		byte[] data = null;
		// 读取图片字节数组
		try {
			data = new byte[input.available()];
			input.read(data);
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 对字节数组Base64编码
		return new String(Base64.encodeBase64(data));
	}

	
	/**
	 * 图片转化成base64字符串 将图片文 件转化为字节数组字符串，并对其进行Base64编码处理
	 * 
	 * @return
	 */
	public static String GetImageStr(File file) {
		InputStream in = null;
		byte[] data = null;
		// 读取图片字节数组
		try {
			in = new FileInputStream(file);
			data = new byte[in.available()];
			in.read(data);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 对字节数组Base64编码
		return new String(Base64.encodeBase64(data));
	}

	/**
	 * base64字符串转化成图片 对字节数组字符串进行Base64解码并生成图片
	 * 
	 * @param imgStr
	 *            数据内容(字符串)
	 * @param path
	 *            输出路径
	 * @return
	 */
	public static boolean generateImage(String imgStr, String path) {
		if (imgStr == null) // 图像数据为空
			return false;
		try {
			byte[] b = Base64.decodeBase64(imgStr.getBytes());  // Base64解码
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {// 调整异常数据
					b[i] += 256;
				}
			}
			// 生成jpeg图片
			OutputStream out = new FileOutputStream(path);
			out.write(b);
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
