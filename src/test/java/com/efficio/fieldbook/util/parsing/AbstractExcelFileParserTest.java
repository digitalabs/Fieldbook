package com.efficio.fieldbook.util.parsing;

import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.web.common.exception.FileParsingException;
import com.efficio.fieldbook.web.util.parsing.InventoryImportParser;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Stubber;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 3/13/2015
 * Time: 2:06 PM
 */

@RunWith(MockitoJUnitRunner.class)
public class AbstractExcelFileParserTest {
	@Mock
	protected FileService fileService;

	@Mock
	protected MessageSource messageSource;

	@InjectMocks
	private InventoryImportParser importParser;

	@Test(expected = FileParsingException.class)
	public void testStoreAndRetrieveWorkbookInvalidFormat() throws FileParsingException{
		MultipartFile file = mock(MultipartFile.class);

		InventoryImportParser mole = spy(importParser);
		doReturn(false).when(mole).isFileExtensionSupported(anyString());

		mole.storeAndRetrieveWorkbook(file);
	}

	@Test
	public void testStoreAndRetrieveWorkbook() throws FileParsingException, IOException, InvalidFormatException {
		MultipartFile file = mock(MultipartFile.class);

		InventoryImportParser mole = spy(importParser);
		doReturn(true).when(mole).isFileExtensionSupported(anyString());
		Workbook dummy = mock(Workbook.class);
		when(fileService.saveTemporaryFile(any(InputStream.class))).thenReturn("DUMMY");
		when(fileService.retrieveWorkbook(anyString())).thenReturn(dummy);

		Workbook retrieved = mole.storeAndRetrieveWorkbook(file);
		assertEquals(dummy, retrieved);
	}

	@Test
	public void testIsFileExtensionSupportedProperExcelFormats() {
		assertTrue(importParser.isFileExtensionSupported("trial.xls"));
		assertTrue(importParser.isFileExtensionSupported("trial.xlsx"));
		assertTrue(importParser.isFileExtensionSupported("TRIAL.XLS"));
		assertTrue(importParser.isFileExtensionSupported("TRIAL.XLSX"));
	}

	@Test
	public void testIsFileExtensionSupportedNonExcel() {
		assertFalse(importParser.isFileExtensionSupported("icon.png"));
		assertFalse(importParser.isFileExtensionSupported("file.csv"));
		assertFalse(importParser.isFileExtensionSupported("file.doc"));
		assertFalse(importParser.isFileExtensionSupported("file.txt"));
	}

	@Test
	public void testIsHeadersInvalidSuccess() {
		Stubber currentStub = null;
		InventoryImportParser mole = spy(importParser);
		for (String header : InventoryImportParser.HEADER_LABEL_ARRAY) {
			if (currentStub == null) {
				currentStub = doReturn(header);
			} else {
				currentStub = currentStub.doReturn(header);
			}
		}

		currentStub.when(mole).getCellStringValue(anyInt(), anyInt(), anyInt());
		assertFalse(mole.isHeaderInvalid(0, 0, InventoryImportParser.HEADER_LABEL_ARRAY));
	}

	@Test
		public void testIsHeadersInvalidFail() {
			Stubber currentStub = null;
			InventoryImportParser mole = spy(importParser);
			for (String header : InventoryImportParser.HEADER_LABEL_ARRAY) {
				if (currentStub == null) {
					currentStub = doReturn("RANDOM STRING");
				} else {
					currentStub = currentStub.doReturn("RANDOM STRING");
				}
			}

			currentStub.when(mole).getCellStringValue(anyInt(), anyInt(), anyInt());
			assertTrue(mole.isHeaderInvalid(0, 0, InventoryImportParser.HEADER_LABEL_ARRAY));
		}
}
