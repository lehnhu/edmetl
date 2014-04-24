package com.citics.edm.etf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestOne {
	
	public static void main(String [] args){
		SimpleDateFormat fmt=new SimpleDateFormat("yyyymmdd");
		try {
			Date d=fmt.parse("20130201");
			System.out.println(d);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
