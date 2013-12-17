package com.efficio.fieldbook.web.fieldmap.bean;

import java.io.Serializable;



public class SelectedFieldmapRow implements Comparable<SelectedFieldmapRow>, Serializable {

    private static final long serialVersionUID = 1435511995297357029L;

    private Integer order;
    private String studyName;
    private String trialInstanceNo;
    private Long repCount;
    private Long entryCount;
    private Long plotCount;
    private String datasetName;
    
    private Integer studyId;
    private Integer datasetId;
    private Integer geolocationId;
    
    /**
     * @return the order
     */
    public Integer getOrder() {
        return order;
    }
    
    /**
     * @param order the order to set
     */
    public void setOrder(Integer order) {
        this.order = order;
    }
    
    /**
     * @return the studyName
     */
    public String getStudyName() {
        return studyName;
    }


    
    /**
     * @param studyName the studyName to set
     */
    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }


    
    /**
     * @return the trialInstanceNo
     */
    public String getTrialInstanceNo() {
        return trialInstanceNo;
    }


    
    /**
     * @param trialInstanceNo the trialInstanceNo to set
     */
    public void setTrialInstanceNo(String trialInstanceNo) {
        this.trialInstanceNo = trialInstanceNo;
    }


    
    /**
     * @return the repCount
     */
    public Long getRepCount() {
        return repCount;
    }


    
    /**
     * @param repCount the repCount to set
     */
    public void setRepCount(Long repCount) {
        this.repCount = repCount;
    }


    
    /**
     * @return the entryCount
     */
    public Long getEntryCount() {
        return entryCount;
    }


    
    /**
     * @param entryCount the entryCount to set
     */
    public void setEntryCount(Long entryCount) {
        this.entryCount = entryCount;
    }


    
    /**
     * @return the plotCount
     */
    public Long getPlotCount() {
        return plotCount;
    }


    
    /**
     * @param plotCount the plotCount to set
     */
    public void setPlotCount(Long plotCount) {
        this.plotCount = plotCount;
    }


    
    /**
     * @return the studyId
     */
    public Integer getStudyId() {
        return studyId;
    }

    
    /**
     * @param studyId the studyId to set
     */
    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }

    
    /**
     * @return the datasetId
     */
    public Integer getDatasetId() {
        return datasetId;
    }

    
    /**
     * @param datasetId the datasetId to set
     */
    public void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
    }

    
    /**
     * @return the geolocationId
     */
    public Integer getGeolocationId() {
        return geolocationId;
    }

    
    /**
     * @param geolocationId the geolocationId to set
     */
    public void setGeolocationId(Integer geolocationId) {
        this.geolocationId = geolocationId;
    }

    
    /**
     * @return the datasetName
     */
    public String getDatasetName() {
        return datasetName;
    }

    
    /**
     * @param datasetName the datasetName to set
     */
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    @Override
    public int compareTo(SelectedFieldmapRow o) {
        if (this.order != null && o != null) {
            return this.getOrder().compareTo(o.getOrder());
        }
        return 0;
    }
    
}
