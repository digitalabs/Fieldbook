package com.efficio.fieldbook.util.parsing;

import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.web.common.exception.FileParsingException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.middleware.util.PoiUtil;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/24/2015
 * Time: 5:38 PM
 */
public abstract class AbstractExcelFileParser<T> {

	protected Workbook workbook;
	protected String originalFilename;

	protected final String[] EXCEL_FILE_EXTENSIONS = new String[] {"xls", "xlsx"};

	@Resource
	protected FileService fileService;

	@Resource
	protected MessageSource messageSource;


	public T parseFile(MultipartFile file) throws FileParsingException{
		try {
			this.workbook = storeAndRetrieveWorkbook(file);
		} catch (IOException | InvalidFormatException e) {
			throw new FileParsingException(messageSource.getMessage("common.error.invalid.file", new Object[] {},
					Locale.getDefault()));
		}

		return parseWorkbook(workbook);
	}

	public String[] getSupportedFileExtensions() {
		return EXCEL_FILE_EXTENSIONS;
	}

	public abstract T parseWorkbook(Workbook workbook) throws FileParsingException;

	public Workbook storeAndRetrieveWorkbook(MultipartFile multipartFile)
			throws IOException, InvalidFormatException {
		this.originalFilename = multipartFile.getOriginalFilename();

		if (!isFileExtensionSupported(originalFilename)) {
			throw new InvalidFormatException("Unsupported file format");
		}

		String serverFilename = fileService.saveTemporaryFile(multipartFile.getInputStream());

		return fileService.retrieveWorkbook(serverFilename);
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

	public Map<String, Integer> convertToHeaderColumnMap(String[] headers) {
		Map<String, Integer> headerColumnMap = new HashMap<>();

		int i = 1;
		for (String header : headers) {
			headerColumnMap.put(header, i++);
		}

		return headerColumnMap;
	}
}
