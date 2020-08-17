
package com.efficio.fieldbook.web.trial.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 7/8/2014 Time: 5:05 PM
 */
public class InstanceInfo implements TabInfoBean {

	private int numberOfInstances;
	private List<Instance> instances;

	public InstanceInfo() {
		this.numberOfInstances = 0;
		this.instances = new ArrayList<Instance>();
	}

	public int getNumberOfInstances() {
		return this.numberOfInstances;
	}

	public void setNumberOfInstances(int numberOfInstances) {
		this.numberOfInstances = numberOfInstances;
	}

	public List<Instance> getInstances() {
		return this.instances;
	}

	public void setInstances(List<Instance> instances) {
		this.instances = instances;
	}
}
