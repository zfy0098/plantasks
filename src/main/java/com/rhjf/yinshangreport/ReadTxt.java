package com.rhjf.yinshangreport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import net.sf.json.JSONObject;

public class ReadTxt {

	public void init(){
		String path = "/new.txt";
		
		
		String newpath = "/sql.txt";
		
		try {
			File file = new File(path);
			InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"utf-8");
			BufferedReader bufferedReader = new BufferedReader(reader);
			String textlint = null;
			int line = 0;
			int type = 0;
			JSONObject json = new JSONObject();
			while((textlint=bufferedReader.readLine())!=null){
				line +=1;
				String content = ""; 
				if(line == 3){
					// 获取请求报文
					content = textlint.substring(textlint.indexOf("{"));
					json = JSONObject.fromObject(content);
				}else if(line == 5){
					//  获取商户号
					
					content = textlint.substring(textlint.indexOf("{")+1, textlint.indexOf("}"));
					
					System.out.print(content + "','" );
					
					
					String number =  json.getString("account");
					String certNo = json.getString("certNo");
					
					type +=1;
					
					String t0PayResult = "0";
					
					switch (type) {
					case 1:
						t0PayResult = "3";
						break;
					case 2:
						t0PayResult = "4";
						break;
					case 3:
						t0PayResult = "0";
						break;
					case 4:
						t0PayResult = "1";
						break;
					}
					
					if(type == 4){
						type = 0;
					}
					
					line = 0;
					
					String sql = "insert into TBL_PAY_MERCHANTCHANNEL "
							+ "(optimistic,channelCode,channelFlag,channelName,createDate,creator,merchantNo,openId,superMerchantNo,superTermid,"
							+ "channelSign,channelDesKey,channelQueryKey,idCard,t0PayResult) "
							+ " values (0,'YINSHANG',0,'银商通道',now(),'admin','"+content+"','','"+number+"','','','','','"+certNo+"','"+t0PayResult+"')";
					
					writeTxtFile(sql, new File(newpath));
					
				}
//				System.out.println(textlint);
			}
			reader.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) { 
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeTxtFile(String content, File fileName) throws Exception {
		try {
			// 打开一个随机访问文件流，按读写方式
			RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
			// 文件长度，字节数
			long fileLength = randomFile.length();
			// 将写文件指针移到文件尾。
			randomFile.seek(fileLength);
			randomFile.write((content + "\n").getBytes());
			randomFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	} 
	
	
	
	public static void main(String[] args) {
		ReadTxt read = new ReadTxt();
		read.init();
	}
}
