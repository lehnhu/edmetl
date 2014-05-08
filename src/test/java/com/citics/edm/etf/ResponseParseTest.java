package com.citics.edm.etf;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.citics.edm.etl.bb.BBHistoryPriceItem;
import com.citics.edm.etl.bb.EdmHistoryPriceKit;
import com.citics.edm.etl.bb.RequestReplyKit;
import com.citics.edm.etl.bb.ResponseMessage;
import com.citics.edm.etl.bb.util.Utils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-applicationContext.xml")
public class ResponseParseTest {
	
	@Autowired
	private RequestReplyKit requestReplyKit;
	
	@Autowired
	private EdmHistoryPriceKit edmHistoryPriceKit;
	
	

	@Test
	public void testParse() throws Exception{
		List<String> lines=Utils.readLines("D:\\test_re_hist2014050713");
		ResponseMessage rm=requestReplyKit.parseResponseMessage(lines);
		List<BBHistoryPriceItem> bblist=edmHistoryPriceKit.parseResponseMessage(rm);
		edmHistoryPriceKit.linkage(bblist);
		for(BBHistoryPriceItem bb:bblist){
			System.out.println(bb);
		}
	}
}
