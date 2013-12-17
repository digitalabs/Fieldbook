package com.efficio.fieldbook.web.fieldmap.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;


public class SelectedFieldmapList implements Serializable {

    private static final long serialVersionUID = 3166386440351404690L;

    private List<SelectedFieldmapRow> rows = new ArrayList<SelectedFieldmapRow>();
    
    private boolean isTrial;

    public SelectedFieldmapList(List<FieldMapInfo> studies, boolean isTrial) {
        if (studies != null && !studies.isEmpty()) {
            for (FieldMapInfo study : studies) {
                if (study.getDatasets() != null) {
                    for (FieldMapDatasetInfo dataset : study.getDatasets()) {
                        if (dataset.getTrialInstances() != null) {
                            for (FieldMapTrialInstanceInfo trial : dataset.getTrialInstances()) {
                                SelectedFieldmapRow row = new SelectedFieldmapRow();
                                row.setOrder(trial.getOrder());
                                row.setStudyName(study.getFieldbookName());
                                row.setEntryCount(trial.getEntryCount());
                                row.setPlotCount(trial.getPlotCount());
                                row.setRepCount(trial.getRepCount());
                                row.setTrialInstanceNo(trial.getTrialInstanceNo());
                                row.setStudyId(study.getFieldbookId());
                                row.setDatasetId(dataset.getDatasetId());
                                row.setGeolocationId(trial.getGeolocationId());
                                row.setDatasetName(dataset.getDatasetName());
                                rows.add(row);
                            }
                        }
                    }
                }
            }
            Collections.sort(rows);
        }
        
        setTrial(isTrial);
    }
    
    public boolean isTrial() {
        return isTrial;
    }
    
    public void setTrial(boolean isTrial) {
        this.isTrial = isTrial;
    }
    
    /**
     * @return the rows
     */
    public List<SelectedFieldmapRow> getRows() {
        return rows;
    }

    
    /**
     * @param rows the rows to set
     */
    public void setRows(List<SelectedFieldmapRow> rows) {
        this.rows = rows;
    }

    public long getTotalNumberOfPlots() {
        long total = 0;
        if (this.rows != null && !this.rows.isEmpty()) {
            for (SelectedFieldmapRow row : this.rows) {
                if (isTrial()) {
                    total += (row.getPlotCount() != null ? row.getPlotCount() : 0);
                } else {
                    total += (row.getEntryCount() != null ? row.getEntryCount() : 0);
                }
            }
        }
        return total;
    }
    
    public boolean isEmpty() {
        return rows != null ? rows.isEmpty() : true;
    }
}
