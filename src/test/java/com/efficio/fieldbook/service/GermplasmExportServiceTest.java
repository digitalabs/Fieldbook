
package com.efficio.fieldbook.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.pojo.GermplasmListExportInputValues;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;

public class GermplasmExportServiceTest {

	private static final String CATEG_CODE_VALUE = "CATEG_CODE_VALUE";
	private static final String DESIG_VALUE = "DESIG VALUE";
	private static final String CROSS_VALUE = "Cross value";
	private static final String SOURCE_VALUE = "Source value";
	private static final String ENTRY_CODE_VALUE = "9742";
	private static final String ENTRY_NO_VALUE = "1";
	private static final String GID_VALUE = "12345";
	private static final String CHECK_VALUE = "1";
	private static final String NUMERIC_VARIABLE = "NUMERIC VARIABLE";
	private static final String TEST_METHOD = "TEST METHOD";
	private static final String TEST_SCALE = "TEST SCALE";
	private static final String TEST_PROPERTY = "TEST PROPERTY";
	private static final String TEST_DESCRIPTION = "TEST DESCRIPTION";
	// Styles
	public static final String LABEL_STYLE = "labelStyle";
	public static final String HEADING_STYLE = "headingStyle";
	public static final String NUMERIC_STYLE = "numericStyle";

	private static final String CURRENT_USER_NAME = "User User";
	private static final int CURRENT_USER_ID = 1;
	// Columns
	private static final String ENTRY_NO = "entryNo";
	private static final String GID = "gid";
	private static final String ENTRY_CODE = "entryCode";
	private static final String DESIGNATION = "desig";
	private static final String PARENTAGE = "parentage";
	private static final String SEED_SOURCE = "seedSource";
	private static final String CHECK = "check";
	private static final String ENTRY_NUMBER_STORAGE = "entryNoStorage";

	@InjectMocks
	private GermplasmExportService exportService;
	
	private String testFileName;
	private String sheetName;

	private GermplasmListExportInputValues input;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private UserSelection userSelection;
	
	@Mock
	private ContextUtil contextUtil;

