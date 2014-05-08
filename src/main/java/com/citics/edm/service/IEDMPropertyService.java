package com.citics.edm.service;

import java.math.BigDecimal;

/**
 * @author huliang
 * 读取数据库配置
 */
public interface IEDMPropertyService {
	
	<T> T getProperty(String key,Class<T> clazz);
	
	String getStringProperty(String key);
	
	Long getLongProperty(String key);
	
	BigDecimal getNumberProperty(String key);
	
	Query getQuery(String queryId);
}
