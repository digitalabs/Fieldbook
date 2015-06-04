
package com.efficio.fieldbook.web.trial.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 7/8/2014 Time: 5:05 PM
 */
public class EnvironmentData implements TabInfoBean {

	private int noOfEnvironments;
	private List<Environment> environments;

	public EnvironmentData() {
		this.noOfEnvironments = 0;
		this.environments = new ArrayList<Environment>();
	}

	public int getNoOfEnvironments() {
		return this.noOfEnvironments;
	}

	public void setNoOfEnvironments(int noOfEnvironments) {
		this.noOfEnvironments = noOfEnvironments;
	}

	public List<Environment> getEnvironments() {
		return this.environments;
	}

	public void setEnvironments(List<Environment> environments) {
		this.environments = environments;
	}
}
