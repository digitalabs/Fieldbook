package com.efficio.fieldbook.web.util.parsing;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.service.FileService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.common.bean.DesignImportData;

import java.io.File;
import java.net.URISyntaxException;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


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
	private DesignImportParser parser = spy(new DesignImportParser());

	public static final String TEST_FILE_NAME = "Design_Import_Template.csv";


	@Test
	public void testParseFile() throws FileParsingException, URISyntaxException{
		
		File csvFile = new File(ClassLoader.getSystemClassLoader().getResource(TEST_FILE_NAME).toURI());

		assert csvFile.exists();
		
		doReturn(csvFile).when(parser).storeAndRetrieveFile(multiPartFile);
		
		DesignImportData result = parser.parseFile(multiPartFile);
		
		assertTrue(!result.getUnmappedHeaders().isEmpty());
		
	}
	
	@Test
	public void testParseFileInvalidFileFormat() {
		
		doReturn("invalidformat.xls").when(multiPartFile).getOriginalFilename();
		
		try{
			parser.parseFile(multiPartFile);
			fail();
		}catch(FileParsingException e){
			assert true;
		}
		
		
	}

	
}

