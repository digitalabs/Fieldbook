
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

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
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
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

public class DesignImportServiceImplTest {

	@Spy
	@Resource
	private DesignImportParser parser;

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

	private final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

	@Before
	public void setUp() throws MiddlewareException, FileParsingException, URISyntaxException {

		MockitoAnnotations.initMocks(this);

		this.initializeOntologyService();
		this.initializeDesignImportData();
		this.initializeGermplasmList();

	}

	@Test
	public void testAddGermplasmDetailsToDataList() throws FileParsingException {

		List<ImportedGermplasm> importedGermplasm = this.createImportedGermplasmList();
		Map<Integer, StandardVariable> germplasmStandardVariables =
				this.getStandardVariables(PhenotypicType.GERMPLASM, this.workbook.getFactors());
		List<MeasurementData> dataList = new ArrayList<>();

		this.service.addGermplasmDetailsToDataList(importedGermplasm, germplasmStandardVariables, dataList, 1);

		Assert.assertEquals("The added MeasurementData should Match the germplasm Standard Variables", germplasmStandardVariables.size(),
				dataList.size());

		ImportedGermplasm germplasmEntry = importedGermplasm.get(0);

		for (MeasurementData measurementData : dataList) {

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

		Mockito.doReturn(this.createImportedGermplasmMainInfo()).when(this.userSelection).getImportedGermplasmMainInfo();

		EnvironmentData environmentData = this.createEnvironmentData(1);

		this.processEnvironmentData(environmentData);
		this.createDesignHeaderItemMap(this.designImportData);

		List<MeasurementRow> measurements = this.service.generateDesign(this.workbook, this.designImportData, environmentData);

		// trigger the addition of variates by setting the Operation to 'ADD' or 'UPDATE'
		for (MeasurementVariable measurementVariable : this.workbook.getVariates()) {
			measurementVariable.setOperation(Operation.ADD);
		}

		this.service.addVariatesToMeasurementRows(this.workbook, measurements);

		Assert.assertEquals("The size of the data list should be 12 since 2 variates are added", 12, measurements.get(0).getDataList()
				.size());

	}

	@Test
	public void testAreTrialInstancesMatchTheSelectedEnvironments() throws MiddlewareException {

		this.createDesignHeaderItemMap(this.designImportData);

		Assert.assertFalse("Should be false because the no of environments in trials don't match the no of trials in the design file",
				this.service.areTrialInstancesMatchTheSelectedEnvironments(1, this.designImportData));
		Assert.assertTrue("Should be true because the no of environments in trials match the no of trials in the design file",
				this.service.areTrialInstancesMatchTheSelectedEnvironments(3, this.designImportData));
	}

	@Test
	public void testCategorizeHeadersByPhenotype() throws MiddlewareException {

		Map<PhenotypicType, List<DesignHeaderItem>> result =
				this.service.categorizeHeadersByPhenotype(this.designImportData.getUnmappedHeaders());

		Assert.assertEquals("Total No of TRIAL in file is 2", 2, result.get(PhenotypicType.TRIAL_ENVIRONMENT).size());
		Assert.assertEquals("Total No of GERMPLASM FACTOR in file is 1", 1, result.get(PhenotypicType.GERMPLASM).size());
		Assert.assertEquals("Total No of DESIGN FACTOR in file is 3", 3, result.get(PhenotypicType.TRIAL_DESIGN).size());
		Assert.assertEquals("Total No of VARIATE in file is 0", 0, result.get(PhenotypicType.VARIATE).size());

	}

	@Test
	public void testConvertToStandardVariables() {

		Map<Integer, StandardVariable> result = this.service.convertToStandardVariables(this.workbook.getGermplasmFactors());

		Assert.assertEquals(result.size(), this.workbook.getGermplasmFactors().size());

		for (MeasurementVariable measurementVar : this.workbook.getFactors()) {
			StandardVariable stdVar = result.get(measurementVar.getTermId());
			if (stdVar != null) {
				Assert.assertTrue(stdVar.getId() == measurementVar.getTermId());
				Assert.assertTrue(stdVar.getPhenotypicType().getLabelList().contains(measurementVar.getLabel()));

			}

		}

	}

	@Test
	public void testCreateMeasurementData() {

		MeasurementVariable measurementVariable = this.workbook.getFactors().get(0);
		MeasurementData data = this.service.createMeasurementData(measurementVariable, "1");

		Assert.assertEquals("1", data.getValue());
		Assert.assertEquals(measurementVariable, data.getMeasurementVariable());

	}

	@Test
	public void testExtractTrialInstancesFromEnvironmentData() {

		EnvironmentData environmentData = this.createEnvironmentData(5);
		this.processEnvironmentData(environmentData);

		Set<String> result = this.service.extractTrialInstancesFromEnvironmentData(environmentData);

		Assert.assertEquals(5, result.size());

	}

	@Test
	public void testFilterDesignHeaderItemsByTermId() throws MiddlewareException {

		this.createDesignHeaderItemMap(this.designImportData);

		List<DesignHeaderItem> headerDesignItems = this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT);
		DesignHeaderItem result = this.service.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR, headerDesignItems);

