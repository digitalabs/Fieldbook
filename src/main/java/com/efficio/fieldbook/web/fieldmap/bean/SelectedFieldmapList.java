
package com.efficio.fieldbook.web.fieldmap.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;

/**
 * The Class SelectedFieldmapList.
 */
public class SelectedFieldmapList implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3166386440351404690L;

	/** The rows. */
	private List<SelectedFieldmapRow> rows = new ArrayList<SelectedFieldmapRow>();

	/** The is trial. */
	private boolean isTrial;

	/**
	 * Instantiates a new selected fieldmap list.
	 *
	 * @param studies the studies
	 * @param isTrial the is trial
	 */
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
								this.rows.add(row);
							}
						}
					}
				}
			}
			Collections.sort(this.rows);
		}

		this.setTrial(isTrial);
	}

	/**
	 * Checks if is trial.
	 *
	 * @return true, if is trial
	 */
	public boolean isTrial() {
		return this.isTrial;
	}

	/**
	 * Sets the trial.
	 *
	 * @param isTrial the new trial
	 */
	public void setTrial(boolean isTrial) {
		this.isTrial = isTrial;
	}

	/**
	 * Gets the rows.
	 *
	 * @return the rows
	 */
	public List<SelectedFieldmapRow> getRows() {
		return this.rows;
	}

	/**
	 * Sets the rows.
	 *
	 * @param rows the rows to set
	 */
	public void setRows(List<SelectedFieldmapRow> rows) {
		this.rows = rows;
	}

	/**
	 * Gets the total number of plots.
	 *
	 * @return the total number of plots
	 */
	public long getTotalNumberOfPlots() {
		long total = 0;
		if (this.rows != null && !this.rows.isEmpty()) {
			for (SelectedFieldmapRow row : this.rows) {
				if (this.isTrial()) {
					total += row.getPlotCount() != null ? row.getPlotCount() : 0;
				} else {
					total += row.getEntryCount() != null ? row.getEntryCount() : 0;
				}
			}
		}
		return total;
	}

	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		return this.rows != null ? this.rows.isEmpty() : true;
	}
}
