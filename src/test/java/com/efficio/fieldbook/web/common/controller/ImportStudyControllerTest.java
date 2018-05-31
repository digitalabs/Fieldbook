
package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.GermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.study.ImportStudyType;
import com.efficio.fieldbook.web.study.service.ExcelImportStudyService;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.lowagie.text.pdf.codec.Base64.InputStream;
import junit.framework.Assert;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.service.FileService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImportStudyControllerTest {

	private static final String SAMPLE_FILE_XLS = "SampleFile.xls";
	private static final String SAMPLE_FILE_XLSX = "SampleFile.xlsx";
	private static final String SAMPLE_FILE_CSV = "SampleFile.csv";

	private static final String ORIGINAL_DESIG_PREFIX = "OrigDesig";
	private static final String NEW_DESIG_PREFIX = "NewDesig";

	private static final int TEST_USER_ID = 1;
	private static final int APPLY_CHANGE_DETAIL_TEST_OBSERVATIONS = 3;

	@Mock
	private BindingResult result;
	@Mock
	private MultipartFile file;
	@Mock
	private FileService fileService;
	@Mock
	private ExcelImportStudyService excelImportStudyService;
	@Mock
	private InputStream inputStream;
	@Mock
	private OntologyService ontologyService;
	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Mock
	private UserSelection userSelection;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private WorkbenchService workbenchService;

	@Mock
	private ContextUtil contextUtil;

	private Workbook workbook;

	@InjectMocks
	private final ImportStudyController unitUnderTest = new ImportStudyController();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

	}

	@Test
	public void testSaveImportedFiles() {
		final CreateTrialForm form = Mockito.mock(CreateTrialForm.class);
		final Model model = Mockito.mock(Model.class);
		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook();
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);
		final Map<String, Object> result = this.unitUnderTest.saveImportedFiles(form, model);
		Assert.assertEquals("1", result.get(ImportStudyController.SUCCESS));
		Mockito.verify(this.fieldbookMiddlewareService).saveMeasurementRows(workbook,
				this.contextUtil.getCurrentProgramUUID(), true);
		Mockito.verify(this.fieldbookService).saveStudyColumnOrdering(
				this.userSelection.getWorkbook().getStudyDetails().getId(),
				this.userSelection.getWorkbook().getStudyDetails().getStudyName(), form.getColumnOrders(),
				this.userSelection.getWorkbook());
	}

	@Test
	public void testApplyChangeDetailsAddGidName() throws IOException, FieldbookException {
		final String dummyUserResponse = "";
		final GermplasmChangeDetail[] changeDetails = this.createTestChangeDetail();
		for (final GermplasmChangeDetail changeDetail : changeDetails) {
			changeDetail.setStatus(ImportStudyController.STATUS_ADD_NAME_TO_GID);
		}

		final Workbook testWorkbook = WorkbookDataUtil
				.getTestWorkbook(ImportStudyControllerTest.APPLY_CHANGE_DETAIL_TEST_OBSERVATIONS, StudyTypeDto.getNurseryDto());
		this.setTestMeasurementValues(testWorkbook);

		Mockito.when(this.userSelection.getWorkbook()).thenReturn(testWorkbook);
		Mockito.when(this.objectMapper.readValue(dummyUserResponse, GermplasmChangeDetail[].class))
				.thenReturn(changeDetails);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(Mockito.mock(Project.class));
		Mockito.when(this.workbenchService.getCurrentIbdbUserId(Matchers.anyLong(), Matchers.anyInt()))
				.thenReturn(ImportStudyControllerTest.TEST_USER_ID);

		this.unitUnderTest.applyChangeDetails(dummyUserResponse);
		final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

		Mockito.verify(this.fieldbookMiddlewareService).addGermplasmNames(captor.capture());

		final List<Name> names = captor.getValue();
		Assert.assertEquals(ImportStudyControllerTest.APPLY_CHANGE_DETAIL_TEST_OBSERVATIONS, names.size());

		// verify that the expected name values are the ones passed to the
		// fieldbook service for saving
		for (int i = 0; i < ImportStudyControllerTest.APPLY_CHANGE_DETAIL_TEST_OBSERVATIONS; i++) {
			Assert.assertEquals(changeDetails[i].getNewDesig(), names.get(i).getNval());
		}

		// verify that the values in observation data are equal to expected
		// values coming from the GermplasmChangeDetail object
		int i = 0;
		String instanceNumber = "1";
		for (final MeasurementRow row : testWorkbook.getObservations()) {
			final MeasurementData trialInst = row.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId());
			if (!instanceNumber.equals(trialInst.getValue())) {
				i = 0;
				instanceNumber = trialInst.getValue();
			}

			final MeasurementData desig = row.getMeasurementData(TermId.DESIG.getId());
			Assert.assertEquals(changeDetails[i].getNewDesig(), desig.getValue());

			final MeasurementData gid = row.getMeasurementData(TermId.GID.getId());
			Assert.assertEquals(changeDetails[i].getOriginalGid(), gid.getValue());
			i++;
		}
	}

	@Test
	@Ignore(value = "This test fails on mvn command line because the WorkbookDataUtil class used to setup data "
			+ " has an internal state which is modified by other tests see calls to com.efficio.fieldbook.utils.test.WorkbookDataUtil.setTestWorkbook(Workbook), "
			+ " casuing this test to fail when run alongside other tests.")
	public void testApplyChangeDetailsAddGermplasmAndName() throws IOException, FieldbookException {
		final String dummyUserResponse = "";
		final Integer newGidOffset = 5;
		final GermplasmChangeDetail[] changeDetails = this.createTestChangeDetail();
		for (final GermplasmChangeDetail changeDetail : changeDetails) {
			changeDetail.setStatus(ImportStudyController.STATUS_ADD_GERMPLASM_AND_NAME);
		}

		final Workbook testWorkbook = WorkbookDataUtil
				.getTestWorkbook(ImportStudyControllerTest.APPLY_CHANGE_DETAIL_TEST_OBSERVATIONS, StudyTypeDto.getNurseryDto());
		this.setTestMeasurementValues(testWorkbook);

		final List<Integer> newGids = new ArrayList<>();
		for (int i = 0; i < ImportStudyControllerTest.APPLY_CHANGE_DETAIL_TEST_OBSERVATIONS; i++) {
			newGids.add(newGidOffset + i);
		}

		Mockito.when(this.userSelection.getWorkbook()).thenReturn(testWorkbook);
		Mockito.when(this.objectMapper.readValue(dummyUserResponse, GermplasmChangeDetail[].class))
				.thenReturn(changeDetails);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(Mockito.mock(Project.class));
		Mockito.when(this.workbenchService.getCurrentIbdbUserId(Matchers.anyLong(), Matchers.anyInt()))
				.thenReturn(ImportStudyControllerTest.TEST_USER_ID);
		Mockito.when(this.fieldbookMiddlewareService.addGermplasm(Matchers.anyList())).thenReturn(newGids);

		this.unitUnderTest.applyChangeDetails(dummyUserResponse);
		final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

		Mockito.verify(this.fieldbookMiddlewareService).addGermplasm(captor.capture());

		final List<Pair<Germplasm, Name>> germplasmPairs = captor.getValue();
		Assert.assertEquals(ImportStudyControllerTest.APPLY_CHANGE_DETAIL_TEST_OBSERVATIONS, germplasmPairs.size());

		// verify that the expected name values are the ones passed to the
		// fieldbook service for saving
		for (int i = 0; i < ImportStudyControllerTest.APPLY_CHANGE_DETAIL_TEST_OBSERVATIONS; i++) {
			final Pair<Germplasm, Name> pair = germplasmPairs.get(i);
			final Name name = pair.getRight();
			Assert.assertEquals(changeDetails[i].getNewDesig(), name.getNval());
		}

		// verify that the values in observation data are equal to expected
		// values coming from the GermplasmChangeDetail object and the new
		// GID saved from database
		int i = 0;
		for (final MeasurementRow row : testWorkbook.getObservations()) {
			final MeasurementData desig = row.getMeasurementData(TermId.DESIG.getId());
			Assert.assertEquals(changeDetails[i].getNewDesig(), desig.getValue());

			final MeasurementData gid = row.getMeasurementData(TermId.GID.getId());
			Assert.assertEquals(newGids.get(i).toString(), gid.getValue());
			i++;
		}
	}

	protected GermplasmChangeDetail[] createTestChangeDetail() {
		final GermplasmChangeDetail[] changeDetails = new GermplasmChangeDetail[ImportStudyControllerTest.APPLY_CHANGE_DETAIL_TEST_OBSERVATIONS];

		for (int i = 0; i < changeDetails.length; i++) {
			changeDetails[i] = new GermplasmChangeDetail(i, ImportStudyControllerTest.ORIGINAL_DESIG_PREFIX + i,
					Integer.toString(i), ImportStudyControllerTest.NEW_DESIG_PREFIX + i, Integer.toString(i + 1), "1",
					Integer.toString(i), "1");
			changeDetails[i].setImportDate("2015-12-31");
		}

		return changeDetails;
	}

	protected void setTestMeasurementValues(final Workbook workbook) {

		final List<MeasurementRow> testValues = workbook.getObservations();

		for (int i = 0; i < ImportStudyControllerTest.APPLY_CHANGE_DETAIL_TEST_OBSERVATIONS; i++) {
			final MeasurementData desig = testValues.get(i).getMeasurementData(TermId.DESIG.getId());
			desig.setValue(ImportStudyControllerTest.ORIGINAL_DESIG_PREFIX + i);

			final MeasurementData gid = testValues.get(i).getMeasurementData(TermId.GID.getId());
			gid.setValue(Integer.toString(i));

			final MeasurementData entryNum = testValues.get(i).getMeasurementData(TermId.ENTRY_NO.getId());
			entryNum.setValue(Integer.toString(i));
		}
	}

	@Test
	public void testValidateImportFile_ForExcel_FileIsXLSX() {
		Mockito.when(this.file.getOriginalFilename()).thenReturn(ImportStudyControllerTest.SAMPLE_FILE_XLSX);
		final ImportStudyType importType = ImportStudyType.IMPORT_NURSERY_EXCEL;
		this.unitUnderTest.validateImportFile(this.file, this.result, importType);

		Mockito.verify(this.result, Mockito.times(0)).rejectValue("file",
				AppConstants.FILE_NOT_EXCEL_ERROR.getString());
	}

	@Test
	public void testValidateImportFile_ForExcel_FileIsXLS() {
		Mockito.when(this.file.getOriginalFilename()).thenReturn(ImportStudyControllerTest.SAMPLE_FILE_XLS);
		final ImportStudyType importType = ImportStudyType.IMPORT_NURSERY_EXCEL;
		this.unitUnderTest.validateImportFile(this.file, this.result, importType);

		Mockito.verify(this.result, Mockito.times(0)).rejectValue("file",
				AppConstants.FILE_NOT_EXCEL_ERROR.getString());
	}

	@Test
	public void testValidateImportFile_ForExcel_FileIsNotXLS() {
		Mockito.when(this.file.getOriginalFilename()).thenReturn(ImportStudyControllerTest.SAMPLE_FILE_CSV);
		final ImportStudyType importType = ImportStudyType.IMPORT_NURSERY_EXCEL;
		this.unitUnderTest.validateImportFile(this.file, this.result, importType);

		Mockito.verify(this.result, Mockito.times(1)).rejectValue("file",
				AppConstants.FILE_NOT_EXCEL_ERROR.getString());
	}

	@Test
	public void testValidateImportFile_ForKsuExcel_FileIsXLS() {
		Mockito.when(this.file.getOriginalFilename()).thenReturn(ImportStudyControllerTest.SAMPLE_FILE_XLS);
		final ImportStudyType importType = ImportStudyType.IMPORT_KSU_EXCEL;
		this.unitUnderTest.validateImportFile(this.file, this.result, importType);

		Mockito.verify(this.result, Mockito.times(0)).rejectValue("file",
				AppConstants.FILE_NOT_EXCEL_ERROR.getString());
	}

	@Test
	public void testValidateImportFile_ForKsuExcel_FileIsXLSX() {
		Mockito.when(this.file.getOriginalFilename()).thenReturn(ImportStudyControllerTest.SAMPLE_FILE_XLSX);
		final ImportStudyType importType = ImportStudyType.IMPORT_KSU_EXCEL;
		this.unitUnderTest.validateImportFile(this.file, this.result, importType);

		Mockito.verify(this.result, Mockito.times(0)).rejectValue("file",
				AppConstants.FILE_NOT_EXCEL_ERROR.getString());
	}

	@Test
	public void testValidateImportFile_ForKsuExcel_FileIsNotXLS() {
		Mockito.when(this.file.getOriginalFilename()).thenReturn(ImportStudyControllerTest.SAMPLE_FILE_CSV);
		final ImportStudyType importType = ImportStudyType.IMPORT_KSU_EXCEL;
		this.unitUnderTest.validateImportFile(this.file, this.result, importType);

		Mockito.verify(this.result, Mockito.times(1)).rejectValue("file",
				AppConstants.FILE_NOT_EXCEL_ERROR.getString());
	}

}
