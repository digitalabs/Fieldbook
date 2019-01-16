
package com.efficio.fieldbook.web.util.parsing;

import org.generationcp.commons.parsing.FileParsingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class DesignImportParserTest {

	@Mock
	DesignImportCsvParser designImportCsvParser;

	@Mock
	DesignImportExcelParser designImportExcelParser;

	@Mock
	MultipartFile multipartFile;

	@InjectMocks
	DesignImportParser designImportParser = new DesignImportParser();

	@Test
	public void testParseFileCsv() throws FileParsingException {

		this.designImportParser.parseFile(DesignImportParser.FILE_TYPE_CSV, this.multipartFile);

		Mockito.verify(this.designImportCsvParser).parseFile(this.multipartFile);
		Mockito.verify(this.designImportExcelParser, Mockito.times(0)).parseFile(this.multipartFile, null);

	}

	@Test(expected = FileParsingException.class)
	public void testParseFileCsvFailed() throws FileParsingException {

		Mockito.when(this.designImportCsvParser.parseFile(this.multipartFile)).thenThrow(new FileParsingException());
		this.designImportParser.parseFile(DesignImportParser.FILE_TYPE_CSV, this.multipartFile);

	}

	@Test
	public void testParseFileExcel() throws FileParsingException {

		this.designImportParser.parseFile(DesignImportParser.FILE_TYPE_EXCEL, this.multipartFile);

		Mockito.verify(this.designImportExcelParser).parseFile(this.multipartFile, null);
		Mockito.verify(this.designImportCsvParser, Mockito.times(0)).parseFile(this.multipartFile);

	}

	@Test(expected = FileParsingException.class)
	public void testParseFileExcelFailed() throws FileParsingException {

		Mockito.when(this.designImportExcelParser.parseFile(this.multipartFile, null)).thenThrow(new FileParsingException());
		this.designImportParser.parseFile(DesignImportParser.FILE_TYPE_EXCEL, this.multipartFile);

	}

}
