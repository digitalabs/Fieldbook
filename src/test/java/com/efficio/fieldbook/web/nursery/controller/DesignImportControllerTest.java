
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
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Assert;
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
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.form.ImportDesignForm;
import com.efficio.fieldbook.web.common.service.DesignImportService;
import com.efficio.fieldbook.web.common.service.impl.DesignImportServiceImpl;
import com.efficio.fieldbook.web.trial.bean.Environment;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.efficio.fieldbook.web.util.parsing.DesignImportParser;

/**
 * Created by cyrus on 5/28/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class DesignImportControllerTest {

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
	private DesignImportService designImportService;

	@Mock
	private MessageSource messageSource;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@InjectMocks
	private final DesignImportController designImportController = Mockito.spy(new DesignImportController());

	@InjectMocks
	private DesignImportServiceImpl service;

	private DesignImportData designImportData;
	private MultipartFile multipartFile;

	@Before
	public void initTests() throws Exception {
		File designImportFile = new File(ClassLoader.getSystemClassLoader().getResource(DesignImportControllerTest.TEST_FILE_NAME).toURI());
		assert designImportFile.exists();

		this.multipartFile = Mockito.mock(MultipartFile.class);
		Mockito.doReturn(designImportFile).when(this.parser).storeAndRetrieveFile(this.multipartFile);

		this.designImportData = Mockito.spy(this.parser.parseFile(this.multipartFile));
		Mockito.when(this.userSelection.getDesignImportData()).thenReturn(this.designImportData);

		this.initializeOntologyService();
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

		Mockito.when(this.designImportService.areTrialInstancesMatchTheSelectedEnvironments(3, this.userSelection.getDesignImportData()))
				.thenReturn(false);

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
		Mockito.when(this.designImportService.categorizeHeadersByPhenotype(this.designImportData.getUnmappedHeaders())).thenReturn(result);

		this.designImportController.performAutomap(this.designImportData);

		// just verify were setting mapped headers and unmapped headers to designImportData
		Mockito.verify(this.designImportData).setMappedHeaders(result);
		Mockito.verify(this.designImportData).setUnmappedHeaders(result.get(null));

	}

	@Test
	public void testImportFile() throws Exception {
		Mockito.doNothing().when(this.designImportController).initializeTemporaryWorkbook(Matchers.anyString());
		ImportDesignForm form = Mockito.mock(ImportDesignForm.class);
		Mockito.when(form.getFile()).thenReturn(this.multipartFile);

		String resultsMap = this.designImportController.importFile(form, "N");

		Mockito.verify(this.userSelection).setDesignImportData(Matchers.any(DesignImportData.class));

		assert resultsMap.contains("{\"isSuccess\":1}");
	}

	@Test
	public void testImportFileFail() throws Exception {
		ImportDesignForm form = Mockito.mock(ImportDesignForm.class);
		Mockito.when(form.getFile()).thenReturn(this.multipartFile);

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
		Mockito.when(this.userSelection.getDesignImportData()).thenReturn(this.designImportData);
		Mockito.when(this.designImportService.getDesignMeasurementVariables(workbook, this.designImportData, true)).thenReturn(
				Mockito.mock(Set.class));

		Model model = Mockito.mock(Model.class);

		String html = this.designImportController.showDetails(model);

		Mockito.verify(model).addAttribute(Matchers.eq("measurementVariables"), Matchers.any(Set.class));

		assert DesignImportController.REVIEW_DETAILS_PAGINATION_TEMPLATE.equals(html);
	}

	@Test
	public void testResolveTheEnvironmentFactorsWithIDNamePairing() throws URISyntaxException, FileParsingException {

		Workbook workbook = Mockito.mock(Workbook.class);
		Set<MeasurementVariable> trialVariables = new HashSet<>();
		DesignImportData data = this.createDesignImportData();

		Mockito.doReturn(1).when(this.designImportController).getCurrentIbdbUserId();
		Mockito.doReturn(this.createProject()).when(this.designImportController).getCurrentProject();

		this.designImportController.resolveTheEnvironmentFactorsWithIDNamePairing(this.createEnvironmentData(4), workbook, data,
				trialVariables);

		Assert.assertEquals(3, trialVariables.size());
		Assert.assertEquals("LOCATION_NAME_ID should be added to the Trial Variables", "LOCATION_NAME_ID",
				this.getMeasurementVariable(TermId.LOCATION_ID.getId(), trialVariables).getName());
		Assert.assertEquals("SITE_NAME should be added to the Trial Variables", "SITE_NAME",
				this.getMeasurementVariable(TermId.SITE_NAME.getId(), trialVariables).getName());
		Assert.assertEquals("TRIAL_INSTANCE should be added to the Trial Variables", "TRIAL_INSTANCE",
				this.getMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), trialVariables).getName());

	}

	protected MeasurementVariable getMeasurementVariable(int termId, Set<MeasurementVariable> trialVariables) {
		for (MeasurementVariable mvar : trialVariables) {
			if (termId == mvar.getTermId()) {
				return mvar;
			}
		}
		return null;

	}

	protected EnvironmentData createEnvironmentData(int numberOfIntances) {
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
		return map;
	}

	protected DesignImportData createDesignImportData() throws URISyntaxException, FileParsingException {

		File file = new File(ClassLoader.getSystemClassLoader().getResource("Design_Import_Template.csv").toURI());
		Mockito.doReturn(file).when(this.parser).storeAndRetrieveFile(this.multiPartFile);
		return this.createDesignHeaderItemMap(this.parser.parseFile(this.multiPartFile));

	}

	protected DesignImportData createDesignHeaderItemMap(DesignImportData data) throws MiddlewareQueryException {

		data.getMappedHeaders().clear();
		data.getMappedHeaders().putAll(this.service.categorizeHeadersByPhenotype(this.designImportData.getUnmappedHeaders()));

		for (DesignHeaderItem item : data.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT)) {
			item.setId(item.getVariable().getId());
		}

		data.getUnmappedHeaders().clear();

		return data;
	}

	protected void initializeOntologyService() throws MiddlewareQueryException {

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

		Mockito.doReturn(map).when(this.ontologyDataManager).getStandardVariablesInProjects(Matchers.anyList());

		Mockito.doReturn(trialInstance).when(this.ontologyDataManager).getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId());
		Mockito.doReturn(siteName).when(this.ontologyDataManager).getStandardVariable(TermId.SITE_NAME.getId());
		Mockito.doReturn(locationName).when(this.ontologyDataManager).getStandardVariable(TermId.TRIAL_LOCATION.getId());
		Mockito.doReturn(locationID).when(this.ontologyDataManager).getStandardVariable(TermId.LOCATION_ID.getId());
		Mockito.doReturn(entryNo).when(this.ontologyDataManager).getStandardVariable(TermId.ENTRY_NO.getId());
		Mockito.doReturn(plotNo).when(this.ontologyDataManager).getStandardVariable(TermId.PLOT_NO.getId());
		Mockito.doReturn(blockNo).when(this.ontologyDataManager).getStandardVariable(TermId.BLOCK_NO.getId());
		Mockito.doReturn(repNo).when(this.ontologyDataManager).getStandardVariable(TermId.REP_NO.getId());

		Mockito.doReturn(trialInstance).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId());
		Mockito.doReturn(siteName).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.SITE_NAME.getId());
		Mockito.doReturn(locationName).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.TRIAL_LOCATION.getId());
		Mockito.doReturn(locationID).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.LOCATION_ID.getId());
		Mockito.doReturn(entryNo).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.ENTRY_NO.getId());
		Mockito.doReturn(plotNo).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.PLOT_NO.getId());
		Mockito.doReturn(blockNo).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.BLOCK_NO.getId());
		Mockito.doReturn(repNo).when(this.fieldbookMiddlewareService).getStandardVariable(TermId.REP_NO.getId());

	}

	protected StandardVariable createStandardVariable(PhenotypicType phenotypicType, int id, String name, String property, String scale,
			String method, String dataType, String storedIn, String isA) {

		StandardVariable stdVar =
				new StandardVariable(new Term(0, property, ""), new Term(0, scale, ""), new Term(0, method, ""), new Term(0, dataType, ""),
						new Term(0, storedIn, ""), new Term(0, isA, ""), phenotypicType);

		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription("");

		return stdVar;
	}

	protected List<StandardVariable> createList(StandardVariable... stdVar) {
		List<StandardVariable> stdVarList = new ArrayList<>();
		for (StandardVariable var : stdVar) {
			stdVarList.add(var);
		}
		return stdVarList;

	}

	protected Project createProject() {
		Project project = new Project();
		project.setUniqueID("");
		return project;
	}

}
