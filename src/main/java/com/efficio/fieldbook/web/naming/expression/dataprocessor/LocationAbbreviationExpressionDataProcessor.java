package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Location;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

@Component
public class LocationAbbreviationExpressionDataProcessor implements ExpressionDataProcessor {

    @Resource
    LocationDataManager locationDataManager;

    @Override
    public void processEnvironmentLevelData(AdvancingSource source, Workbook workbook, AdvancingNursery nurseryInfo, Study study) throws FieldbookException {
        String locationAbbreviation = nurseryInfo.getHarvestLocationAbbreviation();
        source.setLocationAbbreviation(locationAbbreviation);
    }

    @Override
    public void processPlotLevelData(AdvancingSource source, MeasurementRow row, Map<String, String> possibleValuesMap) throws FieldbookException {
        // Trial Advancing does not have Harvest location so setting harvestLocationAbbr at plot level
        if(source.getStudyType().equals(StudyType.T) && source.getTrailInstanceObservation() != null &&
                source.getTrailInstanceObservation().getDataList() != null &&  !source.getTrailInstanceObservation().getDataList().isEmpty()){
                for(MeasurementData measurementData : source.getTrailInstanceObservation().getDataList()){
                if(measurementData.getMeasurementVariable().getTermId() == TermId.LOCATION_ID.getId()){
                    String locationAbbreviation = null;
                    String locationIdString = measurementData.getValue();
                    Integer locationId = StringUtils.isEmpty(locationIdString) ? null : Integer.valueOf(locationIdString);
                    if(locationId != null){
                        if(possibleValuesMap.get(locationIdString) != null){
                        	locationAbbreviation = possibleValuesMap.get(locationIdString);
                        } else{
                        	Location location = locationDataManager.getLocationByID(locationId);
                            locationAbbreviation = location.getLabbr();
                            possibleValuesMap.put(locationIdString, locationAbbreviation);
                        }
                    }
                    source.setLocationAbbreviation(locationAbbreviation);
                    break;
                }
            }
        }
    }
}
