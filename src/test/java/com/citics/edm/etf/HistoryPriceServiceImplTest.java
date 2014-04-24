package com.citics.edm.etf;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.citics.edm.ws.service.IHistoryPriceService;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-applicationContext.xml")
public class HistoryPriceServiceImplTest {

	@Autowired
	private IHistoryPriceService historyPriceService;
	
	@Test
	public void test() throws ParseException, IOException, SQLException{
		historyPriceService.loadHistoryPrices("NIASQ Index\nAAPL Equity", "20140101", "20140401");
	}
}
