
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
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
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
	private ImmutableMap<Integer, ImportedGermplasm> importedGermplasm;
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

		final ImmutableMap<Integer, ImportedGermplasm> importedGermplasm = Maps.uniqueIndex(
				ImportedGermplasmMainInfoInitializer.createImportedGermplasmList(), new Function<ImportedGermplasm, Integer>() {

					@Override
					public Integer apply(ImportedGermplasm input) {
						return input.getEntryId();
					}
				});

		final Map<Integer, StandardVariable> germplasmStandardVariables =
				DesignImportTestDataInitializer.getStandardVariables(PhenotypicType.GERMPLASM, workbook.getFactors());
		final List<MeasurementData> dataList = new ArrayList<>();

		this.generator.setWorkbook(workbook);
		this.generator.addGermplasmDetailsToDataList(importedGermplasm, germplasmStandardVariables, dataList, 1, false);

		Assert.assertEquals("The added MeasurementData should Match the germplasm Standard Variables", germplasmStandardVariables.size(),
				dataList.size());

		final ImportedGermplasm germplasmEntry = importedGermplasm.get(1);

		for (final MeasurementData measurementData : dataList) {

			if (TermId.ENTRY_NO.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value : " + TermId.ENTRY_NO.toString(),
						measurementData.getValue().toString(), germplasmEntry.getEntryId().toString());
			}
			if (TermId.GID.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value : " + TermId.GID.toString(),
						measurementData.getValue().toString(), germplasmEntry.getGid().toString());
			}
			if (TermId.DESIG.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value : " + TermId.DESIG.toString(),
						measurementData.getValue().toString(), germplasmEntry.getDesig().toString());
			}
			if (TermId.ENTRY_TYPE.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value : " + TermId.ENTRY_TYPE.toString(),
						measurementData.getValue().toString(), germplasmEntry.getEntryTypeValue().toString());
			}
			if (TermId.CROSS.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value : " + TermId.CROSS.toString(),
						measurementData.getValue().toString(), germplasmEntry.getCross().toString());
			}
			if (TermId.ENTRY_CODE.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value : " + TermId.ENTRY_CODE.toString(),
						measurementData.getValue().toString(), germplasmEntry.getEntryCode().toString());
			}
			if (TermId.GERMPLASM_SOURCE.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals(
						"The value of MeasurementData should match the germplasm value : " + TermId.GERMPLASM_SOURCE.toString(),
						measurementData.getValue().toString(), germplasmEntry.getSource().toString());
			}
			if (TermId.SEED_SOURCE.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value : " + TermId.SEED_SOURCE.toString(),
						measurementData.getValue().toString(), germplasmEntry.getSource().toString());
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
	public void testCreateMeasurementData() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final MeasurementVariable measurementVariable = workbook.getFactors().get(0);
		this.generator.setWorkbook(workbook);
		final MeasurementData data = this.generator.createMeasurementData(measurementVariable, "1");

		Assert.assertEquals("1", data.getValue());
		Assert.assertEquals(measurementVariable, data.getMeasurementVariable());

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
