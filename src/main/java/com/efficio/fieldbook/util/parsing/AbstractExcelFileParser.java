package com.efficio.fieldbook.util.parsing;

import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.web.common.exception.FileParsingException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.middleware.util.PoiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/24/2015
 * Time: 5:38 PM
 */
public abstract class AbstractExcelFileParser<T> {
	private static final Logger LOG = LoggerFactory.getLogger(
			AbstractExcelFileParser.class);
	protected Workbook workbook;
	protected String originalFilename;

	protected final static String[] EXCEL_FILE_EXTENSIONS = new String[] {"xls", "xlsx"};

	@Resource
	protected FileService fileService;

	@Resource
	protected MessageSource messageSource;

	public T parseFile(MultipartFile file) throws FileParsingException {
		this.workbook = storeAndRetrieveWorkbook(file);
		return parseWorkbook(workbook);
	}

	public String[] getSupportedFileExtensions() {
		return EXCEL_FILE_EXTENSIONS;
	}

	public abstract T parseWorkbook(Workbook workbook) throws FileParsingException;

	public Workbook storeAndRetrieveWorkbook(MultipartFile multipartFile)
			throws FileParsingException {
		try {
			this.originalFilename = multipartFile.getOriginalFilename();

			if (!isFileExtensionSupported(originalFilename)) {
				throw new InvalidFormatException("Unsupported file format");
			}

			String serverFilename = fileService.saveTemporaryFile(multipartFile.getInputStream());

			return fileService.retrieveWorkbook(serverFilename);
		} catch (InvalidFormatException | IOException e) {
			LOG.debug(e.getMessage(), e);
			throw new FileParsingException("common.error.invalid.file");
		}
	}

	protected boolean isFileExtensionSupported(String filename) {
		boolean extensionCheckResult = false;

		String extension = filename.substring(filename.lastIndexOf(".") + 1,
						filename.length());

		String[] supportedExtensions = getSupportedFileExtensions();
		assert supportedExtensions != null && supportedExtensions.length > 0;

		for (String supported : supportedExtensions) {
			if (supported.equalsIgnoreCase(extension)) {
				extensionCheckResult = true;
				break;
			}
		}

		return extensionCheckResult;
	}

	protected boolean isHeaderInvalid(int headerNo, int sheetNumber, String[] headers) {
		boolean isInvalid = false;

		for (int i = 0; i < headers.length; i++) {
			isInvalid = isInvalid || !headers[i].equalsIgnoreCase(
					getCellStringValue(sheetNumber, headerNo, i));
		}

		return isInvalid;
	}

	/**
	 * Wrapper to PoiUtil.getCellStringValue static call so we can stub the methods on unit tests
	 *
	 * @param sheetNo
	 * @param rowNo
	 * @param columnNo
	 * @return
	 */
	public String getCellStringValue(int sheetNo, int rowNo, Integer columnNo) {
		String out = (null == columnNo) ?
				"" :
				PoiUtil.getCellStringValue(this.workbook, sheetNo, rowNo, columnNo);
		return (null == out) ? "" : out;
	}

	/**
	 * Wrapper to PoiUtil.rowIsEmpty static call so we can stub the methods on unit tests
	 *
	 * @param sheetNo
	 * @param rowNo
	 * @param colCount
	 * @return
	 */
	public boolean isRowEmpty(int sheetNo, int rowNo, int colCount) {
		return PoiUtil.rowIsEmpty(workbook.getSheetAt(sheetNo), rowNo, 0, colCount - 1);
	}

}
