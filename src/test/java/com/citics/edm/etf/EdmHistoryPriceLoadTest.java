package com.citics.edm.etf;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.citics.edm.etl.bb.EdmHistoryPriceKit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-applicationContext.xml")
public class EdmHistoryPriceLoadTest {
	
	@Autowired
	private EdmHistoryPriceKit EdmHistoryPriceKit;
	
	@Test
	public void testLoad() throws Exception{
		EdmHistoryPriceKit.standardLoad("D:\\edm_0000012333");
	}

}
