package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class SelectionTraitExpressionDataProcessor implements ExpressionDataProcessor{

    public static final String SELECTION_TRAIT_PROPERTY = "Selection Criteria";

    @Resource
    private OntologyVariableDataManager ontologyVariableDataManager;

    @Resource
    private ContextUtil contextUtil;

    @Override
    public void processEnvironmentLevelData(AdvancingSource source, Workbook workbook, AdvancingNursery nurseryInfo, Study study) {
        for (MeasurementVariable condition : workbook.getConditions()) {
            if (condition.getProperty().equalsIgnoreCase(SELECTION_TRAIT_PROPERTY)) {
                source.setSelectionTraitValue(extractVariableValue(condition));
            }
        }
    }

    @Override
    public void processPlotLevelData(AdvancingSource source, MeasurementRow row) {
        List<MeasurementData> rowData = row.getDataList();

        for (MeasurementData measurementData : rowData) {
            if (measurementData.getMeasurementVariable().getProperty().equalsIgnoreCase(SELECTION_TRAIT_PROPERTY)) {
                source.setSelectionTraitValue(extractDataValue(measurementData));
            }
        }
    }

    protected String extractVariableValue(MeasurementVariable variable) {
        if (variable.getDataType().equalsIgnoreCase(MeasurementVariable.CATEGORICAL_VARIABLE_TYPE)) {
            return this.ontologyVariableDataManager.retrieveVariableCategoricalValue(contextUtil.getCurrentProgramUUID(), variable.getTermId(), Integer.parseInt(variable.getValue()));
        } else {
            return variable.getValue();
        }
    }

    protected String extractDataValue(MeasurementData measurementData) {
        if (measurementData.getMeasurementVariable().getDataType().equalsIgnoreCase(MeasurementVariable.CATEGORICAL_VARIABLE_TYPE)) {
            return this.ontologyVariableDataManager.retrieveVariableCategoricalValue(contextUtil.getCurrentProgramUUID(),
                    measurementData.getMeasurementVariable().getTermId(), Integer.parseInt(measurementData.getValue()));
        } else {
            return measurementData.getValue();
        }
    }
}