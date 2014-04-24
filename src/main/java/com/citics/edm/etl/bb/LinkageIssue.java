package com.citics.edm.etl.bb;

public class LinkageIssue {
	
	private String instr_id;
	
	private String denom_curr_code;

	private String issid_oid;
	
	private String mkt_oid;
	
	public String getInstr_id() {
		return instr_id;
	}

	public void setInstr_id(String instr_id) {
		this.instr_id = instr_id;
	}

	public String getDenom_curr_code() {
		return denom_curr_code;
	}

	public void setDenom_curr_code(String denom_curr_code) {
		this.denom_curr_code = denom_curr_code;
	}

	public String getIssid_oid() {
		return issid_oid;
	}

	public void setIssid_oid(String issid_oid) {
		this.issid_oid = issid_oid;
	}

	public String getMkt_oid() {
		return mkt_oid;
	}

	public void setMkt_oid(String mkt_oid) {
		this.mkt_oid = mkt_oid;
	}

	@Override
	public String toString() {
		return "LinkageIssue [instr_id=" + instr_id + ", denom_curr_code="
				+ denom_curr_code + ", issid_oid=" + issid_oid + ", mkt_oid="
				+ mkt_oid + "]";
	}	
	

}
