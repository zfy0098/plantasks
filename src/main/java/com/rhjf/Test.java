package com.rhjf;

import com.rhjf.utils.AmountUtil;
import com.rhjf.utils.HttpClient;
import com.rhjf.utils.Pinyin;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Test {
	

    public static void main(String[] args) {

        String trxAmount = "000001810000";

        double x = AmountUtil.div(trxAmount ,"100" , 2);

    	System.out.println(x);
    }
    

}
