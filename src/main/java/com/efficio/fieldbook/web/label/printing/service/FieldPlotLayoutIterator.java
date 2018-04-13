
package com.efficio.fieldbook.web.label.printing.service;

import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;

import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;

/**
 * The Interface FieldPlotLayoutIterator.
 */
public interface FieldPlotLayoutIterator {

	/**
	 * Creates the field map.
	 *
	 * @param col the col
	 * @param range the range
	 * @param startRange the start range
	 * @param startCol the start col
	 * @param isSerpentine the is serpentine
	 * @param deletedPlot the deleted plot
	 * @param labels the labels
	 * @return the plot[][]
	 */
	Plot[][] createFieldMap(int col, int range, int startRange, int startCol, boolean isSerpentine, Map<String, String> deletedPlot,
		List<FieldMapLabel> labels, Plot[][] currentPlots);

	/**
	 * Sets the other field map information.
	 * 
	 * This would be use when generating a field map from a saved information in the DB
	 *
	 * @param info the info
	 * @param plots the plots
	 * @param totalColumns the total columns
	 * @param totalRanges the total ranges
	 * @param isSerpentine the is serpentine
	 */
	void setOtherFieldMapInformation(UserFieldmap info, Plot[][] plots, int totalColumns, int totalRanges, boolean isSerpentine);

}
