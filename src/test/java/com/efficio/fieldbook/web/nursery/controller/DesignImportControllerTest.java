package com.efficio.fieldbook.web.nursery.controller;

import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.form.ImportDesignForm;
import com.efficio.fieldbook.web.common.service.DesignImportService;
import com.efficio.fieldbook.web.util.parsing.DesignImportParser;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.Workbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by cyrus on 5/28/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class DesignImportControllerTest {
	public static final String TEST_FILE_NAME = "Design_Import_Template.csv";

	private DesignImportParser parser = spy(new DesignImportParser());

	@Mock
	private UserSelection userSelection;

	@Mock
	private DesignImportService designImportService;

	@Mock
	private MessageSource messageSource;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@InjectMocks
	private DesignImportController designImportController = spy(new DesignImportController());

	private DesignImportData designImportData;
	private MultipartFile multipartFile;

	@Before
	public void initTests() throws Exception {
		File designImportFile = new File(ClassLoader.getSystemClassLoader().getResource(TEST_FILE_NAME).toURI());
	    assert  designImportFile.exists();

		multipartFile = mock(MultipartFile.class);
		doReturn(designImportFile).when(parser).storeAndRetrieveFile(multipartFile);

		designImportData = spy(parser.parseFile(multipartFile));

		when(userSelection.getDesignImportData()).thenReturn(designImportData);
	}

	@Test
	public void testValidateAndSaveNewMapping() throws Exception {
		doNothing().when(designImportController).updateDesignMapping(any(Map.class));

		when(designImportService.areTrialInstancesMatchTheSelectedEnvironments(3,
				userSelection.getDesignImportData())).thenReturn(true);

		Map<String,Object> results = designImportController.validateAndSaveNewMapping(mock(Map.class),3);

		verify(designImportService).validateDesignData(userSelection.getDesignImportData());

		assert (Boolean)results.get("success");
	}

	@Test
	public void testValidateAndSaveNewMappingWithWarning() throws Exception {
		doNothing().when(designImportController).updateDesignMapping(any(Map.class));

		when(designImportService.areTrialInstancesMatchTheSelectedEnvironments(3,
				userSelection.getDesignImportData())).thenReturn(false);

		when(messageSource.getMessage("design.import.warning.trial.instances.donotmatch", null,
				Locale.ENGLISH)).thenReturn("WARNING_MSG");

		Map<String,Object> results = designImportController.validateAndSaveNewMapping(mock(Map.class),3);

		verify(designImportService).validateDesignData(userSelection.getDesignImportData());

		assert (Boolean)results.get("success");
		assertEquals("returns a warning message","WARNING_MSG",results.get("warning"));
	}


	@Test
	public void testValidateAndSaveNewMappingWithException() throws Exception {
		doNothing().when(designImportController).updateDesignMapping(any(Map.class));

		when(designImportService.areTrialInstancesMatchTheSelectedEnvironments(3,
				userSelection.getDesignImportData())).thenReturn(false);

		doThrow(new DesignValidationException("DesignValidationException thrown")).when(
				designImportService).validateDesignData(any(DesignImportData.class));

		Map<String,Object> results = designImportController.validateAndSaveNewMapping(mock(Map.class),3);

		assert !(Boolean)results.get("success");
		assertEquals("returns a error message", "DesignValidationException thrown",
				results.get("error"));
		assertEquals("error = message", results.get("error"), results.get("message"));
	}

	@Test
	public void testPerformAutomap() throws Exception {
		Map<PhenotypicType,List<DesignHeaderItem>> result = new HashMap<>();
		when(designImportService.categorizeHeadersByPhenotype(designImportData.getUnmappedHeaders())).thenReturn(
				result);

		designImportController.performAutomap(designImportData);

		// just verify were setting mapped headers and unmapped headers to designImportData
		verify(designImportData).setMappedHeaders(result);
		verify(designImportData).setUnmappedHeaders(result.get(null));

	}

	@Test
	public void testImportFile() throws Exception {
		doNothing().when(designImportController).initializeTemporaryWorkbook(anyString());
		ImportDesignForm form = mock(ImportDesignForm.class);
		when(form.getFile()).thenReturn(multipartFile);

		String resultsMap = designImportController.importFile(form,"N");

		verify(userSelection).setDesignImportData(any(DesignImportData.class));

		assert resultsMap.contains("{\"isSuccess\":1}");
	}

	@Test
	public void testImportFileFail() throws Exception {
		ImportDesignForm form = mock(ImportDesignForm.class);
		when(form.getFile()).thenReturn(multipartFile);

		doNothing().when(designImportController).initializeTemporaryWorkbook(anyString());
		doThrow(new FileParsingException("force file parse exception")).when(parser).parseFile(
				any(MultipartFile.class));


		String resultsMap = designImportController.importFile(form,"N");

		assert resultsMap.contains("{\"error\":[\"force file parse exception\"],\"isSuccess\":0}");
	}



	@Test
	public void testGetMappingData() throws Exception {
		Set<String> mappingTypes =  designImportController.getMappingData().keySet();

		assert mappingTypes.contains("unmappedHeaders");
		assert mappingTypes.contains("mappedEnvironmentalFactors");
		assert mappingTypes.contains("mappedDesignFactors");
		assert mappingTypes.contains("mappedGermplasmFactors");
		assert mappingTypes.contains("mappedTraits");
	}

	@Test
	public void testShowDetails() throws Exception {
		Workbook workbook = mock(Workbook.class);
		when(userSelection.getTemporaryWorkbook()).thenReturn(workbook);
		when(userSelection.getDesignImportData()).thenReturn(designImportData);
		when(designImportService
				.getDesignMeasurementVariables(workbook, designImportData)).thenReturn(
				mock(Set.class));

		Model model = mock(Model.class);

		String html = designImportController.showDetails(model);

		verify(model).addAttribute(eq("measurementVariables"), any(Set.class));

		assert DesignImportController.REVIEW_DETAILS_PAGINATION_TEMPLATE.equals(html);
	}

}