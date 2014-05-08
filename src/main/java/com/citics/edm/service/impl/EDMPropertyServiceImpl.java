package com.citics.edm.service.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.citics.edm.service.IEDMPropertyService;
import com.citics.edm.service.Query;

@Service
public class EDMPropertyServiceImpl implements IEDMPropertyService {

	protected static final Log LOG = LogFactory
		.getLog(EDMPropertyServiceImpl.class);

	@Autowired
	@Qualifier("gsJdbcTemplate")
	private JdbcTemplate gsJdbcTemplate;

	@Override
	public <T> T getProperty(String key, Class<T> clazz) {
		if (clazz == Long.class) {
			return clazz.cast(this.getLongProperty(key));
		} else if (clazz == Integer.class) {
			return clazz
				.cast(new Integer(this.getLongProperty(key).intValue()));
		} else if (clazz == BigDecimal.class) {
			return clazz.cast(this.getNumberProperty(key));
		} else if (clazz == String.class) {
			return clazz.cast(this.getStringProperty(key));
		} else {
			throw new RuntimeException("unsupported class type");
		}
	}

	@Override
	public String getStringProperty(String key) {
		String sql = "SELECT t.stringvalue,t.clobvalue FROM CIT_PROPS t where t.key= ?";
		List<String> value = gsJdbcTemplate.query(sql, new Object[] { key },
			new RowMapper<String>() {

				@Override
				public String mapRow(ResultSet rs, int rownum)
					throws SQLException {
					String stringValue = null;
					if ((stringValue = rs.getString("stringvalue")) != null) {
						return stringValue;
					} else
						return rs.getString("clobvalue");
				}
			});
		LOG.debug("size=" + value.size());
		if (value != null && value.size() > 0)
			return value.get(0);
		return "";
	}

	@Override
	public Long getLongProperty(String key) {
		return gsJdbcTemplate.queryForLong(
			"SELECT t.intvalue FROM CIT_PROPS t where t.key= ?", key);
	}

	@Override
	public BigDecimal getNumberProperty(String key) {
		String sql = "SELECT t.NUMBERVALUE FROM CIT_PROPS t where t.key= ?";
		List<BigDecimal> value = gsJdbcTemplate.query(sql,
			new Object[] { key }, new RowMapper<BigDecimal>() {

				@Override
				public BigDecimal mapRow(ResultSet rs, int rownum)
					throws SQLException {
					return rs.getBigDecimal("NUMBERVALUE");
				}
			});
		LOG.debug("size=" + value.size());
		if (value != null && value.size() > 0)
			return value.get(0);
		return null;
	}

	final class EdmQuery extends Query {

		private String queryId;
		private String query;
		private String[] argNames;
		private String[] argTypes;

		public EdmQuery(String queryId, String query, String[] argNames,
			String[] argTypes) {
			super();
			this.queryId = queryId;
			this.query = query;
			this.argNames = argNames;
			this.argTypes = argTypes;
		}

		@Override
		public int argNum() {
			if (argTypes == null)
				return 0;
			return argTypes.length;
		}

		@Override
		public String queryId() {
			return this.queryId;
		}

		@Override
		public String query() {
			return this.query;
		}

		@Override
		public String[] argTypes() {
			return argTypes == null ? new String[0] : argTypes;
		}

		@Override
		public String[] argNames() {
			return argNames == null ? new String[0] : argNames;
		}

		@Override
		public String toString() {
			return "EdmQuery [queryId=" + queryId + ", query=" + query
				+ ", argNames=" + Arrays.toString(argNames) + ", argTypes="
				+ Arrays.toString(argTypes) + "]";
		}

	}

	@Override
	public Query getQuery(String queryId) {
		String query = gsJdbcTemplate.queryForObject(
			"select querystring from cit_query where queryid=?", String.class,
			queryId);
		final List<String> argtypes = new ArrayList<String>();
		final List<String> argnames = new ArrayList<String>();
		if (query == null || query.equalsIgnoreCase(""))
			return null;
		gsJdbcTemplate
			.query(
				"select t.argindex,t.argname,t.argtype from CIT_QUERY_PARAMS t where t.queryid=?  order by t.argindex asc",
				new RowMapper<String>() {

					@Override
					public String mapRow(ResultSet rs, int rownum)
						throws SQLException {
						String argname= rs.getString("argname");
						String argtype=rs.getString("argtype");
						argtypes.add( argtype);
						argnames.add( argname);
						return "";
					}
				},queryId);

		return new EdmQuery(queryId, query, argnames.toArray(new String[0]),
			argtypes.toArray(new String[0]));
	}

}
