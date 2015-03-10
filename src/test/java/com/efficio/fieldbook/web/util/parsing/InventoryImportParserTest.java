package com.efficio.fieldbook.web.util.parsing;

import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.web.common.exception.FileParsingException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.io.File;

import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/26/2015
 * Time: 4:42 PM
 */

@RunWith(MockitoJUnitRunner.class)
public class InventoryImportParserTest {

	@Mock
	private FileService fileService;

	@Mock
	private MessageSource messageSource;

	@InjectMocks
	private InventoryImportParser parser;

	private InventoryImportParser moled;

	private Workbook workbook;

	public static final String TEST_FILE_NAME = "Inventory_Import_Template_v1.xlsx";

	@Before
	public void setUp() throws Exception {
		moled = spy(parser);
		File workbookFile = new File(ClassLoader.getSystemClassLoader().getResource(TEST_FILE_NAME).toURI());

		assert workbookFile.exists();

		workbook = WorkbookFactory.create(workbookFile);
	}

	@Test
	public void testExcelFileParse() throws FileParsingException{
		parser.parseWorkbook(workbook);
	}
}
