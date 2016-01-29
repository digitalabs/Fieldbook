package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import junit.framework.Assert;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SelectionTraitExpressionDataProcessorTest {

    public static final String TEST_PROGRAM_UUID = "ABCD";
    public static final Integer TEST_TERM_ID = 1;

    @Mock
    private OntologyVariableDataManager ontologyVariableDataManager;

    @Mock
    private ContextUtil contextUtil;

    @InjectMocks
    private SelectionTraitExpressionDataProcessor unitUnderTest;

    @Before
    public void setUp() throws Exception {
        Mockito.when(contextUtil.getCurrentProgramUUID()).thenReturn(TEST_PROGRAM_UUID);
    }

    @Test
    public void testRetrieveEnvironmentalValueStudyDetail() {
        Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
        String testValue = "test";
        MeasurementVariable detail = new MeasurementVariable();
        detail.setProperty(SelectionTraitExpressionDataProcessor.SELECTION_TRAIT_PROPERTY);
        detail.setValue(testValue);

        // study details are placed within the conditions portion of the workbook
        workbook.getConditions().add(detail);

        AdvancingSource source = Mockito.mock(AdvancingSource.class);
        AdvancingNursery nursery = Mockito.mock(AdvancingNursery.class);
        Study study = Mockito.mock(Study.class);

        unitUnderTest.processEnvironmentLevelData(source, workbook, nursery, study);
        Mockito.verify(source).setSelectionTraitValue(testValue);

    }

    @Test
    public void testRetrieveEnvironmentalValueNurseryCondition() {
        Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
        String testValue = "test";
        MeasurementVariable detail = new MeasurementVariable();
        detail.setProperty(SelectionTraitExpressionDataProcessor.SELECTION_TRAIT_PROPERTY);
        detail.setValue(testValue);

        // nursery conditions are placed within the constants section of the workbook
        workbook.getConstants().add(detail);

        AdvancingSource source = Mockito.mock(AdvancingSource.class);
        AdvancingNursery nursery = Mockito.mock(AdvancingNursery.class);
        Study study = Mockito.mock(Study.class);

        unitUnderTest.processEnvironmentLevelData(source, workbook, nursery, study);
        Mockito.verify(source).setSelectionTraitValue(testValue);

    }

    @Test
    public void testExtractCategoricalValueNonOutofBounds() {
        String categoricalValue = "test";
        String testCategoricalValueID = "1";
        Mockito.when(ontologyVariableDataManager.retrieveVariableCategoricalValue(TEST_PROGRAM_UUID, TEST_TERM_ID, Integer.parseInt(testCategoricalValueID))).thenReturn(categoricalValue);

        String output = unitUnderTest.extractValue(testCategoricalValueID, TEST_TERM_ID);

        Assert.assertEquals("Unable to properly extract the value of a categorical value given the categorical value ID", categoricalValue, output);
    }

    @Test
    public void testExtractCategoricalValueOutOfBoundsNumeric() {
        String categoricalValue = "1";

        Mockito.when(ontologyVariableDataManager.retrieveVariableCategoricalValue(TEST_PROGRAM_UUID, TEST_TERM_ID, Integer.parseInt(categoricalValue))).thenReturn(null);

        String output = unitUnderTest.extractValue(categoricalValue, TEST_TERM_ID);

        Assert.assertEquals("Unable to properly return the value of a numeric out of bounds value for a categorical variable", categoricalValue, output);
    }

    @Test
    public void testExtractCategoricalValueOutOfBoundsNonNumeric() {
        String categoricalValue = "OK";

        String output = unitUnderTest.extractValue(categoricalValue, TEST_TERM_ID);

        Assert.assertEquals("Unable to properly return the value of a non numeric out of bounds value for a categorical variable", categoricalValue, output);
    }

}