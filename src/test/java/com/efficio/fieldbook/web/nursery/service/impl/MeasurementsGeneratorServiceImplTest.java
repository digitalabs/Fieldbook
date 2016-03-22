package com.efficio.fieldbook.web.nursery.service.impl;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import junit.framework.Assert;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class MeasurementsGeneratorServiceImplTest {
    @Mock
    private FieldbookService fieldbookMiddlewareService;

    @InjectMocks
    private MeasurementsGeneratorServiceImpl unitUnderTest;

    private final static Integer TEST_ENTRY_ID = 1;
    private final static String TEST_DESIGNATION = "ABC123";


    @Test
    public void testGenerateFactorsNoCheckDataPresentWithEntryTypeVar() {

        final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook();
        final MeasurementVariable entryTypeVariable = WorkbookTestDataInitializer.createMeasurementVariable(TermId.ENTRY_TYPE.getId());

        workbook.getNonTrialFactors().add(entryTypeVariable);
        final UserSelection userSelection = new UserSelection();
        userSelection.setWorkbook(workbook);

        final List<MeasurementData> outputList = new ArrayList<>();
        final ImportedGermplasm germplasm = new ImportedGermplasm(TEST_ENTRY_ID, TEST_DESIGNATION, "");

        // using a blank standard variable map is sufficient, as it is mainly used when the term ID in the standard variable present in the workbook
        // is empty, which is not the case with our test data
        final Map<String, Integer> standardVariableMap = new HashMap<>();

        unitUnderTest.createFactorDataList(outputList, userSelection, 1, 1, germplasm, 1, 1, standardVariableMap);

        MeasurementData entryTypeData = null;

        for (final MeasurementData measurementData : outputList) {
            if (measurementData.getMeasurementVariable().getTermId() == TermId.ENTRY_TYPE.getId()) {
                entryTypeData = measurementData;
            }
        }

        Assert.assertNotNull("Entry type measurement data not properly created given workbook factors", entryTypeData);
        Assert.assertNotNull("Entry type measurement data value should not be empty", entryTypeData.getcValueId());
        Assert.assertEquals("Entry type measurement data not properly initialized given empty check information on target germplasm", Integer.toString(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()),
                entryTypeData.getcValueId());
    }

    @Test
    public void testGenerateFactorsWithCheckDataPresentWithEntryTypeVar() {
        final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook();
        final MeasurementVariable entryTypeVariable = WorkbookTestDataInitializer.createMeasurementVariable(TermId.ENTRY_TYPE.getId());

        workbook.getNonTrialFactors().add(entryTypeVariable);
        final UserSelection userSelection = new UserSelection();
        userSelection.setWorkbook(workbook);

        final List<MeasurementData> outputList = new ArrayList<>();
        final ImportedGermplasm germplasm = new ImportedGermplasm(TEST_ENTRY_ID, TEST_DESIGNATION, "");
        germplasm.setEntryTypeCategoricalID(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());
        germplasm.setEntryTypeValue(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeValue());
        germplasm.setEntryTypeName(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeName());


        // using a blank standard variable map is sufficient, as it is mainly used when the term ID in the standard variable present in the workbook
        // is empty, which is not the case with our test data
        final Map<String, Integer> standardVariableMap = new HashMap<>();

        unitUnderTest.createFactorDataList(outputList, userSelection, 1, 1, germplasm, 1, 1, standardVariableMap);

        MeasurementData entryTypeData = null;

        for (final MeasurementData measurementData : outputList) {
            if (measurementData.getMeasurementVariable().getTermId() == TermId.ENTRY_TYPE.getId()) {
                entryTypeData = measurementData;
            }
        }

        Assert.assertNotNull("Entry type measurement data not properly created given workbook factors", entryTypeData);
        Assert.assertNotNull("Entry type measurement data value should not be empty", entryTypeData.getcValueId());
        Assert.assertEquals("Entry type measurement data not properly initialized given information on target germplasm", Integer.toString(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId()),
                entryTypeData.getcValueId());
    }

}
