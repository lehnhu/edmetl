package com.citics.edm.service;

public abstract class Query {
	
	public abstract int argNum();
	
	public abstract String queryId();

	public abstract String query();
	
	public abstract String[] argTypes();
	
	public abstract String[] argNames();
}
