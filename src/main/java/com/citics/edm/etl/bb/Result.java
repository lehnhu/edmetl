package com.citics.edm.etl.bb;

public class Result {
	public enum STATUS {
		SUCCESS, 
		ERROR ,
		TIMEOUT
	}
	
	public STATUS status;
	public String message;
	public Result(STATUS status, String message) {
		super();
		this.status = status;
		this.message = message;
	}
	public STATUS getStatus() {
		return status;
	}
	public void setStatus(STATUS status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		return "Result [status=" + status + ", message=" + message + "]";
	}
	
	
}
