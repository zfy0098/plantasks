package com.rhjf;

import com.rhjf.utils.AmountUtil;
import net.sf.json.JSON;
import net.sf.json.JSONObject;

public class Test {
	

    public static void main(String[] args) {

       String test = "{row-aa/colfam1=a/1523409594738/Put/vlen=1/mvcc=0}";
       JSONObject json = JSONObject.fromObject(test);
       System.out.println(json);
    }
    

}
