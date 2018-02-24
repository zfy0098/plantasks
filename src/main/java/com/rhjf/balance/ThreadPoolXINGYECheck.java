package com.rhjf.balance;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.rhjf.base.BaseDao;
import com.rhjf.utils.HttpClient;


public class ThreadPoolXINGYECheck extends BaseDao{

	public final static String orgStr = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	public final static String url = "https://download.swiftpass.cn/gateway";
	
	public final static String channelName = "XINGYE";
	
	public final static Integer listSize = 1000;
	
	public static Integer reconciliationSEQ = 0;
	
	public static BigDecimal sumAmount = new BigDecimal("0").setScale(2, BigDecimal.ROUND_HALF_UP);
	public static BigDecimal sumFee = new BigDecimal("0").setScale(2, BigDecimal.ROUND_HALF_UP);
	
	List<Object[]> dblist = new ArrayList<Object[]>();
	
	CheckAccountHandler  checkAcount = new CheckAccountHandler();
	
	Logger log = Logger.getLogger(this.getClass());
	
	
	public void init(){
		int threadCount = 10;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -1);
		
		String yesterday = sdf.format(c.getTime());
		
		SimpleDateFormat yyyy_mm_dd = new SimpleDateFormat("yyyy-MM-dd");
		c = Calendar.getInstance();
		String today = yyyy_mm_dd.format(c.getTime());
		
		
		log.info("查询兴业商户信息");
		
		String sql = "select * from TBL_PAY_MERCHANTCHANNELCONFIG where channelId=?  and  superMerchantNo in"
				+ " (select superMerchantNo from tbl_online_order where left(createDate,10)='" + yesterday +"'"
				+ " and orderStatus='SUCCESS' GROUP BY superMerchantNo)";
		
		log.info("查询兴业商户" + sql); 
		
		List<Map<String, Object>> merchantlist = queryForList(sql, new Object[]{channelName});

		log.info("需要对账的商户数量：" + merchantlist.size()); 
		
		reconciliationSEQ = Integer.parseInt(queryForMap("select * from sequence_online_reconciliation", null).get("next_val").toString());
		
		CyclicBarrier cyclicBarrier = new CyclicBarrier(threadCount);
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		for (int i = 0; i < threadCount; i++){
			
			executorService.execute(new ThreadPoolXINGYECheck().new Task(cyclicBarrier , merchantlist.get(i) , yesterday,today));  
		}
		executorService.shutdown();
		while (!executorService.isTerminated()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public class Task implements Runnable {
		private CyclicBarrier cyclicBarrier;

		private Map<String, Object> map;
		
		private String yesterday;
		
		private String today;
		
		public Task(CyclicBarrier cyclicBarrier , Map<String, Object> map , String yesterday,String today) {
			this.cyclicBarrier = cyclicBarrier;
			this.map = map;
			this.yesterday = yesterday;
			this.today = today;
			
		}

		public void run() {
			try {
				cyclicBarrier.await();

				int count = 0;
				String trxDate = null;
				String superMerchantNo = map.get("superMerchantNo").toString().trim();
				String channelSign = map.get("channelSign").toString().trim();
				log.info("对账商户编号:" + superMerchantNo);

				SortedMap<Object, Object> params = checkAcount.createCheckAccountMessageForSign(yesterday,superMerchantNo);
				log.info("兴业对账计算密钥的报文params为===========" + params.toString());
				// 计算sign值
				String sign = checkAcount.createSign("UTF-8", params, channelSign);
				log.info("兴业对账计算出的密钥为==========" + sign);
				String nonce_str = (String) params.get("nonce_str");
				// 制作对账报文
				String plainXML = checkAcount.createCheckAccountMessage(yesterday, nonce_str, sign, superMerchantNo);

				log.info("兴业对账请求报文==================：" + plainXML);

				String payrequest_result = HttpClient.xml(url, plainXML);
				log.info("兴业对账返回报文=================" + payrequest_result);

				List<Map<String, Object>> list = checkAcount.dealCheckAccountMessage(payrequest_result, channelName);

				log.info("兴业对账list的size的大小是================" + list.size());
				count += list.size();
				if (list.size() > 0) {
					log.info("兴业list=============" + list.toString());
					for (Map<String, Object> s1 : list) {

						trxDate = s1.get("trxDate") == null ? yesterday : s1.get("trxDate").toString();
						log.info("兴业对账s1================" + s1.toString());
						Object[] obj = new Object[] { reconciliationSEQ, 0, s1.get("bankOrderId"), channelName,
								superMerchantNo, s1.get("trxAmount"), trxDate, s1.get("trxFee"), 0, today };
						dblist.add(obj);

						synchronized (this) {
							reconciliationSEQ++;
							sumAmount = sumAmount.add(new BigDecimal(s1.get("trxAmount").toString()));
							sumFee = sumFee.add(new BigDecimal(s1.get("trxFee").toString()));
						}

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
