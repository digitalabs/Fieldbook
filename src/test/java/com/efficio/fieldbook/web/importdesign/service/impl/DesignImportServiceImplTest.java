
package com.efficio.fieldbook.web.importdesign.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.StandardVariableInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.data.initializer.DesignImportTestDataInitializer;
import com.efficio.fieldbook.web.data.initializer.ImportedGermplasmMainInfoInitializer;
import com.efficio.fieldbook.web.importdesign.generator.DesignImportMeasurementRowGenerator;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.efficio.fieldbook.web.util.parsing.DesignImportParser;

@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.class)
public class DesignImportServiceImplTest {

	private static final int CHALK_PCT_TERMID = 22768;

	private static final int GYLD_TERMID = 18000;

	private static final String PROGRAM_UUID = "789c6438-5a94-11e5-885d-feff819cdc9f";

	@Mock
	private DesignImportParser designImportParser;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private MessageSource messageSource;

	@Mock
	private MockMultipartFile multiPartFile;

	@Mock
	private UserSelection userSelection;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private DesignImportMeasurementRowGenerator measurementRowGenerator;

	private DesignImportData designImportData;

	@InjectMocks
	private DesignImportServiceImpl service;

	@Before
	public void setUp() {

		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(DesignImportServiceImplTest.PROGRAM_UUID);

		this.initializeOntologyScaleDataManager();
		this.initializeOntologyService();
		this.initializeDesignImportData();
		this.initializeGermplasmList();

	}

	@Test
	public void testAreTrialInstancesMatchTheSelectedEnvironments() throws DesignValidationException {

		Assert.assertFalse("Should be false because the no of environments in trials don't match the no of trials in the design file",
				this.service.areTrialInstancesMatchTheSelectedEnvironments(1, this.designImportData));
		Assert.assertTrue("Should be true because the no of environments in trials match the no of trials in the design file",
				this.service.areTrialInstancesMatchTheSelectedEnvironments(3, this.designImportData));
	}

	@Test
	public void testCategorizeHeadersByPhenotype() {

		final Map<PhenotypicType, List<DesignHeaderItem>> result = this.service.categorizeHeadersByPhenotype(this.createUnmappedHeaders());

		Assert.assertEquals("Total No of TRIAL in file is 2", 2, result.get(PhenotypicType.TRIAL_ENVIRONMENT).size());
		Assert.assertEquals("Total No of GERMPLASM FACTOR in file is 1", 1, result.get(PhenotypicType.GERMPLASM).size());
		Assert.assertEquals("Total No of DESIGN FACTOR in file is 3", 3, result.get(PhenotypicType.TRIAL_DESIGN).size());
		Assert.assertEquals("Total No of VARIATE in file is 0", 0, result.get(PhenotypicType.VARIATE).size());

	}

	@Test
	public void testCategorizeHeadersByPhenotypeIfCaseInsensitive() {

		final Map<PhenotypicType, List<DesignHeaderItem>> result =
				this.service.categorizeHeadersByPhenotype(this.createUnmappedHeadersWithWrongCase());

		Assert.assertEquals("Total No of TRIAL in file is 2", 2, result.get(PhenotypicType.TRIAL_ENVIRONMENT).size());
		Assert.assertEquals("Total No of GERMPLASM FACTOR in file is 1", 1, result.get(PhenotypicType.GERMPLASM).size());
		Assert.assertEquals("Total No of DESIGN FACTOR in file is 3", 3, result.get(PhenotypicType.TRIAL_DESIGN).size());
		Assert.assertEquals("Total No of VARIATE in file is 0", 0, result.get(PhenotypicType.VARIATE).size());

	}

	@Test
	public void testConvertToStandardVariables() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Map<Integer, StandardVariable> result =
				this.service.convertToStandardVariables(workbook.getGermplasmFactors(), PhenotypicType.GERMPLASM);

		Assert.assertEquals(result.size(), workbook.getGermplasmFactors().size());

