package com.citics.edm;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.citics.edm.service.IEDMPropertyService;
import com.google.gson.Gson;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-applicationContext.xml")
public class TestGson {
	
	@Autowired
	private IEDMPropertyService edmPropertyService;
	
	@Test
	public void test(){
		String json=edmPropertyService.getStringProperty("edmetl.bb.ispc_mapping");
		Map o=new HashMap();
		Map pxlast=new HashMap();
		pxlast.put("prc_typ", "003");
		Map pxopen=new HashMap();
		pxopen.put("prc_typ", "OPEN");
		o.put("PX_LAST", pxlast);
		o.put("PX_OPEN", pxopen);
		Gson gson=new Gson();
		System.out.println(gson.toJson(o));
		Map map=gson.fromJson(json,Map.class);
		for(Object key:map.keySet()){
			System.out.println(map.get(key).getClass());
		}
	}

}
