
package com.efficio.fieldbook.web.util.parsing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.service.FileService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;

import au.com.bytecode.opencsv.CSVReader;

import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;

@RunWith(MockitoJUnitRunner.class)
public class DesignImportParserTest {

	@Mock
	private FileService fileService;

	@Mock
	private MessageSource messageSource;

	@Mock
	private MultipartFile multiPartFile;

	@InjectMocks
	private DesignImportParser parser;

	private static File csvFile;

	public static final String TEST_FILE_NAME = "Design_Import_Template.csv";
	public static final String TEST_FILE_NAME_INVALID = "Design_Import_Template.xls";

	@BeforeClass
	public static void runOnce() throws URISyntaxException {

		csvFile = new File(ClassLoader.getSystemClassLoader().getResource(DesignImportParserTest.TEST_FILE_NAME).toURI());

		assert csvFile.exists();

	}

	@Test
	public void testParseFile() throws FileParsingException, IOException {

		Mockito.doReturn(TEST_FILE_NAME).when(this.multiPartFile).getOriginalFilename();
		Mockito.doReturn(new FileInputStream(csvFile)).when(this.multiPartFile).getInputStream();
		Mockito.doReturn(csvFile).when(this.fileService).retrieveFileFromFileName(Mockito.anyString());

		DesignImportData result = this.parser.parseFile(this.multiPartFile);

		Assert.assertTrue(!result.getUnmappedHeaders().isEmpty());

		Assert.assertEquals("There must be 6 headers in file.", 6, result.getUnmappedHeaders().size());
		Assert.assertEquals("There must be 85 rows in file.", 85, result.getCsvData().size());
		Assert.assertEquals("TRIAL_INSTANCE", result.getUnmappedHeaders().get(0).getName());
		Assert.assertEquals("SITE_NAME", result.getUnmappedHeaders().get(1).getName());
		Assert.assertEquals("ENTRY_NO", result.getUnmappedHeaders().get(2).getName());
		Assert.assertEquals("PLOT_NO", result.getUnmappedHeaders().get(3).getName());
		Assert.assertEquals("REP_NO", result.getUnmappedHeaders().get(4).getName());
		Assert.assertEquals("BLOCK_NO", result.getUnmappedHeaders().get(5).getName());

		Assert.assertTrue(!result.getCsvData().isEmpty());

	}

	@Test
	public void testParseFileInvalidFileFormat() {

		Mockito.doReturn(TEST_FILE_NAME_INVALID).when(this.multiPartFile).getOriginalFilename();

		try {
			this.parser.parseFile(this.multiPartFile);
			Assert.fail();
		} catch (FileParsingException e) {
			assert true;
		}

	}

	@Test
	public void testParseCsv() throws FileParsingException, IOException {

		Map<Integer, List<String>> csvMap = new HashMap<>();
		CSVReader reader = new CSVReader(new FileReader(this.csvFile));

		String nextLine[];
		Integer key = 0;
		while ((nextLine = reader.readNext()) != null) {
			csvMap.put(key++, Arrays.asList(nextLine));
		}

		reader.close();

		DesignImportData result = this.parser.parseCsvMap(csvMap);

		Assert.assertTrue(!result.getUnmappedHeaders().isEmpty());

		Assert.assertEquals("There must be 6 headers in file.", 6, result.getUnmappedHeaders().size());
		Assert.assertEquals("There must be 85 rows in file.", 85, result.getCsvData().size());
		Assert.assertEquals("TRIAL_INSTANCE", result.getUnmappedHeaders().get(0).getName());
		Assert.assertEquals("SITE_NAME", result.getUnmappedHeaders().get(1).getName());
		Assert.assertEquals("ENTRY_NO", result.getUnmappedHeaders().get(2).getName());
		Assert.assertEquals("PLOT_NO", result.getUnmappedHeaders().get(3).getName());
		Assert.assertEquals("REP_NO", result.getUnmappedHeaders().get(4).getName());
		Assert.assertEquals("BLOCK_NO", result.getUnmappedHeaders().get(5).getName());

		Assert.assertTrue(!result.getCsvData().isEmpty());

	}

	@Test
	public void testCreateDesignHeaders() {

		List<String> headers = new ArrayList<>();
		headers.add("TRIAL_INSTANCE");
		headers.add("ENTRY_NO");
		headers.add("PLOT_NO");

		List<DesignHeaderItem> designHeaderItems = this.parser.createDesignHeaders(headers);

		Assert.assertEquals(3, designHeaderItems.size());
		Assert.assertEquals("TRIAL_INSTANCE", designHeaderItems.get(0).getName());
		Assert.assertEquals("ENTRY_NO", designHeaderItems.get(1).getName());
		Assert.assertEquals("PLOT_NO", designHeaderItems.get(2).getName());

		// the column indices should follow the order of header in the headers list
		Assert.assertEquals(0, designHeaderItems.get(0).getColumnIndex());
		Assert.assertEquals(1, designHeaderItems.get(1).getColumnIndex());
		Assert.assertEquals(2, designHeaderItems.get(2).getColumnIndex());

	}

}
