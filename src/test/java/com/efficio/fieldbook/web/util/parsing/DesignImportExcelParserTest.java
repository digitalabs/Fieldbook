
package com.efficio.fieldbook.web.util.parsing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.parsing.FileParsingException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;

public class DesignImportExcelParserTest extends AbstractBaseIntegrationTest {

	private static final String BLOCK_NO = "BLOCK_NO";

	private static final String REP_NO = "REP_NO";

	private static final String SITE_NAME = "SITE_NAME";

	private static final String PLOT_NO = "PLOT_NO";

	private static final String ENTRY_NO = "ENTRY_NO";

	private static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";

	private static final String VALUE_3 = "3";

	private static final String VALUE_2 = "2";

	private static final String VALUE_1 = "1";

	private static File excelFile;

	public static final String TEST_FILE_NAME = "Design_Import_Template.xls";

	public static final String TEST_FILE_NAME_INVALID = "Design_Import_Template.csv";

	@Resource
	private DesignImportExcelParser parser;

	@BeforeClass
	public static void runOnce() throws URISyntaxException, IOException {

		DesignImportExcelParserTest.excelFile =
				new File(ClassLoader.getSystemClassLoader().getResource(DesignImportExcelParserTest.TEST_FILE_NAME).toURI());

		assert DesignImportExcelParserTest.excelFile.exists();

	}

	@Test
	public void testParseFile() throws FileParsingException, IOException {

		FileInputStream input = new FileInputStream(excelFile);
		MultipartFile multiPartFile = new MockMultipartFile("file", excelFile.getName(), "text/plain", IOUtils.toByteArray(input));

		final DesignImportData result = this.parser.parseFile(multiPartFile, null);

		Assert.assertTrue(!result.getUnmappedHeaders().isEmpty());

		Assert.assertEquals("There must be 6 headers in file.", 6, result.getUnmappedHeaders().size());
		Assert.assertEquals("There must be 85 rows in file.", 85, result.getRowDataMap().size());
		Assert.assertEquals(TRIAL_INSTANCE, result.getUnmappedHeaders().get(0).getName());
		Assert.assertEquals(SITE_NAME, result.getUnmappedHeaders().get(1).getName());
		Assert.assertEquals(ENTRY_NO, result.getUnmappedHeaders().get(2).getName());
		Assert.assertEquals(PLOT_NO, result.getUnmappedHeaders().get(3).getName());
		Assert.assertEquals(REP_NO, result.getUnmappedHeaders().get(4).getName());
		Assert.assertEquals(BLOCK_NO, result.getUnmappedHeaders().get(5).getName());

		Assert.assertTrue(!result.getRowDataMap().isEmpty());

	}

	@Test
	public void testParseFileInvalidFileFormat() throws FileParsingException, IOException {

		FileInputStream input = new FileInputStream(excelFile);
		MultipartFile multiPartFile = new MockMultipartFile("file", TEST_FILE_NAME_INVALID, "text/plain", IOUtils.toByteArray(input));

		try {
			this.parser.parseFile(multiPartFile, null);
			Assert.fail();
		} catch (final FileParsingException e) {
			assert true;
		}

	}

	@Test
	public void testCreateDesignHeaders() throws FileParsingException {

		final Workbook workbook = this.createTestWorkbook();
		final List<DesignHeaderItem> designHeaderItems =
				this.parser.createDesignHeaders(workbook.getSheetAt(DesignImportExcelParser.SHEET_INDEX).getRow(
						DesignImportExcelParser.HEADER_ROW_INDEX));

		Assert.assertEquals(3, designHeaderItems.size());
		Assert.assertEquals(TRIAL_INSTANCE, designHeaderItems.get(0).getName());
		Assert.assertEquals(ENTRY_NO, designHeaderItems.get(1).getName());
		Assert.assertEquals(PLOT_NO, designHeaderItems.get(2).getName());

		// the column indices should follow the order of header in the headers list
		Assert.assertEquals(0, designHeaderItems.get(0).getColumnIndex());
		Assert.assertEquals(1, designHeaderItems.get(1).getColumnIndex());
		Assert.assertEquals(2, designHeaderItems.get(2).getColumnIndex());

	}

	@Test(expected = FileParsingException.class)
	public void testCreateDesignHeadersWithEmptyFile() throws FileParsingException {

		final Workbook workbook = this.createEmptyWorkbook();
		this.parser.createDesignHeaders(workbook.getSheetAt(DesignImportExcelParser.SHEET_INDEX).getRow(
				DesignImportExcelParser.HEADER_ROW_INDEX));
	}
	
	@Test
	public void testConvertRowsToMap() {

		final Workbook workbook = this.createTestWorkbook();
		final Sheet sheet = workbook.getSheetAt(DesignImportExcelParser.SHEET_INDEX);
		Map<Integer, List<String>> result = this.parser.convertRowsToMap(sheet, 3);

		Assert.assertEquals(2, result.size());
		Assert.assertEquals(TRIAL_INSTANCE, result.get(0).get(0));
		Assert.assertEquals(ENTRY_NO, result.get(0).get(1));
		Assert.assertEquals(PLOT_NO, result.get(0).get(2));
		Assert.assertEquals(VALUE_1, result.get(1).get(0));
		Assert.assertEquals(VALUE_2, result.get(1).get(1));
		Assert.assertEquals(VALUE_3, result.get(1).get(2));
	}

	private Workbook createTestWorkbook() {
		Workbook workbook = this.createEmptyWorkbook();
		Sheet sheet = workbook.getSheetAt(DesignImportExcelParser.SHEET_INDEX);
		Row header = sheet.createRow(0);
		header.createCell(0).setCellValue(TRIAL_INSTANCE);
		header.createCell(1).setCellValue(ENTRY_NO);
		header.createCell(2).setCellValue(PLOT_NO);

		Row dataRow = sheet.createRow(1);
		dataRow.createCell(0).setCellValue(VALUE_1);
		dataRow.createCell(1).setCellValue(VALUE_2);
		dataRow.createCell(2).setCellValue(VALUE_3);

		return workbook;
	}
	
	private Workbook createEmptyWorkbook(){
		HSSFWorkbook workbook = new HSSFWorkbook();
		workbook.createSheet();

		return workbook;
	}

}
