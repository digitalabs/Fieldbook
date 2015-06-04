
package com.efficio.fieldbook.web.util.parsing;

import java.io.File;
import java.net.URISyntaxException;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.service.FileService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.common.bean.DesignImportData;

@RunWith(MockitoJUnitRunner.class)
public class DesignImportParserTest {

	@Mock
	private FileService fileService;

	@Mock
	private MessageSource messageSource;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private MultipartFile multiPartFile;

	@InjectMocks
	private final DesignImportParser parser = Mockito.spy(new DesignImportParser());

	public static final String TEST_FILE_NAME = "Design_Import_Template.csv";

	@Test
	public void testParseFile() throws FileParsingException, URISyntaxException {

		File csvFile = new File(ClassLoader.getSystemClassLoader().getResource(DesignImportParserTest.TEST_FILE_NAME).toURI());

		assert csvFile.exists();

		Mockito.doReturn(csvFile).when(this.parser).storeAndRetrieveFile(this.multiPartFile);

		DesignImportData result = this.parser.parseFile(this.multiPartFile);

		Assert.assertTrue(!result.getUnmappedHeaders().isEmpty());

	}

	@Test
	public void testParseFileInvalidFileFormat() {

		Mockito.doReturn("invalidformat.xls").when(this.multiPartFile).getOriginalFilename();

		try {
			this.parser.parseFile(this.multiPartFile);
			Assert.fail();
		} catch (FileParsingException e) {
			assert true;
		}

	}

}
