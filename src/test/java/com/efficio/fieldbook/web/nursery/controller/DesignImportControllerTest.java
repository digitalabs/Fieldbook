
package com.efficio.fieldbook.web.nursery.controller;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.form.ImportDesignForm;
import com.efficio.fieldbook.web.common.service.impl.DesignImportServiceImpl;
import com.efficio.fieldbook.web.trial.bean.Environment;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.efficio.fieldbook.web.util.parsing.DesignImportParser;

/**
 * Created by cyrus on 5/28/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class DesignImportControllerTest {

	private static final int COOPERATOR_TERMID = 8373;

	public static final String TEST_FILE_NAME = "Design_Import_Template_With_Environment_Factors.csv";

	private final DesignImportParser parser = Mockito.spy(new DesignImportParser());

	@Mock
	protected FieldbookService fieldbookService;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private MockMultipartFile multiPartFile;

	@Mock
	private UserSelection userSelection;

	@Mock
	private MessageSource messageSource;

	@Mock
	private SettingsService settingsService;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@InjectMocks
	private final DesignImportController designImportController = Mockito.spy(new DesignImportController());

	@InjectMocks
	private DesignImportServiceImpl designImportService = Mockito.spy(new DesignImportServiceImpl());

	@Before
	public void initTests() throws Exception {

		DesignImportData data = Mockito.spy(this.createDesignImportData());
		Mockito.doReturn(data).when(this.userSelection).getDesignImportData();

		WorkbookDataUtil.setTestWorkbook(null);

		this.initializeOntologyData();
	}

	@Test
	public void testValidateAndSaveNewMapping() throws Exception {
		Mockito.doNothing().when(this.designImportController).updateDesignMapping(Matchers.any(Map.class));

		Mockito.when(this.designImportService.areTrialInstancesMatchTheSelectedEnvironments(3, this.userSelection.getDesignImportData()))
				.thenReturn(true);

		Map<String, Object> results = this.designImportController.validateAndSaveNewMapping(Mockito.mock(Map.class), 3);

		Mockito.verify(this.designImportService).validateDesignData(this.userSelection.getDesignImportData());

		assert (Boolean) results.get("success");
	}

	@Test
	public void testValidateAndSaveNewMappingWithWarning() throws Exception {

		Mockito.doNothing().when(this.designImportController).updateDesignMapping(Matchers.any(Map.class));
		Mockito.doNothing().when(this.designImportService).validateDesignData(Mockito.any(DesignImportData.class));

		Mockito.doReturn(false).when(this.designImportService)
				.areTrialInstancesMatchTheSelectedEnvironments(Mockito.anyInt(), Mockito.any(DesignImportData.class));

		Mockito.when(this.messageSource.getMessage("design.import.warning.trial.instances.donotmatch", null, Locale.ENGLISH)).thenReturn(
				"WARNING_MSG");

		Map<String, Object> results = this.designImportController.validateAndSaveNewMapping(Mockito.mock(Map.class), 3);

		Mockito.verify(this.designImportService).validateDesignData(this.userSelection.getDesignImportData());

		assert (Boolean) results.get("success");
		Assert.assertEquals("returns a warning message", "WARNING_MSG", results.get("warning"));

	}

	@Test
	public void testValidateAndSaveNewMappingWithException() throws Exception {
		Mockito.doNothing().when(this.designImportController).updateDesignMapping(Matchers.any(Map.class));

		Mockito.when(this.designImportService.areTrialInstancesMatchTheSelectedEnvironments(3, this.userSelection.getDesignImportData()))
				.thenReturn(false);

		Mockito.doThrow(new DesignValidationException("DesignValidationException thrown")).when(this.designImportService)
				.validateDesignData(Matchers.any(DesignImportData.class));

		Map<String, Object> results = this.designImportController.validateAndSaveNewMapping(Mockito.mock(Map.class), 3);

		assert !(Boolean) results.get("success");
		Assert.assertEquals("returns a error message", "DesignValidationException thrown", results.get("error"));
		Assert.assertEquals("error = message", results.get("error"), results.get("message"));
	}

	@Test
	public void testPerformAutomap() throws Exception {

		Map<PhenotypicType, List<DesignHeaderItem>> result = new HashMap<>();

		Mockito.doReturn(result).when(this.designImportService).categorizeHeadersByPhenotype(Mockito.anyList());

		this.designImportController.performAutomap(this.userSelection.getDesignImportData());

		// just verify were setting mapped headers and unmapped headers to designImportData
		Mockito.verify(this.userSelection.getDesignImportData()).setMappedHeaders(result);
		Mockito.verify(this.userSelection.getDesignImportData()).setUnmappedHeaders(result.get(null));

	}

	@Test
	public void testImportFile() throws Exception {
		Mockito.doNothing().when(this.designImportController).initializeTemporaryWorkbook(Matchers.anyString());
		ImportDesignForm form = Mockito.mock(ImportDesignForm.class);
		Mockito.when(form.getFile()).thenReturn(this.multiPartFile);

		String resultsMap = this.designImportController.importFile(form, "N");

		Mockito.verify(this.userSelection).setDesignImportData(Matchers.any(DesignImportData.class));

		assert resultsMap.contains("{\"isSuccess\":1}");
	}

	@Test
	public void testImportFileFail() throws Exception {
		ImportDesignForm form = Mockito.mock(ImportDesignForm.class);
		Mockito.when(form.getFile()).thenReturn(this.multiPartFile);

		Mockito.doNothing().when(this.designImportController).initializeTemporaryWorkbook(Matchers.anyString());
		Mockito.doThrow(new FileParsingException("force file parse exception")).when(this.parser)
				.parseFile(Matchers.any(MultipartFile.class));

		String resultsMap = this.designImportController.importFile(form, "N");

		assert resultsMap.contains("{\"error\":[\"force file parse exception\"],\"isSuccess\":0}");
	}

	@Test
	public void testGetMappingData() throws Exception {
		Set<String> mappingTypes = this.designImportController.getMappingData().keySet();

		assert mappingTypes.contains("unmappedHeaders");
		assert mappingTypes.contains("mappedEnvironmentalFactors");
		assert mappingTypes.contains("mappedDesignFactors");
		assert mappingTypes.contains("mappedGermplasmFactors");
		assert mappingTypes.contains("mappedTraits");
	}

	@Test
	public void testShowDetails() throws Exception {
		Workbook workbook = Mockito.mock(Workbook.class);

		Mockito.when(this.userSelection.getTemporaryWorkbook()).thenReturn(workbook);

		Mockito.doReturn(Mockito.mock(Set.class)).when(this.designImportService)
				.getDesignMeasurementVariables(Mockito.any(Workbook.class), Mockito.any(DesignImportData.class), Mockito.anyBoolean());

		Model model = Mockito.mock(Model.class);

		String html = this.designImportController.showDetails(model);

		Mockito.verify(model).addAttribute(Matchers.eq("measurementVariables"), Matchers.any(Set.class));

		assert DesignImportController.REVIEW_DETAILS_PAGINATION_TEMPLATE.equals(html);
	}

	@Test
	public void testResolveTheEnvironmentFactorsWithIDNamePairingForTrial() throws URISyntaxException, FileParsingException {

		Set<MeasurementVariable> trialVariables = new HashSet<>();

		DesignImportData designImportData = this.createDesignImportData();
		EnvironmentData environmentData = this.createEnvironmentData(1);

		Mockito.doReturn(1).when(this.designImportController).getCurrentIbdbUserId();
		Mockito.doReturn(this.createProject()).when(this.designImportController).getCurrentProject();
		Mockito.doReturn(this.createProject().getProjectId().toString()).when(this.designImportController).getCurrentProjectId();

		this.designImportController.resolveTheEnvironmentFactorsWithIDNamePairing(environmentData, designImportData, trialVariables);

		Assert.assertEquals(5, trialVariables.size());
		Assert.assertEquals("LOCATION_NAME_ID should be added to the Trial Variables", "LOCATION_NAME_ID",
				this.getMeasurementVariable(TermId.LOCATION_ID.getId(), trialVariables).getName());
		Assert.assertEquals("COOPERATOR_ID should be added to the Trial Variables", "COOPERATOR_ID",
				this.getMeasurementVariable(TermId.COOPERATOOR_ID.getId(), trialVariables).getName());
		Assert.assertEquals("PI_NAME should be added to the Trial Variables", "PI_NAME",
				this.getMeasurementVariable(TermId.PI_NAME.getId(), trialVariables).getName());
		Assert.assertEquals("SITE_NAME should be added to the Trial Variables", "SITE_NAME",
				this.getMeasurementVariable(TermId.SITE_NAME.getId(), trialVariables).getName());
		Assert.assertEquals("TRIAL_INSTANCE should be added to the Trial Variables", "TRIAL_INSTANCE",
				this.getMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), trialVariables).getName());

		Assert.assertEquals(1, environmentData.getNoOfEnvironments());
		Map<String, String> managementDetailValuesMap = environmentData.getEnvironments().get(0).getManagementDetailValues();

		Assert.assertTrue("LOCATION_NAME_ID should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.LOCATION_ID.getId())));
		Assert.assertTrue("COOPERATOOR_ID should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.COOPERATOOR_ID.getId())));
		Assert.assertTrue("PI_NAME should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.PI_NAME.getId())));
		Assert.assertTrue("SITE_NAME should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.SITE_NAME.getId())));
		Assert.assertTrue("TRIAL_INSTANCE_FACTOR should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.TRIAL_INSTANCE_FACTOR.getId())));

	}

	@Test
	public void testResolveTheEnvironmentFactorsWithIDNamePairingForNursery() throws URISyntaxException, FileParsingException {

		Mockito.mock(Workbook.class);
		List<SettingDetail> newDetails = new ArrayList<>();

		EnvironmentData environmentData = this.createEnvironmentData(1);
		DesignImportData designImportData = this.createDesignImportData();

		Mockito.doReturn(1).when(this.designImportController).getCurrentIbdbUserId();
		Mockito.doReturn(this.createProject()).when(this.designImportController).getCurrentProject();
		Mockito.doReturn(this.createProject().getProjectId().toString()).when(this.designImportController).getCurrentProjectId();

		this.designImportController.resolveTheEnvironmentFactorsWithIDNamePairing(environmentData, designImportData, newDetails);

		Assert.assertEquals(3, newDetails.size());
		Assert.assertEquals("LOCATION_NAME should be added to the Trial Variables", "LOCATION_NAME",
				this.getSettingDetail(TermId.LOCATION_ID.getId(), newDetails).getVariable().getName());
		Assert.assertEquals("COOPERATOR should be added to the Trial Variables", "COOPERATOR",
				this.getSettingDetail(TermId.COOPERATOOR_ID.getId(), newDetails).getVariable().getName());
		Assert.assertEquals("PI_NAME_ID should be added to the Trial Variables", "PI_NAME_ID",
				this.getSettingDetail(TermId.PI_ID.getId(), newDetails).getVariable().getName());

		Assert.assertEquals(1, environmentData.getNoOfEnvironments());
		Map<String, String> managementDetailValuesMap = environmentData.getEnvironments().get(0).getManagementDetailValues();

		Assert.assertTrue("LOCATION_NAME_ID should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.LOCATION_ID.getId())));
		Assert.assertTrue("COOPERATOOR_ID should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.COOPERATOOR_ID.getId())));
		Assert.assertTrue("PI_ID should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.PI_ID.getId())));
		Assert.assertTrue("SITE_NAME should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.SITE_NAME.getId())));
		Assert.assertTrue("TRIAL_INSTANCE_FACTOR should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.TRIAL_INSTANCE_FACTOR.getId())));

	}

	@Test
	public void testCheckTheDeletedSettingDetails() throws URISyntaxException, FileParsingException {

		DesignImportData designImportData = this.createDesignImportData();
		Set<MeasurementVariable> measurementVariables = this.createMeasurementVariables();
		List<SettingDetail> deletedTrialLevelVariables = this.createDeletedTrialLevelVariables();
		UserSelection selection = new UserSelection();

		selection.setTrialLevelVariableList(new ArrayList<SettingDetail>());
		selection.setDeletedTrialLevelVariables(deletedTrialLevelVariables);

		Mockito.doReturn(measurementVariables).when(this.designImportService)
				.getMeasurementVariablesFromDataFile(Mockito.any(Workbook.class), Mockito.any(DesignImportData.class));

		this.designImportController.checkTheDeletedSettingDetails(selection, designImportData);

	}

	@Test
	public void testAddFactorsIfNecessary() throws URISyntaxException, FileParsingException {

		Set<MeasurementVariable> measurementVariables = new HashSet<>();
		measurementVariables.add(this.createMeasurementVariable(TermId.ROW.getId(), "ROW", "ENTRY"));
		measurementVariables.add(this.createMeasurementVariable(TermId.COL.getId(), "COL", "ENTRY"));

		Mockito.doReturn(measurementVariables).when(this.designImportService)
				.extractMeasurementVariable(Mockito.any(PhenotypicType.class), Mockito.anyMap());

		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(5, 1);

		DesignImportData data = this.createDesignImportData();

		this.designImportController.addFactorsIfNecessary(workbook, data);

		Assert.assertEquals(10, workbook.getFactors().size());
		Assert.assertEquals("ROW should be added to the Factors since it isn't in the list", "ROW",
				this.getMeasurementVariable(TermId.ROW.getId(), new HashSet<MeasurementVariable>(workbook.getFactors())).getName());
		Assert.assertEquals("COL should be added to the Factors since it isn't in the list", "COL",
				this.getMeasurementVariable(TermId.COL.getId(), new HashSet<MeasurementVariable>(workbook.getFactors())).getName());

	}

	@Test
	public void testAddFactorsIfNecessaryVariablesToAddAlreadyExist() throws URISyntaxException, FileParsingException {

		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(5, 1);

		Set<MeasurementVariable> measurementVariables = new HashSet<>();
		measurementVariables.add(this.getMeasurementVariable(TermId.ENTRY_NO.getId(), new HashSet<>(workbook.getFactors())));
		measurementVariables.add(this.getMeasurementVariable(TermId.GID.getId(), new HashSet<>(workbook.getFactors())));

		Mockito.doReturn(measurementVariables).when(this.designImportService)
				.extractMeasurementVariable(Mockito.any(PhenotypicType.class), Mockito.anyMap());

		DesignImportData data = this.createDesignImportData();

		this.designImportController.addFactorsIfNecessary(workbook, data);

		Assert.assertEquals("ENTRY_NO and GID should not added to the Factors, so the size of Factor must remain 8", 8, workbook
				.getFactors().size());

	}

	@Test
	public void testAddConditionsIfNecessaryVariablesToAddAlreadyExist() throws URISyntaxException, FileParsingException {

		Workbook workbook = WorkbookDataUtil.getTestWorkbook(5, StudyType.N);

		Set<MeasurementVariable> measurementVariables = new HashSet<>();
		measurementVariables.add(this.getMeasurementVariable(TermId.TRIAL_LOCATION.getId(), new HashSet<>(workbook.getConditions())));

		Mockito.doReturn(measurementVariables).when(this.designImportService)
				.extractMeasurementVariable(Mockito.any(PhenotypicType.class), Mockito.anyMap());

		DesignImportData data = this.createDesignImportData();
		this.designImportController.addConditionsIfNecessary(workbook, data);

		Assert.assertEquals("LOCATION_NAME should not added to the Conditions, so the size of Conditions must remain 7", 7, workbook
				.getConditions().size());

	}

	@Test
	public void testAddConditionsIfNecessary() throws URISyntaxException, FileParsingException {

		Set<MeasurementVariable> measurementVariables = new HashSet<>();
		measurementVariables.add(this.createMeasurementVariable(TermId.SITE_NAME.getId(), "SITENAME", "TRIAL"));

		Mockito.doReturn(measurementVariables).when(this.designImportService)
				.extractMeasurementVariable(Mockito.any(PhenotypicType.class), Mockito.anyMap());

		Workbook workbook = WorkbookDataUtil.getTestWorkbook(5, StudyType.N);

		DesignImportData data = this.createDesignImportData();
		this.designImportController.addConditionsIfNecessary(workbook, data);

		Assert.assertEquals(8, workbook.getConditions().size());
		Assert.assertEquals("SITENAME should be added to the Conditions since it isn't in the list", "SITENAME", this
				.getMeasurementVariable(TermId.SITE_NAME.getId(), new HashSet<MeasurementVariable>(workbook.getConditions())).getName());

	}

	@Test
	public void testPopulateTrialLevelVariableListIfNecessary() {

		Project project = this.createProject();
		MeasurementVariable siteName = this.createMeasurementVariable(TermId.SITE_NAME.getId(), "SITE_NAME", "TRIAL");
		SettingDetail siteNameSettingDetail = this.createSettingDetail(TermId.SITE_NAME.getId(), "SITE_NAME", "TRIAL");

		Mockito.doReturn(1).when(this.designImportController).getCurrentIbdbUserId();
		Mockito.doReturn(project).when(this.designImportController).getCurrentProject();
		Mockito.doReturn(siteNameSettingDetail).when(this.settingsService)
				.createSettingDetail(siteName.getTermId(), siteName.getName(), this.userSelection, 1, project.getUniqueID());
		ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);

		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(5, 3);

		// Add trial environment to Factors for testing
		workbook.getFactors().add(siteName);

		this.designImportController.populateTrialLevelVariableListIfNecessary(workbook);

		Mockito.verify(this.designImportController).addNewSettingDetailsIfNecessary(argument.capture());

		List<SettingDetail> settingDetails = argument.getValue();

		SettingDetail settingDetail = settingDetails.get(0);

		// SITE_NAME should be added to setting details passed to addNewSettingDetailsIfNecessary()
		Assert.assertEquals(1, settingDetails.size());
		Assert.assertEquals(TermId.SITE_NAME.getId(), settingDetail.getVariable().getCvTermId().intValue());
		Assert.assertEquals("SITE_NAME", settingDetail.getVariable().getName());
		Assert.assertEquals("Newly added setting detail must always be deletable", true, settingDetail.isDeletable());

	}

	private MeasurementVariable getMeasurementVariable(int termId, Set<MeasurementVariable> trialVariables) {
		for (MeasurementVariable mvar : trialVariables) {
			if (termId == mvar.getTermId()) {
				return mvar;
			}
		}
		return null;

	}

	private SettingDetail getSettingDetail(int termId, List<SettingDetail> settingDetails) {
		for (SettingDetail settingDetail : settingDetails) {
			if (termId == settingDetail.getVariable().getCvTermId().intValue()) {
				return settingDetail;
			}
		}
		return null;

	}

	private EnvironmentData createEnvironmentData(int numberOfIntances) {
		EnvironmentData environmentData = new EnvironmentData();
		List<Environment> environments = new ArrayList<>();

		for (int x = 1; x <= numberOfIntances; x++) {
			Environment env = new Environment();
			env.setManagementDetailValues(this.createManagementDetailValues(x));
			env.setLocationId(x);
			environments.add(env);
		}

		environmentData.setEnvironments(environments);
		environmentData.setNoOfEnvironments(numberOfIntances);
		return environmentData;
	}

	private Map<String, String> createManagementDetailValues(int instanceNo) {
		Map<String, String> map = new HashMap<>();
		map.put(String.valueOf(TermId.TRIAL_INSTANCE_FACTOR.getId()), String.valueOf(instanceNo));
		map.put(String.valueOf(TermId.TRIAL_LOCATION.getId()), "");
		map.put(String.valueOf(TermId.LOCATION_ID.getId()), "");
		map.put(String.valueOf(TermId.SITE_NAME.getId()), "");
		map.put(String.valueOf(TermId.PI_NAME.getId()), "");
		map.put(String.valueOf(COOPERATOR_TERMID), "");
		return map;
	}

	private DesignImportData createDesignImportData() throws URISyntaxException, FileParsingException {

		File file = new File(ClassLoader.getSystemClassLoader().getResource(DesignImportControllerTest.TEST_FILE_NAME).toURI());
		Mockito.doReturn(file).when(this.parser).storeAndRetrieveFile(this.multiPartFile);
		return this.createDesignHeaderItemMap(this.parser.parseFile(this.multiPartFile));

	}

	private DesignImportData createDesignHeaderItemMap(DesignImportData data) throws MiddlewareQueryException {

		data.getMappedHeaders().clear();
		data.getMappedHeaders().putAll(this.designImportService.categorizeHeadersByPhenotype(data.getUnmappedHeaders()));

		for (DesignHeaderItem item : data.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT)) {
			item.setId(item.getVariable().getId());
		}

		data.getUnmappedHeaders().clear();

		return data;
	}

	private void initializeOntologyData() throws MiddlewareQueryException {

		Map<String, List<StandardVariable>> map = new HashMap<>();

		StandardVariable trialInstance =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", "",
						"", "", "", "", "");
		StandardVariable siteName =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.SITE_NAME.getId(), "SITE_NAME", "", "", "", "", "", "");
		StandardVariable locationName =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.TRIAL_LOCATION.getId(), "LOCATION_NAME", "", "", "",
						"", "", "");
		StandardVariable locationID =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.LOCATION_ID.getId(), "LOCATION_NAME_ID", "", "", "",
						"", "", "");
		StandardVariable cooperator =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, COOPERATOR_TERMID, "COOPERATOR", "", "", "", "", "", "");

		StandardVariable cooperatorId =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.COOPERATOOR_ID.getId(), "COOPERATOR_ID", "", "", "",
						"", "", "");
		StandardVariable principalInvestigator =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.PI_NAME.getId(), "PI_NAME", "", "", "", "", "", "");

		StandardVariable principalInvestigatorId =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.PI_ID.getId(), "PI_NAME_ID", "", "", "", "", "", "");
		StandardVariable entryNo =
				this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.ENTRY_NO.getId(), "ENTRY_NO", "", "", "", "", "", "");
		StandardVariable plotNo =
				this.createStandardVariable(PhenotypicType.TRIAL_DESIGN, TermId.PLOT_NO.getId(), "PLOT_NO", "", "", "", "", "", "");
		StandardVariable repNo =
				this.createStandardVariable(PhenotypicType.TRIAL_DESIGN, TermId.REP_NO.getId(), "REP_NO", "", "", "", "", "", "");
		StandardVariable blockNo =
				this.createStandardVariable(PhenotypicType.TRIAL_DESIGN, TermId.BLOCK_NO.getId(), "BLOCK_NO", "", "", "", "", "", "");

		map.put("TRIAL_INSTANCE", this.createList(trialInstance));
		map.put("SITE_NAME", this.createList(siteName));
		map.put("LOCATION_NAME", this.createList(locationName));
		map.put("LOCATION_NAME_ID", this.createList(locationID));
		map.put("ENTRY_NO", this.createList(entryNo));
		map.put("PLOT_NO", this.createList(plotNo));
		map.put("REP_NO", this.createList(repNo));
		map.put("BLOCK_NO", this.createList(blockNo));
		map.put("COOPERATOR", this.createList(cooperator));
		map.put("COOPERATOR_ID", this.createList(cooperatorId));
		map.put("PI_NAME", this.createList(principalInvestigator));
		map.put("PI_NAME_ID", this.createList(principalInvestigatorId));

		Mockito.doReturn(map).when(this.ontologyDataManager).getStandardVariablesInProjects(Matchers.anyList());

		Mockito.doReturn(trialInstance).when(this.ontologyDataManager).getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId());
		Mockito.doReturn(siteName).when(this.ontologyDataManager).getStandardVariable(TermId.SITE_NAME.getId());
		Mockito.doReturn(locationName).when(this.ontologyDataManager).getStandardVariable(TermId.TRIAL_LOCATION.getId());
		Mockito.doReturn(locationID).when(this.ontologyDataManager).getStandardVariable(TermId.LOCATION_ID.getId());
		Mockito.doReturn(entryNo).when(this.ontologyDataManager).getStandardVariable(TermId.ENTRY_NO.getId());
		Mockito.doReturn(plotNo).when(this.ontologyDataManager).getStandardVariable(TermId.PLOT_NO.getId());
		Mockito.doReturn(blockNo).when(this.ontologyDataManager).getStandardVariable(TermId.BLOCK_NO.getId());
		Mockito.doReturn(repNo).when(this.ontologyDataManager).getStandardVariable(TermId.REP_NO.getId());
		Mockito.doReturn(cooperator).when(this.ontologyDataManager).getStandardVariable(COOPERATOR_TERMID);
		Mockito.doReturn(cooperatorId).when(this.ontologyDataManager).getStandardVariable(TermId.COOPERATOOR_ID.getId());
		Mockito.doReturn(principalInvestigator).when(this.ontologyDataManager).getStandardVariable(TermId.PI_NAME.getId());
		Mockito.doReturn(principalInvestigatorId).when(this.ontologyDataManager).getStandardVariable(TermId.PI_ID.getId());

		Mockito.doReturn(trialInstance).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId());
		Mockito.doReturn(siteName).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.SITE_NAME.getId());
		Mockito.doReturn(locationName).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.TRIAL_LOCATION.getId());
		Mockito.doReturn(locationID).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.LOCATION_ID.getId());
		Mockito.doReturn(entryNo).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.ENTRY_NO.getId());
		Mockito.doReturn(plotNo).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.PLOT_NO.getId());
		Mockito.doReturn(blockNo).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.BLOCK_NO.getId());
		Mockito.doReturn(repNo).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.REP_NO.getId());
		Mockito.doReturn(cooperator).when(this.fieldbookMiddlewareService).getStandardVariable(COOPERATOR_TERMID);
		Mockito.doReturn(cooperatorId).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.COOPERATOOR_ID.getId());
		Mockito.doReturn(principalInvestigator).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.PI_NAME.getId());
		Mockito.doReturn(principalInvestigatorId).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.PI_ID.getId());

	}

	private StandardVariable createStandardVariable(PhenotypicType phenotypicType, int id, String name, String property, String scale,
			String method, String dataType, String storedIn, String isA) {

		StandardVariable stdVar =
				new StandardVariable(new Term(0, property, ""), new Term(0, scale, ""), new Term(0, method, ""), new Term(0, dataType, ""),
						new Term(0, storedIn, ""), new Term(0, isA, ""), phenotypicType);

		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription("");

		return stdVar;
	}

	private List<StandardVariable> createList(StandardVariable... stdVar) {
		List<StandardVariable> stdVarList = new ArrayList<>();
		for (StandardVariable var : stdVar) {
			stdVarList.add(var);
		}
		return stdVarList;

	}

	private Project createProject() {
		Project project = new Project();
		project.setUniqueID("");
		project.setProjectId(1L);
		return project;
	}

	private MeasurementVariable createMeasurementVariable(int termId, String name, String label) {
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termId);
		measurementVariable.setName(name);
		measurementVariable.setLabel(label);
		return measurementVariable;
	}

	private SettingDetail createSettingDetail(int termId, String name, String label) {
		SettingDetail settingDetail = new SettingDetail();
		SettingVariable settingVariable = new SettingVariable();
		settingVariable.setCvTermId(termId);
		settingVariable.setName(name);
		settingDetail.setVariable(settingVariable);
		return settingDetail;
	}

	private Set<MeasurementVariable> createMeasurementVariables() {

		Set<MeasurementVariable> measurementVariables = new HashSet<>();
		measurementVariables.add(this.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", "TRIAL"));
		measurementVariables.add(this.createMeasurementVariable(TermId.TRIAL_LOCATION.getId(), "LOCATION_NAME", "TRIAL"));
		measurementVariables.add(this.createMeasurementVariable(TermId.LOCATION_ID.getId(), "LOCATION_NAME_ID", "TRIAL"));
		measurementVariables.add(this.createMeasurementVariable(TermId.SITE_NAME.getId(), "SITE_NAME", "TRIAL"));
		measurementVariables.add(this.createMeasurementVariable(COOPERATOR_TERMID, "COOPERATOR", "TRIAL"));
		measurementVariables.add(this.createMeasurementVariable(TermId.ENTRY_NO.getId(), "ENTRY_NO", "GERMPLASM ENTRY"));
		measurementVariables.add(this.createMeasurementVariable(TermId.PLOT_NO.getId(), "PLOT_NO", "PLOT"));
		measurementVariables.add(this.createMeasurementVariable(TermId.REP_NO.getId(), "REP_NO", "PLOT"));
		measurementVariables.add(this.createMeasurementVariable(TermId.BLOCK_NO.getId(), "BLOCK_NO", "PLOT"));
		return measurementVariables;
	}

	private List<SettingDetail> createDeletedTrialLevelVariables() {

		List<SettingDetail> deletedTrialLevelVariables = new ArrayList<>();

		deletedTrialLevelVariables.add(this.createSettingDetail(TermId.SITE_NAME.getId(), "SITE_NAME", "TRIAL"));
		deletedTrialLevelVariables.add(this.createSettingDetail(TermId.TRIAL_LOCATION.getId(), "LOCATION_NAME", "TRIAL"));
		deletedTrialLevelVariables.add(this.createSettingDetail(TermId.LOCATION_ID.getId(), "LOCATION_NAME_ID", "TRIAL"));

		return deletedTrialLevelVariables;
	}

}
