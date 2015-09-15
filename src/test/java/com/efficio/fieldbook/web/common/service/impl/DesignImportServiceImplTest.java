
package com.efficio.fieldbook.web.common.service.impl;

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
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.OntologyDataManager;
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
import com.efficio.fieldbook.web.trial.bean.Environment;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.efficio.fieldbook.web.util.parsing.DesignImportParser;

@RunWith(MockitoJUnitRunner.class)
public class DesignImportServiceImplTest {

	private static final int CHALK_PCT_TERMID = 22768;

	private static final int GYLD_TERMID = 18000;

	private static final int NO_OF_TEST_ENTRIES = 5;

	private static final String PROGRAM_UUID = "789c6438-5a94-11e5-885d-feff819cdc9f";

	@Mock
	private DesignImportParser designImportParser;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private OntologyService ontologyService;

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

	private DesignImportData designImportData;

	@InjectMocks
	private DesignImportServiceImpl service;

	@Before
	public void setUp() {

		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(DesignImportServiceImplTest.PROGRAM_UUID);

		this.initializeOntologyService();
		this.initializeDesignImportData();
		this.initializeGermplasmList();

		WorkbookDataUtil.setTestWorkbook(null);

	}

	@Test
	public void testAddGermplasmDetailsToDataList() throws FileParsingException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final List<ImportedGermplasm> importedGermplasm = this.createImportedGermplasmList();
		final Map<Integer, StandardVariable> germplasmStandardVariables =
				this.getStandardVariables(PhenotypicType.GERMPLASM, workbook.getFactors());
		final List<MeasurementData> dataList = new ArrayList<>();

		this.service.addGermplasmDetailsToDataList(importedGermplasm, germplasmStandardVariables, dataList, 1);

		Assert.assertEquals("The added MeasurementData should Match the germplasm Standard Variables", germplasmStandardVariables.size(),
				dataList.size());

