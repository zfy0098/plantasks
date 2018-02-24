package com.rhjf.tjyl;

import java.io.BufferedReader; 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;

import cfca.sadk.algorithm.common.Mechanism;
import cfca.sadk.algorithm.common.PKIException;
import cfca.sadk.lib.crypto.JCrypto;
import cfca.sadk.lib.crypto.Session;
import cfca.sadk.util.CertUtil;
import cfca.sadk.util.KeyUtil;
import cfca.sadk.util.Signature;
import cfca.sadk.x509.certificate.X509Cert;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class UnionPayUtil {

	public static Log log = LogFactory.getLog(UnionPayUtil.class);
    //测试环境
    private static final String IP = "211.103.172.38";
    //生产环境
//    private static final String IP = "144.112.33.225";

    
	public static final int PORT = 8830;				//  socket 端口
	
	public static final String MERID = "309113148162011";			//商户代码
	public static final String ENTERPRISENO = "80001";   //企业编号
	
	public static final String BACKURL = "10.10.10.20:8889";         //异步结果回调地址
	
	public static final int LOCALPORT = 8889;       //本地socket端口  接收异步回调
	public static final String SM2PASS = "1234";    //证书密码
	
	
	public static final String pfx = "/usr/local/xydf.pfx";
	
	static Signature engine = new Signature();
	
	
	
	
	
	public String toJSON(UnionPayModel unionpay){
		String result= JSON.toJSONString(unionpay);
		return result;
	}
	
	/**
	 * 发送代付
	 * @param map
	 * @return
	 */
	public static String unionPayExecpayment(Map<String, String> map){
		map.put("txnType", "12");
		Map<String, String> signature = null;
		try {
			signature = UnionPayUtil.signature(map);
		    log.info("---发送的报文---"+signature);
			String res = connServer(signature, UnionPayUtil.IP, UnionPayUtil.PORT);
			log.info("===返回的报文==="+res);
			return res;
		}catch (UnsupportedEncodingException e) {
			log.error("方法：unionPayExecpayment,系统异常(UnsupportedEncodingException)："+e.getMessage());
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			log.error("方法：unionPayExecpayment,系统异常(FileNotFoundException)："+e.getMessage());
			e.printStackTrace();
		} catch (PKIException e) {
			log.error("方法：unionPayExecpayment,系统异常(PKIException)："+e.getMessage());
			e.printStackTrace();
		}
		return new String();
	}
	
	/**
	 * 交易查询
	 * @param map
	 * @return
	 */
    public static String unionPayQuery(Map<String, String> map){
    	map.put("txnType", "00");
		Map<String, String> signature = null;
		try {
			signature = UnionPayUtil.signature(map);
		
		log.info("---发送的查询报文---"+signature);
		
			String res = connServer(signature, UnionPayUtil.IP, UnionPayUtil.PORT);
			log.info("===返回的报文==="+res);
			return res;
		} catch (UnsupportedEncodingException e) {
			log.error("方法：unionPayQuery,系统异常(UnsupportedEncodingException)："+e.getMessage());
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			log.error("方法：unionPayQuery,系统异常(FileNotFoundException)："+e.getMessage());
			e.printStackTrace();
		} catch (PKIException e) {
			log.error("方法：unionPayQuery,系统异常(PKIException)："+e.getMessage());
			e.printStackTrace();
		}
		return new String();
	}
    /**
     * socket发送
     * @param map
     * @param ip
     * @param port
     * @return
     */
    private  static String connServer(Map<String, String> map,String ip, int port) {

		String respXml = "";
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
			 socket.connect(new InetSocketAddress(ip, port),1000);// 连接超时设置
			socket.setSoTimeout(5000); // 读写超时设置
			os = socket.getOutputStream();
			pw = new PrintWriter(os);
			is = socket.getInputStream();

			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			// 3.利用流按照一定的操作，对socket进行读写操作
			String info = JSON.toJSONString(map);
			pw.write(info);
			pw.flush();
			socket.shutdownOutput();
			// 接收服务器的相应
			String reply = null;
			while (!((reply = br.readLine()) == null)) {
				respXml += reply;
			}
			System.out.println("接到返回：\n" + respXml);
			socket.shutdownInput();
		} catch (Exception e) {
			e.printStackTrace();
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
		return respXml;
	}
	/**
	 * 报文签名
	 * @param map
	 * @return
	 * @throws PKIException
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
    private static Map<String, String> signature(Map<String, String> map) throws PKIException, FileNotFoundException, UnsupportedEncodingException{
		map.put("merId", UnionPayUtil.MERID);
		map.put("enterpriseNo", UnionPayUtil.ENTERPRISENO);
		map.put("backUrl", UnionPayUtil.BACKURL);
		map.put("version", "1.0");
		map.put("signMethod", "01");
		//map.put("oldMerid", "");
		
		PrivateKey priKey;
		X509Cert cert = null;
		//X509Cert[] certs = null;
		// 验签证书
		//final String cmbcCertPath =UnionPayUtil.class.getResource(CER).getPath();
		// 消息签名
//		final String sm2Path = UnionPayUtil.class.getResource("2222.pfx").getPath();
		File f = new File(pfx);
		final String sm2Path = f.getAbsolutePath();
		
		// 签名密码
		final String sm2Pass = SM2PASS;
		priKey = KeyUtil.getPrivateKeyFromPFX(sm2Path, sm2Pass);
		//cert = new X509Cert(new FileInputStream(cmbcCertPath));
		//使用私钥也能验签
		cert = CertUtil.getCertFromPFX(sm2Path, sm2Pass);
		//certs = new X509Cert[] { cert };
		final String deviceType = JCrypto.JSOFT_LIB;
		JCrypto.getInstance().initialize(deviceType, null);
		final Session session = JCrypto.getInstance().openSession(deviceType);
		byte[] sourceData = signData(map).toString().getBytes("UTF8");
		// 加密串
		byte[] signature = engine.p1SignMessage(Mechanism.SHA1_RSA, sourceData,
				priKey, session);
		// 消息签名
		map.put("signature", getBase64(new String(signature)));
		return map;
	}
	
    /**
     * 报文验签
     * @param map
     * @return
     * @throws PKIException
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     */
    public static boolean checkSign(Map<String, String> map) throws PKIException, UnsupportedEncodingException, FileNotFoundException{
		PrivateKey priKey;
		X509Cert cert = null;
		X509Cert[] certs = null;
		// 验签证书
		//final String cmbcCertPath = UnionPayUtil.class.getResource("2222.cer").getPath();
		// 消息签名
		//final String sm2Path = UnionPayUtil.class.getResource("2222.pfx").getPath();
		
		File f = new File(pfx);
		final String sm2Path = f.getAbsolutePath();
		// 签名密码
		final String sm2Pass = SM2PASS;
		priKey = KeyUtil.getPrivateKeyFromPFX(sm2Path, sm2Pass);
		//cert = new X509Cert(new FileInputStream(cmbcCertPath));
		//使用私钥也能验签
		cert = CertUtil.getCertFromPFX(sm2Path, sm2Pass);
		//certs = new X509Cert[] { cert };
		final String deviceType = JCrypto.JSOFT_LIB;
		JCrypto.getInstance().initialize(deviceType, null);
		final Session session = JCrypto.getInstance().openSession(deviceType);
		boolean ooo = engine.p1VerifyMessage(Mechanism.SHA1_RSA, signData(map)
				.toString().getBytes("UTF8"),
				getFromBase64(map.get("signature")).getBytes("UTF8"),
				cert.getPublicKey(), session);
		return ooo;
	}
    private static Map<String, String> signData(Map<String, ?> contentData) {
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
	
	public static String getBase64(String str) {  
        byte[] b = null;  
        String s = null;  
        try {  
            b = str.getBytes("utf-8");  
        } catch (UnsupportedEncodingException e) {  
           e.printStackTrace();
        }  
        if (b != null) {  
            s = new BASE64Encoder().encode(b);  
        }  
        return s;  
    }  
  
    /**
     * 
     *********************************************************.<br>
     * [方法] getFromBase64 <br>
     * [描述] (解密  ) <br>
     * [参数] (对参数的描述) <br>
     * [返回] String <br>
     * [时间] 2015-7-26 上午11:49:28 <br>
     *********************************************************.<br>
     */
    public static String getFromBase64(String s) {  
        byte[] b = null;  
        String result = null;  
        if (s != null) {  
            BASE64Decoder decoder = new BASE64Decoder();  
            try {  
                b = decoder.decodeBuffer(s);  
                result = new String(b, "utf-8");  
            } catch (Exception e) {  
            	e.printStackTrace();
            }  
        }  
        return result;
    }
    
	 public static String changeY2F(String amount){    
	        String currency =  amount.replaceAll("\\$|\\￥|\\,", "");  //处理包含, ￥ 或者$的金额    
	        int index = currency.indexOf(".");    
	        int length = currency.length();    
	        Long amLong = 0l;    
	        if(index == -1){    
	            amLong = Long.valueOf(currency+"00");    
	        }else if(length - index >= 3){    
	            amLong = Long.valueOf((currency.substring(0, index+3)).replace(".", ""));    
	        }else if(length - index == 2){    
	            amLong = Long.valueOf((currency.substring(0, index+2)).replace(".", "")+0);    
	        }else{    
	            amLong = Long.valueOf((currency.substring(0, index+1)).replace(".", "")+"00");    
	        }    
	        return amLong.toString();    
	} 
}
