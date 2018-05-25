package com.rhjf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
	

    public static void main(String[] args) {

      String qqTokenResult = "expires_in=7776000&access_token=5682BEBC8A435020F52C4FEEFE186008&refresh_token=E56B760F8FA620234723F706DFA29F49";

        Matcher match = Pattern.compile("access_token" + "=([^&]*)").matcher(qqTokenResult);
        if (match.find()) {
            System.out.println(match.group().substring("access_token".length()+1));
        }

    }
    

}
