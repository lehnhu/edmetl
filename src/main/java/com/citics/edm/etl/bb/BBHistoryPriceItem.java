package com.citics.edm.etl.bb;

/**
 * 
 * @author huliang
 * BloomBerg历史价格数据返回数据的反序列化，
 * 每个id+日期为一个对象实例
 */
public class BBHistoryPriceItem {
	
	private LinkageIssue linkageIssue;
	
	private String idContext;
	
	private String date;
	
	@EDMField
	private String PX_LAST;
	
	@EDMField
	private String PX_OPEN;

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

	@Override
	public String toString() {
		return "BBHistoryPriceItem [linkageIssue=" + linkageIssue
			+ ", idContext=" + idContext + ", date=" + date + ", PX_LAST="
			+ PX_LAST + ", PX_OPEN=" + PX_OPEN + "]";
	}
	
	
}
