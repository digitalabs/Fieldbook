/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.demo.form;

import java.util.List;

/**
 * The Class Test2JavaForm.
 */
@SuppressWarnings("rawtypes") 
public class Test2JavaForm {
	
	/** The location id. */
	private Integer locationId;
	
	/** The name. */
	private String name;
	
	/** The method ids. */
	private Integer[] methodIds;
	
	/** The location list. */
	private List locationList;
	
	/** The method list. */
	private List methodList;
	
	/**
	 * Gets the location list.
	 *
	 * @return the location list
	 */
	public List getLocationList() {
		return locationList;
	}
	
	/**
	 * Sets the location list.
	 *
	 * @param locationList the new location list
	 */
	public void setLocationList(List locationList) {
		this.locationList = locationList;
	}
	
	/**
	 * Gets the method list.
	 *
	 * @return the method list
	 */
	public List getMethodList() {
		return methodList;
	}
	
	/**
	 * Sets the method list.
	 *
	 * @param methodList the new method list
	 */
	public void setMethodList(List methodList) {
		this.methodList = methodList;
	}
	
	/**
	 * Gets the location id.
	 *
	 * @return the location id
	 */
	public Integer getLocationId() {
		return locationId;
	}
	
	/**
	 * Sets the location id.
	 *
	 * @param locationId the new location id
	 */
	public void setLocationId(Integer locationId) {
		this.locationId = locationId;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the method ids.
	 *
	 * @return the method ids
	 */
	public Integer[] getMethodIds() {
		return methodIds;
	}
	
	/**
	 * Sets the method ids.
	 *
	 * @param methodIds the new method ids
	 */
	public void setMethodIds(Integer[] methodIds) {
		this.methodIds = methodIds;
	}
	
}
