package com.rhjf.tecrwin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.rhjf.base.BaseDao;
import com.rhjf.utils.HttpClient;
import com.rhjf.utils.UtilKey;


/**
 *   查询腾势商户注册结果
 * @author hadoop
 *
 */
public class QueryResgin {

	private static final String URL = "http://192.168.17.189:8080/payform/payform";
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private BaseDao db = new BaseDao();
	
	public void init(){
		
		log.info("开始执行注册查询操作");
		
		// 查询注册以后没有更新结果的商户  未验证的商户
		String sql = "select * from tab_loginuser where RegisStatus=1";
		List<Map<String,Object>> userlist = db.queryForList(sql, null);
		
		log.info("需要查询的数量:" + userlist.size()); 
		
		//  查询通道加密key和机构代码
		sql = "select * from tab_wx_merchant where channelID=4";
		Map<String,Object> merchant = db.queryForMap(sql,null);
		
		List<Object[]> list = new ArrayList<Object[]>();
		for (int i = 0; i < userlist.size(); i++) {
			Map<String ,Object> userMap = userlist.get(i);
			
			SimpleDateFormat sf = new SimpleDateFormat ("yyyyMMddHHmmss");
			String nowtime = sf.format(new Date());
		    String orderid = nowtime + String.format("%06d", db.executeProcedure("GetSysSerialNo"));
			
			Map<String,Object> params = new TreeMap<String,Object>();
			params.put("transType", "G002");
			params.put("orgId", merchant.get("OrgCode"));
			params.put("orgOrderNo", orderid);
			params.put("account", userMap.get("LoginID"));
			
			log.info("查询注册状态的商户：" + params.toString());
			
			StringBuilder sbd = new StringBuilder();
			for (String key : params.keySet()) {
				sbd.append(key);
				sbd.append(params.get(key));
			}
			sbd.append(merchant.get("WxKey"));
			
			try {
				params.put("signature", UtilKey.MD5(sbd.toString()));
			} catch (Exception e) {
				log.error("计算签名失败");
				continue;
			}
			
			String content = HttpClient.post(URL, params, null);
			
			log.info(userMap.get("LoginID") + ":注册返回结果:" + content); 
			
			try {
				JSONObject json = JSONObject.fromObject(content);
				String respCode = json.getString("respCode");
				if(respCode.equals("200")){
					String accountSt = json.getString("accountSt");
					list.add(new Object[]{accountSt,userMap.get("LoginID")});
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		
		sql = "update tab_loginuser set RegisStatus=? where LoginID=?";
		db.executeBatchSql(sql, list);
	}
	
}
