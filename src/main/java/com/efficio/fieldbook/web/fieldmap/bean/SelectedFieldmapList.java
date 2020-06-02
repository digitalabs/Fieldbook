
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
	private List<SelectedFieldmapRow> rows = new ArrayList<>();


	/**
	 * Instantiates a new selected fieldmap list.
	 *
	 * @param studies the studies
	 */
	public SelectedFieldmapList(final List<FieldMapInfo> studies) {
		if (studies != null && !studies.isEmpty()) {
			for (final FieldMapInfo study : studies) {
				if (study.getDatasets() != null) {
					for (final FieldMapDatasetInfo dataset : study.getDatasets()) {
						if (dataset.getTrialInstances() != null) {
							for (final FieldMapTrialInstanceInfo trial : dataset.getTrialInstances()) {
								final SelectedFieldmapRow row = new SelectedFieldmapRow();
								row.setOrder(trial.getOrder());
								row.setStudyName(study.getFieldbookName());
								row.setEntryCount(trial.getEntryCount());
								row.setPlotCount(trial.getPlotCount());
								row.setRepCount(trial.getRepCount());
								row.setTrialInstanceNo(trial.getTrialInstanceNo());
								row.setStudyId(study.getFieldbookId());
								row.setDatasetId(dataset.getDatasetId());
								row.setGeolocationId(trial.getEnvironmentId());
								row.setDatasetName(dataset.getDatasetName());
								this.rows.add(row);
							}
						}
					}
				}
			}
			Collections.sort(this.rows);
		}

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
	public void setRows(final List<SelectedFieldmapRow> rows) {
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
			for (final SelectedFieldmapRow row : this.rows) {
				total += row.getPlotCount() != null ? row.getPlotCount() : 0;
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
		return this.rows == null || this.rows.isEmpty();
	}
}
