package com.rhjf;

import com.rhjf.utils.Pinyin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Test {
	

    public static void main(String[] args) {

        System.out.println(Pinyin.getPingYin("和"));

    	
    }
    
    private static Date getLastDate(Date date) {
    	
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        return cal.getTime();
    }
    
    
    public static void test(){
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        Date date = new Date();
        System.out.println(sdf.format(date));
        System.out.println(sdf.format(getLastDate(date)));
    }
}
