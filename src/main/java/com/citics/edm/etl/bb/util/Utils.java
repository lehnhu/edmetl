package com.citics.edm.etl.bb.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.LinkedList;
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

	public static void writeFile(String path, String content)
		throws IOException {
		FileOutputStream out = null;
		out = new FileOutputStream(path);
		try {
			IOUtils.write(content, out);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	public static String toString(Object o) {
		StringBuffer sb = new StringBuffer();
		__toString(o, sb);
		return sb.toString();
	}

	private static void __toString(Object o, StringBuffer sb) {
		if (o == null) {
			sb.append("null");
			return;
		}
		Class<?> c = o.getClass();
		if (c.isArray()) {
			int len = Array.getLength(o);
			sb.append("[");
			if (len > 0) {
				__toString(Array.get(o, 0), sb);
			}
			for (int i = 1; i < len; i++) {
				sb.append(",");
				__toString(Array.get(o, i), sb);
			}
			sb.append("]");
		} else if (o instanceof Iterable<?>) {
			int off = 0;
			Iterable<?> iter = (Iterable<?>) o;
			sb.append("[");
			for (Object sub : iter) {
				if(off>0)
					sb.append(",");
				__toString(sub, sb);
				off++;
			}
			sb.append("]");

		} else {
			sb.append(o.toString());
		}
	}

	public static void main(String[] args) {
		List<String[]> a = new LinkedList<String[]>();
		a.add(new String[] { "1", "2" });
		a.add(new String[] { "3", "2" });
		System.out.print(toString(a));
	}
}
