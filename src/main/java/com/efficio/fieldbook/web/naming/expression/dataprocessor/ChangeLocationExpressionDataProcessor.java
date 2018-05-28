package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import com.efficio.fieldbook.web.trial.bean.AdvancingSource;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.springframework.stereotype.Component;


@Component
public class ChangeLocationExpressionDataProcessor implements ExpressionDataProcessor {

    @Override
    public void processEnvironmentLevelData(AdvancingSource source, Workbook workbook, AdvancingStudy nurseryInfo, Study study) throws FieldbookException {
        String locationIdString = nurseryInfo.getHarvestLocationId();
        Integer locationId = StringUtils.isEmpty(locationIdString) ? null : Integer.valueOf(locationIdString);

        source.setHarvestLocationId(locationId);
    }

    @Override
    public void processPlotLevelData(AdvancingSource source, MeasurementRow row) throws FieldbookException {
        // Trial Advancing does not have Harvest location so overriding/setting harvestLocationId at plot level

		if(StudyTypeDto.TRIAL_NAME.equals(source.getStudyType().getName()) && source.getTrailInstanceObservation() != null &&
                source.getTrailInstanceObservation().getDataList() != null &&  !source.getTrailInstanceObservation().getDataList().isEmpty()){
            for(MeasurementData measurementData : source.getTrailInstanceObservation().getDataList()){
                if(measurementData.getMeasurementVariable().getTermId() == TermId.LOCATION_ID.getId()){
                    String locationIdString = measurementData.getValue();
                    Integer locationId = StringUtils.isEmpty(locationIdString) ? null : Integer.valueOf(locationIdString);
                    source.setHarvestLocationId(locationId);
                    break;
                }
            }
        }
    }
}
