package com.citics.edm.etl.bb;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ResponseMessage {
	
	private Map<String,String> metaData;

	private int fieldBeginIndex;
	
	private String[]headers;
	
	private List<String[]> columns;
	

	public String getMeta(String key){
		return metaData.get(key);
	}
	
	public Map<String, String> getMetaData() {
		return metaData;
	}

	public void setMetaData(Map<String, String> metaData) {
		this.metaData = metaData;
	}

	public String[] getHeaders() {
		return headers;
	}

	public void setHeaders(String[] headers) {
		this.headers = headers;
	}

	public List<String[]> getColumns() {
		return columns;
	}

	public void setColumns(List<String[]> columns) {
		this.columns = columns;
	}

	
	public int getFieldBeginIndex() {
		return fieldBeginIndex;
	}

	public void setFieldBeginIndex(int fieldBeginIndex) {
		this.fieldBeginIndex = fieldBeginIndex;
	}

	@Override
	public String toString() {
		return "ResponseMessage [metaData=" + metaData + ", fieldBeginIndex="
				+ fieldBeginIndex + ", headers=" + Arrays.toString(headers)
				+ ", columns=" + columns + "]";
	}	
	
}
