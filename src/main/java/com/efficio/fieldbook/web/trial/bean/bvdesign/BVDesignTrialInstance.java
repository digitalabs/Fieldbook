package com.efficio.fieldbook.web.trial.bean.bvdesign;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

public class BVDesignTrialInstance {
	
	private Integer instanceNumber;
	
	private List<Map<String, String>> rows;

	
	public BVDesignTrialInstance(final Integer instanceNumber, final List<Map<String, String>> rows) {
		super();
		this.instanceNumber = instanceNumber;
		this.rows = rows;
	}
	
	public Integer getInstanceNumber() {
		return instanceNumber;
	}

	
	public void setInstanceNumber(Integer instanceNumber) {
		this.instanceNumber = instanceNumber;
	}


	public List<Map<String, String>> getRows() {
		return rows;
	}
	
}
