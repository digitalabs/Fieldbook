/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.util;

import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;

import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;

public class FieldMapUtilityHelper {

	/** The Constant NEXT_LINE. */
	private static final String NEXT_LINE = "<br/>";

	public static Plot[][] initializePlots(final int col, final int range) {
		final Plot[][] plots = new Plot[col][range];
		// this creates the initial data
		for (int j = range - 1; j >= 0; j--) {
			for (int i = 0; i < col; i++) {
				plots[i][j] = new Plot(i, j, "");
			}
		}
		return plots;
	}

	/**
	 * Populate plot data.
	 *
	 * @param counter the counter
	 * @param labels the labels
	 * @param col the col
	 * @param range the range
	 * @param plots the plots
	 * @param isUpward the is upward
	 * @param startCol the start col
	 * @param startRange the start range
	 * @param isStartOk the is start ok
	 * @param deletedPlot the deleted plot
	 * @return the int
	 */
	public static int populatePlotData(int counter, final List<FieldMapLabel> labels, final int col, final int range, final Plot[][] plots, final boolean isUpward,
			final int startCol, final int startRange, final boolean isStartOk, final Map<String, String> deletedPlot) {

		String stringToDisplay = "";
		final int i = col;
		final int j = range;
		boolean hasAvailableEntries = true;

		if (counter < labels.size()) {
			stringToDisplay = FieldMapUtilityHelper.getDisplayString(labels.get(counter));
		} else {
			hasAvailableEntries = false;
		}

		if (FieldMapUtilityHelper.isDeleted(i, j, deletedPlot)) {
			plots[i][j].setPlotDeleted(true);
		}
		if (isStartOk) {

			plots[i][j].setNotStarted(false);
			if (!FieldMapUtilityHelper.isDeleted(i, j, deletedPlot)) {
				plots[i][j].setPlotDeleted(false);

				if (!plots[i][j].getDisplayString().equalsIgnoreCase("")) {
					return counter;
				}
				if (counter < labels.size()) {
					final FieldMapLabel oldLabel = labels.get(counter);
					if (oldLabel.getColumn() != null && oldLabel.getRange() != null) {
						return counter;
					}
				}

				if (hasAvailableEntries) {
					// meaning we can plant already and move to the next plant
					plots[i][j].setDisplayString(stringToDisplay);
					labels.get(counter).setColumn(i + 1);
					labels.get(counter).setRange(j + 1);

					plots[i][j].setNoMoreEntries(false);
					counter++;
				} else {
					// there are space but no more entries to plant
					plots[i][j].setNoMoreEntries(true);
				}
			} else {
				// meaing this plot is deleted
				plots[i][j].setPlotDeleted(true);
			}
		} else {
			// meaning we haven't started
			plots[i][j].setNotStarted(true);
		}
		return counter;
	}

	public static void markedDeletedPlot(final Plot[][] plots, final Map<String, String> deletedPlot) {
		final Plot[][] currentPlot = plots;

		if (currentPlot != null) {
			for (int i = 0; i < currentPlot.length; i++) {
				for (int j = 0; j < currentPlot[i].length; j++) {
					final Plot plot = currentPlot[i][j];
					if (FieldMapUtilityHelper.isDeleted(i, j, deletedPlot)) {
						plot.setPlotDeleted(true);
					}
				}
			}
		}
	}

	/**
	 * Checks if is deleted.
	 *
	 * @param col the col
	 * @param range the range
	 * @param deletedPlot the deleted plot
	 * @return true, if is deleted
	 */
	private static boolean isDeleted(final int col, final int range, final Map<String, String> deletedPlot) {
		if (deletedPlot.get(col + "_" + range) != null) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the display string.
	 *
	 * @param label the label
	 * @return the display string
	 */
	public static String getDisplayString(final FieldMapLabel label) {
		final StringBuilder textLabel = new StringBuilder();
		textLabel.append(label.getStudyName());
		textLabel.append("-" + label.getPlotNo());
		textLabel.append(FieldMapUtilityHelper.NEXT_LINE + "Entry " + label.getEntryNumber());
		textLabel.append(FieldMapUtilityHelper.NEXT_LINE + "Rep " + label.getRep());
		return textLabel.toString();
	}

	/**
	 * Render plot cell.
	 *
	 * @param info the info
	 * @param plots the plots
	 * @param i the i
	 * @param j the j
	 * @param isStarted the is started
	 * @param possiblyDeletedCoordinates the possibly deleted coordinates
	 * @param order the order
	 * @return true, if successful
	 */
	public static boolean renderPlotCell(
		final UserFieldmap info, final Plot[][] plots, final int i, final int j, boolean isStarted,
			final List<String> possiblyDeletedCoordinates, final int[] order) {

		final Plot plot = plots[i][j];
		if (plot.getDisplayString() != null && !plot.getDisplayString().isEmpty()) {
			if (!isStarted) {
				info.setStartingColumn(i + 1);
				info.setStartingRange(j + 1);
				isStarted = true;
			}

			FieldMapTrialInstanceInfo trial = null;

			if (plot.getDatasetId() != null && plot.getEnvironmentId() != null) {
				trial = info.getSelectedTrialInstanceByDatasetIdAndEnvironmentId(plot.getDatasetId(), plot.getEnvironmentId());
			}
			if (trial != null && trial.getOrder() == null) {
				trial.setOrder(order[0]);
				order[0] += 1;
			}
		} else {
			if (!isStarted) {
				plot.setNotStarted(true);
			}
		}

		return isStarted;
	}


	/**
	 * Mark deleted coordinates.
	 *
	 * @param plots the plots
	 * @param deletedCoordinates the deleted coordinates
	 */
	public static void markDeletedCoordinates(final Plot[][] plots, final List<String> deletedCoordinates) {
		for (final String deletedIndex : deletedCoordinates) {
			final String[] columnRange = deletedIndex.split("_");
			final int column = Integer.parseInt(columnRange[0]);
			final int range = Integer.parseInt(columnRange[1]);
			plots[column][range].setPlotDeleted(true);
		}
	}
}
