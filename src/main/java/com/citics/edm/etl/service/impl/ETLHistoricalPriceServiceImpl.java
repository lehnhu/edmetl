package com.citics.edm.etl.service.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Service;

import com.citics.edm.etl.bb.LinkageIssue;
import com.citics.edm.etl.service.IETLHistoricalPriceService;
import com.citics.edm.service.IEDMPropertyService;
import com.citics.edm.service.Query;

@Service
public class ETLHistoricalPriceServiceImpl implements IETLHistoricalPriceService {

	protected static final Log LOG = LogFactory.getLog(ETLHistoricalPriceServiceImpl.class);
	
	@Autowired
	@Qualifier("gsJdbcTemplate")
	private JdbcTemplate gsJdbcTemplate;
	
	@Autowired
	private IEDMPropertyService EDMPropertyService;
	
	private Query linkage_sql_query;
	
	private static final char[]MAPPING=new char[]{
		'0','1','2','3','4','5','6','7','8','9',
		'a','b','c','d','e','f','g','h','i','j',
		'k','l','m','n','o','p','q','r','s','t',
		'u','v','w','x','y','z',
		'A','B','C','D','E','F','G','H','I','J',
		'K','L','M','N','O','P','Q','R','S','T',
		'U','V','W','X','Y','Z',
		'+','-'
	};
	
	@PostConstruct
	public void init(){
		linkage_sql_query =EDMPropertyService.getQuery("edmetl.bb.linkage_sql");
	}

//	private String linkage_sql;
//	{
//		try {
//			linkage_sql=IOUtils.toString(this.getClass().getResourceAsStream("/sql/linkage.sql"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	public List<LinkageIssue> selectIssueById(final String id,final String idtype,
			final String marketsector) {

		String sql =linkage_sql_query.query();

//		return gsJdbcTemplate.queryForList(sql,LinkageIssue.class,new Object[]{idtype,id,marketsector});
		return gsJdbcTemplate.execute(sql, new PreparedStatementCallback<List<LinkageIssue>>() {

			public List<LinkageIssue> doInPreparedStatement(
					PreparedStatement pstmt) throws SQLException,
					DataAccessException {
				
				List<LinkageIssue> l_Issues=new LinkedList<LinkageIssue>();
				pstmt.setString(1, idtype);
				pstmt.setString(2, id);
				pstmt.setString(3,marketsector);
				ResultSet rs=pstmt.executeQuery();
				while(rs.next()){
					LinkageIssue iss=new LinkageIssue();
					iss.setInstr_id(rs.getString(1));
					iss.setDenom_curr_code(rs.getString(2)==null?"":rs.getString(2));
					iss.setIssid_oid(rs.getString(3)==null?"":rs.getString(3));
					iss.setMkt_oid(rs.getString(4)==null?"":rs.getString(4));
					l_Issues.add(iss);
				}
				return l_Issues;
			}
		});
		
	}

	public String getOID() {
		long id= gsJdbcTemplate.queryForLong("select CIT_HISPRCOID_SEQ.NEXTVAL FROM DUAL");
		char []b=new char[10];
		for(int i=0;i<10;i++){
			b[i]=MAPPING[(int)(id&0x3f)];
			id>>=6;
		}
		return new String(b);
	}
	

	public String getReqFileSeq() {
		return gsJdbcTemplate.queryForObject("SELECT LPAD(VREQ_FILE_SEQ.NEXTVAL,10,'0') FROM DUAL",String.class);
	}

	public int[] batchUpdate(String sql, List<Object[]> rs) throws SQLException {
		System.out.println(sql);
		System.out.printf("datas:%d\n",rs.size());
		for(Object [] r:rs){
			System.out.println(Arrays.toString(r));
		}
//		return gsJdbcTemplate.batchUpdate(sql, rs);
		return new int[]{1,1};
	}
	
	
}
