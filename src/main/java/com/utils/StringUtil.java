package com.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IDEA by Zhoufy on 2018/5/16.
 *
 * @author Zhoufy
 */
public class StringUtil {


    public static String getParameterValueByURL(String url , String parameter){
        Matcher match = Pattern.compile(parameter + "=([^&]*)").matcher(url);
        if (match.find()) {
            return match.group().substring(parameter.length()+1) ;
        }
        return null ;
    }
}
