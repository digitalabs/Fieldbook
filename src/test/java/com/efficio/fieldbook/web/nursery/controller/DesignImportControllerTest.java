
package com.efficio.fieldbook.web.nursery.controller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.Workbook;
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
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.form.ImportDesignForm;
import com.efficio.fieldbook.web.common.service.DesignImportService;
import com.efficio.fieldbook.web.util.parsing.DesignImportParser;

/**
 * Created by cyrus on 5/28/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class DesignImportControllerTest {

	public static final String TEST_FILE_NAME = "Design_Import_Template.csv";

	private final DesignImportParser parser = Mockito.spy(new DesignImportParser());

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

}
