package com.rhjf.unionpaydf;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import cfca.sadk.algorithm.common.Mechanism;
import cfca.sadk.algorithm.common.PKIException;
import cfca.sadk.lib.crypto.JCrypto;
import cfca.sadk.lib.crypto.Session;
import cfca.sadk.util.KeyUtil;
import cfca.sadk.util.Signature;
import cfca.sadk.x509.certificate.X509Cert;
import net.sf.json.JSONObject;

public class TJYLBase {
//	public static String tjyl_confirmSignCert = LoadPro.loadProperties("http","tjyl_pay_confirmSignCert");
//	private static int reqtimeouts = Integer.parseInt(LoadPro.loadProperties("http", "ac_reqtimeout"));
//	private static int contimeouts = Integer.parseInt(LoadPro.loadProperties("http", "ac_contimeout"));
	
	
	private static final Log logger = LogFactory.getLog(TJYLBase.class);	
	  
	
	public static String  tjyl_confirmSignCert = "/usr/local/unionpaydf.cer";
	private static int reqtimeouts = 60000;
	private static int contimeouts = 60000;
	
	static Signature engine = new Signature();

	public static Map<String, String> submitDate(Map<String, ?> contentData,
			String requestUrl, String signCert, String password ) {
		Map<String, String> submitFromData = (Map<String, String>) signData(contentData, signCert, password, "UTF-8");
		Map<String, String> resData = new HashMap<String, String>();
		String resultString = "";// 上端返回报文
		logger.info("Request to unionpaydf [" + requestUrl + "]"+ submitFromData.toString());
		String[] reqAddress = requestUrl.split(":");
		// 1.建立客户端socket连接，指定服务器位置及端口
		Socket socket = null;
		// 2.得到socket读写流
		OutputStream os = null;
		PrintWriter pw = null;
		// 输入流
		InputStream is = null;
		BufferedReader br = null;
		try {
			socket = new Socket();
			socket.connect(
					new InetSocketAddress(reqAddress[0],
							Integer.parseInt(reqAddress[1])), contimeouts);// 连接超时设置
			socket.setSoTimeout(reqtimeouts); // 读写超时设置
			os = socket.getOutputStream();
			pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);
			is = socket.getInputStream();

			br = new BufferedReader(new InputStreamReader(is, "utf-8"));
			// 3.利用流按照一定的操作，对socket进行读写操作
			String info = jsonObject(submitFromData);
			pw.write(info);
			pw.flush();
			socket.shutdownOutput();
			// 接收服务器的相应
			String reply = null;
			while (!((reply = br.readLine()) == null)) {
				resultString += reply;
			}
			logger.info("Respone from  unionpaydf [" + requestUrl + "]" + resultString);
			socket.shutdownInput();
		} catch (Exception e) {
			e.printStackTrace();
			resData.put("tranFlag", "false");
			resData.put("respCode", "000010");
			return resData;
		} finally {
			try {
				// 4.关闭资源
				if (br != null) {
					br.close();
				}
				if (is != null) {
					is.close();
				}
				if (pw != null) {
					pw.close();
				}
				if (os != null) {
					os.close();
				}
				if (socket != null) {
					socket.close();
				}
			} catch (Exception e2) {
			}
		}
		/**
		 * 验证签名
		 */
		if (null != resultString && !"".equals(resultString)) {
			try {
				final String deviceType = JCrypto.JSOFT_LIB;
				resData = JSONObject.fromObject(resultString);
				X509Cert cert = new X509Cert(new FileInputStream(
						tjyl_confirmSignCert));

				JCrypto.getInstance().initialize(deviceType, null);
				Session session = JCrypto.getInstance().openSession(deviceType);
				// 消息验签
				boolean ooo = engine
						.p1VerifyMessage(Mechanism.SHA1_RSA, signData(resData)
								.toString().getBytes("UTF8"), Base64
								.getFromBase64(resData.get("signature"))
								.getBytes("UTF8"), cert.getPublicKey(), session);
				if (!ooo) {
					logger.info("验证签名失败！");
					return null;
				}
				if (resData.get("respCode") != null) {
					String respCode = resData.get("respCode");
					resData.remove("respCode");
					resData.put("respCode", respCode);
				}
				if (resData.get("origRespCode") != null) {
					String origRespCode = resData.get("origRespCode");
					resData.remove("origRespCode");
					resData.put("origRespCode", origRespCode);
				}
			} catch (FileNotFoundException e) {
				return null;
			} catch (UnsupportedEncodingException e) {
				return null;
			} catch (PKIException e) {
				return null;
			}

		} else {
			// 修复获取不到响应报文时默认状态
			return null;
		}
		return resData;

	}

	/**
	 * 对数据签名
	 * 
	 * @param contentData
	 * @return 签名后的map对象
	 */
	public static Map<String, String> signData(Map<String, ?> contentData,
			String certPath, String certPwd, String encoding) {
		Map<String, String> submitFromData = new TreeMap<String, String>();
		Object[] key_arr = contentData.keySet().toArray();
		Arrays.sort(key_arr);
		for (Object key : key_arr) {
			Object value = contentData.get(key);
			if (value != null && StringUtils.isNotBlank(value.toString())) {
				if (!key.equals("signature")) {
					submitFromData.put(key.toString().trim(), value.toString()
							.trim());
				}
			}
		}

		byte[] signature = null;
		final String deviceType = JCrypto.JSOFT_LIB;
		try {
			PrivateKey priKey = KeyUtil.getPrivateKeyFromPFX(certPath, certPwd);
			JCrypto.getInstance().initialize(deviceType, null);
			Session session = JCrypto.getInstance().openSession(deviceType);
			signature = engine.p1SignMessage(Mechanism.SHA1_RSA, submitFromData
					.toString().getBytes("utf-8"), priKey, session);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (PKIException e) {
			e.printStackTrace();
		}
		submitFromData
				.put("signature", Base64.getBase64(new String(signature)));

		return submitFromData;
	}

	public static Map<String, String> signData(Map<String, ?> contentData) {
		Map<String, String> submitFromData = new TreeMap<String, String>();
		Object[] key_arr = contentData.keySet().toArray();
		Arrays.sort(key_arr);
		for (Object key : key_arr) {
			Object value = contentData.get(key);
			if (value != null && StringUtils.isNotBlank(value.toString())) {
				if (!key.equals("signature")) {
					submitFromData.put(key.toString().trim(), value.toString()
							.trim());
				}
			}
		}
		return submitFromData;
	}

	public static String changeRetCode(String retCode) {
		if (retCode.length() > 2) {

			if (retCode.equals("Z17")) {
				return "01";
			} else if (retCode.equals("Z15")) {
				return "68";
			} else if (retCode.equals("Z14")) {
				return "31";
			} else if (retCode.equals("Z13")) {
				return "10";
			} else if (retCode.equals("Z12")) {
				return "04";
			} else if (retCode.equals("Z11")) {
				return "33";
			} else if (retCode.equals("Z10")) {
				return "11";
			} else {
				return "04";
			}
		} else {
			return retCode;
		}
	}

	protected static String jsonObject(Object obj) {

		String json = "";
		try {
			json = JSONObject.fromObject(obj).toString();
			return json;
		} catch (Exception e) {
			json = "{\"err\":\"" + "JsonObject is wrong" + "\"}";
			return json;
		}
	}
}
