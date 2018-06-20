
package com.efficio.fieldbook.web.trial.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.trial.service.ImportGermplasmFileService;

public class ImportGermplasmFileServiceImplTest extends AbstractBaseIntegrationTest {

	/** The import germplasm file service. */
	@Autowired
	private ImportGermplasmFileService realImportGermplasmFileService;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private UserSelection userSelection;

	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private ImportGermplasmFileServiceImpl importGermplasmFileService;

	/** The workbook basic. */
	private org.apache.poi.ss.usermodel.Workbook workbookBasic;

	/** The workbook advance. */
	private org.apache.poi.ss.usermodel.Workbook workbookAdvance;

	/** The workbook basic xlsx. */
	private org.apache.poi.ss.usermodel.Workbook workbookBasicXlsx;

	/** The workbook advance xlsx. */
	private org.apache.poi.ss.usermodel.Workbook workbookAdvanceXlsx;

	/** The workbook invalid. */
	private org.apache.poi.ss.usermodel.Workbook workbookInvalid;

	@Before
	public void initialize() {

		MockitoAnnotations.initMocks(this);

		try {

			InputStream inp = this.getClass().getClassLoader().getResourceAsStream("GermplasmImportTemplate-Basic-rev4b-with_data.xls");

			this.workbookBasic = WorkbookFactory.create(inp);

			inp = this.getClass().getClassLoader().getResourceAsStream("GermplasmImportTemplate-Advanced-rev4.xls");
			this.workbookAdvance = WorkbookFactory.create(inp);

			inp = this.getClass().getClassLoader().getResourceAsStream("GermplasmImportTemplate-Basic-rev4b-with_data.xlsx");

			this.workbookBasicXlsx = WorkbookFactory.create(inp);

			inp = this.getClass().getClassLoader().getResourceAsStream("GermplasmImportTemplate-Advanced-rev4.xlsx");
			this.workbookAdvanceXlsx = WorkbookFactory.create(inp);

			inp = this.getClass().getClassLoader().getResourceAsStream("Population114_Pheno_FB_1.xls");
			this.workbookInvalid = WorkbookFactory.create(inp);

			Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn("");
			this.importGermplasmFileService.setContextUtil(this.contextUtil);

		} catch (Exception e) {
			Assert.fail("Failed to load the template files for testing.");
		}
	}

