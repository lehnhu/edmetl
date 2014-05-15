package com.citics.edm.etl.bb;

/**
 * 
 * @author huliang BloomBerg历史价格数据返回数据的反序列化， 每个id+日期为一个对象实例
 */
public class BBHistoryPriceItem {

	private LinkageIssue linkageIssue;

	private String idContext;

	private String date;

	@EDMField
	private String PX_LAST;

	@EDMField
	private String PX_OPEN;

	@EDMField
	private String PX_BID;
	
	@EDMField
	private String PX_MID;
	
	@EDMField
	private String PX_ASK;

	@EDMField
	private String PX_HIGH;

	@EDMField
	private String PX_LOW;
	
	@EDMField
	private String PX_FIXING;
	
	@EDMField
	private String PX_NASDAQ_CLOSE;
	
	@EDMField
	private String LOW_52WEEK;
	
	@EDMField
	private String HIGH_52WEEK;

	public String getIdContext() {
		return idContext;
	}

	public void setIdContext(String idContext) {
		this.idContext = idContext;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getPX_LAST() {
		return PX_LAST;
	}

	public void setPX_LAST(String pX_LAST) {
		PX_LAST = pX_LAST;
	}

	public String getPX_OPEN() {
		return PX_OPEN;
	}

	public void setPX_OPEN(String pX_OPEN) {
		PX_OPEN = pX_OPEN;
	}

	public LinkageIssue getLinkageIssue() {
		return linkageIssue;
	}

	public void setLinkageIssue(LinkageIssue linkageIssue) {
		this.linkageIssue = linkageIssue;
	}

	
	public String getPX_BID() {
		return PX_BID;
	}

	public void setPX_BID(String pX_BID) {
		PX_BID = pX_BID;
	}

	public String getPX_MID() {
		return PX_MID;
	}

	public void setPX_MID(String pX_MID) {
		PX_MID = pX_MID;
	}

	public String getPX_ASK() {
		return PX_ASK;
	}

	public void setPX_ASK(String pX_ASK) {
		PX_ASK = pX_ASK;
	}

	public String getPX_HIGH() {
		return PX_HIGH;
	}

	public void setPX_HIGH(String pX_HIGH) {
		PX_HIGH = pX_HIGH;
	}

	public String getPX_LOW() {
		return PX_LOW;
	}

	public void setPX_LOW(String pX_LOW) {
		PX_LOW = pX_LOW;
	}

	public String getPX_FIXING() {
		return PX_FIXING;
	}

	public void setPX_FIXING(String pX_FIXING) {
		PX_FIXING = pX_FIXING;
	}

	public String getPX_NASDAQ_CLOSE() {
		return PX_NASDAQ_CLOSE;
	}

	public void setPX_NASDAQ_CLOSE(String pX_NASDAQ_CLOSE) {
		PX_NASDAQ_CLOSE = pX_NASDAQ_CLOSE;
	}

	public String getLOW_52WEEK() {
		return LOW_52WEEK;
	}

	public void setLOW_52WEEK(String lOW_52WEEK) {
		LOW_52WEEK = lOW_52WEEK;
	}

	public String getHIGH_52WEEK() {
		return HIGH_52WEEK;
	}

	public void setHIGH_52WEEK(String hIGH_52WEEK) {
		HIGH_52WEEK = hIGH_52WEEK;
	}

	@Override
	public String toString() {
		return "BBHistoryPriceItem [linkageIssue=" + linkageIssue
			+ ", idContext=" + idContext + ", date=" + date + ", PX_LAST="
			+ PX_LAST + ", PX_OPEN=" + PX_OPEN + ", PX_BID=" + PX_BID
			+ ", PX_MID=" + PX_MID + ", PX_ASK=" + PX_ASK + ", PX_HIGH="
			+ PX_HIGH + ", PX_LOW=" + PX_LOW + ", PX_FIXING=" + PX_FIXING
			+ ", PX_NASDAQ_CLOSE=" + PX_NASDAQ_CLOSE + ", LOW_52WEEK="
			+ LOW_52WEEK + ", HIGH_52WEEK=" + HIGH_52WEEK + "]";
	}


}
