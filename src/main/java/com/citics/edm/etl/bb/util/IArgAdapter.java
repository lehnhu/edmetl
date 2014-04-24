package com.citics.edm.etl.bb.util;

import java.text.ParseException;
import java.util.Map;

import com.citics.edm.etl.bb.LinkageIssue;

public interface IArgAdapter {
	
	Object[] adapter(Map<String,LinkageIssue> linkage,String [] columns,int index,int mempos,int tmpos) throws ParseException;

}
