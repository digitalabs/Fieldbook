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

import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;

/**
 * The Class BarCodeForm.
 */
public class HandsontableForm {
    /** The measurement row list. */
    private List<MeasurementRow> measurementRowList;
    
    private String measurementRowListJson;
    
    private String dataValuesJson;
    
    /**
     * Gets the measurement row list.
     *
     * @return the measurement row list
     */
    public List<MeasurementRow> getMeasurementRowList() {
            return measurementRowList;
    }

    /**
     * Sets the measurement row list.
     *
     * @param measurementRowList the new measurement row list
     */
    public void setMeasurementRowList(List<MeasurementRow> measurementRowList) {
            this.measurementRowList = measurementRowList;
    }
    
    public String getMeasurementRowListJson() {
        return measurementRowListJson;
    }
    
    public void setMeasurementRowListJson(String measurementRowListJson) {
        this.measurementRowListJson = measurementRowListJson;
    }
    
    public void setMeasurementRowListToJson(List<MeasurementRow> measurementRowList) {
        try {
            ObjectMapper om = new ObjectMapper();
            setMeasurementRowListJson(om.writeValueAsString(measurementRowList));
        }
        catch(Exception e) {
            setMeasurementRowListJson("err");
        }
    }
    
    public String getDataValuesJson() {
        return dataValuesJson;
    }
    
    public void setDataValuesJson(String dataValuesJson) {
        this.dataValuesJson = dataValuesJson;
    }
}
