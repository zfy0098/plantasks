package com.rhjf.tecrwin;

import net.sf.json.JSONObject;

public class Main {

	public static void main(String[] args) {
		String str = "token=6251640032001543&trId=170327144941001&tokenLevel=90&tokenBegin=20170418140717&tokenEnd=20220417140717&tokenType=01";
		JSONObject json = JSONObject.fromObject(str);
		System.out.println(json.toString()); 
	}
}
