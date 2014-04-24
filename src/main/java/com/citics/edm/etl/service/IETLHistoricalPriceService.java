package com.citics.edm.etl.service;

import java.sql.SQLException;
import java.util.List;

import com.citics.edm.etl.bb.LinkageIssue;

public interface IETLHistoricalPriceService {
	
	List<LinkageIssue> selectIssueById(String id,String idtype,String marketsector);
	
	String getOID();
	
	int [] batchUpdate(String sql,List<Object []>rs) throws SQLException ;

	String getReqFileSeq();
}
