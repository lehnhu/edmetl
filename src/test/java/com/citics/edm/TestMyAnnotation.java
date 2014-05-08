package com.citics.edm;

import java.lang.reflect.Field;

import com.citics.edm.etl.bb.BBHistoryPriceItem;
import com.citics.edm.etl.bb.EDMField;

public class TestMyAnnotation {

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
		BBHistoryPriceItem bb = new BBHistoryPriceItem();
		Field[] a=bb.getClass().getDeclaredFields();
		for(Field f:a){
//			System.out.println(f.getName());
			if(f.isAnnotationPresent(EDMField.class)){
				f.setAccessible(true);
				System.out.println(f.getName());
				System.out.println(f.get(bb));
			}
		}
	}

}
