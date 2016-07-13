package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

public class ChangeLocationExpressionDataProcessorTest {

    ChangeLocationExpressionDataProcessor changeLocationExpressionDataProcessor;

    @Before
    public void setup(){
        changeLocationExpressionDataProcessor = new ChangeLocationExpressionDataProcessor();
    }

    @Test
    public void testProcessEnvironmentLevelDataWithHarvestLocationId() throws Exception {
        AdvancingSource source = Mockito.mock(AdvancingSource.class);
        AdvancingNursery advancingNursery = new AdvancingNursery();
        advancingNursery.setHarvestLocationId("205");

        changeLocationExpressionDataProcessor.processEnvironmentLevelData(source, null, advancingNursery , null);
        Mockito.verify(source).setHarvestLocationId(205);
    }

    @Test
    public void testProcessEnvironmentLevelDataWithNoHarvestLocationId() throws Exception {
        AdvancingSource source = Mockito.mock(AdvancingSource.class);
        AdvancingNursery advancingNursery = new AdvancingNursery();

        changeLocationExpressionDataProcessor.processEnvironmentLevelData(source, null, advancingNursery , null);
        Mockito.verify(source).setHarvestLocationId(null);
    }

    @Test
    public void testProcessPlotLevelDataWithLocationMeasurementData() throws Exception {
        AdvancingSource source = Mockito.mock(AdvancingSource.class);

        Mockito.when(source.getStudyType()).thenReturn(StudyType.T);

        MeasurementRow measurementRow = Mockito.mock(MeasurementRow.class);
        List<MeasurementData> listMeasurementData = Lists.newArrayList();
        MeasurementData locationId = new MeasurementData();
        locationId.setValue("205");
        MeasurementVariable locationVariable = new MeasurementVariable();
        locationVariable.setTermId(TermId.LOCATION_ID.getId());
        locationId.setMeasurementVariable(locationVariable);
        listMeasurementData.add(locationId);

        Mockito.when(measurementRow.getDataList()).thenReturn(listMeasurementData);
        Mockito.when(source.getTrailInstanceObservation()).thenReturn(measurementRow);


        changeLocationExpressionDataProcessor.processPlotLevelData(source, null);

        Mockito.verify(source).setHarvestLocationId(205);
    }

    @Test
    public void testProcessPlotLevelDataWithoutLocationMeasurementData() throws Exception {
        AdvancingSource source = Mockito.mock(AdvancingSource.class);

        Mockito.when(source.getStudyType()).thenReturn(StudyType.T);

        MeasurementRow measurementRow = Mockito.mock(MeasurementRow.class);
        List<MeasurementData> listMeasurementData = Lists.newArrayList();
        MeasurementData locationId = new MeasurementData();
        locationId.setValue("205");
        MeasurementVariable locationVariable = new MeasurementVariable();
        locationVariable.setTermId(TermId.LOCATION_ABBR.getId());
        locationId.setMeasurementVariable(locationVariable);
        listMeasurementData.add(locationId);

        Mockito.when(measurementRow.getDataList()).thenReturn(listMeasurementData);
        Mockito.when(source.getTrailInstanceObservation()).thenReturn(measurementRow);


        changeLocationExpressionDataProcessor.processPlotLevelData(source, null);

        Mockito.verify(source, Mockito.never()).setHarvestLocationId(205);
    }

    @Test
    public void testProcessPlotLevelDataWithNoMeasurementData() throws Exception {
        AdvancingSource source = Mockito.mock(AdvancingSource.class);

        Mockito.when(source.getStudyType()).thenReturn(StudyType.T);

        MeasurementRow measurementRow = Mockito.mock(MeasurementRow.class);
        Mockito.when(measurementRow.getDataList()).thenReturn(null);
        Mockito.when(source.getTrailInstanceObservation()).thenReturn(measurementRow);


        changeLocationExpressionDataProcessor.processPlotLevelData(source, null);

        Mockito.verify(source, Mockito.never()).setHarvestLocationId(205);
    }
}
