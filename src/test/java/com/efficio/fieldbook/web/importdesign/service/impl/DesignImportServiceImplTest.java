
package com.efficio.fieldbook.web.importdesign.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
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
import com.efficio.fieldbook.web.data.initializer.DesignImportDataInitializer;
import com.efficio.fieldbook.web.data.initializer.ImportedGermplasmMainInfoInitializer;
import com.efficio.fieldbook.web.importdesign.service.impl.DesignImportServiceImpl;
import com.efficio.fieldbook.web.trial.bean.Environment;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.efficio.fieldbook.web.util.parsing.DesignImportParser;

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

		WorkbookDataUtil.setTestWorkbook(null);

	}

	@Test
	public void testAddGermplasmDetailsToDataList() throws FileParsingException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final List<ImportedGermplasm> importedGermplasm = ImportedGermplasmMainInfoInitializer.createImportedGermplasmList();
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

		Mockito.doReturn(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo()).when(this.userSelection)
				.getImportedGermplasmMainInfo();

		final EnvironmentData environmentData = this.createEnvironmentData(1);

		this.processEnvironmentData(environmentData);

		final List<MeasurementRow> measurements = this.service.generateDesign(workbook, this.designImportData, environmentData, true);

		// trigger the addition of variates by setting the Operation to 'ADD' or
		// 'UPDATE'
		for (final MeasurementVariable measurementVariable : workbook.getVariates()) {
			measurementVariable.setOperation(Operation.ADD);
		}

		this.service.addVariatesToMeasurementRows(workbook, measurements);

		Assert.assertEquals("The size of the data list should be 14 since 2 variates are added", 14, measurements.get(0).getDataList()
				.size());

	}

	@Test
	public void testAreTrialInstancesMatchTheSelectedEnvironments() throws MiddlewareException, DesignValidationException {

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
	public void testGenerateDesignForOneInstanceOnly() throws MiddlewareException, DesignValidationException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		Mockito.doReturn(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo()).when(this.userSelection)
				.getImportedGermplasmMainInfo();

		final EnvironmentData environmentData = this.createEnvironmentData(1);

		this.processEnvironmentData(environmentData);

		final List<MeasurementRow> measurements = this.service.generateDesign(workbook, this.designImportData, environmentData, true);

		Assert.assertEquals("The first trial instance has only 5 observations", DesignImportDataInitializer.NO_OF_TEST_ENTRIES,
				measurements.size());

	}

	@Test
	public void testGenerateDesignForThreeInstances() throws MiddlewareException, DesignValidationException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		Mockito.doReturn(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo()).when(this.userSelection)
				.getImportedGermplasmMainInfo();

		final EnvironmentData environmentData = this.createEnvironmentData(3);

		this.processEnvironmentData(environmentData);

		final List<MeasurementRow> measurements = this.service.generateDesign(workbook, this.designImportData, environmentData, true);

		Assert.assertEquals("The 3 trial instances have 6 observations", 6, measurements.size());

	}

	@Test
	public void testGenerateDesignForNursery() throws DesignValidationException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(5, StudyType.N);

		final EnvironmentData environmentData = this.createEnvironmentData(1);
		this.processEnvironmentData(environmentData);

		final List<MeasurementRow> measurements = this.service.generateDesign(workbook, this.designImportData, environmentData, true);

		Assert.assertEquals("The first trial instance only has 5 observations", DesignImportDataInitializer.NO_OF_TEST_ENTRIES,
				measurements.size());

	}

	@Test
	public void testGetDesignMeasurementVariables() throws MiddlewareException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Set<MeasurementVariable> result = this.service.getDesignMeasurementVariables(workbook, this.designImportData, true);

		Assert.assertEquals("The total number of Factors and Variates in workbook is 14", 14, result.size());
	}

	@Test
	public void testGetDesignMeasurementVariablesNotPreview() throws MiddlewareException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Set<MeasurementVariable> result = this.service.getDesignMeasurementVariables(workbook, this.designImportData, false);

		// If NOT in PREVIEW mode, the method will remove the trial environment factors in the list except for trial instance. This is
		// because the actual measurements/observations that will be generated from import should not contain trial environment factors.
		Assert.assertEquals("The total number of Factors and Variates (less the trial environments) in workbook is 13", 13, result.size());
	}

	@Test
	public void testGetDesignRequiredMeasurementVariable() throws MiddlewareException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Set<MeasurementVariable> result = this.service.getDesignRequiredMeasurementVariable(workbook, this.designImportData);

		Assert.assertEquals("The total number of Design Factors is 4", 4, result.size());

	}

	@Test
	public void testGetDesignRequiredStandardVariables() throws MiddlewareException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Set<StandardVariable> result = this.service.getDesignRequiredStandardVariables(workbook, this.designImportData);

		Assert.assertEquals("The total number of Design Factors is 4", 4, result.size());

	}

	@Test
	public void testGroupCsvRowsIntoTrialInstance() throws MiddlewareException, DesignValidationException {

		final DesignHeaderItem trialInstanceHeaderItem =
				this.service.validateIfStandardVariableExists(this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId()
						.get(PhenotypicType.TRIAL_ENVIRONMENT), "design.import.error.trial.is.required", TermId.TRIAL_INSTANCE_FACTOR);

		final Map<String, Map<Integer, List<String>>> result =
				this.service.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, this.designImportData.getCsvData());

		Assert.assertEquals("The total number of trial instances in file is 3", 3, result.size());
		Assert.assertEquals("Each trial instance in file has 5 observations", DesignImportDataInitializer.NO_OF_TEST_ENTRIES,
				result.get("1").size());
		Assert.assertEquals("Each trial instance in file has 5 observations", DesignImportDataInitializer.NO_OF_TEST_ENTRIES,
				result.get("2").size());
		Assert.assertEquals("Each trial instance in file has 5 observations", DesignImportDataInitializer.NO_OF_TEST_ENTRIES,
				result.get("3").size());

	}

	@Test
	public void testGetMeasurementVariablesFromDataFile() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Set<MeasurementVariable> returnValue = this.service.getMeasurementVariablesFromDataFile(workbook, this.designImportData);

		Assert.assertEquals("The test data only contains 7 Measurement Variables", 7, returnValue.size());

	}

	@Test
	public void testValidateIfStandardVariableExists() throws MiddlewareException {
		try {

			this.service.validateIfStandardVariableExists(this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId()
					.get(PhenotypicType.TRIAL_ENVIRONMENT), "design.import.error.trial.is.required", TermId.TRIAL_INSTANCE_FACTOR);

		} catch (final DesignValidationException e) {

			Assert.fail("The logic did not detect that the trial number exist");

		}

	}

	@Test
	public void testValidateIfStandardVariableExistsTrialInstanceDoNotExist() throws MiddlewareException {

		try {

			this.service.validateIfStandardVariableExists(this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId()
					.get(PhenotypicType.GERMPLASM), "design.import.error.trial.is.required", TermId.TRIAL_INSTANCE_FACTOR);

			Assert.fail("The logic should detect that the trial number exist");

		} catch (final DesignValidationException e) {

		}

	}

	private void initializeDesignImportData() {

		try {
			Mockito.when(this.designImportParser.parseFile(this.multiPartFile)).thenReturn(
					DesignImportDataInitializer.createDesignImportData());
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

	private void initializeOntologyService() throws MiddlewareException {

		Mockito.doReturn(
				DesignImportDataInitializer.createStandardVariable(PhenotypicType.GERMPLASM, TermId.ENTRY_NO.getId(), "ENTRY_NO", "", "",
						"", DesignImportDataInitializer.NUMERIC_VARIABLE, "N", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.ENTRY_NO.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				DesignImportDataInitializer.createStandardVariable(PhenotypicType.GERMPLASM, TermId.GID.getId(), "GID", "", "", "",
						DesignImportDataInitializer.NUMERIC_VARIABLE, "N", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.GID.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				DesignImportDataInitializer.createStandardVariable(PhenotypicType.GERMPLASM, TermId.DESIG.getId(), "DESIG", "", "", "",
						DesignImportDataInitializer.CHARACTER_VARIABLE, "C", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.DESIG.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				DesignImportDataInitializer.createStandardVariable(PhenotypicType.GERMPLASM, TermId.ENTRY_TYPE.getId(), "ENTRY_TYPE", "",
						"", "", DesignImportDataInitializer.CHARACTER_VARIABLE, "C", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.ENTRY_TYPE.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				DesignImportDataInitializer.createStandardVariable(PhenotypicType.GERMPLASM, TermId.CROSS.getId(), "CROSS", "", "", "",
						DesignImportDataInitializer.CHARACTER_VARIABLE, "C", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.CROSS.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				DesignImportDataInitializer.createStandardVariable(PhenotypicType.GERMPLASM, TermId.ENTRY_CODE.getId(), "ENTRY_CODE", "",
						"", "", DesignImportDataInitializer.CHARACTER_VARIABLE, "C", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.ENTRY_CODE.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				DesignImportDataInitializer.createStandardVariable(PhenotypicType.GERMPLASM, TermId.GERMPLASM_SOURCE.getId(),
						"GERMPLASM_SOURCE", "", "", "", DesignImportDataInitializer.CHARACTER_VARIABLE, "C", "", ""))
				.when(this.ontologyService).getStandardVariable(TermId.GERMPLASM_SOURCE.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				DesignImportDataInitializer.createStandardVariable(PhenotypicType.GERMPLASM, TermId.SEED_SOURCE.getId(), "SEED_SOURCE", "",
						"", "", DesignImportDataInitializer.CHARACTER_VARIABLE, "C", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.SEED_SOURCE.getId(), DesignImportServiceImplTest.PROGRAM_UUID);

		Mockito.doReturn(
				DesignImportDataInitializer.createStandardVariable(PhenotypicType.VARIATE, DesignImportServiceImplTest.GYLD_TERMID, "GYLD",
						"", "", "", DesignImportDataInitializer.NUMERIC_VARIABLE, "N", "", "")).when(this.ontologyService)
				.getStandardVariable(DesignImportServiceImplTest.GYLD_TERMID, DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(
				DesignImportDataInitializer.createStandardVariable(PhenotypicType.VARIATE, DesignImportServiceImplTest.CHALK_PCT_TERMID,
						"CHALK_PCT", "", "", "", DesignImportDataInitializer.NUMERIC_VARIABLE, "N", "", "")).when(this.ontologyService)
				.getStandardVariable(DesignImportServiceImplTest.CHALK_PCT_TERMID, DesignImportServiceImplTest.PROGRAM_UUID);

		final Property prop = new Property();
		final Term term = new Term();
		term.setId(0);
		prop.setTerm(term);
		Mockito.doReturn(prop).when(this.ontologyService).getProperty(Matchers.anyString());

		final Map<String, List<StandardVariable>> map = new HashMap<>();

		map.put("TRIAL_INSTANCE", this.createList(DesignImportDataInitializer.createStandardVariable(VariableType.ENVIRONMENT_DETAIL,
				TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", "", "", "", "", "", "")));
		map.put("SITE_NAME", this.createList(DesignImportDataInitializer.createStandardVariable(VariableType.ENVIRONMENT_DETAIL,
				TermId.TRIAL.getId(), "SITE_NAME", "", "", "", "", "", "")));
		map.put("ENTRY_NO",
				this.createList(DesignImportDataInitializer.createStandardVariable(VariableType.GERMPLASM_DESCRIPTOR,
						TermId.ENTRY_NO.getId(), "ENTRY_NO", "", "", "", "", "", "")));
		map.put("PLOT_NO", this.createList(DesignImportDataInitializer.createStandardVariable(VariableType.EXPERIMENTAL_DESIGN,
				TermId.PLOT_NO.getId(), "PLOT_NO", "", "", "", "", "", "")));
		map.put("REP_NO", this.createList(DesignImportDataInitializer.createStandardVariable(VariableType.EXPERIMENTAL_DESIGN,
				TermId.REP_NO.getId(), "REP_NO", "", "", "", "", "", "")));
		map.put("BLOCK_NO",
				this.createList(DesignImportDataInitializer.createStandardVariable(VariableType.EXPERIMENTAL_DESIGN,
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

	private List<DesignHeaderItem> createUnmappedHeaders() {
		final List<DesignHeaderItem> items = new ArrayList<>();

		items.add(DesignImportDataInitializer.createDesignHeaderItem(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", 0));
		items.add(DesignImportDataInitializer.createDesignHeaderItem(TermId.SITE_NAME.getId(), "SITE_NAME", 1));
		items.add(DesignImportDataInitializer.createDesignHeaderItem(TermId.ENTRY_NO.getId(), "ENTRY_NO", 2));
		items.add(DesignImportDataInitializer.createDesignHeaderItem(TermId.PLOT_NO.getId(), "PLOT_NO", 3));
		items.add(DesignImportDataInitializer.createDesignHeaderItem(TermId.REP_NO.getId(), "REP_NO", 4));
		items.add(DesignImportDataInitializer.createDesignHeaderItem(TermId.BLOCK_NO.getId(), "BLOCK_NO", 5));
		items.add(DesignImportDataInitializer.createDesignHeaderItem(DesignImportDataInitializer.AFLAVER_5_ID, "AflavER_1_5", 6));

		return items;
	}

	private List<DesignHeaderItem> createUnmappedHeadersWithWrongCase() {
		final List<DesignHeaderItem> items = new ArrayList<>();

		items.add(DesignImportDataInitializer.createDesignHeaderItem(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TriAL_iNSTANCE", 0));
		items.add(DesignImportDataInitializer.createDesignHeaderItem(TermId.SITE_NAME.getId(), "SiTe_NaME", 1));
		items.add(DesignImportDataInitializer.createDesignHeaderItem(TermId.ENTRY_NO.getId(), "ENtRY_nO", 2));
		items.add(DesignImportDataInitializer.createDesignHeaderItem(TermId.PLOT_NO.getId(), "PLoT_NO", 3));
		items.add(DesignImportDataInitializer.createDesignHeaderItem(TermId.REP_NO.getId(), "ReP_nO", 4));
		items.add(DesignImportDataInitializer.createDesignHeaderItem(TermId.BLOCK_NO.getId(), "BLoCK_nO", 5));

		return items;
	}
}
