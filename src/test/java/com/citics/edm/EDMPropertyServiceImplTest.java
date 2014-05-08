package com.citics.edm;



import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.citics.edm.service.IEDMPropertyService;
import com.citics.edm.service.Query;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-applicationContext.xml")
public class EDMPropertyServiceImplTest {
	
	@Autowired
	private IEDMPropertyService EDMPropertyService;
	
	@Test
	public void testGetString(){
		String string=EDMPropertyService.getStringProperty("edmetl.bb.request.histpricetmpl");
		System.out.println(string);
		Assert.assertNotEquals(string, "");
	}

	@Test
	public void testGetInt(){
		Long longv=EDMPropertyService.getLongProperty("edmetl.bb.request.histpricetmpl");
		System.out.println(longv);
		Assert.assertNotNull(longv);
	}
	
	@Test
	public void testGetObject(){
		String stringvalue=EDMPropertyService.getProperty("edmetl.bb.request.histpricetmpl",String.class);
		System.out.println(stringvalue);
		Assert.assertNotNull(stringvalue);
	}
	
	@Test
	public void testGetQuery(){
		Query query=EDMPropertyService.getQuery("edmetl.bb.insert_ispc");
		System.out.println(query);
		Assert.assertNotNull(query);
	}
}
