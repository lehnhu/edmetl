package com.citics.edm.etl.bb;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

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

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init() {
		insert_ispc_query = EDMPropertyService
			.getQuery("edmetl.bb.linkage_sql");
		String json_str = EDMPropertyService
			.getStringProperty("edmetl.bb.ispc_mapping");
		Gson gson = new Gson();
		ispc_mapping = gson.fromJson(json_str, Map.class);

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

	public void linkage(List<BBHistoryPriceItem> bblist) {
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

		final List<BBHistoryPriceItem> validPriceValueList = new ArrayList<BBHistoryPriceItem>();
		for (BBHistoryPriceItem bb : bblist) {
			try {
				String priceValue = (String) f.get(bb);
				priceValue = priceValue == null ? priceValue : priceValue
					.toString();
				if (priceValue == null || priceValue.equals("")
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
					if ("prc_tms".equalsIgnoreCase(argNames[i])) {
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
		if (_f.getDeclaringClass() != String.class
			|| !_f.isAnnotationPresent(EDMField.class)) {
			return false;
		}
		EDMField fieldAttr = _f.getAnnotation(EDMField.class);
		String fieldName = fieldAttr.value().equalsIgnoreCase("") ? _f
			.getName() : fieldAttr.value();
		Map<String, String> values = ispc_mapping.get(fieldName);
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

	protected void loadBatchs(List<BBHistoryPriceItem> bblist,
		boolean alwaysInsert) throws SQLException, IllegalArgumentException,
		IllegalAccessException {
		if (alwaysInsert) {
			batchInsertOnly(bblist);
		} else {

		}
	}

	public void standardLoad(ResponseMessage rm) throws SQLException {
		// Map<String, LinkageIssue> linkage = this.linkage(rm);
		// if (this.preprocess(rm)) {
		// this.produceBatchs(rm, linkage, true);
		// } else {
		// LOG.error("unsupport");
		// }
	}

	public List<BBHistoryPriceItem> parseResponseMessage(ResponseMessage rm)
		throws Exception {
		List<BBHistoryPriceItem> bblist = new LinkedList<BBHistoryPriceItem>();
		Map<String, BBHistoryPriceItem> cache = new HashMap<String, BBHistoryPriceItem>();
		String[] header = rm.getHeaders();
		int memIndex = -1, dateIndex = -1;
		int prefixCnt = 2;
		for (int i = 0; i < header.length; i++) {
			if ("##MEM".equalsIgnoreCase(header[i])) {
				memIndex = i;
				prefixCnt--;
			} else if ("##DATE".equalsIgnoreCase(header[i])) {
				dateIndex = i;
				prefixCnt--;
			}
			if (prefixCnt <= 0) {
				break;
			}
		}
		if (prefixCnt > 0) {
			throw new Exception("Invalid Response");
		}
		for (int i = rm.getFieldBeginIndex(); i < header.length; i++) {
			String name = header[i];
			Method setMethod = null;
			try {
				// 通过反射 找到该属性的设置方法
				setMethod = BBHistoryPriceItem.class.getMethod("set" + name,
					String.class);
			} catch (Exception e) {
				// 没有该属性则警告
				LOG.warn("cannot find properties " + name
					+ " in BBHistoryPriceItem class");
				continue;
			}
			for (String[] __clm : rm.getColumns()) {
				String date = __clm[dateIndex];
				String mString = __clm[memIndex];
				String key = "#" + date + "#" + mString;
				BBHistoryPriceItem bbp = cache.get(key);
				// 第一次如果没有找到则创建
				if (bbp == null) {
					bbp = new BBHistoryPriceItem();
					bbp.setDate(date);
					bbp.setIdContext(mString);
					cache.put(key, bbp);
				}
				// 设置属性
				setMethod.invoke(bbp, __clm[i]);
			}
		}
		for (Entry<String, BBHistoryPriceItem> entry : cache.entrySet()) {
			bblist.add(entry.getValue());
		}
		return bblist;
	}

}
