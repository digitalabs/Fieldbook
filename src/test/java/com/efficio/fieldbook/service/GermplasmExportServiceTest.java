package com.efficio.fieldbook.service;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.pojo.GermplasmListExportInputValues;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

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
	//Styles
	public static final String LABEL_STYLE = "labelStyle";
	public static final String HEADING_STYLE = "headingStyle";
	public static final String NUMERIC_STYLE = "numericStyle";

	private static final String CURRENT_USER_NAME = "User User";
	private static final int CURRENT_USER_ID = 1;
	//Columns
    private static final String ENTRY_NO = "entryNo";
    private static final String GID = "gid";
    private static final String ENTRY_CODE = "entryCode";
    private static final String DESIGNATION = "desig";
    private static final String PARENTAGE = "parentage";
    private static final String SEED_SOURCE = "seedSource";
    private static final String CHECK = "check";
    private static final String ENTRY_NUMBER_STORAGE = "entryNoStorage";
	
	private GermplasmExportService exportService;
	private String testFileName;
	private String sheetName;
	
	private GermplasmListExportInputValues input;
	
	@Mock
	private OntologyService ontologyService;
	
	@Mock
	private UserSelection userSelection;

	@Before
	public void setUp() {
		
		MockitoAnnotations.initMocks(this);
		
		testFileName = "test.csv";
		sheetName = "List";
		input = generateGermplasmListExportInputValues();
	
	
		try {
			
			doReturn(createStandardVariable(TermId.ENTRY_NO.getId(), ENTRY_NO)).when(ontologyService).getStandardVariable(TermId.ENTRY_NO.getId());
			doReturn(createStandardVariable(TermId.DESIG.getId(), DESIGNATION)).when(ontologyService).getStandardVariable(TermId.DESIG.getId());
			doReturn(createStandardVariable(TermId.GID.getId(), GID)).when(ontologyService).getStandardVariable(TermId.GID.getId());
			doReturn(createStandardVariable(TermId.CROSS.getId(), PARENTAGE)).when(ontologyService).getStandardVariable(TermId.CROSS.getId());
			doReturn(createStandardVariable(TermId.SEED_SOURCE.getId(), SEED_SOURCE)).when(ontologyService).getStandardVariable(TermId.SEED_SOURCE.getId());
			doReturn(createStandardVariable(TermId.ENTRY_CODE.getId(), ENTRY_CODE)).when(ontologyService).getStandardVariable(TermId.ENTRY_CODE.getId());
			doReturn(createStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId(),ENTRY_NUMBER_STORAGE)).when(ontologyService).getStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId());
			doReturn(createStandardVariable(TermId.CHECK.getId(),CHECK)).when(ontologyService).getStandardVariable(TermId.CHECK.getId());
			doReturn(getPlotLevelList()).when(userSelection).getPlotsLevelList();
		
		} catch (MiddlewareQueryException e) {
			
		}
		
		
	}
	
	@Test 
	public void test_writeListFactorSectionForTrialManager(){
		
		HSSFWorkbook wb = new HSSFWorkbook(); 
		HSSFSheet descriptionSheet = wb.createSheet(sheetName);
		Map<String, CellStyle> styles = createStyles(wb);
		
		exportService = new GermplasmExportService(ontologyService, userSelection, false);
		exportService.writeListFactorSection(styles, descriptionSheet, 1, getVisibleColumnMap());
		
		assertEquals(DESIGNATION, descriptionSheet.getRow(1).getCell(0).getStringCellValue());
		assertEquals(SEED_SOURCE, descriptionSheet.getRow(2).getCell(0).getStringCellValue());
		assertEquals(ENTRY_CODE, descriptionSheet.getRow(3).getCell(0).getStringCellValue());
		assertEquals(PARENTAGE, descriptionSheet.getRow(4).getCell(0).getStringCellValue());
		assertEquals(ENTRY_NO, descriptionSheet.getRow(5).getCell(0).getStringCellValue());
		assertEquals(GID, descriptionSheet.getRow(6).getCell(0).getStringCellValue());

	}
	
	@Test 
	public void test_writeListFactorSectionForTrialManagerWithNullPlotList(){
		
		doReturn(null).when(userSelection).getPlotsLevelList();
		
		HSSFWorkbook wb = new HSSFWorkbook(); 
		HSSFSheet descriptionSheet = wb.createSheet(sheetName);
		Map<String, CellStyle> styles = createStyles(wb);
		
		exportService = new GermplasmExportService(ontologyService, userSelection, false);
		exportService.writeListFactorSection(styles, descriptionSheet, 1, getVisibleColumnMap());
		
		assertNull(descriptionSheet.getRow(1));
		
	}
	
	@Test 
	public void test_writeListFactorSectionForNurseryManager(){
		
		HSSFWorkbook wb = new HSSFWorkbook(); 
		HSSFSheet descriptionSheet = wb.createSheet(sheetName);
		Map<String, CellStyle> styles = createStyles(wb);
		
		exportService = new GermplasmExportService(ontologyService, userSelection, true);
		exportService.writeListFactorSection(styles, descriptionSheet, 1, getVisibleColumnMap());
		
		assertEquals(ENTRY_NO, descriptionSheet.getRow(1).getCell(0).getStringCellValue());
		assertEquals(DESIGNATION, descriptionSheet.getRow(2).getCell(0).getStringCellValue());
		assertEquals(GID, descriptionSheet.getRow(3).getCell(0).getStringCellValue());
		assertEquals(PARENTAGE, descriptionSheet.getRow(4).getCell(0).getStringCellValue());
		assertEquals(SEED_SOURCE, descriptionSheet.getRow(5).getCell(0).getStringCellValue());
		assertEquals(ENTRY_CODE, descriptionSheet.getRow(6).getCell(0).getStringCellValue());
		
	}
	
	@Test 
	public void test_writeObservationSheet_ForTrialManager(){
		HSSFWorkbook wb = new HSSFWorkbook(); 
		HSSFSheet observationSheet = wb.createSheet(sheetName);
		Map<String, CellStyle> styles = createStyles(wb);
		
		exportService = spy(new GermplasmExportService(ontologyService, userSelection, false));
		
		doReturn(generateImportedGermplasms()).when(exportService).getImportedGermplasms();
		
		try {
			
			exportService.writeObservationSheet(styles, observationSheet, input);
			
		} catch (GermplasmListExporterException e) {
			
		}
		
		assertEquals(DESIGNATION , observationSheet.getRow(0).getCell(0).getStringCellValue());
		assertEquals(SEED_SOURCE , observationSheet.getRow(0).getCell(1).getStringCellValue());
		assertEquals(ENTRY_CODE , observationSheet.getRow(0).getCell(2).getStringCellValue());
		assertEquals(PARENTAGE , observationSheet.getRow(0).getCell(3).getStringCellValue());
		assertEquals(ENTRY_NO , observationSheet.getRow(0).getCell(4).getStringCellValue());
		assertEquals(GID , observationSheet.getRow(0).getCell(5).getStringCellValue());
		
		
		assertEquals(DESIG_VALUE , observationSheet.getRow(1).getCell(0).getStringCellValue());
		assertEquals(SOURCE_VALUE , observationSheet.getRow(1).getCell(1).getStringCellValue());
		assertEquals(ENTRY_CODE_VALUE , observationSheet.getRow(1).getCell(2).getStringCellValue());
		assertEquals(CROSS_VALUE , observationSheet.getRow(1).getCell(3).getStringCellValue());
		assertEquals(ENTRY_NO_VALUE , observationSheet.getRow(1).getCell(4).getStringCellValue());
		assertEquals(GID_VALUE , observationSheet.getRow(1).getCell(5).getStringCellValue());
		
	}

	@Test 
	public void test_writeObservationSheet_ForNurseryManager(){
		HSSFWorkbook wb = new HSSFWorkbook(); 
		HSSFSheet observationSheet = wb.createSheet(sheetName);
		Map<String, CellStyle> styles = createStyles(wb);
		
		exportService = spy(new GermplasmExportService(ontologyService, userSelection, true));
		
		doReturn(generateImportedGermplasms()).when(exportService).getImportedGermplasms();
		
		try {
			
			exportService.writeObservationSheet(styles, observationSheet, input);
			
		} catch (GermplasmListExporterException e) {
			
		}

		
		assertEquals(ENTRY_NO , observationSheet.getRow(0).getCell(0).getStringCellValue());
		assertEquals(DESIGNATION , observationSheet.getRow(0).getCell(1).getStringCellValue());
		assertEquals(GID , observationSheet.getRow(0).getCell(2).getStringCellValue());
		assertEquals(PARENTAGE , observationSheet.getRow(0).getCell(3).getStringCellValue());
		assertEquals(SEED_SOURCE , observationSheet.getRow(0).getCell(4).getStringCellValue());
		assertEquals(ENTRY_CODE , observationSheet.getRow(0).getCell(5).getStringCellValue());
		
		assertEquals(ENTRY_NO_VALUE , observationSheet.getRow(1).getCell(0).getStringCellValue());
		assertEquals(DESIG_VALUE , observationSheet.getRow(1).getCell(1).getStringCellValue());
		assertEquals(GID_VALUE , observationSheet.getRow(1).getCell(2).getStringCellValue());
		assertEquals(CROSS_VALUE , observationSheet.getRow(1).getCell(3).getStringCellValue());
		assertEquals(SOURCE_VALUE , observationSheet.getRow(1).getCell(4).getStringCellValue());
		assertEquals(ENTRY_CODE_VALUE , observationSheet.getRow(1).getCell(5).getStringCellValue());

		
	}
	
	@Test
	public void test_getCategoricalCodeValue(){
		SettingDetail settingDetail = generateSettingDetail(TermId.CHECK.getId());
		
	    List<ValueReference> possibleValues = new ArrayList<>();
	    ValueReference valReference =  new ValueReference();
	    valReference.setId(Integer.valueOf(CHECK_VALUE));
	    valReference.setKey(CHECK_VALUE);
	    valReference.setName(CATEG_CODE_VALUE);
	    possibleValues.add(valReference);
		
	    settingDetail.setPossibleValues(possibleValues);
	    
	    exportService = spy(new GermplasmExportService(ontologyService, userSelection, true));
		
		String categValue = exportService.getCategoricalCodeValue(generateImportedGermplasm(), settingDetail);
		
		assertEquals(CATEG_CODE_VALUE, categValue);
		
	}
	
	@Test
	public void test_getCategoricalCodeValuePossibleValuesIsNull(){
		SettingDetail settingDetail = generateSettingDetail(TermId.CHECK.getId());
	    settingDetail.setPossibleValues(null);
	    
	    exportService = spy(new GermplasmExportService(ontologyService, userSelection, true));
		
	    String categValue = exportService.getCategoricalCodeValue(generateImportedGermplasm(), settingDetail);
		
		assertEquals(CHECK_VALUE, categValue);
	}
	
	@After
	public void tearDown() {
		File file = new File(testFileName);
		file.deleteOnExit();
	}
	
	private List<ImportedGermplasm> generateImportedGermplasms() {
		
		List<ImportedGermplasm> importedGermplasms = new ArrayList<>();
		importedGermplasms.add(generateImportedGermplasm());
		
		return importedGermplasms;
	}
	
	private ImportedGermplasm generateImportedGermplasm(){
		
		ImportedGermplasm importedGermplasm = new ImportedGermplasm();
		importedGermplasm.setCheck(CHECK_VALUE);
		importedGermplasm.setIndex(0);
		importedGermplasm.setGid(GID_VALUE);
		importedGermplasm.setEntryCode(ENTRY_CODE_VALUE);
		importedGermplasm.setEntryId(Integer.valueOf(ENTRY_NO_VALUE));
		importedGermplasm.setSource(SOURCE_VALUE);
		importedGermplasm.setCross(CROSS_VALUE);
		importedGermplasm.setDesig(DESIG_VALUE);
		
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
		
		for (Entry<String, Boolean> entry : getVisibleColumnMap().entrySet()){
			plotLevelList.add(generateSettingDetail(Integer.valueOf(entry.getKey())));
		}
		
		return plotLevelList;

	}
	
	private SettingDetail generateSettingDetail(Integer termId){
		SettingDetail settingDetail =  new SettingDetail();
		settingDetail.setHidden(false);
		SettingVariable var = new SettingVariable();
		var.setCvTermId(termId);
		settingDetail.setVariable(var);
		
		
		StandardVariable stdVar;
		try {
			stdVar = ontologyService.getStandardVariable(termId);
			
			settingDetail.getVariable().setName(stdVar.getName());
			settingDetail.getVariable().setDescription(stdVar.getDescription());
			settingDetail.getVariable().setProperty(stdVar.getProperty().getName());
			settingDetail.getVariable().setScale(stdVar.getScale().getName());
			settingDetail.getVariable().setMethod(stdVar.getMethod().getName());
			settingDetail.getVariable().setDataType(stdVar.getDataType().getName());
			
		} catch (MiddlewareQueryException e) {
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
        styles.put(LABEL_STYLE, labelStyle);
        
        // set cell style for headings in the description sheet
        CellStyle headingStyle = wb.createCellStyle();
        headingStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
        headingStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        Font headingFont = wb.createFont();
        headingFont.setColor(IndexedColors.WHITE.getIndex());
        headingFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headingStyle.setFont(headingFont);
        styles.put(HEADING_STYLE, headingStyle);
        
        //set cell style for numeric values (left alignment)
        CellStyle numericStyle = wb.createCellStyle();
        numericStyle.setAlignment(CellStyle.ALIGN_LEFT);
        styles.put(NUMERIC_STYLE, numericStyle);
        
        return styles;
    }
	
	private StandardVariable createStandardVariable(int id, String name){
		StandardVariable stdVar = new StandardVariable();
		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription(TEST_DESCRIPTION);
		
		Term prop = new Term();
		prop.setName(TEST_PROPERTY);
		stdVar.setProperty(prop);
		
		Term scale = new Term();
		scale.setName(TEST_SCALE);
		stdVar.setScale(scale);
		
		Term method = new Term();
		method.setName(TEST_METHOD);
		stdVar.setMethod(method);
		
		Term dataType = new Term();
		dataType.setName(NUMERIC_VARIABLE);
		stdVar.setDataType(dataType);
		
		return stdVar;
	}
	
	private GermplasmListExportInputValues generateGermplasmListExportInputValues() {
		GermplasmListExportInputValues input = new GermplasmListExportInputValues();
		
		input.setFileName(testFileName);
		input.setGermplasmList(null);
		input.setOwnerName(CURRENT_USER_NAME);
		input.setCurrentLocalIbdbUserId(CURRENT_USER_ID);
        input.setExporterName(CURRENT_USER_NAME);
        input.setVisibleColumnMap(getVisibleColumnMap());
		return input;
	}

}
