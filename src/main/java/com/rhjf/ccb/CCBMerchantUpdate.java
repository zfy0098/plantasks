package com.rhjf.ccb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.alibaba.fastjson.JSON;
import com.rhjf.base.BaseDao;
import com.rhjf.utils.AESUtil;
import com.rhjf.utils.HttpClient;
import com.rhjf.utils.RandomUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *  建设银行商户修改
 * @author a
 *
 */

public class CCBMerchantUpdate extends BaseDao{

	/** 修改商户信息 **/
	public static final String modifyMerchantURL = "https://api.jia007.com/api-center/rest/v1.0/yqt/modifyMerchantInfo";
	
	/** 注册测试商编 **/
	public static final String regisMerchatNo = "1051100010000722";
	
	public static final String regisKey = "0bd9016221fa4d3db7fd5b9b9410c51c8709212f605947a08054ee9443ea25cb";
	
	
	public void init (){

		log.info("开始修改建行商户信息 ----- 上报第二种支付类型费率 ");

		String sql = "select * from tbl_pay_merchantchannel where channelCode='CCB' and t0PayResult is not null";

		List<Map<String,Object>> merchantlist = queryForList(sql, null);

		for (Map<String,Object>  merchantMap : merchantlist) {

			log.info("==========修改商户：" + merchantMap.get("merchantNo") + "的信息");


			String merchantinfosql = "select * from tbl_pay_merchant where merchantNo=?";

			Map<String,Object> merchantinfoMap = queryForMap(merchantinfosql, new Object[]{ merchantMap.get("merchantNo")});


			Map<String,Object> map = new HashMap<String,Object>();

			map.put("requestNo", RandomUtils.getRandomString(7));
			map.put("merchantNo", merchantMap.get("superMerchantNo"));

			map.put("merchantShortName", merchantinfoMap.get("showName"));
			map.put("servicePhone", merchantinfoMap.get("linkPhone"));

			String merchantfeeSQL = "select *  from tbl_online_product_fee where merchantNo=? and bankId=?";

			log.info("=========查询商户：" + merchantMap.get("merchantNo") + "支付宝的T1 费率");

			Map<String,Object> wxfee = queryForMap(merchantfeeSQL, new Object[]{merchantMap.get("merchantNo") , "Alipay"});

			if(wxfee==null||wxfee.isEmpty()){
				log.info("=========商户：" + merchantMap.get("merchantNo") + "支付宝T1 费率费率不存在，停止该商户修改操作");
				continue;
			}

			JSONArray payArray = new JSONArray();

			JSONObject payJSON = new JSONObject();

			int wxlength = WXbusiness.length;
			Random random = new Random(wxlength-1);
			int index = random.nextInt(wxlength-1);
			String cccNumber = WXbusiness[index];
			// ALIPAY_SCAN
			payJSON.put("payTool", "ALIPAY_SCAN");
			payJSON.put("feeRate", wxfee.get("value"));
			payJSON.put("business", cccNumber);
			payArray.add(payJSON);

			map.put("payProduct", payArray.toString());

			Map<String,Object> params = new HashMap<String,Object>();
			params.put("appKey", regisMerchatNo);

			log.info("商户修改" + merchantMap.get("merchantNo") + "请求报文：" + JSON.toJSONString(map));

			String data = AESUtil.encrypt(JSON.toJSONString(map), regisKey.substring(0, 16));
			params.put("data", data);

			log.info("商户修改" + merchantMap.get("merchantNo") + "请求密文：" + JSON.toJSONString(params));

			String conntent = HttpClient.post(modifyMerchantURL, params, "1");

			log.info("商户修改" + merchantMap.get("merchantNo") + "响应=====密文：" + conntent);

			String respstr = AESUtil.decrypt(conntent, regisKey.substring(0, 16));

			log.info("商户修改" + merchantMap.get("merchantNo") + "响应=====原文：" + respstr);

			try {
				JSONObject json = JSONObject.fromObject(respstr);
				if("1".equals(json.getString("code"))){

					log.info("商户修改" + merchantMap.get("merchantNo") + "商户修改成功");

					String upsql = "update tbl_pay_merchantchannel set t0PayResult = null where channelCode='CCB' and merchantNo=? ";

					int x = executeSql(upsql, new Object[]{merchantMap.get("merchantNo")});

					if(x < 1){
						log.info("商户修改" + merchantMap.get("merchantNo")  + "更新数据库失败");
					}
				}
			} catch (Exception e) {
				log.info("商户修改" + merchantMap.get("merchantNo")+ "信息异常" + e.getMessage());
			}
		}
	}
	
	public static void main(String[] args) {
		CCBMerchantUpdate ccbupdate = new CCBMerchantUpdate();
		ccbupdate.init();
	}
	
	
	static final String[] WXbusiness = {"100100001","100100002","100100003","100100004","100100005","100100006","100100007","100100008",
			"100100009","100100010","100100011","100100012","100100013","100100014","100100015","100100016","100100017","100100018","100100020",
			"100100019","042042001","042042002","042042003","042042004","042042005","040040001","040040002","040040003","101101001","101101002",
			"101101003","101101004","101101005","101101006","101101007","101101008","101101009","101101010","101101011","101101012","101101013",
			"101101014","101101015","102102001","102102002","102102003","102102004","103103001","103103002","103103003","103103004","104104001",
			"104104002","105105001","105105002","105105003","105105004","105105005","034034001","034034002","034034003","034034004","034034005",
			"034034006","106106001","106106002","106106003","106106004","106106005","106106006","106106007","106106008","106106009","106106010",
			"106106011","106106012","106106013","106106014","106106015","053053001","053053002","053053003","107107001","107107002","107107003",
			"107107004","107107005","107107006","107107007","107107008","107107009","107107010","107107011","107107012","107107013","107107014",
			"107107015","107107016","107107017","107107018","107107019","107107020","108108001","108108002","052052001","032032001","032032002",
			"032032004","032032005","032032006","032032007","032032008","032032009","032032010","032032011","032032012","032032013","032032014",
			"109109001","109109002","109109003","109109004","109109005","109109006","109109007","109109008","109109009","109109010","109109011",
			"109109012","109109013","109109014","046046001","046046002","046046004","046046003","046046005","046046006","038038001","038038002",
			"038038003","038038004","038038005","110110001","110110002","110110003","110110004","110110005","110110006","110110007","110110008",
			"110110009","110110010","039039001","039039002","039039003","039039004","039039005","039039006","036036001","036036002","036036003",
			"036036004","036036005","036036006","036036007","048048001","048048002","048048003","051051001"}; 
	
}
