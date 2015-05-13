package com.efficio.fieldbook.web.common.util;

import org.generationcp.middleware.pojos.ListDataProject;

/**
 * Created by Daniel Villafuerte on 5/8/2015.
 */
public class GermplasmListWebUIUtil {
    public static final String PLOT_DUPE_CLASS = "plotDupe";
    public static final String PEDIGREE_DUPE_CLASS = "pedigreeDupe";
    public static final String PLOT_RECIPROCAL_CLASS = "plotRecip";
    public static final String PEDIGREE_RECIPROCAL_CLASS = "pedigreeRecip";

    public static String getCSSClassForDuplicateType(ListDataProject project) {
        if (project.isPedigreeDupe()) {
            return PEDIGREE_DUPE_CLASS;
        } else if (project.isPedigreeRecip()) {
            return PEDIGREE_RECIPROCAL_CLASS;
        } else if (project.isPlotDupe()) {
            return PLOT_DUPE_CLASS;
        } else if (project.isPlotRecip()) {
            return PLOT_RECIPROCAL_CLASS;
        } else {
            return "";
        }
    }
}
