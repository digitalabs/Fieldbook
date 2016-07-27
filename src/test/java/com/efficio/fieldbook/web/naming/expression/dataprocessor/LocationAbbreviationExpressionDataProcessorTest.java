package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Location;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class LocationAbbreviationExpressionDataProcessorTest {

    @Mock
    private LocationDataManager locationDataManager;

    @InjectMocks
    private LocationAbbreviationExpressionDataProcessor locationAbbreviationExpressionDataProcessor;


    @Test
    public void testProcessEnvironmentLevelDataWithHarvestLocationAbbr() throws FieldbookException {

        AdvancingSource source = Mockito.mock(AdvancingSource.class);
        AdvancingNursery advancingNursery = new AdvancingNursery();
        advancingNursery.setHarvestLocationAbbreviation("abbr");

        locationAbbreviationExpressionDataProcessor.processEnvironmentLevelData(source, null, advancingNursery, null);
        Mockito.verify(source).setLocationAbbreviation("abbr");
    }

    @Test
    public void testProcessEnvironmentLevelDataWithNoHarvestLocationAbbr() throws FieldbookException {

        AdvancingSource source = Mockito.mock(AdvancingSource.class);
        AdvancingNursery advancingNursery = new AdvancingNursery();

        locationAbbreviationExpressionDataProcessor.processEnvironmentLevelData(source, null, advancingNursery, null);
        Mockito.verify(source).setLocationAbbreviation(null);

    }

    @Test
    public void testProcessPlotLevelDataWithLocationMeasurementData() throws FieldbookException {
        AdvancingSource source = Mockito.mock(AdvancingSource.class);

        Mockito.when(source.getStudyType()).thenReturn(StudyType.T);

        MeasurementRow measurementRow = Mockito.mock(MeasurementRow.class);
        List<MeasurementData> listMeasurementData = Lists.newArrayList();
        MeasurementData measurementData = new MeasurementData();
        measurementData.setValue("11");
        MeasurementVariable measurementVariable = new MeasurementVariable();
        measurementVariable.setTermId(TermId.LOCATION_ID.getId());
        measurementData.setMeasurementVariable(measurementVariable);
        listMeasurementData.add(measurementData);

        Location location = new Location();
        location.setLabbr("IND");

        Mockito.when(measurementRow.getDataList()).thenReturn(listMeasurementData);
        Mockito.when(source.getTrailInstanceObservation()).thenReturn(measurementRow);
        Mockito.when(locationDataManager.getLocationByID(11)).thenReturn(location);

        locationAbbreviationExpressionDataProcessor.processPlotLevelData(source, null, new HashMap<String, String>());

        Mockito.verify(source).setLocationAbbreviation("IND");
    }

    @Test
    public void testProcessPlotLevelDataWithoutLocationMeasurementData() throws FieldbookException {
        AdvancingSource source = Mockito.mock(AdvancingSource.class);

        Mockito.when(source.getStudyType()).thenReturn(StudyType.T);

        MeasurementRow measurementRow = Mockito.mock(MeasurementRow.class);
        List<MeasurementData> listMeasurementData = Lists.newArrayList();
        MeasurementData measurementData = new MeasurementData();
        measurementData.setValue("11");
        MeasurementVariable measurementVariable = new MeasurementVariable();
        measurementVariable.setTermId(TermId.LOCATION_ABBR.getId());
        measurementData.setMeasurementVariable(measurementVariable);
        listMeasurementData.add(measurementData);

        Mockito.when(measurementRow.getDataList()).thenReturn(listMeasurementData);
        Mockito.when(source.getTrailInstanceObservation()).thenReturn(measurementRow);

        locationAbbreviationExpressionDataProcessor.processPlotLevelData(source, null, new HashMap<String, String>());

        Mockito.verify(source, Mockito.never()).setLocationAbbreviation("IND");
    }

    @Test
    public void testProcessPlotLevelDataWithNoMeasurementData() throws Exception {
        AdvancingSource source = Mockito.mock(AdvancingSource.class);

        Mockito.when(source.getStudyType()).thenReturn(StudyType.T);

        MeasurementRow measurementRow = Mockito.mock(MeasurementRow.class);
        Mockito.when(measurementRow.getDataList()).thenReturn(null);
        Mockito.when(source.getTrailInstanceObservation()).thenReturn(measurementRow);

        locationAbbreviationExpressionDataProcessor.processPlotLevelData(source, null, new HashMap<String, String>());

        Mockito.verify(source, Mockito.never()).setLocationAbbreviation("IND");
    }
}