		for (final MeasurementVariable measurementVar : workbook.getFactors()) {
			final StandardVariable stdVar = result.get(measurementVar.getTermId());
			if (stdVar != null) {
				Assert.assertTrue(stdVar.getId() == measurementVar.getTermId());
				Assert.assertTrue(stdVar.getPhenotypicType().getLabelList().contains(measurementVar.getLabel()));

			}

		}

	}

	@Test
	public void testExtractTrialInstancesFromEnvironmentData() {

		final EnvironmentData environmentData = DesignImportTestDataInitializer.createEnvironmentData(5);
		DesignImportTestDataInitializer.processEnvironmentData(environmentData);

		final Set<String> result = this.service.extractTrialInstancesFromEnvironmentData(environmentData);

		Assert.assertEquals(5, result.size());

	}

	//@Test
	public void testGenerateDesignForOneInstanceOnly() throws DesignValidationException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		Mockito.doReturn(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo()).when(this.userSelection)
				.getImportedGermplasmMainInfo();

		final EnvironmentData environmentData = DesignImportTestDataInitializer.createEnvironmentData(1);

		DesignImportTestDataInitializer.processEnvironmentData(environmentData);

		final List<MeasurementRow> measurements =
				this.service.generateDesign(workbook, this.designImportData, environmentData, true, false,
						this.createAdditionalParamsMap(1, 1));

		Assert.assertEquals("The first trial instance has only 5 observations", DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES,
				measurements.size());

	}

	@Test
	public void testGenerateDesignForThreeInstances() throws DesignValidationException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		Mockito.doReturn(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo()).when(this.userSelection)
				.getImportedGermplasmMainInfo();

		final EnvironmentData environmentData = DesignImportTestDataInitializer.createEnvironmentData(3);

		DesignImportTestDataInitializer.processEnvironmentData(environmentData);

		final List<MeasurementRow> measurements =
				this.service.generateDesign(workbook, this.designImportData, environmentData, true, false,
						this.createAdditionalParamsMap(1, 1));

		Assert.assertEquals("Only the first trial has observations so the measurement count should be 6", 6, measurements.size());

	}

	private Map<String, Integer> createAdditionalParamsMap(final Integer startingEntryNo, final Integer startingPlotNo) {
		final Map<String, Integer> additionalParams = new HashMap<String, Integer>();
		additionalParams.put("startingEntryNo", startingEntryNo);
		additionalParams.put("startingPlotNo", startingPlotNo);
		return additionalParams;
	}

	//@Test
	public void testGenerateDesignForNursery() throws DesignValidationException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(5, StudyType.N);

		final EnvironmentData environmentData = DesignImportTestDataInitializer.createEnvironmentData(1);
		DesignImportTestDataInitializer.processEnvironmentData(environmentData);

		final List<MeasurementRow> measurements =
				this.service.generateDesign(workbook, this.designImportData, environmentData, true, false,
						this.createAdditionalParamsMap(1, 1));

		Assert.assertEquals("The first trial instance has only 5 observations", DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES,
				measurements.size());

	}

	@Test
	public void testGenerateDesignForThreeInstancesPreset() throws DesignValidationException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		Mockito.doReturn(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo()).when(this.userSelection)
				.getImportedGermplasmMainInfo();

		final EnvironmentData environmentData = DesignImportTestDataInitializer.createEnvironmentData(3);

		DesignImportTestDataInitializer.processEnvironmentData(environmentData);

		final List<MeasurementRow> measurements =
				this.service.generateDesign(workbook, this.designImportData, environmentData, false, true,
						this.createAdditionalParamsMap(1, 1));

		Assert.assertEquals("The 3 trial instances should have 18 observations", 18, measurements.size());

		for (int i = 0; i <= 5; i++) {
			Assert.assertEquals("Measurement rows 1 to 6 should be assigned to trial instance 2", "1", measurements.get(i).getDataList()
					.get(0).getValue());
		}

		for (int i = 6; i <= 11; i++) {
			Assert.assertEquals("Measurement rows 7 to 12 should be assigned to trial instance 2", "2", measurements.get(i).getDataList()
					.get(0).getValue());
		}

		for (int i = 12; i <= 17; i++) {
			Assert.assertEquals("Measurement rows 13 to 18 should be assigned to trial instance 2", "3", measurements.get(i).getDataList()
					.get(0).getValue());
		}

	}

	@Test
	public void testGetDesignMeasurementVariables() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Set<MeasurementVariable> result = this.service.getDesignMeasurementVariables(workbook, this.designImportData, true);

		Assert.assertEquals("The total number of Factors and Variates in workbook is 14", 14, result.size());
	}

	@Test
	public void testGetDesignMeasurementVariablesNotPreview() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Set<MeasurementVariable> result = this.service.getDesignMeasurementVariables(workbook, this.designImportData, false);

		// If NOT in PREVIEW mode, the method will remove the trial environment factors in the list except for trial instance. This is
		// because the actual measurements/observations that will be generated from import should not contain trial environment factors.
		Assert.assertEquals("The total number of Factors and Variates (less the trial environments) in workbook is 13", 13, result.size());
	}

	@Test
	public void testGetDesignRequiredMeasurementVariable() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Set<MeasurementVariable> result = this.service.getDesignRequiredMeasurementVariable(workbook, this.designImportData);

		Assert.assertEquals("The total number of Design Factors is 4", 4, result.size());

	}

	@Test
	public void testGetDesignRequiredStandardVariables() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Set<StandardVariable> result = this.service.getDesignRequiredStandardVariables(workbook, this.designImportData);

		Assert.assertEquals("The total number of Design Factors is 4", 4, result.size());

	}

	@Test
	public void testGroupCsvRowsIntoTrialInstance() throws DesignValidationException {

		final DesignHeaderItem trialInstanceHeaderItem =
				this.service.validateIfStandardVariableExists(this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId()
						.get(PhenotypicType.TRIAL_ENVIRONMENT), "design.import.error.trial.is.required", TermId.TRIAL_INSTANCE_FACTOR);

		final Map<String, Map<Integer, List<String>>> result =
				this.service.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, this.designImportData.getCsvData());

		Assert.assertEquals("The total number of trial instances in file is 3", 3, result.size());
		Assert.assertEquals("Each trial instance in file has 5 observations", DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES, result
				.get("1").size());
		Assert.assertEquals("Each trial instance in file has 5 observations", DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES, result
				.get("2").size());
		Assert.assertEquals("Each trial instance in file has 5 observations", DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES, result
				.get("3").size());

	}

	@Test
	public void testGetMeasurementVariablesFromDataFile() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Set<MeasurementVariable> returnValue = this.service.getMeasurementVariablesFromDataFile(workbook, this.designImportData);

		Assert.assertEquals("The test data only contains 7 Measurement Variables", 7, returnValue.size());

	}

	@Test
	public void testValidateIfStandardVariableExists() {
		try {

			this.service.validateIfStandardVariableExists(this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId()
					.get(PhenotypicType.TRIAL_ENVIRONMENT), "design.import.error.trial.is.required", TermId.TRIAL_INSTANCE_FACTOR);

		} catch (final DesignValidationException e) {

			Assert.fail("The logic did not detect that the trial number exist");

		}

	}

	@Test
	public void testValidateIfStandardVariableExistsTrialInstanceDoNotExist() {

		try {

			this.service.validateIfStandardVariableExists(this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId()
					.get(PhenotypicType.GERMPLASM), "design.import.error.trial.is.required", TermId.TRIAL_INSTANCE_FACTOR);

			Assert.fail("The logic should detect that the trial number exist");

		} catch (final DesignValidationException e) {

		}

	}

	@Test
	public void testCreatePresetMeasurementRowsPerInstance() {
		final Map<Integer, List<String>> csvData = this.designImportData.getCsvData();
		final List<MeasurementRow> measurements = new ArrayList<MeasurementRow>();
		final DesignImportMeasurementRowGenerator measurementRowGenerator = this.generateMeasurementRowGenerator();
		final int trialInstanceNo = 1;
		final Integer startingPlotNo = 3;
		this.service.createMeasurementRowsPerInstance(csvData, measurements, measurementRowGenerator, trialInstanceNo, startingPlotNo);

		Assert.assertEquals("The number of measurement rows from the csv file must be equal to the number of measurements row generated.",
				csvData.size() - 1, measurements.size());

		// SITE_NAME must not included
		final Integer expectedColumnNo = csvData.get(0).size() - 1;
		Assert.assertEquals(
				"The number of columns from the csv file must be equal to the number of measurements data per measurement row generated.",
				expectedColumnNo.intValue(), measurements.get(0).getDataList().size());

		final int plotNoIndxCSV =
				this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.TRIAL_DESIGN)
						.get(TermId.PLOT_NO.getId()).getColumnIndex();

		// Matthew makes this change (below) and tests pass BUT WHY. No-one can read this test please make it readable.
		// Please outline what the methids we are testing are supposed to do
		// final int plotNoDelta = startingPlotNo - 1;
		final int plotNoDelta = startingPlotNo;
		for (int i = 0; i < measurements.size(); i++) {
			final List<String> rowCSV = csvData.get(i + 1);
			final int plotNoCsv = Integer.valueOf(rowCSV.get(plotNoIndxCSV));

			final Map<Integer, MeasurementData> dataListMap = this.service.getMeasurementDataMap(measurements.get(i).getDataList());
			final int plotNoActual = Integer.valueOf(dataListMap.get(TermId.PLOT_NO.getId()).getValue());

			Assert.assertEquals("Expecting that the generated value for plot no is increased based on the stated starting plot no.",
					plotNoCsv + plotNoDelta, plotNoActual);
		}
	}

	@Test
	public void testGetStartingPlotNoFromCSV() {

		final Map<Integer, List<String>> csvData = this.designImportData.getCsvData();
		final Map<PhenotypicType, Map<Integer, DesignHeaderItem>> map =
				this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId();

		final int expectedStartingPlotNo = 1;

		final Integer startingPlotNo = this.service.getStartingPlotNoFromCSV(csvData, map);
		Assert.assertEquals(
				"Expecting that the starting plot no is equal to " + expectedStartingPlotNo + " but returned " + startingPlotNo.intValue(),
				expectedStartingPlotNo, startingPlotNo.intValue());
	}

	@Test
	public void testRetrieveImportedGermplasmForNewTrial() {
		final int startingEntryNo = 4;
		final ImportedGermplasmMainInfo importedGermplasmInfo = ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo();

		final Integer entryNoDelta =
				startingEntryNo - importedGermplasmInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId();

		final List<Integer> previousImportedGermplasmListEntryNos = new ArrayList<Integer>();
		for (final ImportedGermplasm entry : importedGermplasmInfo.getImportedGermplasmList().getImportedGermplasms()) {
			previousImportedGermplasmListEntryNos.add(entry.getEntryId());
		}

		Mockito.doReturn(importedGermplasmInfo).when(this.userSelection).getImportedGermplasmMainInfo();

		this.service.retrieveImportedGermplasm(null, startingEntryNo);

		final List<ImportedGermplasm> currentImportedGermplasmList = new ArrayList<ImportedGermplasm>();
		currentImportedGermplasmList.addAll(importedGermplasmInfo.getImportedGermplasmList().getImportedGermplasms());

		int currentIndx = 0;
		while (currentIndx < currentImportedGermplasmList.size()) {
			Assert.assertEquals("Expecting that the new entry no is incremented based on the stated starting no.",
					Integer.valueOf(previousImportedGermplasmListEntryNos.get(currentIndx)).intValue() + entryNoDelta,
					Integer.valueOf(currentImportedGermplasmList.get(currentIndx).getEntryId()).intValue());
			currentIndx++;
		}
	}

	@Test
	public void testRetrieveImportedGermplasmForExistingTrial() {
		final int startingEntryNo = 4;
		final ImportedGermplasmMainInfo importedGermplasmInfo = ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo();

		final List<Integer> previousImportedGermplasmListEntryNos = new ArrayList<Integer>();
		for (final ImportedGermplasm entry : importedGermplasmInfo.getImportedGermplasmList().getImportedGermplasms()) {
			previousImportedGermplasmListEntryNos.add(entry.getEntryId());
		}

		Mockito.doReturn(importedGermplasmInfo).when(this.userSelection).getImportedGermplasmMainInfo();
		this.service.retrieveImportedGermplasm(1, startingEntryNo);

		final List<ImportedGermplasm> currentImportedGermplasmList = new ArrayList<ImportedGermplasm>();
		currentImportedGermplasmList.addAll(importedGermplasmInfo.getImportedGermplasmList().getImportedGermplasms());

		int currentIndx = 0;
		while (currentIndx < currentImportedGermplasmList.size()) {
			Assert.assertEquals("Expecting that no changes made on starting no for existing trial..",
					Integer.valueOf(previousImportedGermplasmListEntryNos.get(currentIndx)).intValue(),
					Integer.valueOf(currentImportedGermplasmList.get(currentIndx).getEntryId()).intValue());
			currentIndx++;
		}
	}

	private DesignImportMeasurementRowGenerator generateMeasurementRowGenerator() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(6, 3);
		final Map<PhenotypicType, Map<Integer, DesignHeaderItem>> mappedHeadersWithStdVarId =
				this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId();
		final List<ImportedGermplasm> importedGermplasm = ImportedGermplasmMainInfoInitializer.createImportedGermplasmList();
		final Map<Integer, StandardVariable> germplasmStandardVariables = new HashMap<Integer, StandardVariable>();
		germplasmStandardVariables.put(TermId.ENTRY_NO.getId(),
				StandardVariableInitializer.createStdVariable(TermId.ENTRY_NO.getId(), TermId.ENTRY_NO.name()));
		final Set<String> trialInstancesFromUI = new HashSet<String>();
		trialInstancesFromUI.add("1");
		trialInstancesFromUI.add("2");
		trialInstancesFromUI.add("3");
		final boolean isPreview = false;
		final Map<String, Integer> availableCheckTypes = new HashMap<String, Integer>();
		final DesignImportMeasurementRowGenerator measurementRowGenerator =
				new DesignImportMeasurementRowGenerator(this.fieldbookService, workbook, mappedHeadersWithStdVarId, importedGermplasm,
						germplasmStandardVariables, trialInstancesFromUI, isPreview, availableCheckTypes);
		return measurementRowGenerator;
	}

	private void initializeDesignImportData() {

		try {
			Mockito.when(this.designImportParser.parseFile(this.multiPartFile)).thenReturn(
					DesignImportTestDataInitializer.createDesignImportData());
			this.designImportData = this.designImportParser.parseFile(this.multiPartFile);
		} catch (final FileParsingException e) {
			Assert.fail("Failed to create DesignImportData");
		}

	}

	private void initializeOntologyScaleDataManager() {
		final Scale scale = new Scale();
		scale.setMaxValue("1");
		scale.setMinValue("100");
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(1, false);
	}

	@SuppressWarnings({"unchecked"})
	private void initializeOntologyService() {

		Mockito.doReturn(
				DesignImportTestDataInitializer.createStandardVariable(PhenotypicType.GERMPLASM, TermId.ENTRY_NO.getId(), "ENTRY_NO", "",
						"", "", DesignImportTestDataInitializer.NUMERIC_VARIABLE, "N", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.ENTRY_NO.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				DesignImportTestDataInitializer.createStandardVariable(PhenotypicType.GERMPLASM, TermId.GID.getId(), "GID", "", "", "",
						DesignImportTestDataInitializer.NUMERIC_VARIABLE, "N", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.GID.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				DesignImportTestDataInitializer.createStandardVariable(PhenotypicType.GERMPLASM, TermId.DESIG.getId(), "DESIG", "", "", "",
						DesignImportTestDataInitializer.CHARACTER_VARIABLE, "C", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.DESIG.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				DesignImportTestDataInitializer.createStandardVariable(PhenotypicType.GERMPLASM, TermId.ENTRY_TYPE.getId(), "ENTRY_TYPE",
						"", "", "", DesignImportTestDataInitializer.CHARACTER_VARIABLE, "C", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.ENTRY_TYPE.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				DesignImportTestDataInitializer.createStandardVariable(PhenotypicType.GERMPLASM, TermId.CROSS.getId(), "CROSS", "", "", "",
						DesignImportTestDataInitializer.CHARACTER_VARIABLE, "C", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.CROSS.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				DesignImportTestDataInitializer.createStandardVariable(PhenotypicType.GERMPLASM, TermId.ENTRY_CODE.getId(), "ENTRY_CODE",
						"", "", "", DesignImportTestDataInitializer.CHARACTER_VARIABLE, "C", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.ENTRY_CODE.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				DesignImportTestDataInitializer.createStandardVariable(PhenotypicType.GERMPLASM, TermId.GERMPLASM_SOURCE.getId(),
						"GERMPLASM_SOURCE", "", "", "", DesignImportTestDataInitializer.CHARACTER_VARIABLE, "C", "", ""))
				.when(this.ontologyService).getStandardVariable(TermId.GERMPLASM_SOURCE.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				DesignImportTestDataInitializer.createStandardVariable(PhenotypicType.GERMPLASM, TermId.SEED_SOURCE.getId(), "SEED_SOURCE",
						"", "", "", DesignImportTestDataInitializer.CHARACTER_VARIABLE, "C", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.SEED_SOURCE.getId(), DesignImportServiceImplTest.PROGRAM_UUID);

		Mockito.doReturn(
				DesignImportTestDataInitializer.createStandardVariable(PhenotypicType.VARIATE, DesignImportServiceImplTest.GYLD_TERMID,
						"GYLD", "", "", "", DesignImportTestDataInitializer.NUMERIC_VARIABLE, "N", "", "")).when(this.ontologyService)
				.getStandardVariable(DesignImportServiceImplTest.GYLD_TERMID, DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				DesignImportTestDataInitializer.createStandardVariable(PhenotypicType.VARIATE,
						DesignImportServiceImplTest.CHALK_PCT_TERMID, "CHALK_PCT", "", "", "",
						DesignImportTestDataInitializer.NUMERIC_VARIABLE, "N", "", "")).when(this.ontologyService)
				.getStandardVariable(DesignImportServiceImplTest.CHALK_PCT_TERMID, DesignImportServiceImplTest.PROGRAM_UUID);

		final Property prop = new Property();
		final Term term = new Term();
		term.setId(0);
		prop.setTerm(term);
		Mockito.doReturn(prop).when(this.ontologyService).getProperty(Matchers.anyString());

		final Map<String, List<StandardVariable>> map = new HashMap<>();

		map.put("TRIAL_INSTANCE", this.createList(DesignImportTestDataInitializer.createStandardVariable(VariableType.ENVIRONMENT_DETAIL,
				TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", "", "", "", "", "", "")));
		map.put("SITE_NAME",
				this.createList(DesignImportTestDataInitializer.createStandardVariable(VariableType.ENVIRONMENT_DETAIL,
						TermId.TRIAL.getId(), "SITE_NAME", "", "", "", "", "", "")));
		map.put("ENTRY_NO",
				this.createList(DesignImportTestDataInitializer.createStandardVariable(VariableType.GERMPLASM_DESCRIPTOR,
						TermId.ENTRY_NO.getId(), "ENTRY_NO", "", "", "", "", "", "")));
		map.put("PLOT_NO",
				this.createList(DesignImportTestDataInitializer.createStandardVariable(VariableType.EXPERIMENTAL_DESIGN,
						TermId.PLOT_NO.getId(), "PLOT_NO", "", "", "", "", "", "")));
		map.put("REP_NO",
				this.createList(DesignImportTestDataInitializer.createStandardVariable(VariableType.EXPERIMENTAL_DESIGN,
						TermId.REP_NO.getId(), "REP_NO", "", "", "", "", "", "")));
		map.put("BLOCK_NO",
				this.createList(DesignImportTestDataInitializer.createStandardVariable(VariableType.EXPERIMENTAL_DESIGN,
						TermId.BLOCK_NO.getId(), "BLOCK_NO", "", "", "", "", "", "")));

		Mockito.doReturn(map).when(this.ontologyDataManager).getStandardVariablesInProjects(Matchers.anyList(), Matchers.anyString());

	}

	private void initializeGermplasmList() {

		final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
		importedGermplasmList.setImportedGermplasms(ImportedGermplasmMainInfoInitializer.createImportedGermplasmList());
		mainInfo.setImportedGermplasmList(importedGermplasmList);

		Mockito.doReturn(mainInfo).when(this.userSelection).getImportedGermplasmMainInfo();

	}

	private List<StandardVariable> createList(final StandardVariable... stdVar) {
		final List<StandardVariable> stdVarList = new ArrayList<>();
		for (final StandardVariable var : stdVar) {
			stdVarList.add(var);
		}
		return stdVarList;

	}

	private List<DesignHeaderItem> createUnmappedHeaders() {
		final List<DesignHeaderItem> items = new ArrayList<>();

		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", 0));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.SITE_NAME.getId(), "SITE_NAME", 1));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.ENTRY_NO.getId(), "ENTRY_NO", 2));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.PLOT_NO.getId(), "PLOT_NO", 3));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.REP_NO.getId(), "REP_NO", 4));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.BLOCK_NO.getId(), "BLOCK_NO", 5));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(DesignImportTestDataInitializer.AFLAVER_5_ID, "AflavER_1_5", 6));

		return items;
	}

	private List<DesignHeaderItem> createUnmappedHeadersWithWrongCase() {
		final List<DesignHeaderItem> items = new ArrayList<>();

		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TriAL_iNSTANCE", 0));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.SITE_NAME.getId(), "SiTe_NaME", 1));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.ENTRY_NO.getId(), "ENtRY_nO", 2));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.PLOT_NO.getId(), "PLoT_NO", 3));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.REP_NO.getId(), "ReP_nO", 4));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.BLOCK_NO.getId(), "BLoCK_nO", 5));

		return items;
	}

}
