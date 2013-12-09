package com.efficio.fieldbook.web.fieldmap.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;


public class SelectedFieldmapList{

    private List<SelectedFieldmapRow> rows = new ArrayList<SelectedFieldmapRow>();

    public SelectedFieldmapList(List<FieldMapInfo> studies) {
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
                                System.out.println(dataset.getDatasetName());
                                row.setDatasetName(dataset.getDatasetName());
                                rows.add(row);
                            }
                        }
                    }
                }
            }
            Collections.sort(rows);
        }
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
                total += (row.getPlotCount() != null ? row.getPlotCount() : 0);
            }
        }
        return total;
    }
    
    public boolean isEmpty() {
        return rows != null ? rows.isEmpty() : true;
    }
}
