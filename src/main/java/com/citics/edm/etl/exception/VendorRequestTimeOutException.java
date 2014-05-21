package com.citics.edm.etl.exception;

public class VendorRequestTimeOutException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5021859617534222101L;

	public VendorRequestTimeOutException(){
		super();
	}

	public VendorRequestTimeOutException(String message){
		super(message);
	}
}
