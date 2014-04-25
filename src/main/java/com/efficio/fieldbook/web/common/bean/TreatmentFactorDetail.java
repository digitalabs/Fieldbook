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
package com.efficio.fieldbook.web.common.bean;

import java.io.Serializable;

import org.generationcp.middleware.domain.oms.TermId;

import com.efficio.fieldbook.web.nursery.bean.WidgetType;

public class TreatmentFactorDetail implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer levelId;
	private Integer amountId;
	private String levelValue;
	private String amountValue;
	private String levelName;
	private String amountName;
	private Integer amountDataTypeId;
	private String possibleValuesJson;
	private WidgetType widgetType; 

	public TreatmentFactorDetail(){
		super();
	}

	public TreatmentFactorDetail(Integer levelId, Integer amountId, String levelValue, 
	        String amountValue, String levelName, String amountName, Integer amountDataTypeId, String possibleValuesJson) {
		super();
		this.setLevelId(levelId);
		this.setAmountId(amountId);
		this.setLevelValue(levelValue);
		this.amountValue = amountValue;
		this.levelName = levelName;
		this.amountName = amountName;
		this.amountDataTypeId = amountDataTypeId;
		this.possibleValuesJson = possibleValuesJson;
		setWidgetType();
	}

    
        /**
         * @return the amountValue
         */
        public String getAmountValue() {
            return amountValue;
        }
    
        /**
         * @param amountValue the amountValue to set
         */
        public void setAmountValue(String amountValue) {
            this.amountValue = amountValue;
        }
    
        /**
         * @return the levelName
         */
        public String getLevelName() {
            return levelName;
        }
    
        /**
         * @param levelName the levelName to set
         */
        public void setLevelName(String levelName) {
            this.levelName = levelName;
        }
    
        /**
         * @return the amountName
         */
        public String getAmountName() {
            return amountName;
        }
    
        /**
         * @param amountName the amountName to set
         */
        public void setAmountName(String amountName) {
            this.amountName = amountName;
        }

        /**
         * @return the levelId
         */
        public Integer getLevelId() {
            return levelId;
        }

        /**
         * @param levelId the levelId to set
         */
        public void setLevelId(Integer levelId) {
            this.levelId = levelId;
        }

        /**
         * @return the amountId
         */
        public Integer getAmountId() {
            return amountId;
        }

        /**
         * @param amountId the amountId to set
         */
        public void setAmountId(Integer amountId) {
            this.amountId = amountId;
        }

        /**
         * @return the levelValue
         */
        public String getLevelValue() {
            return levelValue;
        }

        /**
         * @param levelValue the levelValue to set
         */
        public void setLevelValue(String levelValue) {
            this.levelValue = levelValue;
        }

        /**
         * @return the widgetType
         */
        public WidgetType getWidgetType() {
            if (amountDataTypeId != null) {
                    if (amountDataTypeId.equals(TermId.DATE_VARIABLE.getId())) {
                            this.widgetType = WidgetType.DATE;
                    }
                    else if (amountDataTypeId.equals(TermId.CATEGORICAL_VARIABLE.getId())) {
                            this.widgetType = WidgetType.DROPDOWN;
                    }
                    else if (amountDataTypeId.equals(TermId.NUMERIC_VARIABLE.getId()) 
                                    || amountDataTypeId.equals(TermId.NUMERIC_DBID_VARIABLE.getId())) {
                            this.widgetType = WidgetType.NTEXT;
                    }
                    else {
                            this.widgetType = WidgetType.CTEXT;
                    }                                       
            }
                
            return this.widgetType;
        }

        /**
         * @param widgetType the widgetType to set
         */
        public void setWidgetType() {
        }

        /**
         * @return the amountDataTypeId
         */
        public Integer getAmountDataTypeId() {
            return amountDataTypeId;
        }

        /**
         * @param amountDataTypeId the amountDataTypeId to set
         */
        public void setAmountDataTypeId(Integer amountDataTypeId) {
            this.amountDataTypeId = amountDataTypeId;
        }

        /**
         * @return the possibleValuesJson
         */
        public String getPossibleValuesJson() {
            return possibleValuesJson;
        }

        /**
         * @param possibleValuesJson the possibleValuesJson to set
         */
        public void setPossibleValuesJson(String possibleValuesJson) {
            this.possibleValuesJson = possibleValuesJson;
        }
	
}
