package com.citics.edm.etl.bb;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.citics.edm.etl.bb.util.IArgAdapter;
import com.citics.edm.etl.service.IETLHistoricalPriceService;

@Service
public class ArgHelper {
	
	
	private Map<String,IArgAdapter> cached=new HashMap<String, IArgAdapter>();
	
	
	@Autowired
	private IETLHistoricalPriceService historicalPriceService;
	
	private void init(){
		cached.put("PX_LAST", new IArgAdapter(){

			public Object[] adapter(Map<String, LinkageIssue> linkage,
					String[] columns, int index,int mempos,int tmpos) throws ParseException {
				SimpleDateFormat fmt=new SimpleDateFormat("yyyyMMdd");
				LinkageIssue issue=linkage.get(columns[mempos]);
				Object [] rs=new Object[9];
				rs[0]=historicalPriceService.getOID(); //iss_prc_id
				rs[1]=issue.getInstr_id();//instr_id
				rs[2]=fmt.parse(columns[tmpos]);//prc_tms
				rs[3]="PRCQUOTE";//prc_qt_meth_typ
				rs[4]="003";//prc_typ
				rs[5]=issue.getIssid_oid();//isid_oid
				rs[6]=issue.getMkt_oid();//mkt_oid
				rs[7]="1";//addnl_prc_qual_typ
				rs[8]=new BigDecimal(columns[index]);//unit_cprc
				return rs;
			}
			
		});
		cached.put("PX_OPEN", new IArgAdapter(){

			public Object[] adapter(Map<String, LinkageIssue> linkage,
					String[] columns, int index,int mempos,int tmpos) throws ParseException {
				SimpleDateFormat fmt=new SimpleDateFormat("yyyyMMdd");
				LinkageIssue issue=linkage.get(columns[mempos]);
				Object [] rs=new Object[9];
				rs[0]=historicalPriceService.getOID(); //iss_prc_id
				rs[1]=issue.getInstr_id();//instr_id
				rs[2]=fmt.parse(columns[tmpos]);//prc_tms
				rs[3]="PRCQUOTE";//prc_qt_meth_typ
				rs[4]="OPEN";//prc_typ
				rs[5]=issue.getIssid_oid();//isid_oid
				rs[6]=issue.getMkt_oid();//mkt_oid
				rs[7]="1";//addnl_prc_qual_typ
				rs[8]=new BigDecimal(columns[index]);//unit_cprc
				return rs;
			}
			
		});
		cached.put("PX_HIGH", new IArgAdapter(){

			public Object[] adapter(Map<String, LinkageIssue> linkage,
					String[] columns, int index, int mempos,int tmpos) throws ParseException {
				SimpleDateFormat fmt=new SimpleDateFormat("yyyyMMdd");
				LinkageIssue issue=linkage.get(columns[mempos]);
				Object [] rs=new Object[9];
				rs[0]=historicalPriceService.getOID(); //iss_prc_id
				rs[1]=issue.getInstr_id();//instr_id
				rs[2]=fmt.parse(columns[tmpos]);//prc_tms
				rs[3]="PRCQUOTE";//prc_qt_meth_typ
				rs[4]="004";//prc_typ
				rs[5]=issue.getIssid_oid();//isid_oid
				rs[6]=issue.getMkt_oid();//mkt_oid
				rs[7]="1";//addnl_prc_qual_typ
				rs[8]=new BigDecimal(columns[index]);//unit_cprc
				return rs;
			}
			
		});
	}
	
	public ArgHelper(){
		init();
	}

	public IArgAdapter getAdapter(String field){
		return this.cached.get(field);
	}
}
