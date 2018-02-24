package com.rhjf;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.rhjf.base.BaseDao;

public class MySQLTest extends BaseDao{

	
	
	public void init(){
		String sql = "select sum(Amount) as amount from test ;";
		
		Map<String,Object> map = queryForMap(sql, null);
		System.out.println(	map.get("amount")); 
	}
	
	public static void main(String[] args) {

		File file = new File("/P");

		File[] fs = file.listFiles();

		for (int i = 0; i < fs.length ; i++) {

			File f = fs[i];


			f.renameTo(new File("/newp/" + f.getName() + ".jpg"));
		}


	}
	
}