	/**
	 * Test valid basic parse import gerplasm.
	 */
    @Ignore
	@Test
	public void testValidBasicParseImportGerplasm() {

		ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		try {
			this.realImportGermplasmFileService.doProcessNow(this.workbookBasic, mainInfo);
		} catch (Exception e) {
			Assert.fail();
		}

		Assert.assertEquals(mainInfo.getListName(), "<Enter name for germplasm list>");
		Assert.assertEquals(mainInfo.getListTitle(),
				"<Enter description of germplasm list here then enter sequence number and names on the Observation sheet>");
		Assert.assertEquals(mainInfo.getListType(), "LST");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().size(), 20);
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(0).getFactor(), "ENTRY");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(1).getFactor(), "DESIGNATION");
		Assert.assertFalse(mainInfo.isAdvanceImportType());
		Assert.assertTrue(mainInfo.getFileIsValid());
		// we check the parse data here
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId(), Integer.valueOf(1));
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getDesig(), "IR 68835-58-1-1-B");

		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(19).getEntryId(), Integer.valueOf(20));
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(19).getDesig(), "IR 67632-14-2-5-1-2-B");
	}

	/**
	 * Test valid advance parse import gerplasm.
	 */
    @Ignore
	@Test
	public void testValidAdvanceParseImportGerplasm() {
		ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		try {

			this.realImportGermplasmFileService.doProcessNow(this.workbookAdvance, mainInfo);

		} catch (Exception e) {
			Assert.fail();
		}

		Assert.assertEquals(mainInfo.getListName(), "<Enter name for germplasm list>");
		Assert.assertEquals(mainInfo.getListTitle(),
				"<Enter description of germplasm list here and details of germplasm to be imported on the Observation sheet>");
		Assert.assertEquals(mainInfo.getListType(), "LST");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(0).getFactor(), "ENTRY");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(1).getFactor(), "DESIGNATION");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(2).getFactor(), "GID");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(3).getFactor(), "CROSS");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(4).getFactor(), "SOURCE");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(5).getFactor(), "ENTRY CODE");
		Assert.assertTrue(mainInfo.isAdvanceImportType());
		Assert.assertTrue(mainInfo.getFileIsValid());
		// test the parsing
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId(), Integer.valueOf(1));
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getDesig(), "IR 68201-21-2-B-4-B-B");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getGid(), "1");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getCross(), "1");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getSource(), "1");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryCode(), "1");

		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getEntryId(), Integer.valueOf(2));
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getDesig(), "IR 67632-14-2-5-1-2-B");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getGid(), "2");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getCross(), "2");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getSource(), "2");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getEntryCode(), "2");
	}

	/**
	 * Test valid basic parse import gerplasm xlsx.
	 */
    @Ignore
	@Test
	public void testValidBasicParseImportGerplasmXlsx() {

		ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		try {
			this.realImportGermplasmFileService.doProcessNow(this.workbookBasicXlsx, mainInfo);
		} catch (Exception e) {
			Assert.fail();
		}

		Assert.assertEquals(mainInfo.getListName(), "<Enter name for germplasm list>");
		Assert.assertEquals(mainInfo.getListTitle(),
				"<Enter description of germplasm list here then enter sequence number and names on the Observation sheet>");
		Assert.assertEquals(mainInfo.getListType(), "LST");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().size(), 20);
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(0).getFactor(), "ENTRY");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(1).getFactor(), "DESIGNATION");
		Assert.assertFalse(mainInfo.isAdvanceImportType());
		Assert.assertTrue(mainInfo.getFileIsValid());
		// we check the parse data here
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId(), Integer.valueOf(1));
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getDesig(), "IR 68835-58-1-1-B");

		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(19).getEntryId(), Integer.valueOf(20));
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(19).getDesig(), "IR 67632-14-2-5-1-2-B");
	}

	/**
	 * Test valid advance parse import gerplasm xlsx.
	 */
    @Ignore
	@Test
	public void testValidAdvanceParseImportGerplasmXlsx() {
		ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		try {
			this.realImportGermplasmFileService.doProcessNow(this.workbookAdvanceXlsx, mainInfo);
		} catch (Exception e) {
			Assert.fail();
		}

		Assert.assertEquals(mainInfo.getListName(), "<Enter name for germplasm list>");
		Assert.assertEquals(mainInfo.getListTitle(),
				"<Enter description of germplasm list here and details of germplasm to be imported on the Observation sheet>");
		Assert.assertEquals(mainInfo.getListType(), "LST");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(0).getFactor(), "ENTRY");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(1).getFactor(), "DESIGNATION");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(2).getFactor(), "GID");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(3).getFactor(), "CROSS");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(4).getFactor(), "SOURCE");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(5).getFactor(), "ENTRY CODE");
		Assert.assertTrue(mainInfo.isAdvanceImportType());
		Assert.assertTrue(mainInfo.getFileIsValid());
		// test the parsing
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId(), Integer.valueOf(1));
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getDesig(), "IR 68201-21-2-B-4-B-B");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getGid(), "1");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getCross(), "1");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getSource(), "1");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryCode(), "1");

		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getEntryId(), Integer.valueOf(2));
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getDesig(), "IR 67632-14-2-5-1-2-B");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getGid(), "2");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getCross(), "2");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getSource(), "2");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getEntryCode(), "2");
	}

	/**
	 * Test valid basic parse import gerplasm xls pagination.
	 */
    @Ignore
	@Test
	public void testValidBasicParseImportGerplasmXlsPagination() {
		// testing when doing pagination, we simulate the pagination
		ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		try {
			this.realImportGermplasmFileService.doProcessNow(this.workbookBasic, mainInfo);
			form.setImportedGermplasmMainInfo(mainInfo);
			form.setImportedGermplasm(mainInfo.getImportedGermplasmList().getImportedGermplasms());
		} catch (Exception e) {
			Assert.fail();
		}
		form.changePage(1);
		Assert.assertEquals(form.getPaginatedImportedGermplasm().get(0).getEntryId(), Integer.valueOf(1));
		Assert.assertEquals(form.getPaginatedImportedGermplasm().get(0).getDesig(), "IR 68835-58-1-1-B"); // we check the parse data here
	}

	/**
	 * Test valid advance parse import gerplasm xls pagination.
	 */
    @Ignore
	@Test
	public void testValidAdvanceParseImportGerplasmXlsPagination() {
		// testing when doing pagination, we simulate the pagination
		ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		try {
			this.realImportGermplasmFileService.doProcessNow(this.workbookAdvance, mainInfo);
			form.setImportedGermplasmMainInfo(mainInfo);
			form.setImportedGermplasm(mainInfo.getImportedGermplasmList().getImportedGermplasms());
		} catch (Exception e) {
			Assert.fail();
		}
		form.changePage(1);
		Assert.assertEquals(form.getPaginatedImportedGermplasm().get(0).getEntryId(), Integer.valueOf(1));
		Assert.assertEquals(form.getPaginatedImportedGermplasm().get(0).getDesig(), "IR 68201-21-2-B-4-B-B"); // we check the parse data
		// here
	}

    @Ignore
	@Test
    public void testValidAndAddCheckFactor() throws MiddlewareException {
		// testing when doing pagination, we simulate the pagination
		ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		try {
			this.realImportGermplasmFileService.doProcessNow(this.workbookAdvance, mainInfo);
			form.setImportedGermplasmMainInfo(mainInfo);
			form.setImportedGermplasm(mainInfo.getImportedGermplasmList().getImportedGermplasms());
		} catch (Exception e) {
			Assert.fail();
		}
		UserSelection userSelection = new UserSelection();
		userSelection.setWorkbook(new org.generationcp.middleware.domain.etl.Workbook());
		List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();

		userSelection.getWorkbook().setFactors(factors);
		userSelection.getWorkbook().setVariates(new ArrayList<MeasurementVariable>());
		userSelection.setImportedGermplasmMainInfo(mainInfo);
		this.realImportGermplasmFileService.validataAndAddCheckFactor(form.getImportedGermplasm(), userSelection
				.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms(), userSelection);
		// no check factor yet
		Assert.assertEquals(0, userSelection.getWorkbook().getMeasurementDatasetVariables().size());
		// we now need to add check
		MeasurementVariable checkVariable =
				new MeasurementVariable("CHECK", "TYPE OF ENTRY", "CODE", "ASSIGNED", "CHECK", "C", "", "ENTRY");
		factors.add(checkVariable);
		userSelection.getWorkbook().reset();
		userSelection.getWorkbook().setFactors(factors);
		// need to check if the CHECK was added
		Assert.assertEquals(1, userSelection.getWorkbook().getMeasurementDatasetVariables().size());
	}

	@Test
	public void testValidataAndAddCheckFactorNoCheckAndWithCheckFactor() throws MiddlewareException {

		Workbook workbook = this.generateWorkbookWithCheckFactor();
		workbook.setCheckFactorAddedOnly(true);
		Mockito.doReturn(workbook).when(this.userSelection).getWorkbook();

		List<ImportedGermplasm> formImportedGermplasm = this.generateFormImportedGermplasmNoCheck();
		List<ImportedGermplasm> sessionImportedGermplasm = this.generateImportedGermplasm();

		this.importGermplasmFileService.validataAndAddCheckFactor(formImportedGermplasm, sessionImportedGermplasm, this.userSelection);

		for (ImportedGermplasm item : sessionImportedGermplasm) {
			int index = sessionImportedGermplasm.indexOf(item);
			Assert.assertEquals(formImportedGermplasm.get(index).getEntryTypeName(), item.getEntryTypeName());
			Assert.assertEquals("", item.getEntryTypeValue());
			Assert.assertEquals(formImportedGermplasm.get(index).getEntryTypeCategoricalID(), item.getEntryTypeCategoricalID());
		}

		Assert.assertEquals(4, workbook.getFactors().size());

	}

	@Test
	public void testValidataAndAddCheckFactorNoCheckAndWithoutCheckFactor() throws MiddlewareException {

		Workbook workbook = this.generateWorkbook();
		workbook.setCheckFactorAddedOnly(false);
		Mockito.doReturn(workbook).when(this.userSelection).getWorkbook();

		List<ImportedGermplasm> formImportedGermplasm = this.generateFormImportedGermplasmNoCheck();
		List<ImportedGermplasm> sessionImportedGermplasm = this.generateImportedGermplasm();

		this.importGermplasmFileService.validataAndAddCheckFactor(formImportedGermplasm, sessionImportedGermplasm, this.userSelection);

		for (ImportedGermplasm item : sessionImportedGermplasm) {
			int index = sessionImportedGermplasm.indexOf(item);
			Assert.assertEquals(formImportedGermplasm.get(index).getEntryTypeName(), item.getEntryTypeName());
			Assert.assertEquals("", item.getEntryTypeValue());
			Assert.assertEquals(formImportedGermplasm.get(index).getEntryTypeCategoricalID(), item.getEntryTypeCategoricalID());
		}

		Assert.assertEquals(4, workbook.getFactors().size());

	}

	@Test
	public void testValidataAndAddCheckFactorWithNoExistingCheckFactor() throws MiddlewareException {

		Workbook workbook = this.generateWorkbook();

		List<ImportedGermplasm> formImportedGermplasm = this.generateFormImportedGermplasm();
		List<ImportedGermplasm> sessionImportedGermplasm = this.generateImportedGermplasm();

		Mockito.doReturn(workbook).when(this.userSelection).getWorkbook();
		Mockito.doReturn(this.generateCheckStandardVariable()).when(this.fieldbookMiddlewareService)
				.getStandardVariable(Matchers.anyInt(), Matchers.anyString());

		this.importGermplasmFileService.validataAndAddCheckFactor(formImportedGermplasm, sessionImportedGermplasm, this.userSelection);

		for (ImportedGermplasm item : sessionImportedGermplasm) {
			int index = sessionImportedGermplasm.indexOf(item);
			Assert.assertEquals(formImportedGermplasm.get(index).getEntryTypeName(), item.getEntryTypeName());
			Assert.assertEquals(formImportedGermplasm.get(index).getEntryTypeValue(), item.getEntryTypeValue());
			Assert.assertEquals(formImportedGermplasm.get(index).getEntryTypeCategoricalID(), item.getEntryTypeCategoricalID());
		}

		Assert.assertEquals("CHECK", workbook.getFactors().get(4).getName());

	}

	@Test
	public void testValidataAndAddCheckFactorWithExistingCheckFactor() throws MiddlewareException {

		Workbook workbook = this.generateWorkbook();
		StandardVariable checkStandardVariable = this.generateCheckStandardVariable();

		List<ImportedGermplasm> formImportedGermplasm = this.generateFormImportedGermplasm();
		List<ImportedGermplasm> sessionImportedGermplasm = this.generateImportedGermplasm();

		Mockito.doReturn(this.generateWorkbookWithCheckFactor()).when(this.userSelection).getWorkbook();
		Mockito.doReturn(checkStandardVariable).when(this.fieldbookMiddlewareService)
				.getStandardVariable(Matchers.anyInt(), Matchers.anyString());
		Mockito.doReturn(TermId.CHECK.getId())
				.when(this.fieldbookMiddlewareService)
				.getStandardVariableIdByPropertyScaleMethodRole(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(),
						Matchers.any(PhenotypicType.class));

		this.importGermplasmFileService.validataAndAddCheckFactor(formImportedGermplasm, sessionImportedGermplasm, this.userSelection);

		for (ImportedGermplasm item : sessionImportedGermplasm) {
			int index = sessionImportedGermplasm.indexOf(item);
			Assert.assertEquals(formImportedGermplasm.get(index).getEntryTypeName(), item.getEntryTypeName());
			Assert.assertEquals(formImportedGermplasm.get(index).getEntryTypeValue(), item.getEntryTypeValue());
			Assert.assertEquals(formImportedGermplasm.get(index).getEntryTypeCategoricalID(), item.getEntryTypeCategoricalID());
		}

		Assert.assertEquals(4, workbook.getFactors().size());

	}

	private List<ImportedGermplasm> generateFormImportedGermplasm() {
		List<ImportedGermplasm> list = new ArrayList<>();

		for (int x = 0; x < 10; x++) {

			ImportedGermplasm importedGermplasm = new ImportedGermplasm();

			importedGermplasm.setIndex(x);
			importedGermplasm.setBreedingMethodId(1);
			importedGermplasm.setEntryTypeValue("CHECK");
			importedGermplasm.setEntryTypeCategoricalID(x);
			importedGermplasm.setEntryTypeName("CHECK NAME");
			importedGermplasm.setDesig("DESIG" + x);
			importedGermplasm.setEntryCode(String.valueOf(x));
			importedGermplasm.setCross("CROSS" + x);
			list.add(importedGermplasm);
		}

		return list;
	}

	private List<ImportedGermplasm> generateFormImportedGermplasmNoCheck() {
		List<ImportedGermplasm> list = new ArrayList<>();

		for (int x = 0; x < 10; x++) {

			ImportedGermplasm importedGermplasm = new ImportedGermplasm();

			importedGermplasm.setIndex(x);
			importedGermplasm.setBreedingMethodId(1);
			importedGermplasm.setDesig("DESIG" + x);
			importedGermplasm.setEntryCode(String.valueOf(x));
			importedGermplasm.setCross("CROSS" + x);
			list.add(importedGermplasm);
		}

		return list;
	}

	private List<ImportedGermplasm> generateImportedGermplasm() {

		List<ImportedGermplasm> list = new ArrayList<>();

		for (int x = 0; x < 10; x++) {

			ImportedGermplasm importedGermplasm = new ImportedGermplasm();
			importedGermplasm.setIndex(x);
			importedGermplasm.setBreedingMethodId(1);
			importedGermplasm.setDesig("DESIG" + x);
			importedGermplasm.setEntryCode(String.valueOf(x));
			importedGermplasm.setCross("CROSS" + x);
			list.add(importedGermplasm);
		}

		return list;
	}

	private StandardVariable generateCheckStandardVariable() {

		StandardVariable stdVar = new StandardVariable();
		stdVar.setId(TermId.CHECK.getId());
		stdVar.setName("ENTRY_TYPE");
		stdVar.setProperty(new Term(2209, "Entry type", "Entry type"));
		stdVar.setScale(new Term(6070, "Type", ""));
		stdVar.setMethod(new Term(4030, "Assigned", "Term, name or id assigned"));
		stdVar.setDataType(new Term(1130, "Categorical  variable",
				"Variable with discrete class values (numeric or character all treated as character)"));

		return stdVar;
	}

	private Workbook generateWorkbook() {
		Workbook wb = new Workbook();
		wb.setFactors(this.generateFactors());
		return wb;
	}

	private Workbook generateWorkbookWithCheckFactor() {
		Workbook wb = new Workbook();
		List<MeasurementVariable> factors = this.generateFactors();
		factors.add(this.createMeasurementVariable("ENTRY_TYPE"));
		wb.setFactors(factors);
		return wb;
	}

	private List<MeasurementVariable> generateFactors() {
		List<MeasurementVariable> list = new ArrayList<>();
		list.add(this.createMeasurementVariable("TRIAL_INSTANCE"));
		list.add(this.createMeasurementVariable("ENTRY_NO"));
		list.add(this.createMeasurementVariable("PLOT_NO"));
		list.add(this.createMeasurementVariable("GID"));
		return list;

	}

	private MeasurementVariable createMeasurementVariable(String name) {
		MeasurementVariable mv = new MeasurementVariable();
		mv.setName(name);
		return mv;
	}
}
