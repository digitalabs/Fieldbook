
package com.efficio.fieldbook.web.common.bean;

public class DesignTypeItem {

	private Integer id;
	private String name;

	// this is an html file that contains the specific fields under design type
	private String params;
	private Boolean isPreset;
	private Boolean withResolvable;
	private Integer repNo;
	private Integer totalNoOfEntries;
	private Boolean isDisabled;
	private String templateName;

	public DesignTypeItem() {
		// do nothing
	}

	public DesignTypeItem(final int id) {
		this.id = id;
	}

	public DesignTypeItem(final Integer id, final String name, final String params, final Boolean isPreset, final Boolean withResolvable,
			final Integer noOfReps, final Integer noOfEntries, final Boolean isDisabled) {
		super();
		this.id = id;
		this.name = name;
		this.params = params;
		this.isPreset = isPreset;
		this.withResolvable = withResolvable;
		this.repNo = noOfReps;
		this.totalNoOfEntries = noOfEntries;
		this.isDisabled = isDisabled;
		this.templateName = name.concat(".csv");
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getTemplateName() {
		return this.templateName;
	}

	public String getParams() {
		return this.params;
	}

	public void setParams(final String params) {
		this.params = params;
	}

	public Boolean getIsPreset() {
		return this.isPreset;
	}

	public void setIsPreset(final Boolean isPreset) {
		this.isPreset = isPreset;
	}

	public Boolean getWithResolvable() {
		return this.withResolvable;
	}

	public void setWithResolvable(final Boolean withResolvable) {
		this.withResolvable = withResolvable;
	}

	public Integer getRepNo() {
		return this.repNo;
	}

	public void setRepNo(final Integer repNo) {
		this.repNo = repNo;
	}

	public Integer getTotalNoOfEntries() {
		return this.totalNoOfEntries;
	}

	public void setTotalNoOfEntries(final Integer totalNoOfEntries) {
		this.totalNoOfEntries = totalNoOfEntries;
	}

	public Boolean getIsDisabled() {
		return this.isDisabled;
	}

	public void setIsDisabled(final Boolean isDisabled) {
		this.isDisabled = isDisabled;
	}

}
