package com.citics.edm.ws.service.impl;

import java.io.File;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.jws.WebService;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.citics.edm.etl.bb.EdmHistoryPriceKit;
import com.citics.edm.etl.bb.RequestReplyKit;
import com.citics.edm.etl.bb.Result;
import com.citics.edm.etl.bb.Result.STATUS;
import com.citics.edm.etl.bb.util.Utils;
import com.citics.edm.etl.exception.VendorRequestTimeOutException;
import com.citics.edm.etl.service.IETLHistoricalPriceService;
import com.citics.edm.ws.service.IHistoryPriceService;

@Service("historyPriceService")
@WebService(
	endpointInterface = "com.citics.edm.ws.service.IHistoryPriceService")
public class HistoryPriceServiceImpl implements IHistoryPriceService {

	@Value("${bb.requestFilePath}")
	private String requestFilePath;

	@Value("${bb.responseFilePath}")
	private String responseFilePath;

	@Autowired
	private IETLHistoricalPriceService etlHistoricalPriceService;

	@Autowired
	private EdmHistoryPriceKit edmHistoryPriceKit;

	@Autowired
	private RequestReplyKit requestReplyKit;

	private ThreadPoolExecutor threadpool;

	@PostConstruct
	public void init() {
		threadpool = new ThreadPoolExecutor(10, 20, 1000,
			TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(20));
	}

	/**
	 * 日期加减
	 * 
	 * @param d
	 *            被加减日期
	 * @param days
	 *            需要加减的天数
	 * @return
	 */
	private Date plusDay(Date d, int days) {
		Calendar cld = Calendar.getInstance();
		cld.setTime(d);
		cld.add(Calendar.DAY_OF_YEAR, days);
		return cld.getTime();
	}

	public boolean syncLoadHistoryPrices(String issueList, String fromDate,
		String toDate) throws Exception {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		Date from = null, to = null;
		if (fromDate == null) {
			to = new Date();
		} else {
			to = fmt.parse(toDate.trim());
		}
		if (toDate == null) {
			from = plusDay(to, -30); // 默认30天
		} else {
			from = fmt.parse(fromDate.trim());
		}
		/*
		 * 格式化issueList
		 */

		@SuppressWarnings("unchecked")
		List<String> issues = IOUtils.readLines(new StringReader(issueList));
		StringBuffer sb = new StringBuffer();
		for (String issue : issues) {
			issue = issue.trim();
			if (issue.length() > 0) {
				sb.append(issue).append("\n");
			}
		}

		String replyFile = "edm_" + etlHistoricalPriceService.getReqFileSeq();
		Properties props = new Properties();
		props.setProperty("startdate", fmt.format(from));
		props.setProperty("enddate", fmt.format(to));
		props.setProperty("replyfilename", replyFile);
		props.setProperty("issuelist", sb.toString());

		
		String requestMsg = requestReplyKit.formartRequestMessage(props);
//		System.out.println(requestMsg);
		String storedFileName = this.requestFilePath + File.separator
			+ replyFile + ".req";
		Utils.writeFile(storedFileName, requestMsg);
		Result result = requestReplyKit.requestReply(storedFileName,
			this.responseFilePath);
		if (result.status == STATUS.SUCCESS) {
			String file = result.message;
			edmHistoryPriceKit.standardLoad(file);
			return true;
		} else if (result.status == STATUS.TIMEOUT) {
			throw new VendorRequestTimeOutException();
		} else {
			throw new Exception(result.getMessage());
		}
	}

	@Override
	public boolean asyncLoadHistoryPrices(final String issueList,
		final String fromDate, final String toDate) throws Exception {
		try {
			threadpool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						syncLoadHistoryPrices(issueList, fromDate, toDate);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
