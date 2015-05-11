package com.efficio.fieldbook.web.nursery.service.impl;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ImportGermplasmFileServiceImplTest {

	@Mock
	private FieldbookService fieldbookMiddlewareService;
	
	@Mock
	private UserSelection userSelection;
	
	@InjectMocks
	@Spy
	private ImportGermplasmFileService importGermplasmFileService = new ImportGermplasmFileServiceImpl();
	
	
	@Before
	public void setUp() {
		
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testValidataAndAddCheckFactorNoCheckAndWithCheckFactor() throws MiddlewareQueryException {
	
		Workbook workbook = generateWorkbookWithCheckFactor();
		workbook.setCheckFactorAddedOnly(true);
		Mockito.doReturn(workbook).when(userSelection).getWorkbook();
		
		List<ImportedGermplasm> formImportedGermplasm = generateFormImportedGermplasmNoCheck();
		List<ImportedGermplasm> sessionImportedGermplasm = generateImportedGermplasm();
		
		importGermplasmFileService.validataAndAddCheckFactor(formImportedGermplasm, sessionImportedGermplasm, userSelection);
		
		for (ImportedGermplasm item : sessionImportedGermplasm){
			int index = sessionImportedGermplasm.indexOf(item);
			assertEquals(formImportedGermplasm.get(index).getCheckName(), item.getCheckName());
			assertEquals("", item.getCheck());
			assertEquals(formImportedGermplasm.get(index).getCheckId(), item.getCheckId());
		}
		
		assertEquals(4, workbook.getFactors().size());
		
	}
	
	@Test
	public void testValidataAndAddCheckFactorNoCheckAndWithoutCheckFactor() throws MiddlewareQueryException {
	
		Workbook workbook = generateWorkbook();
		workbook.setCheckFactorAddedOnly(false);
		Mockito.doReturn(workbook).when(userSelection).getWorkbook();
		
		List<ImportedGermplasm> formImportedGermplasm = generateFormImportedGermplasmNoCheck();
		List<ImportedGermplasm> sessionImportedGermplasm = generateImportedGermplasm();
		
		importGermplasmFileService.validataAndAddCheckFactor(formImportedGermplasm, sessionImportedGermplasm, userSelection);
		
		for (ImportedGermplasm item : sessionImportedGermplasm){
			int index = sessionImportedGermplasm.indexOf(item);
			assertEquals(formImportedGermplasm.get(index).getCheckName(), item.getCheckName());
			assertEquals("", item.getCheck());
			assertEquals(formImportedGermplasm.get(index).getCheckId(), item.getCheckId());
		}
		
		assertEquals(4, workbook.getFactors().size());
		
	}
	
	@Test
	public void testValidataAndAddCheckFactorWithNoExistingCheckFactor() throws MiddlewareQueryException {
	
		Workbook workbook = generateWorkbook();
		
		List<ImportedGermplasm> formImportedGermplasm = generateFormImportedGermplasm();
		List<ImportedGermplasm> sessionImportedGermplasm = generateImportedGermplasm();
		
		Mockito.doReturn(workbook).when(userSelection).getWorkbook();
		Mockito.doReturn(generateCheckStandardVariable()).when(fieldbookMiddlewareService).getStandardVariable(Mockito.anyInt());
		
		importGermplasmFileService.validataAndAddCheckFactor(formImportedGermplasm, sessionImportedGermplasm, userSelection);
	
		for (ImportedGermplasm item : sessionImportedGermplasm){
			int index = sessionImportedGermplasm.indexOf(item);
			assertEquals(formImportedGermplasm.get(index).getCheckName(), item.getCheckName());
			assertEquals(formImportedGermplasm.get(index).getCheck(), item.getCheck());
			assertEquals(formImportedGermplasm.get(index).getCheckId(), item.getCheckId());
		}
		
		assertEquals("CHECK", workbook.getFactors().get(4).getName());
	
	}
	
	@Test
	public void testValidataAndAddCheckFactorWithExistingCheckFactor() throws MiddlewareQueryException {
	
		Workbook workbook = generateWorkbook();
		StandardVariable checkStandardVariable = generateCheckStandardVariable();
		
		List<ImportedGermplasm> formImportedGermplasm = generateFormImportedGermplasm();
		List<ImportedGermplasm> sessionImportedGermplasm = generateImportedGermplasm();
		
		Mockito.doReturn(generateWorkbookWithCheckFactor()).when(userSelection).getWorkbook();
		Mockito.doReturn(checkStandardVariable).when(fieldbookMiddlewareService).getStandardVariable(Mockito.anyInt());
		Mockito.doReturn(TermId.CHECK.getId()).when(fieldbookMiddlewareService).getStandardVariableIdByPropertyScaleMethodRole(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.any(PhenotypicType.class));
		
		importGermplasmFileService.validataAndAddCheckFactor(formImportedGermplasm, sessionImportedGermplasm, userSelection);
		
		for (ImportedGermplasm item : sessionImportedGermplasm){
			int index = sessionImportedGermplasm.indexOf(item);
			assertEquals(formImportedGermplasm.get(index).getCheckName(), item.getCheckName());
			assertEquals(formImportedGermplasm.get(index).getCheck(), item.getCheck());
			assertEquals(formImportedGermplasm.get(index).getCheckId(), item.getCheckId());
		}
		
		assertEquals(4, workbook.getFactors().size());
		
	}
	
	private List<ImportedGermplasm> generateFormImportedGermplasm(){
		List<ImportedGermplasm> list = new ArrayList<>();
		
		for (int x = 0; x < 10; x++){
			
			ImportedGermplasm importedGermplasm = new ImportedGermplasm();
			
			importedGermplasm.setIndex(x);
			importedGermplasm.setBreedingMethodId(1);
			importedGermplasm.setCheck("CHECK");
			importedGermplasm.setCheckId(x);
			importedGermplasm.setCheckName("CHECK NAME");
			importedGermplasm.setDesig("DESIG"+x);
			importedGermplasm.setEntryCode(String.valueOf(x));
			importedGermplasm.setCross("CROSS"+x);
			list.add(importedGermplasm);
		}
		
		return list;
	}
	
	private List<ImportedGermplasm> generateFormImportedGermplasmNoCheck(){
		List<ImportedGermplasm> list = new ArrayList<>();
		
		for (int x = 0; x < 10; x++){
			
			ImportedGermplasm importedGermplasm = new ImportedGermplasm();
			
			importedGermplasm.setIndex(x);
			importedGermplasm.setBreedingMethodId(1);
			importedGermplasm.setDesig("DESIG"+x);
			importedGermplasm.setEntryCode(String.valueOf(x));
			importedGermplasm.setCross("CROSS"+x);
			list.add(importedGermplasm);
		}
		
		return list;
	}
	
	private List<ImportedGermplasm> generateImportedGermplasm(){
		
		List<ImportedGermplasm> list = new ArrayList<>();
		
		for (int x = 0; x < 10; x++){
			
			ImportedGermplasm importedGermplasm = new ImportedGermplasm();
			importedGermplasm.setIndex(x);
			importedGermplasm.setBreedingMethodId(1);
			importedGermplasm.setDesig("DESIG"+x);
			importedGermplasm.setEntryCode(String.valueOf(x));
			importedGermplasm.setCross("CROSS"+x);
			list.add(importedGermplasm);
		}
		
		return list;
	}
	
	private StandardVariable generateCheckStandardVariable(){
		
		StandardVariable stdVar = new StandardVariable();
		stdVar.setId(TermId.CHECK.getId());
		stdVar.setName("ENTRY_TYPE");
		stdVar.setStoredIn(new Term(1040, "Germplasm entry", "Germplasm entry information stored in stockprop.value"));
		stdVar.setProperty(new Term(2209, "Entry type", "Entry type"));	
		stdVar.setScale(new Term(6070, "Type", ""));
		stdVar.setMethod(new Term(4030, "Assigned", "Term, name or id assigned"));
		stdVar.setDataType(new Term(1130, "Categorical  variable", "Variable with discrete class values (numeric or character all treated as character)"));
		
		return stdVar;
	}
	
	private Workbook generateWorkbook(){
		Workbook wb = new Workbook();
		wb.setFactors(generateFactors());
		return wb;
	}
	
	private Workbook generateWorkbookWithCheckFactor(){
		Workbook wb = new Workbook();
		List<MeasurementVariable> factors = generateFactors();
		factors.add(createMeasurementVariable("ENTRY_TYPE"));
		wb.setFactors(factors);
		return wb;
	}
	
	private List<MeasurementVariable> generateFactors(){
		List<MeasurementVariable> list = new ArrayList<>();
		list.add(createMeasurementVariable("TRIAL_INSTANCE"));
		list.add(createMeasurementVariable("ENTRY_NO"));
		list.add(createMeasurementVariable("PLOT_NO"));
		list.add(createMeasurementVariable("GID"));
		return list;
		
	}
	
	private MeasurementVariable createMeasurementVariable(String name){
		MeasurementVariable mv = new MeasurementVariable();
		mv.setName(name);
		return mv;
	}
}
