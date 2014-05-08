package com.citics.edm.etf;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.citics.edm.etl.bb.RequestReplyKit;
import com.citics.edm.etl.bb.ResponseMessage;
import com.citics.edm.etl.bb.util.Utils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-applicationContext.xml")
public class BBRequestReplyTest {

	@Autowired
	private RequestReplyKit requestReplyService;


	//@Test
	public void test() throws IOException {

		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHH");
		String time = fmt.format(new Date());
		String requestFile = "D:\\test_hist" + time+".req";
		String replyFile = "test_re_hist" + time;
		String outputPath="D:";
		Properties props = new Properties();
		props.setProperty("startdate", "20140101");
		props.setProperty("enddate", "20140401");
		props.setProperty("replyfilename", replyFile);
		props.setProperty("issuelist", "NIASQ Index\nAAPL Equity");
		
		String reqStr=requestReplyService.formartRequestMessage(props);

		FileWriter w=new FileWriter(requestFile);
		IOUtils.write(reqStr, w);
		w.close();

		requestReplyService.requestReply(requestFile, replyFile, outputPath);
		
	}

	//@Test
	public void testGetResponse() throws IOException{
		requestReplyService.fetchResponseFile("edm_0000011074", "D:\\citics\\bbloader\\java\\resp");
	}
	
	@Test
	public void testResponseParse() throws IOException{
		List<String> lines=Utils.readLines("D:\\test_re_hist2014050713");
		ResponseMessage rp=requestReplyService.parseResponseMessage(lines);
		System.out.println(rp);
	}
}
