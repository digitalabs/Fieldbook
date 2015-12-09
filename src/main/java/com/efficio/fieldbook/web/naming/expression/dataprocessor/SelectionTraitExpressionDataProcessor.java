package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SelectionTraitExpressionDataProcessor implements ExpressionDataProcessor{

    public static final String SELECTION_TRAIT_PROPERTY = "Selection Criteria";

    @Override
    public void processEnvironmentLevelData(AdvancingSource source, Workbook workbook, AdvancingNursery nurseryInfo, Study study) {
        for (MeasurementVariable condition : workbook.getConditions()) {
            if (condition.getProperty().equalsIgnoreCase(SELECTION_TRAIT_PROPERTY)) {
                source.setSelectionTraitValue(condition.getValue());
            }
        }
    }

    @Override
    public void processPlotLevelData(AdvancingSource source, MeasurementRow row) {
        List<MeasurementData> rowData = row.getDataList();

        for (MeasurementData measurementData : rowData) {
            if (measurementData.getMeasurementVariable().getProperty().equalsIgnoreCase(SELECTION_TRAIT_PROPERTY)) {
                source.setSelectionTraitValue(measurementData.getValue());
            }
        }
    }

}
