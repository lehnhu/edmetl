package com.citics.edm.ws.service;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface IHistoryPriceService {

	/**
	 * 载入历史价格——同步方法
	 * 
	 * @param issueList
	 *            传入券的列表，每行一个券 Ticker+Marketsector
	 * @param fromDate
	 *            开始日期
	 * @param toDate
	 *            结束日期
	 * @return 是否执行成功
	 * @throws ParseException
	 * @throws IOException
	 * @throws SQLException
	 */
	boolean syncLoadHistoryPrices(@WebParam(name="issueList") String issueList,
		@WebParam(name="fromDate") String fromDate,
		@WebParam(name="toDate") String toDate) throws Exception;

	boolean asyncLoadHistoryPrices(@WebParam(name="issueList") String issueList,
		@WebParam(name="fromDate") String fromDate,
		@WebParam(name="toDate") String toDate) throws Exception;
}
