package com.citics.edm.etl.bb.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class Utils {

	@SuppressWarnings("unchecked")
	public static List<String> readLines(String path) throws IOException {

		FileInputStream in = null;
		try {
			in = new FileInputStream(path);
			List<String> lines = IOUtils.readLines(in);
			return lines;
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public static void writeFile(String path,String content) throws IOException {
		FileOutputStream out=null;
		out=new FileOutputStream(path);
		try{
			IOUtils.write(content, out);
		}finally{
			if(out!=null){
				out.close();
			}
		}
	}
}
