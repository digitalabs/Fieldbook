package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import java.util.Map;

import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public interface ExpressionDataProcessor {

    /**
     * Method that should be implemented if the expression needs environment level data. The required data
     * should be set into the provided AdvancingSource  object
     *
     * @param source
     * @param workbook
     * @param nurseryInfo
     * @param study
     */
    void processEnvironmentLevelData(AdvancingSource source, Workbook workbook, AdvancingNursery nurseryInfo,
                                            Study study) throws FieldbookException;

    /**
     * Method that should be implemented if the expression needs plot level data. The required data should be set
     * into the provided AdvancingSource object
     *
     * @param source
     * @param row
     * @param possibleValuesMap
     */
    void processPlotLevelData(AdvancingSource source, MeasurementRow row, Map<String, String> possibleValuesMap) throws FieldbookException;
}