		final ImportedGermplasm germplasmEntry = importedGermplasm.get(0);

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
						measurementData.getValue().toString(), germplasmEntry.getCheck().toString());
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
	public void testAddVariatesToMeasurementRows() throws DesignValidationException, MiddlewareException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		Mockito.doReturn(this.createImportedGermplasmMainInfo()).when(this.userSelection).getImportedGermplasmMainInfo();

		final EnvironmentData environmentData = this.createEnvironmentData(1);

		this.processEnvironmentData(environmentData);

		final List<MeasurementRow> measurements = this.service.generateDesign(workbook, this.designImportData, environmentData, true);

		// trigger the addition of variates by setting the Operation to 'ADD' or
		// 'UPDATE'
		for (final MeasurementVariable measurementVariable : workbook.getVariates()) {
			measurementVariable.setOperation(Operation.ADD);
		}

		this.service.addVariatesToMeasurementRows(workbook, measurements);

		Assert.assertEquals("The size of the data list should be 13 since 2 variates are added", 13, measurements.get(0).getDataList()
				.size());

	}

	@Test
	public void testAreTrialInstancesMatchTheSelectedEnvironments() throws MiddlewareException {

		Assert.assertFalse("Should be false because the no of environments in trials don't match the no of trials in the design file",
				this.service.areTrialInstancesMatchTheSelectedEnvironments(1, this.designImportData));
		Assert.assertTrue("Should be true because the no of environments in trials match the no of trials in the design file",
				this.service.areTrialInstancesMatchTheSelectedEnvironments(3, this.designImportData));
	}

	@Test
	public void testCategorizeHeadersByPhenotype() throws MiddlewareException {

		final Map<PhenotypicType, List<DesignHeaderItem>> result = this.service.categorizeHeadersByPhenotype(this.createUnmappedHeaders());

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
	public void testCreateMeasurementData() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final MeasurementVariable measurementVariable = workbook.getFactors().get(0);
		final MeasurementData data = this.service.createMeasurementData(measurementVariable, "1");

		Assert.assertEquals("1", data.getValue());
		Assert.assertEquals(measurementVariable, data.getMeasurementVariable());

	}

	@Test
	public void testExtractTrialInstancesFromEnvironmentData() {

		final EnvironmentData environmentData = this.createEnvironmentData(5);
		this.processEnvironmentData(environmentData);

		final Set<String> result = this.service.extractTrialInstancesFromEnvironmentData(environmentData);

		Assert.assertEquals(5, result.size());

	}

	@Test
	public void testFilterDesignHeaderItemsByTermId() throws MiddlewareException {

		final List<DesignHeaderItem> headerDesignItems = this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT);
		final DesignHeaderItem result = this.service.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR, headerDesignItems);

		Assert.assertNotNull(result);

	}

	@Test
	public void testGenerateDesignForOneInstanceOnly() throws MiddlewareException, DesignValidationException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		Mockito.doReturn(this.createImportedGermplasmMainInfo()).when(this.userSelection).getImportedGermplasmMainInfo();

		final EnvironmentData environmentData = this.createEnvironmentData(1);

		this.processEnvironmentData(environmentData);

		final List<MeasurementRow> measurements = this.service.generateDesign(workbook, this.designImportData, environmentData, true);

		Assert.assertEquals("The first trial instance has only 5 observations", NO_OF_TEST_ENTRIES, measurements.size());

	}

	@Test
	public void testGenerateDesignForThreeInstances() throws MiddlewareException, DesignValidationException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		Mockito.doReturn(this.createImportedGermplasmMainInfo()).when(this.userSelection).getImportedGermplasmMainInfo();

		final EnvironmentData environmentData = this.createEnvironmentData(3);

		this.processEnvironmentData(environmentData);

		final List<MeasurementRow> measurements = this.service.generateDesign(workbook, this.designImportData, environmentData, true);

		Assert.assertEquals("The 3 trial instances have 15 observations", 15, measurements.size());

	}

	@Test
	public void testGenerateDesignForNursery() throws DesignValidationException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(5, StudyType.N);

		final EnvironmentData environmentData = this.createEnvironmentData(1);
		this.processEnvironmentData(environmentData);

		final List<MeasurementRow> measurements = this.service.generateDesign(workbook, this.designImportData, environmentData, true);

		Assert.assertEquals("The first trial instance only has 5 observations", NO_OF_TEST_ENTRIES, measurements.size());

	}

	@Test
	public void testGetDesignMeasurementVariables() throws MiddlewareException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Set<MeasurementVariable> result = this.service.getDesignMeasurementVariables(workbook, this.designImportData, true);

		Assert.assertEquals("The total number of Factors and Variates in workbook is 13", 13, result.size());
	}

	@Test
	public void testGetDesignMeasurementVariablesNotPreview() throws MiddlewareException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Set<MeasurementVariable> result = this.service.getDesignMeasurementVariables(workbook, this.designImportData, false);

		// If NOT in PREVIEW mode, the method will remove the trial environment factors in the list except for trial instance. This is
		// because the actual measurements/observations that will be generated from import should not contain trial environment factors.
		Assert.assertEquals("The total number of Factors and Variates (less the trial environments) in workbook is 12", 12, result.size());
	}

	@Test
	public void testGetDesignRequiredMeasurementVariable() throws MiddlewareException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Set<MeasurementVariable> result = this.service.getDesignRequiredMeasurementVariable(workbook, this.designImportData);

		Assert.assertEquals("The total number of Design Factors is 3", 3, result.size());

	}

	@Test
	public void testGetDesignRequiredStandardVariables() throws MiddlewareException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Set<StandardVariable> result = this.service.getDesignRequiredStandardVariables(workbook, this.designImportData);

		Assert.assertEquals("The total number of Design Factors is 3", 3, result.size());

	}

	@Test
	public void testGroupCsvRowsIntoTrialInstance() throws MiddlewareException {

		final DesignHeaderItem trialInstanceHeaderItem =
				this.service.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR,
						this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));

		final Map<String, Map<Integer, List<String>>> result =
				this.service.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, this.designImportData.getCsvData());

		Assert.assertEquals("The total number of trial instances in file is 3", 3, result.size());
		Assert.assertEquals("Each trial instance in file has 5 observations", NO_OF_TEST_ENTRIES, result.get("1").size());
		Assert.assertEquals("Each trial instance in file has 5 observations", NO_OF_TEST_ENTRIES, result.get("2").size());
		Assert.assertEquals("Each trial instance in file has 5 observations", NO_OF_TEST_ENTRIES, result.get("3").size());

	}

	@Test
	public void testValidateDesignData() throws MiddlewareException {

		try {

			this.service.validateDesignData(this.designImportData);

		} catch (final DesignValidationException e) {

			Assert.fail("The data should pass the validateDesignData test");
		}

	}

	@Test
	public void testValidateEntryNoMustBeUniquePerInstance() throws MiddlewareException {

		try {

			final DesignHeaderItem trialInstanceHeaderItem =
					this.service.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR, this.designImportData.getMappedHeaders()
							.get(PhenotypicType.TRIAL_ENVIRONMENT));
			final DesignHeaderItem entryNoHeaderItem =
					this.service.filterDesignHeaderItemsByTermId(TermId.ENTRY_NO,
							this.designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));

			final Map<String, Map<Integer, List<String>>> data =
					this.service.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, this.designImportData.getCsvData());

			this.service.validateEntryNoMustBeUniquePerInstance(entryNoHeaderItem, data);

		} catch (final DesignValidationException e) {

			Assert.fail("The list should pass the validateEntryNoMustBeUniquePerInstance test");
		}

	}

	@Test
	public void testValidateEntryNoMustBeUniquePerInstanceEntryNoIsNotUnique() throws MiddlewareException {

		final DesignHeaderItem trialInstanceHeaderItem =
				this.service.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR,
						this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		final DesignHeaderItem entryNoHeaderItem =
				this.service.filterDesignHeaderItemsByTermId(TermId.ENTRY_NO,
						this.designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));

		final Map<Integer, List<String>> csvData = this.designImportData.getCsvData();
		csvData.get(1).set(entryNoHeaderItem.getColumnIndex(), "1");
		csvData.get(2).set(entryNoHeaderItem.getColumnIndex(), "1");
		csvData.get(3).set(entryNoHeaderItem.getColumnIndex(), "1");

		final Map<String, Map<Integer, List<String>>> data = this.service.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, csvData);

		try {

			this.service.validateEntryNoMustBeUniquePerInstance(entryNoHeaderItem, data);

			Assert.fail("The list shouldn't pass the validateEntryNoMustBeUniquePerInstance test");

		} catch (final DesignValidationException e) {

		}
	}

	@Test
	public void testValidateGermplasmEntriesFromShouldMatchTheGermplasmList() {

		final Set<String> entryNumbers = new HashSet<>();
		for (int x = 1; x <= NO_OF_TEST_ENTRIES; x++) {
			entryNumbers.add(String.valueOf(x));
		}

		try {

			this.service.validateGermplasmEntriesFromShouldMatchTheGermplasmList(entryNumbers);

		} catch (final DesignValidationException e) {

			Assert.fail("The data should pass the validateGermplasmEntriesFromShouldMatchTheGermplasmList test");
		}

	}

	@Test
	public void testValidateIfEntryNumberExistsNoEntryNumber() throws MiddlewareException {

		try {

			this.service.validateIfEntryNumberExists(this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));

			Assert.fail("The logic did not detect that the entry number doesn't exist");

		} catch (final DesignValidationException e) {

		}

	}

	@Test
	public void testValidateIfEntryNumberExistsWithEntryNumber() throws MiddlewareException {

		try {

			this.service.validateIfEntryNumberExists(this.designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));

		} catch (final DesignValidationException e) {

			Assert.fail("The logic did not detect that the entry number doesn't exist");

		}

	}

	@Test
	public void testValidateIfPlotNumberExistsNoPlotNumber() throws MiddlewareException {

		try {

			this.service.validateIfPlotNumberExists(this.designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));

			Assert.fail("The logic did not detect that the plot number do not exist");

		} catch (final DesignValidationException e) {

		}

	}

	@Test
	public void testValidateIfPlotNumberExistsWithPlotNumber() throws MiddlewareException {

		try {

			this.service.validateIfPlotNumberExists(this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN));

		} catch (final DesignValidationException e) {

			Assert.fail("The logic did not detect that the plot number exist");

		}

	}

	@Test
	public void testValidateIfPlotNumberIsUnique() throws MiddlewareException {

		final DesignHeaderItem trialInstanceHeaderItem =
				this.service.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR,
						this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		final Map<String, Map<Integer, List<String>>> csvMap =
				this.service.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, this.designImportData.getCsvData());

		try {

			this.service.validateIfPlotNumberIsUniquePerInstance(this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN),
					csvMap);

		} catch (final DesignValidationException e) {

			Assert.fail("The list should pass the validateIfPlotNumberIsUnique test");
		}

	}

	@Test
	public void testValidateIfPlotNumberIsUniquePerInstance() throws MiddlewareException {

		final DesignHeaderItem trialInstanceHeaderItem =
				this.service.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR,
						this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		final DesignHeaderItem plotNoHeaderItem =
				this.service.filterDesignHeaderItemsByTermId(TermId.PLOT_NO,
						this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN));

		final Map<Integer, List<String>> csvData = this.designImportData.getCsvData();
		csvData.get(0).set(plotNoHeaderItem.getColumnIndex(), "1");
		csvData.get(1).set(plotNoHeaderItem.getColumnIndex(), "1");
		csvData.get(2).set(plotNoHeaderItem.getColumnIndex(), "1");

		final Map<String, Map<Integer, List<String>>> csvMap = this.service.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, csvData);

		try {

			this.service.validateIfPlotNumberIsUniquePerInstance(this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN),
					csvMap);

			Assert.fail("The list shouldn't pass the validateIfPlotNumberIsUniquePerInstance test");

		} catch (final DesignValidationException e) {

		}

	}

	@Test
	public void testValidateIfTrialFactorExists() throws MiddlewareException {

		try {

			this.service.validateIfTrialFactorExists(this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));

		} catch (final DesignValidationException e) {

			Assert.fail("The logic did not detect that the trial number exist");

		}

	}

	@Test
	public void testValidateIfTrialFactorExistsTrialInstanceDoNotExist() throws MiddlewareException {

		try {

			this.service.validateIfTrialFactorExists(this.designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));

			Assert.fail("The logic should detect that the trial number exist");

		} catch (final DesignValidationException e) {

		}

	}

	@Test
	public void testGetMeasurementVariablesFromDataFile() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Set<MeasurementVariable> returnValue = this.service.getMeasurementVariablesFromDataFile(workbook, this.designImportData);

		Assert.assertEquals("The test data only contains 6 Measurement Variables", 6, returnValue.size());

	}

	private void initializeDesignImportData() {

		try {
			Mockito.when(this.designImportParser.parseFile(this.multiPartFile)).thenReturn(this.createDesignImportData());
			this.designImportData = this.designImportParser.parseFile(this.multiPartFile);
		} catch (final FileParsingException e) {
			Assert.fail("Failed to create DesignImportData");
		}

	}

	private void initializeOntologyService() throws MiddlewareException {

		Mockito.doReturn(this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.ENTRY_NO.getId(), "ENTRY_NO", "", "", "", "", "", ""))
				.when(this.ontologyService).getStandardVariable(TermId.ENTRY_NO.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.GID.getId(), "GID", "", "", "", "", "", ""))
				.when(this.ontologyService).getStandardVariable(TermId.GID.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.DESIG.getId(), "DESIG", "", "", "", "", "", ""))
				.when(this.ontologyService).getStandardVariable(TermId.DESIG.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.ENTRY_TYPE.getId(), "ENTRY_TYPE", "", "", "", "", "", ""))
				.when(this.ontologyService).getStandardVariable(TermId.ENTRY_TYPE.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.CROSS.getId(), "CROSS", "", "", "", "", "", ""))
				.when(this.ontologyService).getStandardVariable(TermId.CROSS.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.ENTRY_CODE.getId(), "ENTRY_CODE", "", "", "", "", "", ""))
				.when(this.ontologyService).getStandardVariable(TermId.ENTRY_CODE.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.GERMPLASM_SOURCE.getId(), "GERMPLASM_SOURCE", "", "", "", "",
						"", "")).when(this.ontologyService)
				.getStandardVariable(TermId.GERMPLASM_SOURCE.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.SEED_SOURCE.getId(), "SEED_SOURCE", "", "", "", "", "", ""))
				.when(this.ontologyService).getStandardVariable(TermId.SEED_SOURCE.getId(), DesignImportServiceImplTest.PROGRAM_UUID);

		Mockito.doReturn(
				this.createStandardVariable(PhenotypicType.VARIATE, DesignImportServiceImplTest.GYLD_TERMID, "GYLD", "", "", "", "N", "",
						"")).when(this.ontologyService)
				.getStandardVariable(DesignImportServiceImplTest.GYLD_TERMID, DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				this.createStandardVariable(PhenotypicType.VARIATE, DesignImportServiceImplTest.CHALK_PCT_TERMID, "CHALK_PCT", "", "", "N",
						"", "", "")).when(this.ontologyService)
				.getStandardVariable(DesignImportServiceImplTest.CHALK_PCT_TERMID, DesignImportServiceImplTest.PROGRAM_UUID);

		final Property prop = new Property();
		final Term term = new Term();
		term.setId(0);
		prop.setTerm(term);
		Mockito.doReturn(prop).when(this.ontologyService).getProperty(Matchers.anyString());

		final Map<String, List<StandardVariable>> map = new HashMap<>();

		map.put("TRIAL_INSTANCE", this.createList(this.createStandardVariable(VariableType.ENVIRONMENT_DETAIL,
				TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", "", "", "", "", "", "")));
		map.put("SITE_NAME", this.createList(this.createStandardVariable(VariableType.ENVIRONMENT_DETAIL, TermId.TRIAL.getId(),
				"SITE_NAME", "", "", "", "", "", "")));
		map.put("ENTRY_NO", this.createList(this.createStandardVariable(VariableType.GERMPLASM_DESCRIPTOR, TermId.ENTRY_NO.getId(),
				"ENTRY_NO", "", "", "", "", "", "")));
		map.put("PLOT_NO", this.createList(this.createStandardVariable(VariableType.EXPERIMENTAL_DESIGN, TermId.PLOT_NO.getId(), "PLOT_NO",
				"", "", "", "", "", "")));
		map.put("REP_NO", this.createList(this.createStandardVariable(VariableType.EXPERIMENTAL_DESIGN, TermId.REP_NO.getId(), "REP_NO",
				"", "", "", "", "", "")));
		map.put("BLOCK_NO", this.createList(this.createStandardVariable(VariableType.EXPERIMENTAL_DESIGN, TermId.BLOCK_NO.getId(),
				"BLOCK_NO", "", "", "", "", "", "")));

		Mockito.doReturn(map).when(this.ontologyDataManager).getStandardVariablesInProjects(Matchers.anyList(), Matchers.anyString());

	}

	private void initializeGermplasmList() {

		final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
		importedGermplasmList.setImportedGermplasms(this.createImportedGermplasmList());
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

	private List<ImportedGermplasm> createImportedGermplasmList() {
		final List<ImportedGermplasm> importedGermplasmList = new ArrayList<>();
		for (int x = 1; x <= NO_OF_TEST_ENTRIES; x++) {
			importedGermplasmList.add(this.createImportedGermplasm(x));
		}

		return importedGermplasmList;
	}

	private ImportedGermplasm createImportedGermplasm(final int entryNo) {
		final ImportedGermplasm importedGermplasm = new ImportedGermplasm();
		importedGermplasm.setEntryId(entryNo);
		importedGermplasm.setEntryCode(String.valueOf(entryNo));
		importedGermplasm.setDesig("DESIG" + entryNo);
		importedGermplasm.setSource("SOURCE" + entryNo);
		importedGermplasm.setBreedingMethodId(0);
		importedGermplasm.setCheck("");
		importedGermplasm.setGid("");
		importedGermplasm.setCheckId(0);
		importedGermplasm.setCheckName("");
		importedGermplasm.setCross("");
		importedGermplasm.setGnpgs(0);
		importedGermplasm.setGpid1(0);
		importedGermplasm.setGpid2(0);
		importedGermplasm.setGroupName("");
		importedGermplasm.setIndex(0);
		importedGermplasm.setNames(null);

		return importedGermplasm;
	}

	private Map<Integer, StandardVariable> getStandardVariables(final PhenotypicType phenotypicType,
			final List<MeasurementVariable> germplasmFactors) {
		final Map<Integer, StandardVariable> standardVariables = new HashMap<>();

		for (final MeasurementVariable measurementVar : germplasmFactors) {

			if (phenotypicType.getLabelList().contains(measurementVar.getLabel())) {
				final StandardVariable stdVar = this.convertToStandardVariable(measurementVar);
				standardVariables.put(stdVar.getId(), stdVar);
			}

		}

		return standardVariables;
	}

	protected EnvironmentData createEnvironmentData(final int numberOfIntances) {
		final EnvironmentData environmentData = new EnvironmentData();
		final List<Environment> environments = new ArrayList<>();

		for (int x = 0; x < numberOfIntances; x++) {
			final Environment env = new Environment();
			env.setLocationId(x);
			environments.add(env);
		}

		environmentData.setEnvironments(environments);
		environmentData.setNoOfEnvironments(numberOfIntances);
		return environmentData;
	}

	protected ImportedGermplasmMainInfo createImportedGermplasmMainInfo() {
		final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
		importedGermplasmList.setImportedGermplasms(this.createImportedGermplasmList());
		mainInfo.setImportedGermplasmList(importedGermplasmList);
		return mainInfo;
	}

	protected void processEnvironmentData(final EnvironmentData data) {
		for (int i = 0; i < data.getEnvironments().size(); i++) {
			final Map<String, String> values = data.getEnvironments().get(i).getManagementDetailValues();
			if (!values.containsKey(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()))) {
				values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
			} else if (values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())) == null
					|| values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())).isEmpty()) {
				values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
			}
		}
	}

	protected StandardVariable convertToStandardVariable(final MeasurementVariable measurementVar) {
		final StandardVariable stdVar = new StandardVariable();
		stdVar.setId(measurementVar.getTermId());
		stdVar.setProperty(new Term(0, measurementVar.getProperty(), ""));
		stdVar.setScale(new Term(0, measurementVar.getScale(), ""));
		stdVar.setMethod(new Term(0, measurementVar.getMethod(), ""));
		stdVar.setDataType(new Term(measurementVar.getDataTypeId(), measurementVar.getDataType(), ""));
		stdVar.setPhenotypicType(PhenotypicType.getPhenotypicTypeForLabel(measurementVar.getLabel()));
		return stdVar;
	}

	protected StandardVariable createStandardVariable(final PhenotypicType phenotypicType, final int id, final String name,
			final String property, final String scale, final String method, final String dataType, final String storedIn, final String isA) {

		final StandardVariable stdVar =
				new StandardVariable(new Term(0, property, ""), new Term(0, scale, ""), new Term(0, method, ""), new Term(0, dataType, ""),
						new Term(0, isA, ""), phenotypicType);

		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription("");

		return stdVar;
	}

	protected StandardVariable createStandardVariable(final VariableType variableType, final int id, final String name,
			final String property, final String scale, final String method, final String dataType, final String storedIn, final String isA) {

		final StandardVariable stdVar =
				new StandardVariable(new Term(0, property, ""), new Term(0, scale, ""), new Term(0, method, ""), new Term(0, dataType, ""),
						new Term(0, isA, ""), null);

		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription("");

		final Set<VariableType> variableTypes = new HashSet<>();
		variableTypes.add(variableType);

		stdVar.setVariableTypes(variableTypes);

		return stdVar;
	}

	private DesignImportData createDesignImportData() {

		final DesignImportData designImportData = new DesignImportData();

		designImportData.setMappedHeaders(this.createTestMappedHeadersForDesignImportData());
		designImportData.setCsvData(this.createTestCsvDataForDesignImportData());

		return designImportData;

	}

	private Map<PhenotypicType, List<DesignHeaderItem>> createTestMappedHeadersForDesignImportData() {

		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = new HashMap<>();

		final List<DesignHeaderItem> trialEvironmentItems = new ArrayList<>();
		trialEvironmentItems.add(this.createDesignHeaderItem(PhenotypicType.TRIAL_ENVIRONMENT, TermId.TRIAL_INSTANCE_FACTOR.getId(),
				"TRIAL_INSTANCE", 0));
		trialEvironmentItems.add(this.createDesignHeaderItem(PhenotypicType.TRIAL_ENVIRONMENT, TermId.SITE_NAME.getId(), "SITE_NAME", 1));

		final List<DesignHeaderItem> germplasmItems = new ArrayList<>();
		germplasmItems.add(this.createDesignHeaderItem(PhenotypicType.GERMPLASM, TermId.ENTRY_NO.getId(), "ENTRY_NO", 2));

		final List<DesignHeaderItem> trialDesignItems = new ArrayList<>();
		trialDesignItems.add(this.createDesignHeaderItem(PhenotypicType.TRIAL_DESIGN, TermId.PLOT_NO.getId(), "PLOT_NO", 3));
		trialDesignItems.add(this.createDesignHeaderItem(PhenotypicType.TRIAL_DESIGN, TermId.REP_NO.getId(), "REP_NO", 4));
		trialDesignItems.add(this.createDesignHeaderItem(PhenotypicType.TRIAL_DESIGN, TermId.BLOCK_NO.getId(), "BLOCK_NO", 5));

		final List<DesignHeaderItem> variateItems = new ArrayList<>();

		mappedHeaders.put(PhenotypicType.TRIAL_ENVIRONMENT, trialEvironmentItems);
		mappedHeaders.put(PhenotypicType.GERMPLASM, germplasmItems);
		mappedHeaders.put(PhenotypicType.TRIAL_DESIGN, trialDesignItems);
		mappedHeaders.put(PhenotypicType.VARIATE, variateItems);

		return mappedHeaders;

	}

	private Map<Integer, List<String>> createTestCsvDataForDesignImportData() {

		final Map<Integer, List<String>> csvData = new HashMap<>();

		// The first row is the header
		csvData.put(0, this.createListOfString("TRIAL_INSTANCE", "SITE_NAME", "ENTRY_NO", "PLOT_NO", "REP_NO", "BLOCK_NO"));

		// csv data
		csvData.put(1, this.createListOfString("1", "Laguna", "1", "1", "1", "1"));
		csvData.put(2, this.createListOfString("1", "Laguna", "2", "2", "1", "1"));
		csvData.put(3, this.createListOfString("1", "Laguna", "3", "3", "1", "1"));
		csvData.put(4, this.createListOfString("1", "Laguna", "4", "4", "1", "1"));
		csvData.put(5, this.createListOfString("1", "Laguna", "5", "5", "1", "1"));
		csvData.put(6, this.createListOfString("2", "Bicol", "1", "6", "1", "1"));
		csvData.put(7, this.createListOfString("2", "Bicol", "2", "7", "1", "1"));
		csvData.put(8, this.createListOfString("2", "Bicol", "3", "8", "1", "2"));
		csvData.put(9, this.createListOfString("2", "Bicol", "4", "9", "1", "2"));
		csvData.put(10, this.createListOfString("2", "Bicol", "5", "10", "1", "2"));
		csvData.put(11, this.createListOfString("3", "Bulacan", "1", "11", "1", "2"));
		csvData.put(12, this.createListOfString("3", "Bulacan", "2", "12", "1", "2"));
		csvData.put(13, this.createListOfString("3", "Bulacan", "3", "13", "1", "2"));
		csvData.put(14, this.createListOfString("3", "Bulacan", "4", "14", "1", "2"));
		csvData.put(15, this.createListOfString("3", "Bulacan", "5", "15", "2", "3"));

		return csvData;

	}

	private List<String> createListOfString(final String... listData) {
		final List<String> list = new ArrayList<>();
		for (final String data : listData) {
			list.add(data);
		}
		return list;
	}

	private DesignHeaderItem createDesignHeaderItem(final PhenotypicType phenotypicType, final int termId, final String headerName,
			final int columnIndex) {
		final DesignHeaderItem designHeaderItem = this.createDesignHeaderItem(termId, headerName, columnIndex);
		designHeaderItem.setVariable(this.createStandardVariable(phenotypicType, termId, headerName, "", "", "", "", "", ""));
		return designHeaderItem;
	}

	private DesignHeaderItem createDesignHeaderItem(final int termId, final String headerName, final int columnIndex) {
		final DesignHeaderItem designHeaderItem = new DesignHeaderItem();
		designHeaderItem.setId(termId);
		designHeaderItem.setName(headerName);
		designHeaderItem.setColumnIndex(columnIndex);
		return designHeaderItem;
	}

	private List<DesignHeaderItem> createUnmappedHeaders() {
		final List<DesignHeaderItem> items = new ArrayList<>();

		items.add(this.createDesignHeaderItem(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", 0));
		items.add(this.createDesignHeaderItem(TermId.SITE_NAME.getId(), "SITE_NAME", 1));
		items.add(this.createDesignHeaderItem(TermId.ENTRY_NO.getId(), "ENTRY_NO", 2));
		items.add(this.createDesignHeaderItem(TermId.PLOT_NO.getId(), "PLOT_NO", 3));
		items.add(this.createDesignHeaderItem(TermId.REP_NO.getId(), "REP_NO", 4));
		items.add(this.createDesignHeaderItem(TermId.BLOCK_NO.getId(), "BLOCK_NO", 5));

		return items;
	}
}