		Assert.assertNotNull(result);

	}

	@Test
	public void testGenerateDesignForOneInstanceOnly() throws MiddlewareException, DesignValidationException {

		Mockito.doReturn(this.createImportedGermplasmMainInfo()).when(this.userSelection).getImportedGermplasmMainInfo();

		EnvironmentData environmentData = this.createEnvironmentData(1);

		this.processEnvironmentData(environmentData);
		this.createDesignHeaderItemMap(this.designImportData);

		List<MeasurementRow> measurements = this.service.generateDesign(this.workbook, this.designImportData, environmentData);

		Assert.assertEquals("The first trial instance has only 28 observations", 28, measurements.size());

	}

	@Test
	public void testGenerateDesignForThreeInstances() throws MiddlewareException, DesignValidationException {

		Mockito.doReturn(this.createImportedGermplasmMainInfo()).when(this.userSelection).getImportedGermplasmMainInfo();

		EnvironmentData environmentData = this.createEnvironmentData(3);

		this.processEnvironmentData(environmentData);
		this.createDesignHeaderItemMap(this.designImportData);

		List<MeasurementRow> measurements = this.service.generateDesign(this.workbook, this.designImportData, environmentData);

		Assert.assertEquals("The 3 trial instances have 84 observations", 84, measurements.size());

	}

	@Test
	public void testGetDesignMeasurementVariables() throws MiddlewareException {

		this.createDesignHeaderItemMap(this.designImportData);
		Set<MeasurementVariable> result = this.service.getDesignMeasurementVariables(this.workbook, this.designImportData);

		Assert.assertEquals("The total number of Factors and Variates in workbook is 12", 12, result.size());
	}

	@Test
	public void testGetDesignRequiredMeasurementVariable() throws MiddlewareException {

		this.createDesignHeaderItemMap(this.designImportData);
		Set<MeasurementVariable> result = this.service.getDesignRequiredMeasurementVariable(this.workbook, this.designImportData);

		Assert.assertEquals("The total number of Design Factors is 3", 3, result.size());

	}

	@Test
	public void testGetDesignRequiredStandardVariables() throws MiddlewareException {

		this.createDesignHeaderItemMap(this.designImportData);
		Set<StandardVariable> result = this.service.getDesignRequiredStandardVariables(this.workbook, this.designImportData);

		Assert.assertEquals("The total number of Design Factors is 3", 3, result.size());

	}

	@Test
	public void testGroupCsvRowsIntoTrialInstance() throws MiddlewareException {

		this.createDesignHeaderItemMap(this.designImportData);

		DesignHeaderItem trialInstanceHeaderItem =
				this.service.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR,
						this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));

		Map<String, Map<Integer, List<String>>> result =
				this.service.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, this.designImportData.getCsvData());

		Assert.assertEquals("The total number of trial instances in file is 3", 3, result.size());
		Assert.assertEquals("The each trial instance in file has 28 observations", 28, result.get("1").size());
		Assert.assertEquals("The each trial instance in file has 28 observations", 28, result.get("2").size());
		Assert.assertEquals("The each trial instance in file has 28 observations", 28, result.get("3").size());

	}

	@Test
	public void testValidateDesignData() throws MiddlewareException {

		this.createDesignHeaderItemMap(this.designImportData);

		try {

			this.service.validateDesignData(this.designImportData);

		} catch (DesignValidationException e) {

			Assert.fail("The data should pass the validateDesignData test");
		}

	}

	@Test
	public void testValidateEntryNoMustBeUniquePerInstance() throws MiddlewareException {

		this.createDesignHeaderItemMap(this.designImportData);

		try {

			DesignHeaderItem trialInstanceHeaderItem =
					this.service.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR, this.designImportData.getMappedHeaders()
							.get(PhenotypicType.TRIAL_ENVIRONMENT));
			DesignHeaderItem entryNoHeaderItem =
					this.service.filterDesignHeaderItemsByTermId(TermId.ENTRY_NO,
							this.designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));

			Map<String, Map<Integer, List<String>>> data =
					this.service.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, this.designImportData.getCsvData());

			this.service.validateEntryNoMustBeUniquePerInstance(entryNoHeaderItem, data);

		} catch (DesignValidationException e) {

			Assert.fail("The list should pass the validateEntryNoMustBeUniquePerInstance test");
		}

	}

	@Test
	public void testValidateEntryNoMustBeUniquePerInstanceEntryNoIsNotUnique() throws MiddlewareException {

		this.createDesignHeaderItemMap(this.designImportData);

		DesignHeaderItem trialInstanceHeaderItem =
				this.service.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR,
						this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		DesignHeaderItem entryNoHeaderItem =
				this.service.filterDesignHeaderItemsByTermId(TermId.ENTRY_NO,
						this.designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));

		Map<Integer, List<String>> csvData = this.designImportData.getCsvData();
		csvData.get(0).set(entryNoHeaderItem.getColumnIndex(), "1");
		csvData.get(1).set(entryNoHeaderItem.getColumnIndex(), "1");
		csvData.get(2).set(entryNoHeaderItem.getColumnIndex(), "1");

		Map<String, Map<Integer, List<String>>> data = this.service.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, csvData);

		try {

			this.service.validateEntryNoMustBeUniquePerInstance(entryNoHeaderItem, data);

			// Assert.fail("The list shouldn't pass the validateEntryNoMustBeUniquePerInstance test");

		} catch (DesignValidationException e) {

		}
	}

	@Test
	public void testValidateGermplasmEntriesFromShouldMatchTheGermplasmList() {

		Set<String> entryNumbers = new HashSet<>();
		for (int x = 1; x <= 14; x++) {
			entryNumbers.add(String.valueOf(x));
		}

		try {

			this.service.validateGermplasmEntriesFromShouldMatchTheGermplasmList(entryNumbers);

		} catch (DesignValidationException e) {

			Assert.fail("The data should pass the validateGermplasmEntriesFromShouldMatchTheGermplasmList test");
		}

	}

	@Test
	public void testValidateIfEntryNumberExistsNoEntryNumber() throws MiddlewareException {

		try {

			this.createDesignHeaderItemMap(this.designImportData);

			this.service.validateIfEntryNumberExists(this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));

			Assert.fail("The logic did not detect that the entry number doesn't exist");

		} catch (DesignValidationException e) {

		}

	}

	@Test
	public void testValidateIfEntryNumberExistsWithEntryNumber() throws MiddlewareException {

		try {

			this.createDesignHeaderItemMap(this.designImportData);

			this.service.validateIfEntryNumberExists(this.designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));

		} catch (DesignValidationException e) {

			Assert.fail("The logic did not detect that the entry number doesn't exist");

		}

	}

	@Test
	public void testValidateIfPlotNumberExistsNoPlotNumber() throws MiddlewareException {

		try {

			this.createDesignHeaderItemMap(this.designImportData);

			this.service.validateIfPlotNumberExists(this.designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));

			Assert.fail("The logic did not detect that the plot number do not exist");

		} catch (DesignValidationException e) {

		}

	}

	@Test
	public void testValidateIfPlotNumberExistsWithPlotNumber() throws MiddlewareException {

		try {

			this.createDesignHeaderItemMap(this.designImportData);

			this.service.validateIfPlotNumberExists(this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN));

		} catch (DesignValidationException e) {

			Assert.fail("The logic did not detect that the plot number exist");

		}

	}

	@Test
	public void testValidateIfPlotNumberIsUnique() throws MiddlewareException {

		this.createDesignHeaderItemMap(this.designImportData);

		DesignHeaderItem trialInstanceHeaderItem =
				this.service.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR,
						this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		Map<String, Map<Integer, List<String>>> csvMap =
				this.service.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, this.designImportData.getCsvData());

		try {

			this.service.validateIfPlotNumberIsUniquePerInstance(this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN),
					csvMap);

		} catch (DesignValidationException e) {

			Assert.fail("The list should pass the validateIfPlotNumberIsUnique test");
		}

	}

	@Test
	public void testValidateIfPlotNumberIsUniquePerInstance() throws MiddlewareException {

		this.createDesignHeaderItemMap(this.designImportData);

		DesignHeaderItem trialInstanceHeaderItem =
				this.service.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR,
						this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		DesignHeaderItem plotNoHeaderItem =
				this.service.filterDesignHeaderItemsByTermId(TermId.PLOT_NO,
						this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN));

		Map<Integer, List<String>> csvData = this.designImportData.getCsvData();
		csvData.get(0).set(plotNoHeaderItem.getColumnIndex(), "1");
		csvData.get(1).set(plotNoHeaderItem.getColumnIndex(), "1");
		csvData.get(2).set(plotNoHeaderItem.getColumnIndex(), "1");

		Map<String, Map<Integer, List<String>>> csvMap = this.service.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, csvData);

		try {

			this.service.validateIfPlotNumberIsUniquePerInstance(this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN),
					csvMap);

			Assert.fail("The list shouldn't pass the validateIfPlotNumberIsUniquePerInstance test");

		} catch (DesignValidationException e) {

		}

	}

	@Test
	public void testValidateIfTrialFactorExists() throws MiddlewareException {

		try {

			this.createDesignHeaderItemMap(this.designImportData);

			this.service.validateIfTrialFactorExists(this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));

		} catch (DesignValidationException e) {

			Assert.fail("The logic did not detect that the trial number exist");

		}

	}

	@Test
	public void testValidateIfTrialFactorExistsTrialInstanceDoNotExist() throws MiddlewareException {

		try {

			this.createDesignHeaderItemMap(this.designImportData);

			this.service.validateIfTrialFactorExists(this.designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));

			Assert.fail("The logic should detect that the trial number exist");

		} catch (DesignValidationException e) {

		}

	}

	private void initializeDesignImportData() throws URISyntaxException, FileParsingException {

		File file = new File(ClassLoader.getSystemClassLoader().getResource("Design_Import_Template.csv").toURI());
		Mockito.doReturn(file).when(this.parser).storeAndRetrieveFile(this.multiPartFile);
		this.designImportData = this.parser.parseFile(this.multiPartFile);

	}

	private void initializeOntologyService() throws MiddlewareException {

		Mockito.doReturn(this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.ENTRY_NO.getId(), "ENTRY_NO", "", "", "", "", "", ""))
				.when(this.ontologyService).getStandardVariable(TermId.ENTRY_NO.getId(),contextUtil.getCurrentProgramUUID());
		Mockito.doReturn(this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.GID.getId(), "GID", "", "", "", "", "", ""))
				.when(this.ontologyService).getStandardVariable(TermId.GID.getId(),contextUtil.getCurrentProgramUUID());
		Mockito.doReturn(this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.DESIG.getId(), "DESIG", "", "", "", "", "", ""))
				.when(this.ontologyService).getStandardVariable(TermId.DESIG.getId(),contextUtil.getCurrentProgramUUID());
		Mockito.doReturn(
				this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.ENTRY_TYPE.getId(), "ENTRY_TYPE", "", "", "", "", "", ""))
				.when(this.ontologyService).getStandardVariable(TermId.ENTRY_TYPE.getId(),contextUtil.getCurrentProgramUUID());
		Mockito.doReturn(this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.CROSS.getId(), "CROSS", "", "", "", "", "", ""))
				.when(this.ontologyService).getStandardVariable(TermId.CROSS.getId(),contextUtil.getCurrentProgramUUID());
		Mockito.doReturn(
				this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.ENTRY_CODE.getId(), "ENTRY_CODE", "", "", "", "", "", ""))
				.when(this.ontologyService).getStandardVariable(TermId.ENTRY_CODE.getId(),contextUtil.getCurrentProgramUUID());
		Mockito.doReturn(
				this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.GERMPLASM_SOURCE.getId(), "GERMPLASM_SOURCE", "", "", "", "",
						"", "")).when(this.ontologyService).getStandardVariable(TermId.GERMPLASM_SOURCE.getId(),contextUtil.getCurrentProgramUUID());
		Mockito.doReturn(
				this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.SEED_SOURCE.getId(), "SEED_SOURCE", "", "", "", "", "", ""))
				.when(this.ontologyService).getStandardVariable(TermId.SEED_SOURCE.getId(),contextUtil.getCurrentProgramUUID());

		Mockito.doReturn(this.createStandardVariable(PhenotypicType.VARIATE, 18000, "GYLD", "", "", "", "N", "", ""))
				.when(this.ontologyService).getStandardVariable(18000,contextUtil.getCurrentProgramUUID());
		Mockito.doReturn(this.createStandardVariable(PhenotypicType.VARIATE, 22768, "CHALK_PCT", "", "", "N", "", "", ""))
				.when(this.ontologyService).getStandardVariable(22768,contextUtil.getCurrentProgramUUID());

		Property prop = new Property();
		Term term = new Term();
		term.setId(0);
		prop.setTerm(term);
		Mockito.doReturn(prop).when(this.ontologyService).getProperty(Matchers.anyString());

		Map<String, List<StandardVariable>> map = new HashMap<>();

		map.put("TRIAL_INSTANCE", this.createList(this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT,
				TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", "", "", "", "", "", "")));
		map.put("SITE_NAME", this.createList(this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.TRIAL.getId(),
				"SITE_NAME", "", "", "", "", "", "")));
		map.put("ENTRY_NO", this.createList(this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.ENTRY_NO.getId(), "ENTRY_NO", "",
				"", "", "", "", "")));
		map.put("PLOT_NO", this.createList(this.createStandardVariable(PhenotypicType.TRIAL_DESIGN, TermId.PLOT_NO.getId(), "PLOT_NO", "",
				"", "", "", "", "")));
		map.put("REP_NO", this.createList(this.createStandardVariable(PhenotypicType.TRIAL_DESIGN, TermId.REP_NO.getId(), "REP_NO", "", "",
				"", "", "", "")));
		map.put("BLOCK_NO", this.createList(this.createStandardVariable(PhenotypicType.TRIAL_DESIGN, TermId.BLOCK_NO.getId(), "BLOCK_NO",
				"", "", "", "", "", "")));

		Mockito.doReturn(map).when(this.ontologyDataManager).getStandardVariablesInProjects(Matchers.anyList(),Matchers.anyString());

	}

	private void initializeGermplasmList() {

		ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
		importedGermplasmList.setImportedGermplasms(this.createImportedGermplasmList());
		mainInfo.setImportedGermplasmList(importedGermplasmList);

		Mockito.doReturn(mainInfo).when(this.userSelection).getImportedGermplasmMainInfo();

	}

	private List<StandardVariable> createList(StandardVariable... stdVar) {
		List<StandardVariable> stdVarList = new ArrayList<>();
		for (StandardVariable var : stdVar) {
			stdVarList.add(var);
		}
		return stdVarList;

	}

	private List<ImportedGermplasm> createImportedGermplasmList() {
		List<ImportedGermplasm> importedGermplasmList = new ArrayList<>();
		for (int x = 1; x <= 14; x++) {
			importedGermplasmList.add(this.createImportedGermplasm(x));
		}

		return importedGermplasmList;
	}

	private ImportedGermplasm createImportedGermplasm(int entryNo) {
		ImportedGermplasm importedGermplasm = new ImportedGermplasm();
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

	private Map<Integer, StandardVariable> getStandardVariables(PhenotypicType phenotypicType, List<MeasurementVariable> germplasmFactors) {
		Map<Integer, StandardVariable> standardVariables = new HashMap<>();

		for (MeasurementVariable measurementVar : germplasmFactors) {

			if (phenotypicType.getLabelList().contains(measurementVar.getLabel())) {
				StandardVariable stdVar = this.convertToStandardVariable(measurementVar);
				standardVariables.put(stdVar.getId(), stdVar);
			}

		}

		return standardVariables;
	}

	protected EnvironmentData createEnvironmentData(int numberOfIntances) {
		EnvironmentData environmentData = new EnvironmentData();
		List<Environment> environments = new ArrayList<>();

		for (int x = 0; x < numberOfIntances; x++) {
			Environment env = new Environment();
			env.setLocationId(x);
			environments.add(env);
		}

		environmentData.setEnvironments(environments);
		environmentData.setNoOfEnvironments(numberOfIntances);
		return environmentData;
	}

	protected ImportedGermplasmMainInfo createImportedGermplasmMainInfo() {
		ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
		importedGermplasmList.setImportedGermplasms(this.createImportedGermplasmList());
		mainInfo.setImportedGermplasmList(importedGermplasmList);
		return mainInfo;
	}

	protected void processEnvironmentData(EnvironmentData data) {
		for (int i = 0; i < data.getEnvironments().size(); i++) {
			Map<String, String> values = data.getEnvironments().get(i).getManagementDetailValues();
			if (!values.containsKey(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()))) {
				values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
			} else if (values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())) == null
					|| values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())).isEmpty()) {
				values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
			}
		}
	}

	protected void createDesignHeaderItemMap(DesignImportData designImportData) throws MiddlewareException {

		designImportData.getMappedHeaders().clear();
		designImportData.getMappedHeaders().putAll(this.service.categorizeHeadersByPhenotype(designImportData.getUnmappedHeaders()));
		designImportData.getUnmappedHeaders().clear();
	}

	protected StandardVariable convertToStandardVariable(MeasurementVariable measurementVar) {
		StandardVariable stdVar = new StandardVariable();
		stdVar.setId(measurementVar.getTermId());
		stdVar.setProperty(new Term(0, measurementVar.getProperty(), ""));
		stdVar.setScale(new Term(0, measurementVar.getScale(), ""));
		stdVar.setMethod(new Term(0, measurementVar.getMethod(), ""));
		stdVar.setDataType(new Term(measurementVar.getDataTypeId(), measurementVar.getDataType(), ""));
		stdVar.setPhenotypicType(PhenotypicType.getPhenotypicTypeForLabel(measurementVar.getLabel()));
		return stdVar;
	}

	protected StandardVariable createStandardVariable(PhenotypicType phenotypicType, int id, String name, String property, String scale,
			String method, String dataType, String storedIn, String isA) {

		StandardVariable stdVar =
				new StandardVariable(new Term(0, property, ""), new Term(0, scale, ""), new Term(0, method, ""), new Term(0, dataType, ""),
						new Term(0, isA, ""), phenotypicType);

		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription("");

		return stdVar;
	}

}