	@Before
	public void setUp() {

		MockitoAnnotations.initMocks(this);

		this.testFileName = "test.csv";
		this.sheetName = "List";
		this.input = this.generateGermplasmListExportInputValues();

		try {

			Mockito.doReturn(this.createStandardVariable(TermId.ENTRY_NO.getId(), GermplasmExportServiceTest.ENTRY_NO))
					.when(this.ontologyService).getStandardVariable(TermId.ENTRY_NO.getId(),contextUtil.getCurrentProgramUUID());
			Mockito.doReturn(this.createStandardVariable(TermId.DESIG.getId(), GermplasmExportServiceTest.DESIGNATION))
					.when(this.ontologyService).getStandardVariable(TermId.DESIG.getId(),contextUtil.getCurrentProgramUUID());
			Mockito.doReturn(this.createStandardVariable(TermId.GID.getId(), GermplasmExportServiceTest.GID)).when(this.ontologyService)
					.getStandardVariable(TermId.GID.getId(),contextUtil.getCurrentProgramUUID());
			Mockito.doReturn(this.createStandardVariable(TermId.CROSS.getId(), GermplasmExportServiceTest.PARENTAGE))
					.when(this.ontologyService).getStandardVariable(TermId.CROSS.getId(),contextUtil.getCurrentProgramUUID());
			Mockito.doReturn(this.createStandardVariable(TermId.SEED_SOURCE.getId(), GermplasmExportServiceTest.SEED_SOURCE))
					.when(this.ontologyService).getStandardVariable(TermId.SEED_SOURCE.getId(),contextUtil.getCurrentProgramUUID());
			Mockito.doReturn(this.createStandardVariable(TermId.ENTRY_CODE.getId(), GermplasmExportServiceTest.ENTRY_CODE))
					.when(this.ontologyService).getStandardVariable(TermId.ENTRY_CODE.getId(),contextUtil.getCurrentProgramUUID());
			Mockito.doReturn(
					this.createStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId(), GermplasmExportServiceTest.ENTRY_NUMBER_STORAGE))
					.when(this.ontologyService).getStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId(),contextUtil.getCurrentProgramUUID());
			Mockito.doReturn(this.createStandardVariable(TermId.CHECK.getId(), GermplasmExportServiceTest.CHECK))
					.when(this.ontologyService).getStandardVariable(TermId.CHECK.getId(),contextUtil.getCurrentProgramUUID());
			Mockito.doReturn(this.getPlotLevelList()).when(this.userSelection).getPlotsLevelList();

		} catch (MiddlewareException e) {

		}

	}

	@Test
	public void test_writeListFactorSectionForTrialManager() {

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet descriptionSheet = wb.createSheet(this.sheetName);
		Map<String, CellStyle> styles = this.createStyles(wb);

		this.exportService = new GermplasmExportService(this.ontologyService, this.userSelection, false);
		this.exportService.writeListFactorSection(styles, descriptionSheet, 1, this.getVisibleColumnMap());

		Assert.assertEquals(GermplasmExportServiceTest.DESIGNATION, descriptionSheet.getRow(1).getCell(0).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.SEED_SOURCE, descriptionSheet.getRow(2).getCell(0).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.ENTRY_CODE, descriptionSheet.getRow(3).getCell(0).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.PARENTAGE, descriptionSheet.getRow(4).getCell(0).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.ENTRY_NO, descriptionSheet.getRow(5).getCell(0).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.GID, descriptionSheet.getRow(6).getCell(0).getStringCellValue());

	}

	@Test
	public void test_writeListFactorSectionForTrialManagerWithNullPlotList() {

		Mockito.doReturn(null).when(this.userSelection).getPlotsLevelList();

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet descriptionSheet = wb.createSheet(this.sheetName);
		Map<String, CellStyle> styles = this.createStyles(wb);

		this.exportService = new GermplasmExportService(this.ontologyService, this.userSelection, false);
		this.exportService.writeListFactorSection(styles, descriptionSheet, 1, this.getVisibleColumnMap());

		Assert.assertNull(descriptionSheet.getRow(1));

	}

	@Test
	public void test_writeListFactorSectionForNurseryManager() {

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet descriptionSheet = wb.createSheet(this.sheetName);
		Map<String, CellStyle> styles = this.createStyles(wb);

		this.exportService = new GermplasmExportService(this.ontologyService, this.userSelection, true);
		this.exportService.writeListFactorSection(styles, descriptionSheet, 1, this.getVisibleColumnMap());

		Assert.assertEquals(GermplasmExportServiceTest.ENTRY_NO, descriptionSheet.getRow(1).getCell(0).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.DESIGNATION, descriptionSheet.getRow(2).getCell(0).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.GID, descriptionSheet.getRow(3).getCell(0).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.PARENTAGE, descriptionSheet.getRow(4).getCell(0).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.SEED_SOURCE, descriptionSheet.getRow(5).getCell(0).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.ENTRY_CODE, descriptionSheet.getRow(6).getCell(0).getStringCellValue());

	}

	@Test
	public void test_writeObservationSheet_ForTrialManager() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet observationSheet = wb.createSheet(this.sheetName);
		Map<String, CellStyle> styles = this.createStyles(wb);

		this.exportService = Mockito.spy(new GermplasmExportService(this.ontologyService, this.userSelection, false));

		Mockito.doReturn(this.generateImportedGermplasms()).when(this.exportService).getImportedGermplasms();

		try {

			this.exportService.writeObservationSheet(styles, observationSheet, this.input);

		} catch (GermplasmListExporterException e) {

		}

		Assert.assertEquals(GermplasmExportServiceTest.DESIGNATION, observationSheet.getRow(0).getCell(0).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.SEED_SOURCE, observationSheet.getRow(0).getCell(1).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.ENTRY_CODE, observationSheet.getRow(0).getCell(2).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.PARENTAGE, observationSheet.getRow(0).getCell(3).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.ENTRY_NO, observationSheet.getRow(0).getCell(4).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.GID, observationSheet.getRow(0).getCell(5).getStringCellValue());

		Assert.assertEquals(GermplasmExportServiceTest.DESIG_VALUE, observationSheet.getRow(1).getCell(0).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.SOURCE_VALUE, observationSheet.getRow(1).getCell(1).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.ENTRY_CODE_VALUE, observationSheet.getRow(1).getCell(2).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.CROSS_VALUE, observationSheet.getRow(1).getCell(3).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.ENTRY_NO_VALUE, observationSheet.getRow(1).getCell(4).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.GID_VALUE, observationSheet.getRow(1).getCell(5).getStringCellValue());

	}

	@Test
	public void test_writeObservationSheet_ForNurseryManager() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet observationSheet = wb.createSheet(this.sheetName);
		Map<String, CellStyle> styles = this.createStyles(wb);

		this.exportService = Mockito.spy(new GermplasmExportService(this.ontologyService, this.userSelection, true));

		Mockito.doReturn(this.generateImportedGermplasms()).when(this.exportService).getImportedGermplasms();

		try {

			this.exportService.writeObservationSheet(styles, observationSheet, this.input);

		} catch (GermplasmListExporterException e) {

		}

		Assert.assertEquals(GermplasmExportServiceTest.ENTRY_NO, observationSheet.getRow(0).getCell(0).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.DESIGNATION, observationSheet.getRow(0).getCell(1).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.GID, observationSheet.getRow(0).getCell(2).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.PARENTAGE, observationSheet.getRow(0).getCell(3).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.SEED_SOURCE, observationSheet.getRow(0).getCell(4).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.ENTRY_CODE, observationSheet.getRow(0).getCell(5).getStringCellValue());

		Assert.assertEquals(GermplasmExportServiceTest.ENTRY_NO_VALUE, observationSheet.getRow(1).getCell(0).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.DESIG_VALUE, observationSheet.getRow(1).getCell(1).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.GID_VALUE, observationSheet.getRow(1).getCell(2).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.CROSS_VALUE, observationSheet.getRow(1).getCell(3).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.SOURCE_VALUE, observationSheet.getRow(1).getCell(4).getStringCellValue());
		Assert.assertEquals(GermplasmExportServiceTest.ENTRY_CODE_VALUE, observationSheet.getRow(1).getCell(5).getStringCellValue());

	}

	@Test
	public void test_getCategoricalCodeValue() {
		SettingDetail settingDetail = this.generateSettingDetail(TermId.CHECK.getId());

		List<ValueReference> possibleValues = new ArrayList<>();
		ValueReference valReference = new ValueReference();
		valReference.setId(Integer.valueOf(GermplasmExportServiceTest.CHECK_VALUE));
		valReference.setKey(GermplasmExportServiceTest.CHECK_VALUE);
		valReference.setName(GermplasmExportServiceTest.CATEG_CODE_VALUE);
		possibleValues.add(valReference);

		settingDetail.setPossibleValues(possibleValues);

		this.exportService = Mockito.spy(new GermplasmExportService(this.ontologyService, this.userSelection, true));

		String categValue = this.exportService.getCategoricalCodeValue(this.generateImportedGermplasm(), settingDetail);

		Assert.assertEquals(GermplasmExportServiceTest.CATEG_CODE_VALUE, categValue);

	}

	@Test
	public void test_getCategoricalCodeValuePossibleValuesIsNull() {
		SettingDetail settingDetail = this.generateSettingDetail(TermId.CHECK.getId());
		settingDetail.setPossibleValues(null);

		this.exportService = Mockito.spy(new GermplasmExportService(this.ontologyService, this.userSelection, true));

		String categValue = this.exportService.getCategoricalCodeValue(this.generateImportedGermplasm(), settingDetail);

		Assert.assertEquals(GermplasmExportServiceTest.CHECK_VALUE, categValue);
	}

	@After
	public void tearDown() {
		File file = new File(this.testFileName);
		file.deleteOnExit();
	}

	private List<ImportedGermplasm> generateImportedGermplasms() {

		List<ImportedGermplasm> importedGermplasms = new ArrayList<>();
		importedGermplasms.add(this.generateImportedGermplasm());

		return importedGermplasms;
	}

	private ImportedGermplasm generateImportedGermplasm() {

		ImportedGermplasm importedGermplasm = new ImportedGermplasm();
		importedGermplasm.setCheck(GermplasmExportServiceTest.CHECK_VALUE);
		importedGermplasm.setIndex(0);
		importedGermplasm.setGid(GermplasmExportServiceTest.GID_VALUE);
		importedGermplasm.setEntryCode(GermplasmExportServiceTest.ENTRY_CODE_VALUE);
		importedGermplasm.setEntryId(Integer.valueOf(GermplasmExportServiceTest.ENTRY_NO_VALUE));
		importedGermplasm.setSource(GermplasmExportServiceTest.SOURCE_VALUE);
		importedGermplasm.setCross(GermplasmExportServiceTest.CROSS_VALUE);
		importedGermplasm.setDesig(GermplasmExportServiceTest.DESIG_VALUE);

		return importedGermplasm;

	}

	private Map<String, Boolean> getVisibleColumnMap() {
		Map<String, Boolean> visibleColumnMap = new HashMap<String, Boolean>();

		visibleColumnMap.put(String.valueOf(TermId.GID.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.CROSS.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.ENTRY_NO.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.DESIG.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.SEED_SOURCE.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.ENTRY_CODE.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.ENTRY_NUMBER_STORAGE.getId()), null);
		visibleColumnMap.put(String.valueOf(TermId.CHECK.getId()), false);

		return visibleColumnMap;

	}

	private List<SettingDetail> getPlotLevelList() {
		List<SettingDetail> plotLevelList = new ArrayList<>();

		for (Entry<String, Boolean> entry : this.getVisibleColumnMap().entrySet()) {
			plotLevelList.add(this.generateSettingDetail(Integer.valueOf(entry.getKey())));
		}

		return plotLevelList;

	}

	private SettingDetail generateSettingDetail(Integer termId) {
		SettingDetail settingDetail = new SettingDetail();
		settingDetail.setHidden(false);
		SettingVariable var = new SettingVariable();
		var.setCvTermId(termId);
		settingDetail.setVariable(var);

		StandardVariable stdVar;
		try {
			stdVar = this.ontologyService.getStandardVariable(termId,contextUtil.getCurrentProgramUUID());

			settingDetail.getVariable().setName(stdVar.getName());
			settingDetail.getVariable().setDescription(stdVar.getDescription());
			settingDetail.getVariable().setProperty(stdVar.getProperty().getName());
			settingDetail.getVariable().setScale(stdVar.getScale().getName());
			settingDetail.getVariable().setMethod(stdVar.getMethod().getName());
			settingDetail.getVariable().setDataType(stdVar.getDataType().getName());

		} catch (MiddlewareException e) {
			// do nothing
		}

		return settingDetail;
	}

	private Map<String, CellStyle> createStyles(HSSFWorkbook wb) {
		Map<String, CellStyle> styles = new HashMap<String, CellStyle>();

		// set cell style for labels in the description sheet
		CellStyle labelStyle = wb.createCellStyle();
		labelStyle.setFillForegroundColor(IndexedColors.BROWN.getIndex());
		labelStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		Font labelFont = wb.createFont();
		labelFont.setColor(IndexedColors.WHITE.getIndex());
		labelFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		labelStyle.setFont(labelFont);
		styles.put(GermplasmExportServiceTest.LABEL_STYLE, labelStyle);

		// set cell style for headings in the description sheet
		CellStyle headingStyle = wb.createCellStyle();
		headingStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
		headingStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		Font headingFont = wb.createFont();
		headingFont.setColor(IndexedColors.WHITE.getIndex());
		headingFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headingStyle.setFont(headingFont);
		styles.put(GermplasmExportServiceTest.HEADING_STYLE, headingStyle);

		// set cell style for numeric values (left alignment)
		CellStyle numericStyle = wb.createCellStyle();
		numericStyle.setAlignment(CellStyle.ALIGN_LEFT);
		styles.put(GermplasmExportServiceTest.NUMERIC_STYLE, numericStyle);

		return styles;
	}

	private StandardVariable createStandardVariable(int id, String name) {
		StandardVariable stdVar = new StandardVariable();
		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription(GermplasmExportServiceTest.TEST_DESCRIPTION);

		Term prop = new Term();
		prop.setName(GermplasmExportServiceTest.TEST_PROPERTY);
		stdVar.setProperty(prop);

		Term scale = new Term();
		scale.setName(GermplasmExportServiceTest.TEST_SCALE);
		stdVar.setScale(scale);

		Term method = new Term();
		method.setName(GermplasmExportServiceTest.TEST_METHOD);
		stdVar.setMethod(method);

		Term dataType = new Term();
		dataType.setName(GermplasmExportServiceTest.NUMERIC_VARIABLE);
		stdVar.setDataType(dataType);

		return stdVar;
	}

	private GermplasmListExportInputValues generateGermplasmListExportInputValues() {
		GermplasmListExportInputValues input = new GermplasmListExportInputValues();

		input.setFileName(this.testFileName);
		input.setGermplasmList(null);
		input.setOwnerName(GermplasmExportServiceTest.CURRENT_USER_NAME);
		input.setCurrentLocalIbdbUserId(GermplasmExportServiceTest.CURRENT_USER_ID);
		input.setExporterName(GermplasmExportServiceTest.CURRENT_USER_NAME);
		input.setVisibleColumnMap(this.getVisibleColumnMap());
		return input;
	}

}
