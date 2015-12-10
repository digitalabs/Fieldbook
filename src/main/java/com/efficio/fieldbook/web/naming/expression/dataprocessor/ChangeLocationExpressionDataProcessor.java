package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.springframework.stereotype.Component;

@Component
public class ChangeLocationExpressionDataProcessor implements ExpressionDataProcessor {
    @Override
    public void processEnvironmentLevelData(AdvancingSource source, Workbook workbook, AdvancingNursery nurseryInfo, Study study) throws FieldbookException {
        String locationIdString = nurseryInfo.getHarvestLocationId();
        Integer locationId = StringUtils.isEmpty(locationIdString) ? null : Integer.valueOf(locationIdString);

        source.setLocationId(locationId);
    }

    @Override
    public void processPlotLevelData(AdvancingSource source, MeasurementRow row) throws FieldbookException {
        // empty method. Expression does not need plot level data.
    }
}
