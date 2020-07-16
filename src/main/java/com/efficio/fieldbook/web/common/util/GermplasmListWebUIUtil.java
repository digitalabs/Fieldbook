
package com.efficio.fieldbook.web.common.util;

import org.generationcp.middleware.domain.inventory.InventoryDetails;

/**
 * Created by Daniel Villafuerte on 5/8/2015.
 */
public class GermplasmListWebUIUtil {

	public static final String PLOT_DUPE_CLASS = "plotDupe";
	public static final String PEDIGREE_DUPE_CLASS = "pedigreeDupe";
	public static final String PLOT_RECIPROCAL_CLASS = "plotRecip";
	public static final String PEDIGREE_RECIPROCAL_CLASS = "pedigreeRecip";

	private GermplasmListWebUIUtil() {
		super();
	}

	public static String getCSSClassForDuplicateType(ListDataProject project) {
		if (project.isPedigreeDupe()) {
			return GermplasmListWebUIUtil.PEDIGREE_DUPE_CLASS;
		} else if (project.isPedigreeRecip()) {
			return GermplasmListWebUIUtil.PEDIGREE_RECIPROCAL_CLASS;
		} else if (project.isPlotDupe()) {
			return GermplasmListWebUIUtil.PLOT_DUPE_CLASS;
		} else if (project.isPlotRecip()) {
			return GermplasmListWebUIUtil.PLOT_RECIPROCAL_CLASS;
		} else {
			return "";
		}
	}

	public static String getCSSClassForDuplicateType(InventoryDetails inventoryDetails) {
		ListDataProject project = new ListDataProject();
		project.setDuplicate(inventoryDetails.getDuplicate());
		return GermplasmListWebUIUtil.getCSSClassForDuplicateType(project);
	}
}
