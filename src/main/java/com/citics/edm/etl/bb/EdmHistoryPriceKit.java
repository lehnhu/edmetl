package com.citics.edm.etl.bb;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.citics.edm.etl.bb.util.IArgAdapter;
import com.citics.edm.etl.service.IETLHistoricalPriceService;

@Component
public class EdmHistoryPriceKit {

	protected static final Log LOG = LogFactory.getLog(EdmHistoryPriceKit.class);

	private String insert_ispc;

	@Autowired
	private IETLHistoricalPriceService historicalPriceService;

	@Autowired
	private ArgHelper argHelper;

	public EdmHistoryPriceKit() {
		try {
			insert_ispc = IOUtils.toString(this.getClass().getResourceAsStream(
					"/sql/insert_ispc.sql"));
		} catch (IOException e) {
			LOG.fatal(e.getCause(), e);
		}
	}

	/**
	 * 
	 * @param rm
	 * @return
	 */
	protected boolean preprocess(ResponseMessage rm) {
		if (!"horizontal".equalsIgnoreCase(rm.getMeta("HIST_FORMAT")))
			return false;
		if (!"gethistory".equalsIgnoreCase(rm.getMeta("PROGRAMNAME")))
			return false;
		return true;
	}

	protected Map<String, LinkageIssue> linkage(ResponseMessage rm) {
		LOG.debug("linkage...");
		Map<String, LinkageIssue> res = new HashMap<String, LinkageIssue>();
		List<String[]> columns = rm.getColumns();
		Set<String> reduceSet = new HashSet<String>();
		int idxReq = 0;
		for (; idxReq < rm.getHeaders().length
				&& "MEM".equalsIgnoreCase(rm.getHeaders()[idxReq]); idxReq++)
			;
		if(idxReq==rm.getHeaders().length)
			throw new RuntimeException("error message type");
		for (String[] lines : columns) {
			String reqText = lines[idxReq];
			reduceSet.add(reqText);

		}
		for (String reqText : reduceSet) {
			int idxSpace=reqText.lastIndexOf(' ');
			String id = reqText.substring(0,idxSpace);
			String marketsector = reqText.substring(idxSpace+1);

			List<LinkageIssue> issIds = historicalPriceService.selectIssueById(
					id, "TICKER", marketsector);
			if (issIds == null || issIds.size() == 0) {
				LOG.error("cannot find issue by " + reqText);
			} else if (issIds.size() > 1) {
				LOG.error("issue duplicate " + issIds);
			} else {
				LinkageIssue iss = issIds.get(0);
				res.put(reqText, iss);
			}
		}
		return res;
	}

	protected void produceBatchs(ResponseMessage rm,
			Map<String, LinkageIssue> linkage, boolean alwaysInsert)
			throws SQLException {
		if (alwaysInsert) {
			int idxReq = 0,tmReq=0;
			for (; idxReq < rm.getHeaders().length
					&& !"##MEM".equalsIgnoreCase(rm.getHeaders()[idxReq]); idxReq++)
				;
			for (; tmReq < rm.getHeaders().length
					&& !"##DATE".equalsIgnoreCase(rm.getHeaders()[tmReq]); tmReq++)
				;
			if(idxReq==rm.getHeaders().length || tmReq== rm.getHeaders().length){
				throw new RuntimeException("");
			}
			String[] headers = rm.getHeaders();
			List<String[]> lines = rm.getColumns();
			for (int i = rm.getFieldBeginIndex(); i < headers.length; i++) {
				String field = headers[i];
				LOG.info("start load filed:" + field);
				List<Object[]> args = new ArrayList<Object[]>();
				IArgAdapter argAdapter = argHelper.getAdapter(field);
				if (argAdapter == null) {
					LOG.error("cannot find adapter to handle FIELD:" + field);
				} else {
					for (String[] line : lines) {
						try {
							System.out.printf("linkage:%s\n",line[idxReq]);
							if (linkage.get(line[idxReq]) != null)
								args.add(argAdapter.adapter(linkage, line, i,idxReq,tmReq));
						} catch (ParseException e) {
							LOG.error("dataformat error in : " + line, e);
						}
					}
					int[] results = historicalPriceService.batchUpdate(
							insert_ispc, args);
					for (int j = 0; j < results.length; j++) {
						if (results[j] == Statement.SUCCESS_NO_INFO
								|| results[j] > 0) {
							// success
						} else {
							LOG.error("Load failed:"
									+ Arrays.toString(args.get(j)));
						}
					}
				}
			}
		} else {
			// unspported in current version
		}
	}
	
	public void standardLoad(ResponseMessage rm) throws SQLException{
		Map<String, LinkageIssue> linkage=this.linkage(rm);
		if(this.preprocess(rm)){
			this.produceBatchs(rm, linkage, true);
		}else{
			LOG.error("unsupport");
		}
	}
}
