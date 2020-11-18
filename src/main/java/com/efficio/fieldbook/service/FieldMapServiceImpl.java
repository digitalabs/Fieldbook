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

package com.efficio.fieldbook.service;

import com.efficio.fieldbook.service.api.FieldMapService;
import com.efficio.fieldbook.util.FieldMapUtilityHelper;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.label.printing.service.FieldPlotLayoutIterator;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * The Class FieldMapServiceImpl.
 */
@Service
@Transactional
public class FieldMapServiceImpl implements FieldMapService {


	@Override
	public Plot[][] createDummyData(
		final int col, final int range, int startRange, int startCol, final boolean isSerpentine,
			final Map<String, String> deletedPlot, final List<FieldMapLabel> fieldMapLabels, final FieldPlotLayoutIterator plotLayouIterator) {
		startRange--;
		startCol--;
		return plotLayouIterator.createFieldMap(col, range, startRange, startCol, isSerpentine, deletedPlot, fieldMapLabels, null);
	}

	@Override
	public Plot[][] generateFieldmap(final UserFieldmap info, final FieldPlotLayoutIterator plotIterator, final boolean isSavedAlready)
			throws MiddlewareQueryException {

		return this.generateFieldmap(info, plotIterator, isSavedAlready, null);
	}


	@Override
	public Plot[][] generateFieldmap(
		final UserFieldmap info, final FieldPlotLayoutIterator plotIterator, final boolean isSavedAlready,
			final List<String> deletedPlots) throws MiddlewareQueryException {

		final int totalColumns = info.getNumberOfColumnsInBlock();
		final int totalRanges = info.getNumberOfRangesInBlock();
		final boolean isSerpentine = info.getPlantingOrder() == 2;
		final Plot[][] plots = new Plot[totalColumns][totalRanges];

		if (totalColumns == 0 && totalRanges == 0 && info.getBlockId() == 0) {
			
		}

		final List<FieldMapLabel> labels = info.getFieldMapLabels();
		this.initializeFieldMapArray(plots, totalColumns, totalRanges);
		for (final FieldMapLabel label : labels) {
			if (label.getColumn() != null && label.getRange() != null) {
				final int column = label.getColumn();
				final int range = label.getRange();
				if ((column <= totalColumns && range <= totalRanges)) {
					final Plot plot = plots[column - 1][range - 1];
					plot.setColumn(column);
					plot.setRange(range);
					plot.setDatasetId(label.getDatasetId());
					plot.setEnvironmentId(label.getInstanceId());
					plot.setDisplayString(FieldMapUtilityHelper.getDisplayString(label));
					plot.setNotStarted(false);
					plot.setSavedAlready(isSavedAlready);
				} else {
					throw new MiddlewareQueryException("The Column/Range of the Field Map exceeded the Total Columns/Ranges");
				}
			}
		}

		if (deletedPlots != null) {
			for (final String deletedPlot : deletedPlots) {
				final String[] coordinates = deletedPlot.split(",");
				if (coordinates != null && coordinates.length == 2 && NumberUtils.isNumber(coordinates[0])
						&& NumberUtils.isNumber(coordinates[1])) {

					final int column = Integer.valueOf(coordinates[0]);
					final int range = Integer.valueOf(coordinates[1]);
					if (column < totalColumns && range < totalRanges) {
						plots[column][range].setPlotDeleted(true);
						plots[column][range].setSavedAlready(isSavedAlready);
					}
				}
			}
		}

		plotIterator.setOtherFieldMapInformation(info, plots, totalColumns, totalRanges, isSerpentine);
		return plots;
	}

	/**
	 * Initialize field map array.
	 *
	 * @param plots the plots
	 * @param totalColumns the total columns
	 * @param totalRanges the total ranges
	 */
	private void initializeFieldMapArray(final Plot[][] plots, final int totalColumns, final int totalRanges) {
		for (int i = 0; i < totalColumns; i++) {
			for (int j = 0; j < totalRanges; j++) {

				final Plot plot = new Plot(i, j, "");
				plots[i][j] = plot;
				plot.setNotStarted(false);
			}
		}
	}

	/**
	 * Mark deleted coordinates.
	 *
	 * @param plots the plots
	 * @param deletedCoordinates the deleted coordinates
	 */
	@SuppressWarnings("unused")
	private void markDeletedCoordinates(final Plot[][] plots, final List<String> deletedCoordinates) {
		for (final String deletedIndex : deletedCoordinates) {
			final String[] columnRange = deletedIndex.split("_");
			final int column = Integer.parseInt(columnRange[0]);
			final int range = Integer.parseInt(columnRange[1]);
			plots[column][range].setPlotDeleted(true);
		}
	}

}
