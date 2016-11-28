package com.efficio.fieldbook.util.labelprinting.comparators;

import java.util.Comparator;

import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.oms.TermId;

/**
 * This comparator first checks for the existence of a plot number variable to perform comparison. If that is not available, then values
 * for entry number are used. Comparison is done in ascending order
 */
public class FieldMapLabelComparator implements Comparator<FieldMapLabel> {

	@Override
	public int compare(final FieldMapLabel mapLabel1, final FieldMapLabel mapLabel2) {
		Object plotNumber1 = mapLabel1.getPlotNo();
		if (plotNumber1 == null) {
			plotNumber1 = mapLabel1.getUserFields().get(TermId.PLOT_NO.getId());
		}

		Object plotNumber2 = mapLabel2.getPlotNo();
		if (plotNumber2 == null) {
			plotNumber2 = mapLabel2.getUserFields().get(TermId.PLOT_NO.getId());
		}

		Object entryNumber1 = null;
		Object entryNumber2 = null;

		if (mapLabel1.getUserFields() != null) {
			entryNumber1 = mapLabel1.getUserFields().get(TermId.ENTRY_NO.getId());
			entryNumber2 = mapLabel2.getUserFields().get(TermId.ENTRY_NO.getId());
		}

		if (plotNumber1 != null || plotNumber2 != null) {
			return this.compareTermValues(plotNumber1, plotNumber2);
		} else if (entryNumber1 != null || entryNumber2 != null) {
			return this.compareTermValues(entryNumber1, entryNumber2);
		} else {
			return -1;
		}
	}

	private int compareTermValues(final Object term1, final Object term2) {
		if (term1 != null && term2 != null && !term1.toString().isEmpty() && !term2.toString().isEmpty()) {
			return Integer.compare(Integer.parseInt(term1.toString()), Integer.parseInt(term2.toString()));
		} else if (term1 == null && term2 == null || (
				term1 != null && term2 != null && term1.toString().isEmpty() && term2.toString().isEmpty())) {
			return 0;
		} else if (term2 == null || term2.toString().isEmpty()) {
			return 1;
		} else {
			return -1;
		}
	}
}
