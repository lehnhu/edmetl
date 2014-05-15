package com.citics.edm.etl.bb;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.citics.edm.etl.bb.util.Utils;
import com.citics.edm.etl.service.IETLHistoricalPriceService;
import com.citics.edm.service.IEDMPropertyService;
import com.citics.edm.service.Query;
import com.google.gson.Gson;

@Component
public class EdmHistoryPriceKit {

	protected static final Log LOG = LogFactory
		.getLog(EdmHistoryPriceKit.class);

	private Query insert_ispc_query;
	private Map<String, Map<String, String>> ispc_mapping;

	@Autowired
	private IETLHistoricalPriceService historicalPriceService;

	@Autowired
	@Qualifier("gsJdbcTemplate")
	private JdbcTemplate gsJdbcTemplate;

	@Autowired
	private IEDMPropertyService EDMPropertyService;

	@Autowired
	private RequestReplyKit requestReplyKit;
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init() {
		insert_ispc_query = EDMPropertyService
			.getQuery("edmetl.bb.insert_ispc");
		String json_str = EDMPropertyService
			.getStringProperty("edmetl.bb.ispc_mapping");
		Gson gson = new Gson();
		ispc_mapping = gson.fromJson(json_str, Map.class);

	}
	private void linkage(List<BBHistoryPriceItem> bblist) {
		LOG.debug("linkage...");
		Map<String, LinkageIssue> cache = new HashMap<String, LinkageIssue>();
		for (BBHistoryPriceItem bb : bblist) {
			String key = bb.getIdContext();
			LinkageIssue _linkissue = cache.get(key);
			if (_linkissue == null) {
				int idxSpace = key.lastIndexOf(' ');
				String id = key.substring(0, idxSpace);
				String marketsector = key.substring(idxSpace + 1);
				List<LinkageIssue> issIds = historicalPriceService
					.selectIssueById(id, "TICKER", marketsector);
				if (issIds == null || issIds.size() == 0) {
					LOG.error("cannot find issue by " + key);
				} else if (issIds.size() > 1) {
					LOG.error("issue duplicate " + issIds);
				} else {
					_linkissue = issIds.get(0);
					cache.put(key, _linkissue);
				}
				bb.setLinkageIssue(_linkissue);
			}
		}
	}

