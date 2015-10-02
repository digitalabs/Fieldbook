
package com.efficio.fieldbook.web.nursery.controller;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
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

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.form.ImportDesignForm;
import com.efficio.fieldbook.web.common.service.impl.DesignImportServiceImpl;
import com.efficio.fieldbook.web.nursery.validator.DesignImportValidator;
import com.efficio.fieldbook.web.trial.bean.Environment;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.efficio.fieldbook.web.util.parsing.DesignImportParser;

/**
 * Created by cyrus on 5/28/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class DesignImportControllerTest {

	private static final int GW_100G_TERMID = 9999;

	private static final int COOPERATOR_TERMID = 8373;

	@Mock
	private HttpServletRequest httpRequest;

	@Mock
	private HttpSession httpSession;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private WorkbenchService workbenchService;

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

	@Mock
	private DesignImportServiceImpl designImportService;

	@Mock
	private DesignImportParser designImportParser;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private DesignImportValidator designImportValidator;

	@InjectMocks
	private DesignImportController designImportController;

	private final Project project = this.createProject();

	@Before
	public void init() throws Exception {

		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(this.project.getUniqueID());
		Mockito.when(this.httpSession.getAttribute(ContextConstants.SESSION_ATTR_CONTEXT_INFO)).thenReturn(new ContextInfo(1, 1L));
		Mockito.when(this.httpRequest.getSession(Matchers.anyBoolean())).thenReturn(this.httpSession);
		Mockito.when(this.workbenchDataManager.getProjectById(1L)).thenReturn(this.project);
		Mockito.when(this.workbenchService.getCurrentIbdbUserId(Matchers.anyLong(), Matchers.anyInt())).thenReturn(1);
		Mockito.when(this.designImportParser.parseFile(this.multiPartFile)).thenReturn(this.createDesignImportData());

		final DesignImportData data = this.createDesignImportData();
		Mockito.doReturn(data).when(this.userSelection).getDesignImportData();

		WorkbookDataUtil.setTestWorkbook(null);

		this.initializeOntologyData();
		this.initializeDesignImportService();

	}

	@Test
	public void testValidateAndSaveNewMapping() throws Exception {

		Mockito.when(this.designImportService.areTrialInstancesMatchTheSelectedEnvironments(3, this.userSelection.getDesignImportData()))
				.thenReturn(true);

		final Map<String, Object> results = this.designImportController.validateAndSaveNewMapping(this.createTestMappedHeaders(), 3);

		Mockito.verify(this.designImportValidator).validateDesignData(this.userSelection.getDesignImportData());

		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = this.userSelection.getDesignImportData().getMappedHeaders();

		Assert.assertEquals(1, mappedHeaders.get(PhenotypicType.TRIAL_ENVIRONMENT).size());
		Assert.assertEquals(0, mappedHeaders.get(PhenotypicType.GERMPLASM).size());
		Assert.assertEquals(0, mappedHeaders.get(PhenotypicType.TRIAL_DESIGN).size());
		Assert.assertEquals(0, mappedHeaders.get(PhenotypicType.VARIATE).size());

		final DesignHeaderItem designHeaderItem = mappedHeaders.get(PhenotypicType.TRIAL_ENVIRONMENT).get(0);
		Assert.assertEquals("The DesignHeaderItem SITE_NAME should be mapped to the SITE_NAME Standard Variable", TermId.SITE_NAME.getId(),
				designHeaderItem.getVariable().getId());

		Assert.assertTrue((Boolean) results.get("success"));
		Assert.assertFalse((Boolean) results.get("hasConflict"));
	}

	@Test
	public void testValidateAndSaveNewMappingWithExistingWorkbook() throws Exception {

		final List<MeasurementVariable> workbookMeasurementVariables = new ArrayList<>();
		final Set<MeasurementVariable> designFileMeasurementVariables = new HashSet<>();

		workbookMeasurementVariables.add(this.createMeasurementVariable(TermId.SITE_NAME.getId(), "SITE_NAME", "Location", "Text",
				"Assigned", "TRIAL"));
		workbookMeasurementVariables.add(this.createMeasurementVariable(TermId.TRIAL_LOCATION.getId(), "LOCATION_NAME", "Location", "DBCV",
				"Assigned", "TRIAL"));
		designFileMeasurementVariables.add(this.createMeasurementVariable(TermId.SITE_NAME.getId(), "SITE_NAME", "Location", "Text",
				"Assigned", "TRIAL"));
		designFileMeasurementVariables.add(this.createMeasurementVariable(TermId.PI_NAME.getId(), "PI_NAME", "Person", "DBCV", "Assigned",
				"TRIAL"));

		final Workbook workbook = Mockito.mock(Workbook.class);
		Mockito.when(workbook.getMeasurementDatasetVariables()).thenReturn(workbookMeasurementVariables);
		Mockito.doReturn(designFileMeasurementVariables).when(this.designImportService)
				.getMeasurementVariablesFromDataFile(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class));
		Mockito.when(this.designImportService.areTrialInstancesMatchTheSelectedEnvironments(3, this.userSelection.getDesignImportData()))
				.thenReturn(true);
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);

		final Map<String, Object> results = this.designImportController.validateAndSaveNewMapping(this.createTestMappedHeaders(), 3);

		Mockito.verify(this.designImportValidator).validateDesignData(this.userSelection.getDesignImportData());

		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = this.userSelection.getDesignImportData().getMappedHeaders();

		Assert.assertEquals(1, mappedHeaders.get(PhenotypicType.TRIAL_ENVIRONMENT).size());
		Assert.assertEquals(0, mappedHeaders.get(PhenotypicType.GERMPLASM).size());
		Assert.assertEquals(0, mappedHeaders.get(PhenotypicType.TRIAL_DESIGN).size());
		Assert.assertEquals(0, mappedHeaders.get(PhenotypicType.VARIATE).size());

		final DesignHeaderItem designHeaderItem = mappedHeaders.get(PhenotypicType.TRIAL_ENVIRONMENT).get(0);
		Assert.assertEquals("The DesignHeaderItem SITE_NAME should be mapped to the SITE_NAME Standard Variable", TermId.SITE_NAME.getId(),
				designHeaderItem.getVariable().getId());

		Assert.assertTrue((Boolean) results.get("success"));
		Assert.assertTrue((Boolean) results.get("hasConflict"));
	}

	@Test
	public void testValidateAndSaveNewMappingWithWarning() throws Exception {

		Mockito.doReturn(false).when(this.designImportService)
				.areTrialInstancesMatchTheSelectedEnvironments(Matchers.anyInt(), Matchers.any(DesignImportData.class));

		Mockito.when(this.messageSource.getMessage("design.import.warning.trial.instances.donotmatch", null, Locale.ENGLISH)).thenReturn(
				"WARNING_MSG");

		final Map<String, Object> results = this.designImportController.validateAndSaveNewMapping(this.createTestMappedHeaders(), 3);

		Mockito.verify(this.designImportValidator).validateDesignData(this.userSelection.getDesignImportData());

		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = this.userSelection.getDesignImportData().getMappedHeaders();

		Assert.assertEquals(1, mappedHeaders.get(PhenotypicType.TRIAL_ENVIRONMENT).size());
		Assert.assertEquals(0, mappedHeaders.get(PhenotypicType.GERMPLASM).size());
		Assert.assertEquals(0, mappedHeaders.get(PhenotypicType.TRIAL_DESIGN).size());
		Assert.assertEquals(0, mappedHeaders.get(PhenotypicType.VARIATE).size());

		final DesignHeaderItem designHeaderItem = mappedHeaders.get(PhenotypicType.TRIAL_ENVIRONMENT).get(0);
		Assert.assertEquals("The DesignHeaderItem SITE_NAME should be mapped to the SITE_NAME Standard Variable", TermId.SITE_NAME.getId(),
				designHeaderItem.getVariable().getId());

		assert (Boolean) results.get("success");
		Assert.assertEquals("returns a warning message", "WARNING_MSG", results.get("warning"));

	}

	@Test
	public void testValidateAndSaveNewMappingWithException() throws Exception {

		Mockito.when(this.designImportService.areTrialInstancesMatchTheSelectedEnvironments(3, this.userSelection.getDesignImportData()))
				.thenReturn(false);

		Mockito.doThrow(new DesignValidationException("DesignValidationException thrown")).when(this.designImportValidator)
				.validateDesignData(Matchers.any(DesignImportData.class));

		final Map<String, Object> results = this.designImportController.validateAndSaveNewMapping(this.createTestMappedHeaders(), 3);

		this.userSelection.getDesignImportData().getMappedHeaders();

		assert !(Boolean) results.get("success");
		Assert.assertEquals("returns a error message", "DesignValidationException thrown", results.get("error"));
		Assert.assertEquals("error = message", results.get("error"), results.get("message"));
	}

	@Test
	public void testPerformAutomap() throws Exception {

		final Map<PhenotypicType, List<DesignHeaderItem>> result = new HashMap<>();
		Mockito.doReturn(result).when(this.designImportService).categorizeHeadersByPhenotype(Matchers.anyList());

		this.designImportController.performAutomap(this.userSelection.getDesignImportData());

		// Verify that the categorizeHeadersByPhenotype is called, which is actually the method that automatically maps headers to standard
		// variables
		Mockito.verify(this.designImportService).categorizeHeadersByPhenotype(Matchers.anyList());
	}

	@Test
	public void testImportFile() throws Exception {

		final ImportDesignForm form = Mockito.mock(ImportDesignForm.class);
		Mockito.when(form.getFile()).thenReturn(this.multiPartFile);

		final String resultsMap = this.designImportController.importFile(form, "N");

		Mockito.verify(this.userSelection).setDesignImportData(Matchers.any(DesignImportData.class));

		Assert.assertTrue(resultsMap.contains("{\"isSuccess\":1}"));
	}

	@Test
	public void testImportFileFail() throws Exception {

		final ImportDesignForm form = Mockito.mock(ImportDesignForm.class);
		Mockito.when(form.getFile()).thenReturn(this.multiPartFile);
		Mockito.when(this.designImportParser.parseFile(this.multiPartFile)).thenThrow(
				new FileParsingException("force file parse exception"));

		final String resultsMap = this.designImportController.importFile(form, "N");

		Assert.assertTrue(resultsMap.contains("{\"error\":[\"force file parse exception\"],\"isSuccess\":0}"));
	}

	@Test
	public void testGetMappingData() throws Exception {
		final Set<String> mappingTypes = this.designImportController.getMappingData().keySet();

		assert mappingTypes.contains("unmappedHeaders");
		assert mappingTypes.contains("mappedEnvironmentalFactors");
		assert mappingTypes.contains("mappedDesignFactors");
		assert mappingTypes.contains("mappedGermplasmFactors");
		assert mappingTypes.contains("mappedTraits");
	}

	@Test
	public void testShowDetails() throws Exception {
		final Workbook workbook = Mockito.mock(Workbook.class);

		Mockito.when(this.userSelection.getTemporaryWorkbook()).thenReturn(workbook);

		Mockito.doReturn(Mockito.mock(Set.class)).when(this.designImportService)
				.getDesignMeasurementVariables(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class), Matchers.anyBoolean());

		final Model model = Mockito.mock(Model.class);

		final String html = this.designImportController.showDetails(model);

		Mockito.verify(model).addAttribute(Matchers.eq("measurementVariables"), Matchers.any(Set.class));

		assert DesignImportController.REVIEW_DETAILS_PAGINATION_TEMPLATE.equals(html);
	}

	@Test
	public void testShowDetailsData() throws DesignValidationException {

		final Model model = Mockito.mock(Model.class);
		final ImportDesignForm form = Mockito.mock(ImportDesignForm.class);
		final EnvironmentData environmentData = Mockito.mock(EnvironmentData.class);
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(5, 1);

		Mockito.when(this.userSelection.getTemporaryWorkbook()).thenReturn(workbook);
		Mockito.doReturn(workbook.getObservations())
				.when(this.designImportService)
				.generateDesign(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class), Matchers.any(EnvironmentData.class),
						Matchers.anyBoolean());
		;

		final List<Map<String, Object>> result = this.designImportController.showDetailsData(environmentData, model, form);

		Assert.assertEquals(workbook.getObservations().size(), result.size());
	}

	@Test
	public void testShowDetailsDataGenerateDesignFailed() throws DesignValidationException {

		final Model model = Mockito.mock(Model.class);
		final ImportDesignForm form = Mockito.mock(ImportDesignForm.class);
		final EnvironmentData environmentData = Mockito.mock(EnvironmentData.class);
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(5, 1);

		Mockito.when(this.userSelection.getTemporaryWorkbook()).thenReturn(workbook);
		Mockito.doThrow(new DesignValidationException(""))
				.when(this.designImportService)
				.generateDesign(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class), Matchers.any(EnvironmentData.class),
						Matchers.anyBoolean());
		;

		final List<Map<String, Object>> result = this.designImportController.showDetailsData(environmentData, model, form);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testFindInEnumerationMatchedByName() {

		final List<Enumeration> enumerations = new ArrayList<>();
		enumerations.add(new Enumeration(1, "Item 1", "Description 1", 0));
		enumerations.add(new Enumeration(2, "Item 2", "Description 2", 0));

		final Enumeration result = this.designImportController.findInEnumeration("Item 1", enumerations);

		Assert.assertEquals("Item 1", result.getName());
	}

	@Test
	public void testFindInEnumerationMatchedByDescription() {

		final List<Enumeration> enumerations = new ArrayList<>();
		enumerations.add(new Enumeration(1, "Item 1", "Description 1", 0));
		enumerations.add(new Enumeration(2, "Item 2", "Description 2", 0));

		final Enumeration result = this.designImportController.findInEnumeration("Description 2", enumerations);

		Assert.assertEquals("Description 2", result.getDescription());
	}

	@Test
	public void testFindInEnumerationMatchedNoMatch() {

		final List<Enumeration> enumerations = new ArrayList<>();
		enumerations.add(new Enumeration(1, "Item 1", "Description 1", 0));
		enumerations.add(new Enumeration(2, "Item 2", "Description 2", 0));

		final Enumeration result = this.designImportController.findInEnumeration("Some text", enumerations);

		Assert.assertNull(result);
	}

	@Test
	public void testUpdateDesignMapping() {

		this.designImportController.updateDesignMapping(this.createMappedHeaders());

		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = this.userSelection.getDesignImportData().getMappedHeaders();

		Assert.assertEquals(1, mappedHeaders.get(PhenotypicType.TRIAL_ENVIRONMENT).size());
		Assert.assertEquals(1, mappedHeaders.get(PhenotypicType.GERMPLASM).size());
		Assert.assertEquals(1, mappedHeaders.get(PhenotypicType.TRIAL_DESIGN).size());
		Assert.assertEquals(1, mappedHeaders.get(PhenotypicType.VARIATE).size());

	}

	@Test
	public void testResolveTheEnvironmentFactorsWithIDNamePairingForTrial() {

		final Set<MeasurementVariable> trialVariables = new HashSet<>();

		final DesignImportData designImportData = this.createDesignImportData();
		final EnvironmentData environmentData = this.createEnvironmentData(1);

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
		final Map<String, String> managementDetailValuesMap = environmentData.getEnvironments().get(0).getManagementDetailValues();

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
	public void testResolveTheEnvironmentFactorsWithIDNamePairingForNursery() {

		Mockito.mock(Workbook.class);
		final List<SettingDetail> newDetails = new ArrayList<>();

		final EnvironmentData environmentData = this.createEnvironmentData(1);
		final DesignImportData designImportData = this.createDesignImportData();

		this.designImportController.resolveTheEnvironmentFactorsWithIDNamePairing(environmentData, designImportData, newDetails);

		Assert.assertEquals(3, newDetails.size());
		Assert.assertEquals("LOCATION_NAME should be added to the Trial Variables", "LOCATION_NAME",
				this.getSettingDetail(TermId.LOCATION_ID.getId(), newDetails).getVariable().getName());
		Assert.assertEquals("COOPERATOR should be added to the Trial Variables", "COOPERATOR",
				this.getSettingDetail(TermId.COOPERATOOR_ID.getId(), newDetails).getVariable().getName());
		Assert.assertEquals("PI_NAME_ID should be added to the Trial Variables", "PI_NAME_ID",
				this.getSettingDetail(TermId.PI_ID.getId(), newDetails).getVariable().getName());

		Assert.assertEquals(1, environmentData.getNoOfEnvironments());
		final Map<String, String> managementDetailValuesMap = environmentData.getEnvironments().get(0).getManagementDetailValues();

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
	public void testAddFactorsIfNecessary() {

		final Set<MeasurementVariable> measurementVariables = new HashSet<>();
		measurementVariables.add(this.createMeasurementVariable(TermId.ROW.getId(), "ROW", "ENTRY"));
		measurementVariables.add(this.createMeasurementVariable(TermId.COL.getId(), "COL", "ENTRY"));

		Mockito.doReturn(measurementVariables).when(this.designImportService)
				.extractMeasurementVariable(Matchers.any(PhenotypicType.class), Matchers.anyMap());

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(5, 1);

		final DesignImportData data = this.createDesignImportData();

		this.designImportController.addFactorsIfNecessary(workbook, data);

		Assert.assertEquals(10, workbook.getFactors().size());
		Assert.assertEquals("ROW should be added to the Factors since it isn't in the list", "ROW",
				this.getMeasurementVariable(TermId.ROW.getId(), new HashSet<MeasurementVariable>(workbook.getFactors())).getName());
		Assert.assertEquals("COL should be added to the Factors since it isn't in the list", "COL",
				this.getMeasurementVariable(TermId.COL.getId(), new HashSet<MeasurementVariable>(workbook.getFactors())).getName());

	}

	@Test
	public void testAddFactorsIfNecessaryVariablesToAddAlreadyExist() throws URISyntaxException, FileParsingException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(5, 1);

		final Set<MeasurementVariable> measurementVariables = new HashSet<>();
		measurementVariables.add(this.getMeasurementVariable(TermId.ENTRY_NO.getId(), new HashSet<>(workbook.getFactors())));
		measurementVariables.add(this.getMeasurementVariable(TermId.GID.getId(), new HashSet<>(workbook.getFactors())));

		Mockito.doReturn(measurementVariables).when(this.designImportService)
				.extractMeasurementVariable(Matchers.any(PhenotypicType.class), Matchers.anyMap());

		final DesignImportData data = this.createDesignImportData();

		this.designImportController.addFactorsIfNecessary(workbook, data);

		Assert.assertEquals("ENTRY_NO and GID should not added to the Factors, so the size of Factor must remain 8", 8, workbook
				.getFactors().size());

	}

	@Test
	public void testAddConditionsIfNecessaryVariablesToAddAlreadyExist() throws URISyntaxException, FileParsingException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(5, StudyType.N);

		final Set<MeasurementVariable> measurementVariables = new HashSet<>();
		measurementVariables.add(this.getMeasurementVariable(TermId.TRIAL_LOCATION.getId(), new HashSet<>(workbook.getConditions())));

		Mockito.doReturn(measurementVariables).when(this.designImportService)
				.extractMeasurementVariable(Matchers.any(PhenotypicType.class), Matchers.anyMap());

		final DesignImportData data = this.createDesignImportData();
		this.designImportController.addConditionsIfNecessary(workbook, data);

		Assert.assertEquals("LOCATION_NAME should not added to the Conditions, so the size of Conditions must remain 7", 7, workbook
				.getConditions().size());

	}

	@Test
	public void testAddConditionsIfNecessary() throws URISyntaxException, FileParsingException {

		final Set<MeasurementVariable> measurementVariables = new HashSet<>();
		measurementVariables.add(this.createMeasurementVariable(TermId.SITE_NAME.getId(), "SITENAME", "TRIAL"));

		Mockito.doReturn(measurementVariables).when(this.designImportService)
				.extractMeasurementVariable(Matchers.any(PhenotypicType.class), Matchers.anyMap());

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(5, StudyType.N);

		final DesignImportData data = this.createDesignImportData();
		this.designImportController.addConditionsIfNecessary(workbook, data);

		Assert.assertEquals(8, workbook.getConditions().size());
		Assert.assertEquals("SITENAME should be added to the Conditions since it isn't in the list", "SITENAME", this
				.getMeasurementVariable(TermId.SITE_NAME.getId(), new HashSet<MeasurementVariable>(workbook.getConditions())).getName());

	}

	@Test
	public void testPopulateTrialLevelVariableListIfNecessary() {

		final Project project = this.createProject();
		final MeasurementVariable siteName = this.createMeasurementVariable(TermId.SITE_NAME.getId(), "SITE_NAME", "TRIAL");
		final SettingDetail siteNameSettingDetail = this.createSettingDetail(TermId.SITE_NAME.getId(), "SITE_NAME", "TRIAL");

		final List<SettingDetail> settingDetails = new ArrayList<>();
		Mockito.doReturn(settingDetails).when(this.userSelection).getTrialLevelVariableList();

		Mockito.doReturn(siteNameSettingDetail).when(this.settingsService)
				.createSettingDetail(siteName.getTermId(), siteName.getName(), this.userSelection, 1, project.getUniqueID());

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(5, 3);

		// Add trial environment to Factors for testing
		workbook.getFactors().add(siteName);

		this.designImportController.populateTrialLevelVariableListIfNecessary(workbook);

		final SettingDetail settingDetail = settingDetails.get(0);

		// SITE_NAME should be added to setting details passed to addNewSettingDetailsIfNecessary()
		Assert.assertEquals(1, settingDetails.size());
		Assert.assertEquals(TermId.SITE_NAME.getId(), settingDetail.getVariable().getCvTermId().intValue());
		Assert.assertEquals("SITE_NAME", settingDetail.getVariable().getName());
		Assert.assertEquals("Newly added setting detail must always be deletable", true, settingDetail.isDeletable());

	}

	@Test
	public void testPopulateStudyLevelVariableListIfNecessary() throws URISyntaxException, FileParsingException {

		final Project project = this.createProject();
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(5, StudyType.N);
		final EnvironmentData environmentData = this.createEnvironmentData(1);
		final DesignImportData designImportData = this.createDesignImportData();

		final MeasurementVariable siteName = this.createMeasurementVariable(TermId.SITE_NAME.getId(), "SITE_NAME", "TRIAL");
		final SettingDetail siteNameSettingDetail = this.createSettingDetail(TermId.SITE_NAME.getId(), "SITE_NAME", "TRIAL");
		final MeasurementVariable piName = this.createMeasurementVariable(TermId.PI_NAME.getId(), "PI_NAME", "TRIAL");
		final SettingDetail piNameSettingDetail = this.createSettingDetail(TermId.PI_NAME.getId(), "PI_NAME", "TRIAL");

		workbook.getConditions().clear();
		workbook.getConditions().add(siteName);
		workbook.getConditions().add(piName);

		final List<SettingDetail> newSettingDetails = new ArrayList<>();

		Mockito.doReturn(siteNameSettingDetail).when(this.settingsService)
				.createSettingDetail(siteName.getTermId(), siteName.getName(), this.userSelection, 1, project.getUniqueID());
		Mockito.doReturn(piNameSettingDetail).when(this.settingsService)
				.createSettingDetail(piName.getTermId(), piName.getName(), this.userSelection, 1, project.getUniqueID());
		Mockito.doReturn(newSettingDetails).when(this.userSelection).getStudyLevelConditions();

		this.designImportController.populateStudyLevelVariableListIfNecessary(workbook, environmentData, designImportData);

		Assert.assertEquals(5, newSettingDetails.size());

		final SettingDetail settingDetail = this.getSettingDetail(TermId.SITE_NAME.getId(), newSettingDetails);

		Assert.assertEquals(siteName.getTermId(), settingDetail.getVariable().getCvTermId().intValue());
		Assert.assertEquals(siteName.getName(), settingDetail.getVariable().getName());
		Assert.assertEquals(siteName.getOperation(), settingDetail.getVariable().getOperation());
		Assert.assertEquals("Test Site", settingDetail.getValue());

		final SettingDetail settingDetail2 = this.getSettingDetail(TermId.PI_NAME.getId(), newSettingDetails);

		Assert.assertEquals(piName.getTermId(), settingDetail2.getVariable().getCvTermId().intValue());
		Assert.assertEquals(piName.getName(), settingDetail2.getVariable().getName());
		Assert.assertEquals(piName.getOperation(), settingDetail2.getVariable().getOperation());
		Assert.assertEquals("", settingDetail2.getValue());

	}

	@Test
	public void testCheckTheDeletedSettingDetails() {

		final DesignImportData designImportData = this.createDesignImportData();
		final Set<MeasurementVariable> measurementVariables = this.createMeasurementVariables();
		final List<SettingDetail> deletedTrialLevelVariables = this.createDeletedTrialLevelVariables();
		final UserSelection selection = new UserSelection();

		selection.setTrialLevelVariableList(new ArrayList<SettingDetail>());
		selection.setDeletedTrialLevelVariables(deletedTrialLevelVariables);

		Mockito.doReturn(measurementVariables).when(this.designImportService)
				.getMeasurementVariablesFromDataFile(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class));

		this.designImportController.checkTheDeletedSettingDetails(selection, designImportData);

		// The deletedTrialLevelVariables tracks the list of variables that are deleted by the user in the UI. So if the user re-imports a
		// design file which has variables that are already in the deleted list, they should be removed in the deleted list.

		Assert.assertEquals(0, deletedTrialLevelVariables.size());
		Assert.assertEquals(3, selection.getTrialLevelVariableList().size());

		// Make sure variables that were in the deletedTrialLevelVariables list are added in the TrialLevelVariableList
		final SettingDetail siteName = this.getSettingDetail(TermId.SITE_NAME.getId(), selection.getTrialLevelVariableList());
		Assert.assertNotNull(siteName);
		Assert.assertTrue(siteName.getVariable().getOperation().equals(Operation.UPDATE));

		final SettingDetail locatioName = this.getSettingDetail(TermId.TRIAL_LOCATION.getId(), selection.getTrialLevelVariableList());
		Assert.assertNotNull(locatioName);
		Assert.assertTrue(locatioName.getVariable().getOperation().equals(Operation.UPDATE));

		final SettingDetail locatioNameId = this.getSettingDetail(TermId.LOCATION_ID.getId(), selection.getTrialLevelVariableList());
		Assert.assertNotNull(locatioNameId);
		Assert.assertTrue(locatioNameId.getVariable().getOperation().equals(Operation.UPDATE));

	}

	@Test
	public void testGenerateMeasurements() {

		final Set<MeasurementVariable> measurementVariables = this.createMeasurementVariables();
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(5, 1);

		Mockito.doReturn(workbook).when(this.userSelection).getTemporaryWorkbook();
		Mockito.doReturn(measurementVariables).when(this.designImportService)
				.getMeasurementVariablesFromDataFile(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class));

		final EnvironmentData environmentData = this.createEnvironmentData(1);

		final Map<String, Object> resultsMap = this.designImportController.generateMeasurements(environmentData);

		Assert.assertEquals(1, resultsMap.get(DesignImportController.IS_SUCCESS));

	}

	@Test
	public void testGenerateMeasurementsFail() throws DesignValidationException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(5, 1);

		Mockito.doReturn(workbook).when(this.userSelection).getTemporaryWorkbook();

		Mockito.doThrow(new DesignValidationException(""))
				.when(this.designImportService)
				.generateDesign(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class), Matchers.any(EnvironmentData.class),
						Matchers.anyBoolean());

		final EnvironmentData environmentData = this.createEnvironmentData(1);
		final Map<String, Object> resultsMap = this.designImportController.generateMeasurements(environmentData);

		Assert.assertEquals(0, resultsMap.get(DesignImportController.IS_SUCCESS));
		Assert.assertTrue(resultsMap.containsKey(DesignImportController.ERROR));

	}

	@Test
	public void testUpdateOperationWithTermIdHasMatchInList() {

		final List<SettingDetail> settingDetails = new ArrayList<>();
		final SettingDetail settingDetail = this.createSettingDetail(TermId.SITE_NAME.getId(), "SITE_NAME", "TRIAL");
		settingDetails.add(settingDetail);

		this.designImportController.updateOperation(TermId.SITE_NAME.getId(), settingDetails, Operation.ADD);

		Assert.assertEquals(Operation.ADD, settingDetail.getVariable().getOperation());

	}

	@Test
	public void testUpdateOperationSuppliedTermIdNotInList() {

		final List<SettingDetail> settingDetails = new ArrayList<>();
		final SettingDetail settingDetail = this.createSettingDetail(TermId.SITE_NAME.getId(), "SITE_NAME", "TRIAL");
		settingDetails.add(settingDetail);

		this.designImportController.updateOperation(TermId.BLOCK_ID.getId(), settingDetails, Operation.ADD);

		Assert.assertNull(settingDetail.getVariable().getOperation());

	}

	@Test
	public void testHasConflictTrue() {

		final Set<MeasurementVariable> setA = new HashSet<>();
		final Set<MeasurementVariable> setB = new HashSet<>();

		setA.add(this.createMeasurementVariable(TermId.SITE_NAME.getId(), "SITE_NAME", "Location", "Text", "Assigned", "TRIAL"));
		setA.add(this.createMeasurementVariable(TermId.TRIAL_LOCATION.getId(), "LOCATION_NAME", "Location", "DBCV", "Assigned", "TRIAL"));

		setB.add(this.createMeasurementVariable(TermId.SITE_NAME.getId(), "SITE_NAME", "Location", "Text", "Assigned", "TRIAL"));
		setB.add(this.createMeasurementVariable(TermId.PI_NAME.getId(), "PI_NAME", "Person", "DBCV", "Assigned", "TRIAL"));

		Assert.assertTrue(this.designImportController.hasConflict(setA, setB));

	}

	@Test
	public void testHasConflictFalse() {

		final Set<MeasurementVariable> setA = new HashSet<>();
		final Set<MeasurementVariable> setB = new HashSet<>();

		setA.add(this.createMeasurementVariable(TermId.SITE_NAME.getId(), "SITE_NAME", "Location", "Text", "Assigned", "TRIAL"));
		setA.add(this.createMeasurementVariable(TermId.TRIAL_LOCATION.getId(), "LOCATION_NAME", "Location", "DBCV", "Assigned", "TRIAL"));

		setB.add(this.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", "Trial instance", "Number",
				"Enumerated", "TRIAL"));
		setB.add(this.createMeasurementVariable(TermId.PI_NAME.getId(), "PI_NAME", "Person", "DBCV", "Assigned", "TRIAL"));

		Assert.assertFalse(this.designImportController.hasConflict(setA, setB));

	}

	@Test
	public void testGetLocalNameFromSettingDetailsWithMatch() {

		final List<SettingDetail> settingDetails = new ArrayList<>();
		settingDetails.add(this.createSettingDetail(123, "FACTOR 1", "TRIAL"));
		settingDetails.add(this.createSettingDetail(321, "FACTOR 2", "TRIAL"));

		final String result = this.designImportController.getLocalNameFromSettingDetails(123, settingDetails);

		Assert.assertEquals("FACTOR 1", result);
	}

	@Test
	public void testGetLocalNameFromSettingDetailsWithNoMatch() {

		final List<SettingDetail> settingDetails = new ArrayList<>();
		settingDetails.add(this.createSettingDetail(123, "FACTOR 1", "TRIAL"));
		settingDetails.add(this.createSettingDetail(321, "FACTOR 2", "TRIAL"));

		final String result = this.designImportController.getLocalNameFromSettingDetails(567, settingDetails);

		Assert.assertEquals("", result);
	}

	@Test
	public void testPostSelectedNurseryTypeNotNumeric() {

		this.designImportController.postSelectedNurseryType("asd");

		Mockito.verify(this.userSelection, Mockito.times(0)).setNurseryTypeForDesign(Matchers.anyInt());
	}

	@Test
	public void testPostSelectedNurseryTypeNumeric() {

		this.designImportController.postSelectedNurseryType("1");

		Mockito.verify(this.userSelection).setNurseryTypeForDesign(1);
	}

	@Test
	public void testCreateTrialObservationsForTrial() {

		final DesignImportData designImportData = this.createDesignImportData();
		final EnvironmentData environmentData = this.createEnvironmentData(1);
		final Workbook workbook = Mockito.spy(WorkbookDataUtil.getTestWorkbook(5, StudyType.T));

		this.designImportController.createTrialObservations(environmentData, workbook, designImportData);

		Mockito.verify(workbook).setTrialObservations(Matchers.anyList());
		Mockito.verify(this.fieldbookService).addConditionsToTrialObservationsIfNecessary(Matchers.any(Workbook.class));

	}

	@Test
	public void testCreateTrialObservationsForNursery() {

		final DesignImportData designImportData = this.createDesignImportData();
		final EnvironmentData environmentData = this.createEnvironmentData(1);
		final Workbook workbook = Mockito.spy(WorkbookDataUtil.getTestWorkbook(5, StudyType.N));

		this.designImportController.createTrialObservations(environmentData, workbook, designImportData);

		Mockito.verify(workbook).setTrialObservations(Matchers.anyList());
		Mockito.verify(this.fieldbookService, Mockito.times(0)).addConditionsToTrialObservationsIfNecessary(Matchers.any(Workbook.class));

	}

	@Test
	public void testInitializeTemporaryWorkbookForTrial() {

		this.designImportController.initializeTemporaryWorkbook(StudyType.T.getName());
		final ArgumentCaptor<Workbook> argument = ArgumentCaptor.forClass(Workbook.class);

		Mockito.verify(this.userSelection).setTemporaryWorkbook(argument.capture());

		final Workbook workbook = argument.getValue();
		Assert.assertEquals(StudyType.T, workbook.getStudyDetails().getStudyType());
	}

	@Test
	public void testInitializeTemporaryWorkbookForNursery() {

		this.designImportController.initializeTemporaryWorkbook(StudyType.N.getName());
		final ArgumentCaptor<Workbook> argument = ArgumentCaptor.forClass(Workbook.class);

		Mockito.verify(this.userSelection).setTemporaryWorkbook(argument.capture());

		final Workbook workbook = argument.getValue();
		Assert.assertEquals(StudyType.N, workbook.getStudyDetails().getStudyType());

	}

	@Test
	public void testCancelImportDesign() {

		this.designImportController.cancelImportDesign();

		Mockito.verify(this.userSelection).setTemporaryWorkbook(null);
		Mockito.verify(this.userSelection).setDesignImportData(null);

	}

	private MeasurementVariable getMeasurementVariable(final int termId, final Set<MeasurementVariable> trialVariables) {
		for (final MeasurementVariable mvar : trialVariables) {
			if (termId == mvar.getTermId()) {
				return mvar;
			}
		}
		return null;

	}

	private SettingDetail getSettingDetail(final int termId, final List<SettingDetail> settingDetails) {
		for (final SettingDetail settingDetail : settingDetails) {
			if (termId == settingDetail.getVariable().getCvTermId().intValue()) {
				return settingDetail;
			}
		}
		return null;

	}

	private EnvironmentData createEnvironmentData(final int numberOfIntances) {
		final EnvironmentData environmentData = new EnvironmentData();
		final List<Environment> environments = new ArrayList<>();

		for (int x = 1; x <= numberOfIntances; x++) {
			final Environment env = new Environment();
			env.setManagementDetailValues(this.createManagementDetailValues(x));
			env.setLocationId(x);
			environments.add(env);
		}

		environmentData.setEnvironments(environments);
		environmentData.setNoOfEnvironments(numberOfIntances);
		return environmentData;
	}

	private Map<String, String> createManagementDetailValues(final int instanceNo) {
		final Map<String, String> map = new HashMap<>();
		map.put(String.valueOf(TermId.TRIAL_INSTANCE_FACTOR.getId()), String.valueOf(instanceNo));
		map.put(String.valueOf(TermId.TRIAL_LOCATION.getId()), "Test Location");
		map.put(String.valueOf(TermId.LOCATION_ID.getId()), "1234");
		map.put(String.valueOf(TermId.SITE_NAME.getId()), "Test Site");
		map.put(String.valueOf(TermId.PI_NAME.getId()), null);
		map.put(String.valueOf(DesignImportControllerTest.COOPERATOR_TERMID), "4321");
		return map;
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
		trialEvironmentItems.add(this.createDesignHeaderItem(PhenotypicType.TRIAL_ENVIRONMENT, TermId.TRIAL_LOCATION.getId(),
				"LOCATION_NAME", 1));
		trialEvironmentItems.add(this.createDesignHeaderItem(PhenotypicType.TRIAL_ENVIRONMENT, TermId.SITE_NAME.getId(), "SITE_NAME", 2));
		trialEvironmentItems.add(this.createDesignHeaderItem(PhenotypicType.TRIAL_ENVIRONMENT,
				DesignImportControllerTest.COOPERATOR_TERMID, "COOPERATOR", 3));

		final List<DesignHeaderItem> germplasmItems = new ArrayList<>();
		germplasmItems.add(this.createDesignHeaderItem(PhenotypicType.GERMPLASM, TermId.ENTRY_NO.getId(), "ENTRY_NO", 4));

		final List<DesignHeaderItem> trialDesignItems = new ArrayList<>();
		trialDesignItems.add(this.createDesignHeaderItem(PhenotypicType.TRIAL_DESIGN, TermId.PLOT_NO.getId(), "PLOT_NO", 5));
		trialDesignItems.add(this.createDesignHeaderItem(PhenotypicType.TRIAL_DESIGN, TermId.REP_NO.getId(), "REP_NO", 6));
		trialDesignItems.add(this.createDesignHeaderItem(PhenotypicType.TRIAL_DESIGN, TermId.BLOCK_NO.getId(), "BLOCK_NO", 7));

		final List<DesignHeaderItem> variateItems = new ArrayList<>();
		variateItems.add(this.createDesignHeaderItem(PhenotypicType.VARIATE, DesignImportControllerTest.GW_100G_TERMID, "GW_100G", 8));

		mappedHeaders.put(PhenotypicType.TRIAL_ENVIRONMENT, trialEvironmentItems);
		mappedHeaders.put(PhenotypicType.GERMPLASM, germplasmItems);
		mappedHeaders.put(PhenotypicType.TRIAL_DESIGN, trialDesignItems);
		mappedHeaders.put(PhenotypicType.VARIATE, variateItems);

		return mappedHeaders;

	}

	private Map<Integer, List<String>> createTestCsvDataForDesignImportData() {

		final Map<Integer, List<String>> csvData = new HashMap<>();

		// The first row is the header
		csvData.put(0, this.createListOfString("TRIAL_INSTANCE", "LOCATION_NAME", "SITE_NAME", "COOPERATOR", "ENTRY_NO", "PLOT_NO",
				"REP_NO", "BLOCK_NO", "GW_100G"));

		// csv data
		csvData.put(1, this.createListOfString("1", "Philippines", "Laguna", "John Doe", "1", "1", "1", "1", "98"));
		csvData.put(2, this.createListOfString("1", "Philippines", "Laguna", "John Doe", "2", "2", "1", "1", "4"));
		csvData.put(3, this.createListOfString("1", "Philippines", "Laguna", "John Doe", "3", "3", "1", "1", "13"));

		return csvData;

	}

	private List<String> createListOfString(final String... listData) {
		final List<String> list = new ArrayList<>();
		for (final String data : listData) {
			list.add(data);
		}
		return list;
	}

	private Map<String, List<DesignHeaderItem>> createMappedHeaders() {

		final Map<String, List<DesignHeaderItem>> map = new HashMap<>();

		map.put("mappedEnvironmentalFactors", this.createDesignHeaderItems(TermId.SITE_NAME.getId()));
		map.put("mappedDesignFactors", this.createDesignHeaderItems(TermId.PLOT_NO.getId()));
		map.put("mappedGermplasmFactors", this.createDesignHeaderItems(TermId.ENTRY_NO.getId()));
		map.put("mappedTraits", this.createDesignHeaderItems(DesignImportControllerTest.GW_100G_TERMID));

		return map;
	}

	private List<DesignHeaderItem> createDesignHeaderItems(final int... termIds) {

		final List<DesignHeaderItem> items = new ArrayList<>();
		for (final int termid : termIds) {
			final DesignHeaderItem item = new DesignHeaderItem();
			item.setId(termid);
			items.add(item);
		}

		return items;
	}

	private DesignHeaderItem createDesignHeaderItem(final PhenotypicType phenotypicType, final int termId, final String headerName,
			final int columnIndex) {
		final DesignHeaderItem designHeaderItem = new DesignHeaderItem();
		designHeaderItem.setId(termId);
		designHeaderItem.setName(headerName);
		designHeaderItem.setColumnIndex(columnIndex);
		designHeaderItem.setVariable(this.createStandardVariable(phenotypicType, termId, headerName, "", "", "", "", "", ""));
		return designHeaderItem;
	}

	private void initializeOntologyData() throws MiddlewareQueryException {

		final Map<String, List<StandardVariable>> map = new HashMap<>();

		final StandardVariable trialInstance =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", "",
						"", "", "", "", "");
		final StandardVariable siteName =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.SITE_NAME.getId(), "SITE_NAME", "", "", "", "", "", "");
		final StandardVariable locationName =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.TRIAL_LOCATION.getId(), "LOCATION_NAME", "", "", "",
						"", "", "");
		final StandardVariable locationID =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.LOCATION_ID.getId(), "LOCATION_NAME_ID", "", "", "",
						"", "", "");
		final StandardVariable cooperator =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, DesignImportControllerTest.COOPERATOR_TERMID, "COOPERATOR",
						"", "", "", "", "", "");

		final StandardVariable cooperatorId =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.COOPERATOOR_ID.getId(), "COOPERATOR_ID", "", "", "",
						"", "", "");
		final StandardVariable principalInvestigator =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.PI_NAME.getId(), "PI_NAME", "", "", "", "", "", "");

		final StandardVariable principalInvestigatorId =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.PI_ID.getId(), "PI_NAME_ID", "", "", "", "", "", "");
		final StandardVariable entryNo =
				this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.ENTRY_NO.getId(), "ENTRY_NO", "", "", "", "", "", "");
		final StandardVariable plotNo =
				this.createStandardVariable(PhenotypicType.TRIAL_DESIGN, TermId.PLOT_NO.getId(), "PLOT_NO", "", "", "", "", "", "");
		final StandardVariable repNo =
				this.createStandardVariable(PhenotypicType.TRIAL_DESIGN, TermId.REP_NO.getId(), "REP_NO", "", "", "", "", "", "");
		final StandardVariable blockNo =
				this.createStandardVariable(PhenotypicType.TRIAL_DESIGN, TermId.BLOCK_NO.getId(), "BLOCK_NO", "", "", "", "", "", "");
		final StandardVariable gw100g =
				this.createStandardVariable(PhenotypicType.VARIATE, DesignImportControllerTest.GW_100G_TERMID, "GW_100G", "", "", "", "",
						"", "");

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

		Mockito.doReturn(map).when(this.ontologyDataManager).getStandardVariablesInProjects(Matchers.anyList(), Matchers.anyString());

		Mockito.doReturn(trialInstance).when(this.ontologyDataManager)
				.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), this.project.getUniqueID());
		Mockito.doReturn(siteName).when(this.ontologyDataManager).getStandardVariable(TermId.SITE_NAME.getId(), this.project.getUniqueID());
		Mockito.doReturn(locationName).when(this.ontologyDataManager)
				.getStandardVariable(TermId.TRIAL_LOCATION.getId(), this.project.getUniqueID());
		Mockito.doReturn(locationID).when(this.ontologyDataManager)
				.getStandardVariable(TermId.LOCATION_ID.getId(), this.project.getUniqueID());
		Mockito.doReturn(entryNo).when(this.ontologyDataManager).getStandardVariable(TermId.ENTRY_NO.getId(), this.project.getUniqueID());
		Mockito.doReturn(plotNo).when(this.ontologyDataManager).getStandardVariable(TermId.PLOT_NO.getId(), this.project.getUniqueID());
		Mockito.doReturn(blockNo).when(this.ontologyDataManager).getStandardVariable(TermId.BLOCK_NO.getId(), this.project.getUniqueID());
		Mockito.doReturn(repNo).when(this.ontologyDataManager).getStandardVariable(TermId.REP_NO.getId(), this.project.getUniqueID());
		Mockito.doReturn(cooperator).when(this.ontologyDataManager)
				.getStandardVariable(DesignImportControllerTest.COOPERATOR_TERMID, this.project.getUniqueID());
		Mockito.doReturn(cooperatorId).when(this.ontologyDataManager)
				.getStandardVariable(TermId.COOPERATOOR_ID.getId(), this.project.getUniqueID());
		Mockito.doReturn(principalInvestigator).when(this.ontologyDataManager)
				.getStandardVariable(TermId.PI_NAME.getId(), this.project.getUniqueID());
		Mockito.doReturn(principalInvestigatorId).when(this.ontologyDataManager)
				.getStandardVariable(TermId.PI_ID.getId(), this.project.getUniqueID());
		Mockito.doReturn(gw100g).when(this.ontologyDataManager)
				.getStandardVariable(DesignImportControllerTest.GW_100G_TERMID, this.project.getUniqueID());

		Mockito.doReturn(trialInstance).when(this.fieldbookMiddlewareService)
				.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), this.project.getUniqueID());
		Mockito.doReturn(siteName).when(this.fieldbookMiddlewareService)
				.getStandardVariable(TermId.SITE_NAME.getId(), this.project.getUniqueID());
		Mockito.doReturn(locationName).when(this.fieldbookMiddlewareService)
				.getStandardVariable(TermId.TRIAL_LOCATION.getId(), this.project.getUniqueID());
		Mockito.doReturn(locationID).when(this.fieldbookMiddlewareService)
				.getStandardVariable(TermId.LOCATION_ID.getId(), this.project.getUniqueID());
		Mockito.doReturn(entryNo).when(this.fieldbookMiddlewareService)
				.getStandardVariable(TermId.ENTRY_NO.getId(), this.project.getUniqueID());
		Mockito.doReturn(plotNo).when(this.fieldbookMiddlewareService)
				.getStandardVariable(TermId.PLOT_NO.getId(), this.project.getUniqueID());
		Mockito.doReturn(blockNo).when(this.fieldbookMiddlewareService)
				.getStandardVariable(TermId.BLOCK_NO.getId(), this.project.getUniqueID());
		Mockito.doReturn(repNo).when(this.fieldbookMiddlewareService)
				.getStandardVariable(TermId.REP_NO.getId(), this.project.getUniqueID());
		Mockito.doReturn(cooperator).when(this.fieldbookMiddlewareService)
				.getStandardVariable(DesignImportControllerTest.COOPERATOR_TERMID, this.project.getUniqueID());
		Mockito.doReturn(cooperatorId).when(this.fieldbookMiddlewareService)
				.getStandardVariable(TermId.COOPERATOOR_ID.getId(), this.project.getUniqueID());
		Mockito.doReturn(principalInvestigator).when(this.fieldbookMiddlewareService)
				.getStandardVariable(TermId.PI_NAME.getId(), this.project.getUniqueID());
		Mockito.doReturn(principalInvestigatorId).when(this.fieldbookMiddlewareService)
				.getStandardVariable(TermId.PI_ID.getId(), this.project.getUniqueID());
		Mockito.doReturn(gw100g).when(this.fieldbookMiddlewareService)
				.getStandardVariable(DesignImportControllerTest.GW_100G_TERMID, this.project.getUniqueID());

	}

	private void initializeDesignImportService() throws DesignValidationException {
		Mockito.doReturn(Mockito.mock(Set.class)).when(this.designImportService)
				.getMeasurementVariablesFromDataFile(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class));
		Mockito.doReturn(new ArrayList<MeasurementRow>())
				.when(this.designImportService)
				.generateDesign(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class), Matchers.any(EnvironmentData.class),
						Matchers.anyBoolean());
		Mockito.doReturn(new HashSet<MeasurementVariable>()).when(this.designImportService)
				.getDesignMeasurementVariables(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class), Matchers.anyBoolean());
		Mockito.doReturn(new HashSet<MeasurementVariable>()).when(this.designImportService)
				.getDesignMeasurementVariables(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class), Matchers.anyBoolean());
		Mockito.doReturn(new HashSet<MeasurementVariable>()).when(this.designImportService)
				.getDesignRequiredStandardVariables(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class));
		Mockito.doReturn(new HashSet<MeasurementVariable>()).when(this.designImportService)
				.getDesignRequiredMeasurementVariable(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class));
	}

	private StandardVariable createStandardVariable(final PhenotypicType phenotypicType, final int id, final String name,
			final String property, final String scale, final String method, final String dataType, final String storedIn, final String isA) {

		final StandardVariable stdVar =
				new StandardVariable(new Term(0, property, ""), new Term(0, scale, ""), new Term(0, method, ""), new Term(0, dataType, ""),
						new Term(0, isA, ""), phenotypicType);

		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription("");

		return stdVar;
	}

	private List<StandardVariable> createList(final StandardVariable... stdVar) {
		final List<StandardVariable> stdVarList = new ArrayList<>();
		for (final StandardVariable var : stdVar) {
			stdVarList.add(var);
		}
		return stdVarList;

	}

	private Project createProject() {
		final Project project = new Project();
		project.setUniqueID("");
		project.setProjectId(1L);
		return project;
	}

	private MeasurementVariable createMeasurementVariable(final int termId, final String name, final String label) {
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termId);
		measurementVariable.setName(name);
		measurementVariable.setLabel(label);
		return measurementVariable;
	}

	private MeasurementVariable createMeasurementVariable(final int termId, final String name, final String property, final String scale,
			final String method, final String label) {
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termId);
		measurementVariable.setName(name);
		measurementVariable.setLabel(label);
		measurementVariable.setProperty(property);
		measurementVariable.setScale(scale);
		measurementVariable.setMethod(method);
		return measurementVariable;
	}

	private SettingDetail createSettingDetail(final int termId, final String name, final String label) {
		final SettingDetail settingDetail = new SettingDetail();
		final SettingVariable settingVariable = new SettingVariable();
		settingVariable.setCvTermId(termId);
		settingVariable.setName(name);
		settingDetail.setVariable(settingVariable);
		return settingDetail;
	}

	private Set<MeasurementVariable> createMeasurementVariables() {

		final Set<MeasurementVariable> measurementVariables = new HashSet<>();
		measurementVariables.add(this.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", "TRIAL"));
		measurementVariables.add(this.createMeasurementVariable(TermId.TRIAL_LOCATION.getId(), "LOCATION_NAME", "TRIAL"));
		measurementVariables.add(this.createMeasurementVariable(TermId.LOCATION_ID.getId(), "LOCATION_NAME_ID", "TRIAL"));
		measurementVariables.add(this.createMeasurementVariable(TermId.SITE_NAME.getId(), "SITE_NAME", "TRIAL"));
		measurementVariables.add(this.createMeasurementVariable(DesignImportControllerTest.COOPERATOR_TERMID, "COOPERATOR", "TRIAL"));
		measurementVariables.add(this.createMeasurementVariable(TermId.ENTRY_NO.getId(), "ENTRY_NO", "GERMPLASM ENTRY"));
		measurementVariables.add(this.createMeasurementVariable(TermId.PLOT_NO.getId(), "PLOT_NO", "PLOT"));
		measurementVariables.add(this.createMeasurementVariable(TermId.REP_NO.getId(), "REP_NO", "PLOT"));
		measurementVariables.add(this.createMeasurementVariable(TermId.BLOCK_NO.getId(), "BLOCK_NO", "PLOT"));
		return measurementVariables;
	}

	private List<SettingDetail> createDeletedTrialLevelVariables() {

		final List<SettingDetail> deletedTrialLevelVariables = new ArrayList<>();

		deletedTrialLevelVariables.add(this.createSettingDetail(TermId.SITE_NAME.getId(), "SITE_NAME", "TRIAL"));
		deletedTrialLevelVariables.add(this.createSettingDetail(TermId.TRIAL_LOCATION.getId(), "LOCATION_NAME", "TRIAL"));
		deletedTrialLevelVariables.add(this.createSettingDetail(TermId.LOCATION_ID.getId(), "LOCATION_NAME_ID", "TRIAL"));

		return deletedTrialLevelVariables;
	}

	private Map<String, List<DesignHeaderItem>> createTestMappedHeaders() {
		final Map<String, List<DesignHeaderItem>> testNewMap = new HashMap<>();
		final DesignHeaderItem designHeaderItem = new DesignHeaderItem();
		designHeaderItem.setId(TermId.SITE_NAME.getId());
		designHeaderItem.setName("SITE_NAME");
		final List<DesignHeaderItem> designHeaderItems = new ArrayList<>();
		designHeaderItems.add(designHeaderItem);
		testNewMap.put("mappedEnvironmentalFactors", designHeaderItems);
		testNewMap.put("mappedDesignFactors", new ArrayList<DesignHeaderItem>());
		testNewMap.put("mappedGermplasmFactors", new ArrayList<DesignHeaderItem>());
		testNewMap.put("mappedTraits", new ArrayList<DesignHeaderItem>());
		return testNewMap;
	}

}
