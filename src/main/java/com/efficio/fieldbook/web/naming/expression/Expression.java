package com.efficio.fieldbook.web.naming.expression;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public abstract class Expression {

	public static final String BRACKETS = "[BRACKETS]";
	public static final String BULK_COUNT = "[BCOUNT]";
	public static final String COUNT = "[COUNT]";
	public static final String LOCATION_ABBREVIATION = "[LABBR]";
	public static final String NUMBER = "[NUMBER]";
	public static final String SEASON = "[SEASON]";
	public static final String SEQUENCE = "[SEQUENCE]";
	public static final String TOP_LOCATION_ABBREVIATION = "[TLABBR]";
	
	private AdvancingSource source;
	
	public Expression(AdvancingSource source) {
		this.source = source;
	}
	
	public abstract void apply(StringBuilder value);

	/**
	 * @return the source
	 */
	public AdvancingSource getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(AdvancingSource source) {
		this.source = source;
	}
	
}
