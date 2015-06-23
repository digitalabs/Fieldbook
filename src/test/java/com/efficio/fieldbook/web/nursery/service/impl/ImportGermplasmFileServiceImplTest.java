
package com.efficio.fieldbook.web.nursery.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
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
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;

public class ImportGermplasmFileServiceImplTest {

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private UserSelection userSelection;

	@InjectMocks
	@Spy
	private final ImportGermplasmFileService importGermplasmFileService = new ImportGermplasmFileServiceImpl();

	@Before
	public void setUp() {

		MockitoAnnotations.initMocks(this);
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
			Assert.assertEquals(formImportedGermplasm.get(index).getCheckName(), item.getCheckName());
			Assert.assertEquals("", item.getCheck());
			Assert.assertEquals(formImportedGermplasm.get(index).getCheckId(), item.getCheckId());
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
			Assert.assertEquals(formImportedGermplasm.get(index).getCheckName(), item.getCheckName());
			Assert.assertEquals("", item.getCheck());
			Assert.assertEquals(formImportedGermplasm.get(index).getCheckId(), item.getCheckId());
		}

		Assert.assertEquals(4, workbook.getFactors().size());

	}

	@Test
	public void testValidataAndAddCheckFactorWithNoExistingCheckFactor() throws MiddlewareException {

		Workbook workbook = this.generateWorkbook();

		List<ImportedGermplasm> formImportedGermplasm = this.generateFormImportedGermplasm();
		List<ImportedGermplasm> sessionImportedGermplasm = this.generateImportedGermplasm();

		Mockito.doReturn(workbook).when(this.userSelection).getWorkbook();
		Mockito.doReturn(this.generateCheckStandardVariable()).when(this.fieldbookMiddlewareService).
			getStandardVariable(Matchers.anyInt(),Matchers.anyString());

		this.importGermplasmFileService.validataAndAddCheckFactor(formImportedGermplasm, sessionImportedGermplasm, this.userSelection);

		for (ImportedGermplasm item : sessionImportedGermplasm) {
			int index = sessionImportedGermplasm.indexOf(item);
			Assert.assertEquals(formImportedGermplasm.get(index).getCheckName(), item.getCheckName());
			Assert.assertEquals(formImportedGermplasm.get(index).getCheck(), item.getCheck());
			Assert.assertEquals(formImportedGermplasm.get(index).getCheckId(), item.getCheckId());
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
		Mockito.doReturn(checkStandardVariable).when(this.fieldbookMiddlewareService).getStandardVariable(
				Matchers.anyInt(),Matchers.anyString());
		Mockito.doReturn(TermId.CHECK.getId())
				.when(this.fieldbookMiddlewareService)
				.getStandardVariableIdByPropertyScaleMethodRole(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(),
						Matchers.any(PhenotypicType.class));

		this.importGermplasmFileService.validataAndAddCheckFactor(formImportedGermplasm, sessionImportedGermplasm, this.userSelection);

		for (ImportedGermplasm item : sessionImportedGermplasm) {
			int index = sessionImportedGermplasm.indexOf(item);
			Assert.assertEquals(formImportedGermplasm.get(index).getCheckName(), item.getCheckName());
			Assert.assertEquals(formImportedGermplasm.get(index).getCheck(), item.getCheck());
			Assert.assertEquals(formImportedGermplasm.get(index).getCheckId(), item.getCheckId());
		}

		Assert.assertEquals(4, workbook.getFactors().size());

	}

	private List<ImportedGermplasm> generateFormImportedGermplasm() {
		List<ImportedGermplasm> list = new ArrayList<>();

		for (int x = 0; x < 10; x++) {

			ImportedGermplasm importedGermplasm = new ImportedGermplasm();

			importedGermplasm.setIndex(x);
			importedGermplasm.setBreedingMethodId(1);
			importedGermplasm.setCheck("CHECK");
			importedGermplasm.setCheckId(x);
			importedGermplasm.setCheckName("CHECK NAME");
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
		stdVar.setStoredIn(new Term(1040, "Germplasm entry", "Germplasm entry information stored in stockprop.value"));
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
