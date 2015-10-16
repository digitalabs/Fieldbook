
package com.efficio.fieldbook.web.importdesign.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
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

		WorkbookDataUtil.setTestWorkbook(null);

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

	@Test
	public void testGenerateDesignForOneInstanceOnly() throws DesignValidationException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		Mockito.doReturn(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo()).when(this.userSelection)
				.getImportedGermplasmMainInfo();

		final EnvironmentData environmentData = DesignImportTestDataInitializer.createEnvironmentData(1);

		DesignImportTestDataInitializer.processEnvironmentData(environmentData);

		final List<MeasurementRow> measurements = this.service.generateDesign(workbook, this.designImportData, environmentData, true);

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

		final List<MeasurementRow> measurements = this.service.generateDesign(workbook, this.designImportData, environmentData, true);

		Assert.assertEquals("The 3 trial instances have 6 observations", 6, measurements.size());

	}

	@Test
	public void testGenerateDesignForNursery() throws DesignValidationException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(5, StudyType.N);

		final EnvironmentData environmentData = DesignImportTestDataInitializer.createEnvironmentData(1);
		DesignImportTestDataInitializer.processEnvironmentData(environmentData);

		final List<MeasurementRow> measurements = this.service.generateDesign(workbook, this.designImportData, environmentData, true);

		Assert.assertEquals("The first trial instance only has 5 observations", DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES,
				measurements.size());

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
