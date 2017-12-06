
package com.efficio.fieldbook.web.importdesign.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.data.initializer.DesignImportTestDataInitializer;
import com.efficio.fieldbook.web.data.initializer.ImportedGermplasmMainInfoInitializer;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class DesignImportMeasurementRowGeneratorTest {

	private static final int GW100_G = 51496;

	private static final String PROGRAM_UUID = "789c6438-5a94-11e5-885d-feff819cdc9f";
	private static final int TEST_STANDARD_VARIABLE_TERMID = 1;
	private static final int TEST_PROPERTY_TERMID = 1234;
	private static final int TEST_SCALE_TERMID = 4321;
	private static final int TEST_METHOD_TERMID = 3333;
	private static final int TEST_DATATYPE_TERMID = 4444;

	private static final String TEST_DATATYPE_DESCRIPTION = "TEST DATATYPE";
	private static final String TEST_METHOD_NAME = "TEST METHOD";
	private static final String TEST_SCALE_NAME = "TEST SCALE";
	private static final String TEST_PROPERTY_NAME = "TEST PROPERTY";
	private static final String TEST_VARIABLE_DESCRIPTION = "TEST DESCRIPTION";
	private static final String TEST_VARIABLE_NAME = "TEST VARIABLE";

	private UserSelection userSelection;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private DesignImportMeasurementRowGenerator generator;

	private DesignImportData designImportData;
	private Workbook workbook;
	private Map<PhenotypicType, Map<Integer, DesignHeaderItem>> mappedHeadersWithStdVarId;
	private List<String> rowValues;
	private Map<Integer, ImportedGermplasm> importedGermplasm;
	private Map<Integer, StandardVariable> germplasmStandardVariables;
	private Set<String> trialInstancesFromUI;
	private final boolean isPreview = true;
	private Map<String, Integer> availableCheckTypes;

	@Before
	public void setUp() {

		this.userSelection = new UserSelection();
		this.userSelection.setImportedGermplasmMainInfo(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo());

		Mockito.doReturn(this.createProperty(TermId.BREEDING_METHOD_PROP.getId())).when(this.ontologyService)
				.getProperty(Mockito.anyString());
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(DesignImportMeasurementRowGeneratorTest.PROGRAM_UUID);

		this.workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		this.designImportData = DesignImportTestDataInitializer.createDesignImportData();
		this.mappedHeadersWithStdVarId = this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId();
		this.germplasmStandardVariables = new HashMap<Integer, StandardVariable>();
		this.germplasmStandardVariables.put(TermId.ENTRY_NO.getId(), DesignImportTestDataInitializer.createStandardVariable(
				PhenotypicType.GERMPLASM, TermId.ENTRY_NO.getId(), "ENTRY_NO", "", "", "", TermId.NUMERIC_VARIABLE.getId(), "", "", ""));

		this.rowValues = new ArrayList<String>();

		this.importedGermplasm = Maps.uniqueIndex(ImportedGermplasmMainInfoInitializer.createImportedGermplasmList(),
				new Function<ImportedGermplasm, Integer>() {

					@Override
					public Integer apply(ImportedGermplasm input) {
						return input.getEntryId();
					}
				});

		this.trialInstancesFromUI = new HashSet<String>();
		this.trialInstancesFromUI.add("1");

		this.generator =
				new DesignImportMeasurementRowGenerator(this.fieldbookService, this.workbook, this.mappedHeadersWithStdVarId,
						this.importedGermplasm, this.germplasmStandardVariables, this.trialInstancesFromUI, this.isPreview,
						this.availableCheckTypes);

	}

	@SuppressWarnings("deprecation")
	private Property createProperty(final int id) {
		final Term term = new Term();
		term.setId(id);
		final Property property = new Property();
		property.setTerm(term);
		return property;
	}

	@Test
	public void testAddGermplasmDetailsToDataList() throws FileParsingException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Map<Integer, ImportedGermplasm> importedGermplasm = Maps.uniqueIndex(
				ImportedGermplasmMainInfoInitializer.createImportedGermplasmList(), new Function<ImportedGermplasm, Integer>() {

					@Override
					public Integer apply(ImportedGermplasm input) {
						return input.getEntryId();
					}
				});


		final Map<Integer, StandardVariable> germplasmStandardVariables =
				DesignImportTestDataInitializer.getStandardVariables(PhenotypicType.GERMPLASM, workbook.getFactors());
		final List<MeasurementData> measurementDataList = new ArrayList<>();

		this.generator.setWorkbook(workbook);

		// Generate MeasurementData for entry no 1
		int entryNo = 1;
		this.generator.addGermplasmDetailsToDataList(importedGermplasm, germplasmStandardVariables, measurementDataList, entryNo, false);

		Assert.assertEquals("The size of generated MeasurementData list should match the size of germplasm Standard Variables list", germplasmStandardVariables.size(),
				measurementDataList.size());

		final ImportedGermplasm germplasmEntry = importedGermplasm.get(entryNo);

		for (final MeasurementData measurementData : measurementDataList) {

			if (TermId.ENTRY_NO.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value for " + TermId.ENTRY_NO.toString() + " variable",
						measurementData.getValue().toString(), germplasmEntry.getEntryId().toString());
			} else if (TermId.GID.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value for " + TermId.GID.toString() + " variable.",
						measurementData.getValue().toString(), germplasmEntry.getGid().toString());
			} else if (TermId.DESIG.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value for " + TermId.DESIG.toString()  + " variable.",
						measurementData.getValue().toString(), germplasmEntry.getDesig().toString());
			} else if (TermId.ENTRY_TYPE.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value for " + TermId.ENTRY_TYPE.toString() + " variable.",
						measurementData.getValue().toString(), germplasmEntry.getEntryTypeValue().toString());
			} else if (TermId.CROSS.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value for " + TermId.CROSS.toString() + " variable.",
						measurementData.getValue().toString(), germplasmEntry.getCross().toString());
			} else if (TermId.ENTRY_CODE.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value for " + TermId.ENTRY_CODE.toString() + " variable.",
						measurementData.getValue().toString(), germplasmEntry.getEntryCode().toString());
			} else if (TermId.GERMPLASM_SOURCE.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals(
						"The value of MeasurementData should match the germplasm value for " + TermId.GERMPLASM_SOURCE.toString() + " variable.",
						measurementData.getValue().toString(), germplasmEntry.getSource().toString());
			} else if (TermId.SEED_SOURCE.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value for " + TermId.SEED_SOURCE.toString()  + " variable.",
						measurementData.getValue().toString(), germplasmEntry.getSource().toString());
			} else if (TermId.PLOT_ID.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should be empty for " + TermId.PLOT_ID.toString() + " variable",
						measurementData.getValue().toString(), "");
			}

		}
	}

	@Test
	public void testAddVariatesToMeasurementRows() throws DesignValidationException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);
		final EnvironmentData environmentData = DesignImportTestDataInitializer.createEnvironmentData(1);
		DesignImportTestDataInitializer.processEnvironmentData(environmentData);
		final List<MeasurementRow> measurements = workbook.getObservations();

		final MeasurementVariable variate = new MeasurementVariable();
		variate.setTermId(GW100_G);
		variate.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		variate.setName("GW100_g");

		final List<MeasurementVariable> variates = workbook.getVariates();
		variates.add(variate);

		final Integer beforeSize = measurements.get(0).getDataList().size();

		// trigger the addition of variates by setting the Operation to 'ADD' or
		// 'UPDATE'
		for (final MeasurementVariable measurementVariable : workbook.getVariates()) {
			measurementVariable.setOperation(Operation.ADD);
		}

		this.generator.setWorkbook(workbook);
		this.generator.addVariatesToMeasurementRows(measurements, this.userSelection, this.ontologyService, this.contextUtil);

		final Integer actualSize = measurements.get(0).getDataList().size();
		final Integer noOfAddedVariates = 1;
		final Integer expectedSize = beforeSize + noOfAddedVariates;
		Assert.assertEquals("The size of the data list should be " + expectedSize + " since " + noOfAddedVariates + " variates are added",
				expectedSize, actualSize);

	}

	@Test
	public void testCreateMeasurementDataForMeasurementVariable() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final MeasurementVariable measurementVariable = workbook.getFactors().get(0);
		this.generator.setWorkbook(workbook);
		final MeasurementData data = this.generator.createMeasurementData(measurementVariable, "1");

		Assert.assertEquals("1", data.getValue());
		Assert.assertEquals(measurementVariable, data.getMeasurementVariable());

	}

	@Test
	public void testCreateMeasurementDataForStandardVariableWithPossibleValues() {

		final String measurementDataValue = "F1";

		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(TEST_STANDARD_VARIABLE_TERMID);
		standardVariable.setName(TEST_VARIABLE_NAME);
		standardVariable.setDescription(TEST_VARIABLE_DESCRIPTION);
		standardVariable.setProperty(new Term(TEST_PROPERTY_TERMID, TEST_PROPERTY_NAME, ""));
		standardVariable.setScale(new Term(TEST_SCALE_TERMID, TEST_SCALE_NAME, ""));
		standardVariable.setMethod(new Term(TEST_METHOD_TERMID, TEST_METHOD_NAME, ""));
		standardVariable.setDataType(new Term(TEST_DATATYPE_TERMID, TEST_DATATYPE_DESCRIPTION, ""));
		List<Enumeration> possibleValues = new ArrayList<>();
		possibleValues.add(new Enumeration(1, "F1", "F1 Description", 1));
		possibleValues.add(new Enumeration(2, "F2", "F2 Description", 2));
		possibleValues.add(new Enumeration(3, "F3", "F3 Description", 3));
		standardVariable.setEnumerations(possibleValues);

		final MeasurementData measurementData = this.generator.createMeasurementData(standardVariable, measurementDataValue);

		Assert.assertEquals(measurementDataValue, measurementData.getValue());
		Assert.assertEquals(TEST_VARIABLE_NAME, measurementData.getLabel());
		Assert.assertEquals(TEST_DATATYPE_DESCRIPTION, measurementData.getDataType());
		Assert.assertEquals("1", measurementData.getcValueId());

	}

	@Test
	public void testCreateMeasurementDataForStandardVariableNoPossibleValues() {

		final String measurementDataValue = "F1";

		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(TEST_STANDARD_VARIABLE_TERMID);
		standardVariable.setName(TEST_VARIABLE_NAME);
		standardVariable.setDescription(TEST_VARIABLE_DESCRIPTION);
		standardVariable.setProperty(new Term(TEST_PROPERTY_TERMID, TEST_PROPERTY_NAME, ""));
		standardVariable.setScale(new Term(TEST_SCALE_TERMID, TEST_SCALE_NAME, ""));
		standardVariable.setMethod(new Term(TEST_METHOD_TERMID, TEST_METHOD_NAME, ""));
		standardVariable.setDataType(new Term(TEST_DATATYPE_TERMID, TEST_DATATYPE_DESCRIPTION, ""));

		final MeasurementData measurementData = this.generator.createMeasurementData(standardVariable, measurementDataValue);

		Assert.assertEquals(measurementDataValue, measurementData.getValue());
		Assert.assertEquals(TEST_VARIABLE_NAME, measurementData.getLabel());
		Assert.assertEquals(TEST_DATATYPE_DESCRIPTION, measurementData.getDataType());
		Assert.assertNull(measurementData.getcValueId());

	}

	@Test
	public void testCreateMeasurementRow() {

		this.rowValues = this.designImportData.getRowDataMap().get(2);
		final MeasurementRow measurementRow = this.generator.createMeasurementRow(this.rowValues);
		final List<MeasurementData> dataList = measurementRow.getDataList();

		Assert.assertEquals(
				"Expecting that all column values from imported design will be included to data list of the generated measurement row.",
				dataList.size(), this.rowValues.size());

		final Map<String, String> actualVariableValueMap = new HashMap<String, String>();
		for (final MeasurementData data : dataList) {
			actualVariableValueMap.put(data.getMeasurementVariable().getName(), data.getValue());
		}

		final List<String> headerValues = this.designImportData.getRowDataMap().get(0);
		final Map<String, String> expectedVariableValueMap = new HashMap<String, String>();
		for (int i = 0; i < this.rowValues.size(); i++) {
			expectedVariableValueMap.put(headerValues.get(i), this.rowValues.get(i));
		}

		for (final Map.Entry<String, String> actualEntry : actualVariableValueMap.entrySet()) {
			final String variableName = actualEntry.getKey();
			final String value = actualEntry.getValue();

			Assert.assertTrue("Expecting that the column header: " + variableName
					+ " from Import Design is included with the generated measurement row but didn't.",
					expectedVariableValueMap.containsKey(variableName));
			Assert.assertEquals("Expecting that the column header: " + variableName + " has a value: " + value
					+ " when added in the measurement row.", expectedVariableValueMap.get(variableName).toString(), value);
		}
	}

}
