
package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.common.bean.AdvanceResult;
import com.efficio.fieldbook.web.common.bean.ChoiceKeyVal;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.TableHeader;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import com.efficio.fieldbook.web.trial.form.AdvancingStudyForm;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.pojo.AdvanceGermplasmChangeDetail;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class AdvancingControllerTest {

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Mock
	private PaginationListSelection paginationListSelection;

	@Mock
	private UserSelection userSelection;

	@Mock
	private MessageSource messageSource;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private FieldbookProperties fieldbookProperties;

    @Mock
    private ContextUtil contextUtil;

    @Mock
    private GermplasmDataManager germplasmDataManager;

	@Mock
	private DatasetService datasetService;

	@Mock
	protected OntologyVariableDataManager variableDataManager;

	@InjectMocks
	private final AdvancingController advancingController = Mockito.spy(new AdvancingController());

	@Test
	public void testGetAdvancedNurseryTableHeader_returnsTheValueFromColumLabelDefaultName() throws MiddlewareQueryException {
		final Term fromOntology = new Term();
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);

		final List<TableHeader> tableHeaderList = this.advancingController.getAdvancedStudyTableHeader();
		Assert.assertEquals("Expecting to return 7 columns but returned " + tableHeaderList.size(), 7, tableHeaderList.size());

		Assert.assertTrue("Expecting to have a column name ENTRY_ID.", this.hasColumnHeader(tableHeaderList, "ENTRY_ID"));
		Assert.assertTrue("Expecting to have a column name DESIGNATION.", this.hasColumnHeader(tableHeaderList, "DESIGNATION"));
		Assert.assertTrue("Expecting to have a column name PARENTAGE.", this.hasColumnHeader(tableHeaderList, "PARENTAGE"));
		Assert.assertTrue("Expecting to have a column name GID.", this.hasColumnHeader(tableHeaderList, "GID"));
		Assert.assertTrue("Expecting to have a column name SEED SOURCE.", this.hasColumnHeader(tableHeaderList, "SEED SOURCE"));
	}

	private boolean hasColumnHeader(final List<TableHeader> tableHeaderList, final String columnName) {
		for (final TableHeader tableHeader : tableHeaderList) {
			if (tableHeader.getColumnName().equals(columnName)) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void testGetAdvancedNurseryTableHeader_returnsTheValueFromOntology() throws MiddlewareQueryException {
		final Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.TRIAL_INSTANCE_FACTOR.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.REP_NO.getId())).thenReturn(fromOntology);

		final List<TableHeader> tableHeaderList = this.advancingController.getAdvancedStudyTableHeader();
		Assert.assertEquals("Expecting to return 7 columns but returned " + tableHeaderList.size(), 7, tableHeaderList.size());

		for (final TableHeader tableHeader : tableHeaderList) {
			Assert.assertEquals("Expecting name from ontology but didn't.", fromOntology.getName(), tableHeader.getColumnName());
		}
	}

	@Test
	public void testPostAdvanceNursery() throws RuleException, MiddlewareException, FieldbookException {
		// setup

		final AdvancingStudyForm form = new AdvancingStudyForm();
		form.setStudyId("1");
		final ArrayList<ImportedGermplasm> importedGermplasm = new ArrayList<>();
		importedGermplasm.add(Mockito.mock(ImportedGermplasm.class));

		final Method method = new Method();
		method.setMtype("DER");

		this.preparePostAdvanceNursery(form, method, importedGermplasm);

		// scenario 1, has a method choice and breeding method not a Generative
		Map<String, Object> output = this.advancingController.postAdvanceStudy(form, null, null);

		Assert.assertEquals("should be successful", "1", output.get("isSuccess"));
		Assert.assertEquals("should have at least 1 imported germplasm list", importedGermplasm.size(), output.get("listSize"));
		Assert.assertNotNull("should have advance germplasm change details", output.get("advanceGermplasmChangeDetails"));
		Assert.assertNotNull("should have generated unique id", output.get("uniqueId"));

		form.setMethodChoice(null);
		output = this.advancingController.postAdvanceStudy(form, null, null);
		Assert.assertEquals("should be successful", "1", output.get("isSuccess"));

	}

	@Test
	public void testPostAdvanceNurseryThrowsRuleException() throws MiddlewareException, RuleException, FieldbookException {
		// setup
		final AdvancingStudyForm form = new AdvancingStudyForm();
		form.setStudyId("1");
		final ArrayList<ImportedGermplasm> importedGermplasm = new ArrayList<>();
		final Method method = new Method();
		method.setMtype("DER");

		this.preparePostAdvanceNursery(form, method, importedGermplasm);

		Mockito.when(this.fieldbookService.advanceStudy(Matchers.any(AdvancingStudy.class), ArgumentMatchers.<Workbook>isNull()))
				.thenThrow(Mockito.mock(RuleException.class));

		// scenario 2, has a method throwing exception
		final Map<String, Object> output = this.advancingController.postAdvanceStudy(form, null, null);

		Assert.assertEquals("should fail", "0", output.get("isSuccess"));
		Assert.assertEquals("should have at least 0 imported germplasm list", Integer.valueOf(0), output.get("listSize"));
	}

	@Test
	public void testPostAdvanceNurseryGenerativeMethodError() throws RuleException, MiddlewareException, FieldbookException {
		// setup
		final AdvancingStudyForm form = new AdvancingStudyForm();
		form.setStudyId("1");
		final ArrayList<ImportedGermplasm> importedGermplasm = new ArrayList<>();

		final Method method = new Method();
		method.setMtype("GEN");

		this.preparePostAdvanceNursery(form, method, importedGermplasm);

		// scenario 2, has a method throwing exception
		final Map<String, Object> output = this.advancingController.postAdvanceStudy(form, null, null);

		Assert.assertEquals("should fail", "0", output.get("isSuccess"));
		Assert.assertEquals("should have at least 0 imported germplasm list", 0, output.get("listSize"));
		Assert.assertEquals("should have error message", "error.message", output.get("message"));
	}

	private void preparePostAdvanceNursery(final AdvancingStudyForm form, final Method method, final ArrayList<ImportedGermplasm> importedGermplasm)
			throws RuleException, MiddlewareException, FieldbookException {
		// setup
		form.setMethodChoice("1");
		form.setAdvanceBreedingMethodId("10");

		final AdvanceResult result = new AdvanceResult();
		result.setAdvanceList(importedGermplasm);
		result.setChangeDetails(new ArrayList<AdvanceGermplasmChangeDetail>());

		Mockito.when(this.fieldbookMiddlewareService.getMethodById(Matchers.anyInt())).thenReturn(method);
		Mockito.when(this.fieldbookService.advanceStudy(Matchers.any(AdvancingStudy.class), ArgumentMatchers.<Workbook>isNull())).thenReturn(
                result);
		Mockito.when(this.messageSource.getMessage(Matchers.eq("study.save.advance.error.generative.method"),
				Matchers.any(String[].class), Matchers.eq(LocaleContextHolder.getLocale()))).thenReturn("error.message");

		Mockito.doNothing().when(this.paginationListSelection).addAdvanceDetails(Matchers.anyString(), Matchers.eq(form));

	}

	@Test
	public void testDeleteImportedGermplasmEntriesIfDeleted() {
		List<ImportedGermplasm> importedGermplasms = new ArrayList<ImportedGermplasm>();
		for (int i = 0; i < 10; i++) {
			final ImportedGermplasm germplasm = new ImportedGermplasm();
			germplasm.setEntryId(i);
			importedGermplasms.add(germplasm);
		}
		final String[] entries = {"1", "2", "3"};
		importedGermplasms = this.advancingController.deleteImportedGermplasmEntries(importedGermplasms, entries);
		Assert.assertEquals("Should have a total of 7 germplasms remaining", 7, importedGermplasms.size());
	}

	private List<ImportedGermplasm> generateGermplasm() {
		final List<ImportedGermplasm> importedGermplasms = new ArrayList<ImportedGermplasm>();
		for (int i = 0; i < 10; i++) {
			final ImportedGermplasm germplasm = new ImportedGermplasm();
			germplasm.setEntryId(i);
            germplasm.setDesig("Design"+i);
            germplasm.setGid(i+"");
            germplasm.setSource("Source"+i);
            germplasm.setCross("Cross"+i);
			importedGermplasms.add(germplasm);
		}
		return importedGermplasms;
	}

	@Test
	public void testDeleteImportedGermplasmEntriesIfNoneDeleted() {
		List<ImportedGermplasm> importedGermplasms = this.generateGermplasm();
		final String[] entries = {};
		importedGermplasms = this.advancingController.deleteImportedGermplasmEntries(importedGermplasms, entries);
		Assert.assertEquals("Should have a total of 10 germplasms, since nothing is deleted", 10, importedGermplasms.size());
	}

	@Test
	public void testSetupAdvanceReviewDataList() {
		final List<ImportedGermplasm> importedGermplasms = this.generateGermplasm();
		final List<Map<String, Object>> mapInfos = this.advancingController.setupAdvanceReviewDataList(importedGermplasms);
		Assert.assertEquals("Should have the same number of records", importedGermplasms.size(), mapInfos.size());
	}

    @Test
    public void testShowAdvanceNurseryGetSuccess(){
		Mockito.doReturn("ASDFDSAGFDGHSFDJ").when(this.contextUtil).getCurrentProgramUUID();
		final Project project = new Project();
		final CropType cropType = new CropType();
		cropType.setCropName("maize");
		project.setCropType(cropType);
		project.setUniqueID("ASDASFGSDSDSDSDSDSDSD");

		Mockito.doReturn(project).when(this.contextUtil).getProjectInContext();
		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook(10, StudyTypeDto.getTrialDto());
		WorkbookTestDataInitializer.setTrialObservations(workbook);
		workbook.getStudyDetails().setId(1011);
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);
        Mockito.when(this.fieldbookMiddlewareService.getStudyDataSet(Mockito.anyInt())).thenReturn(workbook);
		final Variable variable = this.createSelectionVariable();
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(205);
		measurementVariable.setVariableType(VariableType.SELECTION_METHOD);
		measurementVariable.setName("SinglePlant");

		Mockito.when(this.variableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), measurementVariable.getTermId(), false)).thenReturn(variable);

		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setVariables(Lists.newArrayList(measurementVariable));
		Mockito.when(this.datasetService.getDataset(Mockito.anyInt())).thenReturn(datasetDTO);
		Mockito.when(this.fieldbookProperties.getProgramBreedingMethodsUrl()).thenReturn("programBreedingMethodUrl");
        final Project testProject = new Project();
        testProject.setProjectId(1L);

        final AdvancingStudyForm form = new AdvancingStudyForm();
        final Model model = new ExtendedModelMap();

		final String returnTemplatePage = this.advancingController.show(form, model, this.request, this.session, 212, null, null, null);

        Assert.assertEquals("StudyManager/advanceStudyModal",returnTemplatePage);
        final Map<String,Object> modelMap = model.asMap();
        Assert.assertEquals(21,((List<ChoiceKeyVal>)modelMap.get("yearChoices")).size());
        Assert.assertEquals(12,((List<ChoiceKeyVal>)modelMap.get("monthChoices")).size());

        Assert.assertEquals("1",form.getMethodChoice());
        Assert.assertEquals("1",form.getLineChoice());
        Assert.assertEquals("1",form.getLineSelected());
        Assert.assertEquals("1",form.getAllPlotsChoice());
        Assert.assertEquals("205",form.getDefaultMethodId());
        Assert.assertEquals(1,form.getMethodVariates().size());
        Assert.assertEquals(205,form.getMethodVariates().get(0).getId().intValue());
        Assert.assertEquals("SinglePlant",form.getMethodVariates().get(0).getName());
    }

	@Test
    public void testApplyChangeDetailsSuccess() throws IOException {
        final List<ImportedGermplasm> importedGermplasmList = generateGermplasm();
        Mockito.when(this.userSelection.getImportedAdvancedGermplasmList()).thenReturn(importedGermplasmList);

        final AdvanceGermplasmChangeDetail[] advanceGermplasmChangeDetailArray = generateAdvanceGermPlasmChangeDetails();
        final ObjectMapper mapper = new ObjectMapper();
        final String responseData = mapper.writeValueAsString(advanceGermplasmChangeDetailArray);


        final Map<String, Object> response = this.advancingController.applyChangeDetails(responseData);
        Assert.assertEquals("1",response.get("isSuccess"));
        Assert.assertEquals(9,response.get("listSize"));

    }

    private AdvanceGermplasmChangeDetail[] generateAdvanceGermPlasmChangeDetails(){
        final List<AdvanceGermplasmChangeDetail> advanceGermplasmChangeDetailList = Lists.newArrayList();

        final AdvanceGermplasmChangeDetail advanceGermplasmChangeDetail = new AdvanceGermplasmChangeDetail(1,1,"newAdvanceName","oldAdvanceName");

        advanceGermplasmChangeDetailList.add(advanceGermplasmChangeDetail);

        final AdvanceGermplasmChangeDetail advanceGermplasmChangeDetailUpdated = new AdvanceGermplasmChangeDetail(3,1,"newAdvanceName","oldAdvanceName");

        advanceGermplasmChangeDetailList.add(advanceGermplasmChangeDetailUpdated);

        return advanceGermplasmChangeDetailList.toArray(new AdvanceGermplasmChangeDetail[advanceGermplasmChangeDetailList.size()]);
    }

    @Test
    public void testShowAdvanceNurserySuccess(){
        final List<ImportedGermplasm> importedGermplasmList = generateGermplasm();
        Mockito.when(this.userSelection.getImportedAdvancedGermplasmList()).thenReturn(importedGermplasmList);

        final Term fromOntology = new Term();
        Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
        Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
        Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
        Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
        Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);

        Mockito.when(this.request.getParameter("uniqueId")).thenReturn("123");

        final AdvancingStudyForm form = new AdvancingStudyForm();
        final Model model = new ExtendedModelMap();

        final String templateUrl = this.advancingController.showAdvanceStudy(form,null,model,this.request);
        Assert.assertEquals("StudyManager/saveAdvanceStudy",templateUrl);
        final Map<String,Object> modelMap = model.asMap();
        final List<Map<String, Object>> listOfGermPlasm = (List<Map<String, Object>>)modelMap.get("advanceDataList");
        Assert.assertEquals(10,listOfGermPlasm.size());

        this.assertGermPlasmList(listOfGermPlasm,0);

        Assert.assertEquals(10,form.getEntries());
        Assert.assertEquals(123l,form.getUniqueId().longValue());

    }

    @Test
    public void testShowAdvanceNurseryThrowNumberFormatException(){
        final AdvancingStudyForm form = new AdvancingStudyForm();
        final String templateUrl = this.advancingController.showAdvanceStudy(form,null,null,this.request);

        Assert.assertEquals(0,form.getEntries());
        Assert.assertEquals(0,form.getGermplasmList().size());

    }

    @Test
    public void testDeleteAdvanceNurseryEntriesSuccess(){

        final List<ImportedGermplasm> importedGermplasmList = generateGermplasm();
        Mockito.when(this.userSelection.getImportedAdvancedGermplasmList()).thenReturn(importedGermplasmList);

        Mockito.when(this.request.getParameter("entryNums")).thenReturn("0,1,2");

        Mockito.when(this.request.getParameter("uniqueId")).thenReturn("123");

        final AdvancingStudyForm form = new AdvancingStudyForm();
        final Model model = new ExtendedModelMap();

        final String templateUrl = this.advancingController.deleteAdvanceStudyEntries(form,null,model,this.request);

        Assert.assertEquals("StudyManager/saveAdvanceStudy",templateUrl);
        final Map<String,Object> modelMap = model.asMap();
        final List<Map<String, Object>> listOfGermPlasm = (List<Map<String, Object>>)modelMap.get("advanceDataList");
        Assert.assertEquals(7,listOfGermPlasm.size());
        this.assertGermPlasmList(listOfGermPlasm,3);
        Assert.assertEquals(7,form.getEntries());
        Assert.assertEquals(123l,form.getUniqueId().longValue());

    }

    @Test
    public void testDeleteAdvanceNurseryEntriesSuccessThrowNumberFormatException(){
        final AdvancingStudyForm form = new AdvancingStudyForm();
        final String templateUrl = this.advancingController.deleteAdvanceStudyEntries(form,null,null,this.request);

        Assert.assertEquals(0,form.getEntries());
        Assert.assertEquals(0,form.getGermplasmList().size());

    }

    @Test
    public void testCountPlotsSuccess(){
        final Workbook workBook = new Workbook();
        workBook.setMeasurementDatesetId(2);
        Mockito.when(this.userSelection.getWorkbook()).thenReturn(workBook);
        Mockito.when(this.fieldbookMiddlewareService.countPlotsWithRecordedVariatesInDataset(Mockito.anyInt(),Mockito.isA(List.class))).thenReturn(new Integer(2));
        final int plotCount = this.advancingController.countPlots("1,2");
        Assert.assertEquals(2,plotCount);

    }

    @Test
    public void testCheckMethodTypeModeLineSuccess(){
        final Workbook workBook = new Workbook();
        final List<MeasurementRow> observations = generateMeasurementRows();
        workBook.setObservations(observations);
        Mockito.when(this.userSelection.getWorkbook()).thenReturn(workBook);

        final List<Method> methods = Lists.newArrayList();
        final Method nonBulkMethod = new Method();
        nonBulkMethod.setGeneq(1510);
        methods.add(nonBulkMethod);
        Mockito.when(this.germplasmDataManager.getMethodsByIDs(Mockito.isA(List.class))).thenReturn(methods);
        final String methodType = this.advancingController.checkMethodTypeMode(12);
        Assert.assertEquals("LINE",methodType);
    }

    @Test
    public void testCheckMethodTypeModeBulkSuccess(){
        final Workbook workBook = new Workbook();
        final List<MeasurementRow> observations = generateMeasurementRows();
        workBook.setObservations(observations);
        Mockito.when(this.userSelection.getWorkbook()).thenReturn(workBook);

        final List<Method> methods = Lists.newArrayList();
        final Method bulkMethod = new Method();
        bulkMethod.setGeneq(1490);
        methods.add(bulkMethod);

        Mockito.when(this.germplasmDataManager.getMethodsByIDs(Mockito.isA(List.class))).thenReturn(methods);
        final String methodType = this.advancingController.checkMethodTypeMode(12);
        Assert.assertEquals("BULK",methodType);
    }

    @Test
    public void testCheckMethodTypeModeMixedSuccess(){
        final Workbook workBook = new Workbook();
        final List<MeasurementRow> observations = generateMeasurementRows();
        workBook.setObservations(observations);
        Mockito.when(this.userSelection.getWorkbook()).thenReturn(workBook);

        final List<Method> methods = Lists.newArrayList();

        final Method nonBulkMethod = new Method();
        nonBulkMethod.setGeneq(1490);
        methods.add(nonBulkMethod);

        final Method bulkMethod = new Method();
        bulkMethod.setGeneq(1510);
        methods.add(bulkMethod);


        Mockito.when(this.germplasmDataManager.getMethodsByIDs(Mockito.isA(List.class))).thenReturn(methods);
        final String methodType = this.advancingController.checkMethodTypeMode(12);
        Assert.assertEquals("MIXED",methodType);
    }

    @Test
    public void testCheckMethodTypeError(){
        final Workbook workBook = new Workbook();
        final List<MeasurementRow> observations = Lists.newArrayList();
        workBook.setObservations(observations);

        Mockito.when(this.userSelection.getWorkbook()).thenReturn(workBook);
        Mockito.when(this.messageSource.getMessage(Mockito.isA(String.class),Mockito.any(Object[].class),Mockito.isA(Locale.class))).thenReturn("The nursery has no methods defined under");
        final List<Method> methods = Lists.newArrayList();

        final String methodType = this.advancingController.checkMethodTypeMode(12);
        System.out.println(methodType);
        Assert.assertTrue(methodType.contains("The nursery has no methods defined under"));
    }

    private void assertGermPlasmList( final List<Map<String, Object>> listOfGermPlasm ,int position){
        for(final Map<String,Object> entryMap : listOfGermPlasm){
            Assert.assertEquals("Design"+position,entryMap.get("desig"));
            Assert.assertEquals("Source"+position,entryMap.get("source"));
            Assert.assertEquals("Cross"+position,entryMap.get("parentage"));
            Assert.assertEquals("Pending",entryMap.get("gid"));
            position++;
        }
    }

    private List<MeasurementRow> generateMeasurementRows(){
        final List<MeasurementRow> observations = Lists.newArrayList();
        final MeasurementRow row1 = new MeasurementRow();

        final List<MeasurementData> row1Data = Lists.newArrayList();
        final MeasurementData measurementData = new MeasurementData();
        measurementData.setcValueId("13");
        final MeasurementVariable measurementVariable = new MeasurementVariable("name", "description", "scale", "method", "property", "dataType",
                "value", "label");
        measurementVariable.setTermId(12);

        measurementData.setMeasurementVariable(measurementVariable);
        row1Data.add(measurementData);
        row1.setDataList(row1Data);

        observations.add(row1);
        return observations;
    }

	private Variable createSelectionVariable() {
		final Variable variable =new Variable();
		variable.setName("NPSEL");
		variable.setDefinition("Number of plants selected - counted (number)");
		variable.addVariableType(VariableType.SELECTION_METHOD);
		variable.setAllowsFormula(false);
		variable.setId(205);
		Scale scale = new Scale();
		scale.setDataType(DataType.NUMERIC_VARIABLE);
		scale.setName("Number");
		scale.setDefinition("Number");
		scale.setId(6040);
		scale.setVocabularyId(1030);
		scale.setObsolete(false);

		Property property = new Property();
		property.setName("Breeding method");
		property.setId(2600);
		property.setVocabularyId(1010);
		property.setDefinition("");
		property.setObsolete(false);
		property.addClass("Breedingprocess");

		org.generationcp.middleware.domain.ontology.Method method = new org.generationcp.middleware.domain.ontology.Method();

		method.setName("Counted");
		method.setDefinition("Counting method");
		method.setObsolete(false);
		method.setId(4080);
		method.setId(1020);

		variable.setScale(scale);
		variable.setProperty(property);
		variable.setMethod(method);
		return variable;
	}
}
