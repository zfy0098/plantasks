package com.rhjf.tecrwin;

import java.awt.image.BufferedImage; 
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.rhjf.base.BaseDao;
import com.rhjf.base.PropertyUtils;
import com.rhjf.utils.HttpClient;
import com.rhjf.utils.UtilKey;

public class UserResgin {
	
	private static final String URL = "http://106.14.36.181:8081/payform/payform";
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private BaseDao db = new BaseDao();
	
	public void userResgin() throws Exception{ 
		log.info("执行腾势商户注册功能");
		String sql = "select * from tab_loginuser where RegisStatus=0 or RegisStatus is null";
		List<Map<String ,Object>> userList = db.queryForList(sql, null);
		
		sql = "select * from tab_wx_merchant where channelid=?";
		Map<String,Object> MerchantInfo = db.queryForMap(sql, new Object[]{4});
		
		List<Object[]> params = new ArrayList<Object[]>();
		for (int i = 0; i < userList.size(); i++) {
			
			Map<String,Object> userMap = userList.get(i);
			
			SimpleDateFormat sf = new SimpleDateFormat ("yyyyMMddHHmmss");
			String sendTime = sf.format(new Date());
		    String orderid = sendTime + String.format("%06d", db.executeProcedure("GetSysSerialNo"));
			
			Map<String,Object> map = new TreeMap<String,Object>();
			map.put("transType", "G001");
			map.put("orgId", MerchantInfo.get("OrgCode").toString()); //必填，6为平台机构号
			map.put("account", userMap.get("LoginID").toString()); // 必填，11位手机号 
			map.put("password", userMap.get("LoginPass").toString()); //必填，商户密码 userMap.get("LoginPass").toString()
			map.put("orgOrderNo", orderid);
			map.put("mchntName", userMap.get("MerchantName").toString()); //必填，商户名称 //userMap.get("MerchantName").toString()
			map.put("cardNo", userMap.get("BankCardNo").toString()); //必填，银行卡号
			map.put("pmsBankNo", userMap.get("BankNo").toString()); //必填，12位联行号
			map.put("certType", "00");
			map.put("certNo", userMap.get("IDCardNum").toString());//必填，证件号
			map.put("mobile", userMap.get("LoginID").toString()); // 必填，结算卡绑定的11位手机号码
			map.put("realName", userMap.get("RealName").toString()); //必填，结算卡对应的真实姓名  userMap.get("RealName").toString()
			
			// 手持身份证
			String certMeetURL = PropertyUtils.getValue("imageurl")
					+ changeUrl(UtilKey.getInstance().decryption(userMap.get("Photo3").toString()));
			
			map.put("certMeet",  encodeImgageToBase64(certMeetURL));
			
			// 身份证 正反面照片
			String certCorrectURL = PropertyUtils.getValue("imageurl")
					+ changeUrl(UtilKey.getInstance().decryption(userMap.get("Photo1").toString()));
			
			map.put("certCorrect" , encodeImgageToBase64(certCorrectURL));
			
			
			StringBuilder sbd = new StringBuilder();
			for (String key : map.keySet()) {
				sbd.append(key);
				sbd.append(map.get(key));
			}
			sbd.append(MerchantInfo.get("WxKey").toString());
			map.put("signature",UtilKey.MD5(sbd.toString()));
			
			String result = HttpClient.post(URL,map,null);
			
			System.out.println(result);
			
			try{
				JSONObject json = JSONObject.fromObject(result);
				
				log.info(userMap.get("LoginID").toString() + "注册返回结果:" + result); 
				
				String respCode = json.getString("respCode");
				if(respCode.equals("200")){
					String accountSt = json.getString("accountSt");
					params.add(new Object[]{accountSt,userMap.get("LoginID").toString()});
				}
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}
		sql = "update tab_loginuser set RegisStatus=? where LoginID=?";

		db.executeBatchSql(sql, params);
	}
	
	/**
	 * // 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
	 * @param imageUrl
	 * @return
	 */
	public  String encodeImgageToBase64(String imageUrl) {
		URL url;
		try {
			url = new URL(imageUrl);
			ByteArrayOutputStream outputStream = null;
			try {
				BufferedImage bufferedImage = ImageIO.read(url);
				outputStream = new ByteArrayOutputStream();
				ImageIO.write(bufferedImage, "jpg", outputStream);
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// 对字节数组Base64编码
			//return  new String(Base64.encode(outputStream.toByteArray()));
			return new String(Base64.encodeBase64(outputStream.toByteArray()));
		} catch (MalformedURLException e2) {
			e2.printStackTrace();
		}
		return null;
	}
	
	public String changeUrl(String url){
		String[] urlBuffer = url.split("/");
		return urlBuffer[urlBuffer.length-2]+"/"+urlBuffer[urlBuffer.length-1];
	}
	
}