	private void singleInsertSinglePriceField(final Field f,
		List<BBHistoryPriceItem> bblist) {

		LOG.debug("step into single price func");
		final List<BBHistoryPriceItem> validPriceValueList = new ArrayList<BBHistoryPriceItem>();
		for (BBHistoryPriceItem bb : bblist) {
			try {
				String priceValue = (String) f.get(bb);
				priceValue = priceValue == null ? priceValue : priceValue
					.toString();
				if (priceValue == null || priceValue.trim().equals("")
					|| priceValue.equals("N.A.")) {
					continue;
				}
				validPriceValueList.add(bb);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		LOG.debug("single Insert price:"+validPriceValueList.size());
		if (validPriceValueList.size() == 0) {
			return;
		}

		final SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		EDMField fieldAttr = f.getAnnotation(EDMField.class);
		String fieldName = fieldAttr.value().equalsIgnoreCase("") ? f.getName()
			: fieldAttr.value();
		final String[] argTypes = insert_ispc_query.argTypes();
		final String[] argNames = insert_ispc_query.argNames();
		final String[] argValues = new String[argTypes.length];
		Map<String, String> values = ispc_mapping.get(fieldName);
		for (int i = 0; i < argTypes.length; i++) {
			argValues[i] = values.get(argNames[i]);
		}
		BatchPreparedStatementSetter setter = new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement pstmt, int row)
				throws SQLException {
				BBHistoryPriceItem bb = validPriceValueList.get(row);
				for (int i = 0; i < argTypes.length; i++) {
					if("iss_prc_id".equalsIgnoreCase(argNames[i])){
						pstmt.setString(i+1, historicalPriceService.getOID());
					}else if("instr_id".equalsIgnoreCase(argNames[i])){
						pstmt.setString(i+1, bb.getLinkageIssue().getInstr_id());
					}else if("isid_oid".equalsIgnoreCase(argNames[i])){
						pstmt.setString(i+1, bb.getLinkageIssue().getIssid_oid());
					}else if("mkt_oid".equalsIgnoreCase(argNames[i])){
						pstmt.setString(i+1, bb.getLinkageIssue().getMkt_oid());
					}else if("prc_curr_cde".equalsIgnoreCase(argNames[i])){
						pstmt.setString(i+1, bb.getLinkageIssue().getDenom_curr_code());
					}
					else if ("prc_tms".equalsIgnoreCase(argNames[i])) {
						try {
							pstmt.setDate(i + 1,
								new java.sql.Date(fmt.parse(bb.getDate())
									.getTime()));
						} catch (ParseException e) {
							e.printStackTrace();
						}
					} else if ("unit_cprc".equalsIgnoreCase(argNames[i])) {
						try {
							pstmt.setBigDecimal(i+1, new BigDecimal((String)f.get(bb)));
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
					
						}
					}else{
						if("VARCHAR".equalsIgnoreCase(argTypes[i])){
							pstmt.setString(i+1, argValues[i]);
						}else if("NUMBER".equalsIgnoreCase(argTypes[i])){
							pstmt.setBigDecimal(i+1, new BigDecimal(argValues[i]));
						}else if("DATE".equalsIgnoreCase(argTypes[i])){
							try {
								pstmt.setDate(i + 1,
									new java.sql.Date(fmt.parse(argValues[i]).getTime()));
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}else{
							LOG.debug("unknown field type :"+argNames[i]+","+argTypes[i]);
						}
					}
				}
			}

			@Override
			public int getBatchSize() {
				return validPriceValueList.size();
			}
		};

		int[] rs = gsJdbcTemplate
			.batchUpdate(insert_ispc_query.query(), setter);
		System.out.println(rs);
	}

	private boolean check(Field _f) {
		if (_f.getType() != String.class
			|| !_f.isAnnotationPresent(EDMField.class)) {
			return false;
		}
		EDMField fieldAttr = _f.getAnnotation(EDMField.class);
		String fieldName = fieldAttr.value().equalsIgnoreCase("") ? _f
			.getName() : fieldAttr.value();
		Map<String, String> values = ispc_mapping.get(fieldName);
		LOG.debug(ispc_mapping);
		if (values == null) {
			return false;
		} else {
			return true;
		}
	}

	private void batchInsertOnly(List<BBHistoryPriceItem> bblist) {
		final List<BBHistoryPriceItem> actualSaveList = new ArrayList<BBHistoryPriceItem>();
		for (BBHistoryPriceItem bb : bblist) {
			if (bb.getLinkageIssue() != null) {
				actualSaveList.add(bb);
			}
		}
		LOG.debug("batch insert only:"+actualSaveList.size());
		if (actualSaveList.size() > 0) {
			Field[] _fs = BBHistoryPriceItem.class.getDeclaredFields();
			for (Field _f : _fs) {
				if (check(_f)) {
					_f.setAccessible(true);
					singleInsertSinglePriceField(_f, actualSaveList);
				}
			}
		}
	}

	private void loadBatchs(List<BBHistoryPriceItem> bblist,
		boolean alwaysInsert) throws SQLException, IllegalArgumentException,
		IllegalAccessException {
		if (alwaysInsert) {
			batchInsertOnly(bblist);
		} else {

		}
	}

	public void standardLoad(String path) throws Exception {
		List<String> lines=Utils.readLines(path);
		List<BBHistoryPriceItem> bblist=requestReplyKit.parseMessage(lines);
		linkage(bblist);
		LOG.debug("parse from response. size="+bblist.size());
		loadBatchs(bblist, true);
	}



}
