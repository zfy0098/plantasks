package com.rhjf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.rhjf.base.BaseDao;
import com.rhjf.utils.CreateExcle;
import com.rhjf.utils.DESUtil;

import net.sf.json.JSONObject;

public class FileTest extends BaseDao{

	
	
	public void init() {
		File file = new File("/portal");
		String[] fileName = file.list();
		// TODO自动生成的方法存根
		for (int i = 0; i < fileName.length; i++) {
			File f = new File("/portal" + File.separator + fileName[i]);

			// 打开一个随机访问文件流，按读写方式
			RandomAccessFile randomFile = null;
			InputStreamReader  read  = null; // 文件输入流
			BufferedReader bufferedReader = null;
			try {
				read  = new InputStreamReader(new FileInputStream(f) , "utf-8"); 
				bufferedReader = new BufferedReader(read);
				
				randomFile = new RandomAccessFile("/1111.txt", "rw" );
				// 文件长度，字节数
				long fileLength = randomFile.length();
				// 将写文件指针移到文件尾。
				randomFile.seek(fileLength);
				
				/*
				 * 1.new File()里面的文件地址也可以写成D:\\David\\Java\\java
				 * 高级进阶\\files\\tiger.jpg,前一个\是用来对后一个
				 * 进行转换的，FileInputStream是有缓冲区的，所以用完之后必须关闭，否则可能导致内存占满，数据丢失。
				 */
				String lineTxt = null;
//				String reqContent = "";
				while ((lineTxt= bufferedReader.readLine())!=null) { // 读取文件字节，并递增指针到下一个字节
					System.out.println(lineTxt);
					
					if(lineTxt.indexOf("请求报文")!=-1||lineTxt.indexOf("响应报文")!=-1||lineTxt.indexOf(": 接收到的参数：")!=-1){
						
//						randomFile.write((lineTxt.substring(lineTxt.indexOf("{")) + "\n").getBytes());
						randomFile.write((lineTxt + "\n").getBytes()); 
					}
					
//					if(lineTxt.indexOf("请求报文")!=-1){
//						// 请求报文
//						if(lineTxt.indexOf("{")!=1){
//							reqContent = lineTxt.substring(lineTxt.indexOf("{")); 
//						}
//					}else if(lineTxt.indexOf("响应报文")!=-1){
//						// "respCode":"200"   "respCode":"0000"
//						if(lineTxt.lastIndexOf("{")!=-1){
//							JSONObject json = JSONObject.fromObject(lineTxt.substring(lineTxt.lastIndexOf("{")));
//							
//							if("200".equals(json.getString("respCode"))||"0000".equals(json.getString("respCode"))){
//								
//							}else{
//								randomFile.write((reqContent + "|" + lineTxt.substring(lineTxt.indexOf("{")) + "\n").getBytes());
//							}
//						}
//					}
				}
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				try {
					read.close();
					bufferedReader.close();
					randomFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public void createTxtFile(){
		File f = new File("/2222.txt");

		// 打开一个随机访问文件流，按读写方式
		RandomAccessFile randomFile = null;
		InputStreamReader  read  = null; // 文件输入流
		BufferedReader bufferedReader = null;
		try {
			read  = new InputStreamReader(new FileInputStream(f) , "utf-8"); 
			bufferedReader = new BufferedReader(read);
			
			randomFile = new RandomAccessFile("/333.txt", "rw" );
			// 文件长度，字节数
			long fileLength = randomFile.length();
			// 将写文件指针移到文件尾。
			randomFile.seek(fileLength);
			
			/*
			 * 1.new File()里面的文件地址也可以写成D:\\David\\Java\\java
			 * 高级进阶\\files\\tiger.jpg,前一个\是用来对后一个
			 * 进行转换的，FileInputStream是有缓冲区的，所以用完之后必须关闭，否则可能导致内存占满，数据丢失。
			 */
			String lineTxt = null;
			String reqContent = "";
			while ((lineTxt= bufferedReader.readLine())!=null) { // 读取文件字节，并递增指针到下一个字节
				System.out.println(lineTxt);
				
				if(lineTxt.indexOf("接收到的参数")!=-1){
					// 请求报文
					if(lineTxt.indexOf("{")!=-1){
						reqContent = lineTxt.substring(lineTxt.indexOf("{")); 
					}
				}else if(lineTxt.indexOf("响应报文")!=-1){
					// "respCode":"200"   "respCode":"0000"
					if(lineTxt.lastIndexOf("{")!=-1){
						JSONObject json = JSONObject.fromObject(lineTxt.substring(lineTxt.lastIndexOf("{")));
						
						if("200".equals(json.getString("respCode"))||"0000".equals(json.getString("respCode"))){
							
						}else{
							randomFile.write((reqContent + "|" + lineTxt.substring(lineTxt.indexOf("{")) + "\n").getBytes());
						}
					}
				}
			}
		}catch (final IOException e) {
			e.printStackTrace();
		} finally {
			try {
				read.close();
				bufferedReader.close();
				randomFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void createTxtFile2() throws Exception{ 
		File f = new File("/333.txt");

		InputStreamReader  read  = null; // 文件输入流
		BufferedReader bufferedReader = null;
		try {
			read  = new InputStreamReader(new FileInputStream(f) , "utf-8"); 
			bufferedReader = new BufferedReader(read);
			
			List<Object[]> list = new ArrayList<Object[]>();
			String[] title = {"持卡人姓名" , "结算账号" , "分行名称"  , "开户行城市" ,  "银联号" , "银行名称" , "开户行省份" ,
					"通道名称" , "通道编号" ,"身份证号" , "手机号" , "原因"};
			
			String lineTxt = null;
			while ((lineTxt= bufferedReader.readLine())!=null) { // 读取文件字节，并递增指针到下一个字节
				
				String[] content = lineTxt.split("\\|");
				
				String msg = "";
				try {
					msg = JSONObject.fromObject(content[1].trim()).getString("respMsg");
				} catch (Exception e) {
					msg = JSONObject.fromObject(content[1].trim()).getString("respCode");
				}
				
				JSONObject reqjson = JSONObject.fromObject(content[0].trim());
				
				if(reqjson.has("accountName")){
					String accountName = reqjson.getString("accountName");
					String accountNo = reqjson.getString("accountNo");
					String bankBranch = reqjson.getString("bankBranch");
					String bankCity = "";
					if(reqjson.has("bankCity")){ 
						bankCity = reqjson.getString("bankCity");
					}
					
					String bankCode = reqjson.getString("bankCode");
					String bankName = reqjson.getString("bankName");
					String bankProv = "";
					
					if(reqjson.has("bankProv")){
						bankProv = reqjson.getString("bankProv");
					}
					String channelName = reqjson.getString("channelName");
					String channelNo = reqjson.getString("channelNo");
					String legalPersonID = reqjson.getString("legalPersonID");
					String merchantPersonPhone = reqjson.getString("merchantPersonPhone");
					
					String sql = "select desKey from tbl_pay_agentparam where channelNo=?";
					
					String deskey = queryForMap(sql, new Object[]{channelNo}).get("desKey").toString();
					
					String cardNo = DESUtil.decode(deskey,accountNo);
					
					Object[] obj = new Object[]{accountName ,cardNo ,bankBranch , bankCity,bankCode ,bankName ,bankProv , channelName
							, channelNo, legalPersonID , merchantPersonPhone ,  msg};
					list.add(obj);
				}
			}
			
			try {
	            CreateExcle.createExcel2( title  , list, "/" ,  "上游入网失败");
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}catch (final IOException e) {
			e.printStackTrace();
		} finally {
			try {
				read.close();
				bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws Exception{
		FileTest fileTest = new FileTest();
		fileTest.createTxtFile2();
	}
}
