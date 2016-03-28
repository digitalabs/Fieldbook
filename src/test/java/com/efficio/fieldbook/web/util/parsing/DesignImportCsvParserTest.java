
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

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.generationcp.commons.parsing.FileParsingException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;

import au.com.bytecode.opencsv.CSVReader;

public class DesignImportCsvParserTest extends AbstractBaseIntegrationTest {

	private static File csvFile;

	public static final String TEST_FILE_NAME = "Design_Import_Template.csv";

	public static final String TEST_FILE_NAME_INVALID = "Design_Import_Template.xls";

	@Resource
	private DesignImportCsvParser parser;

	@BeforeClass
	public static void runOnce() throws URISyntaxException, IOException {

		DesignImportCsvParserTest.csvFile =
				new File(ClassLoader.getSystemClassLoader().getResource(DesignImportCsvParserTest.TEST_FILE_NAME).toURI());

		assert DesignImportCsvParserTest.csvFile.exists();

	}

	@Test
	public void testParseFile() throws FileParsingException, IOException {

		final FileInputStream input = new FileInputStream(DesignImportCsvParserTest.csvFile);
		final MultipartFile multiPartFile =
				new MockMultipartFile("file", DesignImportCsvParserTest.csvFile.getName(), "text/plain", IOUtils.toByteArray(input));

		final DesignImportData result = this.parser.parseFile(multiPartFile);

		Assert.assertTrue(!result.getUnmappedHeaders().isEmpty());

		Assert.assertEquals("There must be 6 headers in file.", 6, result.getUnmappedHeaders().size());
		Assert.assertEquals("There must be 85 rows in file.", 85, result.getRowDataMap().size());
		Assert.assertEquals("TRIAL_INSTANCE", result.getUnmappedHeaders().get(0).getName());
		Assert.assertEquals("SITE_NAME", result.getUnmappedHeaders().get(1).getName());
		Assert.assertEquals("ENTRY_NO", result.getUnmappedHeaders().get(2).getName());
		Assert.assertEquals("PLOT_NO", result.getUnmappedHeaders().get(3).getName());
		Assert.assertEquals("REP_NO", result.getUnmappedHeaders().get(4).getName());
		Assert.assertEquals("BLOCK_NO", result.getUnmappedHeaders().get(5).getName());

		Assert.assertTrue(!result.getRowDataMap().isEmpty());

	}

	@Test
	public void testParseFileInvalidFileFormat() throws FileParsingException, IOException {

		final FileInputStream input = new FileInputStream(DesignImportCsvParserTest.csvFile);
		final MultipartFile multiPartFile =
				new MockMultipartFile("file", DesignImportCsvParserTest.TEST_FILE_NAME_INVALID, "text/plain", IOUtils.toByteArray(input));

		try {
			this.parser.parseFile(multiPartFile);
			Assert.fail();
		} catch (final FileParsingException e) {
			assert true;
		}

	}

	@Test
	public void testParseCsv() throws FileParsingException, IOException {

		final Map<Integer, List<String>> csvMap = new HashMap<>();
		final CSVReader reader = new CSVReader(new FileReader(DesignImportCsvParserTest.csvFile));

		String nextLine[];
		Integer key = 0;
		while ((nextLine = reader.readNext()) != null) {
			csvMap.put(key++, Arrays.asList(nextLine));
		}

		reader.close();

		final DesignImportData result = this.parser.parseCsvMap(csvMap);

		Assert.assertTrue(!result.getUnmappedHeaders().isEmpty());

		Assert.assertEquals("There must be 6 headers in file.", 6, result.getUnmappedHeaders().size());
		Assert.assertEquals("There must be 85 rows in file.", 85, result.getRowDataMap().size());
		Assert.assertEquals("TRIAL_INSTANCE", result.getUnmappedHeaders().get(0).getName());
		Assert.assertEquals("SITE_NAME", result.getUnmappedHeaders().get(1).getName());
		Assert.assertEquals("ENTRY_NO", result.getUnmappedHeaders().get(2).getName());
		Assert.assertEquals("PLOT_NO", result.getUnmappedHeaders().get(3).getName());
		Assert.assertEquals("REP_NO", result.getUnmappedHeaders().get(4).getName());
		Assert.assertEquals("BLOCK_NO", result.getUnmappedHeaders().get(5).getName());

		Assert.assertTrue(!result.getRowDataMap().isEmpty());

	}

	@Test(expected = FileParsingException.class)
	public void testParseCsvWithEMptyCSVFile() throws FileParsingException {
		final Map<Integer, List<String>> csvMap = new HashMap<>();
		this.parser.parseCsvMap(csvMap);
	}

	@Test
	public void testCreateDesignHeaders() {

		final List<String> headers = new ArrayList<>();
		headers.add("TRIAL_INSTANCE");
		headers.add("ENTRY_NO");
		headers.add("PLOT_NO");

		final List<DesignHeaderItem> designHeaderItems = this.parser.createDesignHeaders(headers);

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
