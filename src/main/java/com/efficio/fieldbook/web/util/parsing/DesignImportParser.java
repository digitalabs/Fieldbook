
package com.efficio.fieldbook.web.util.parsing;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.commons.parsing.AbstractCsvFileProcessor;
import org.generationcp.commons.parsing.FileParsingException;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.common.bean.DesignImportData;

public class DesignImportParser extends AbstractCsvFileProcessor<DesignImportData> {

	public static final Integer FILE_TYPE_CSV = 1;
	public static final Integer FILE_TYPE_EXCEL = 2;

	@Resource
	private DesignImportCsvParser csvParser;

	@Resource
	private DesignImportExcelParser excelParser;

	/**
	 * Parses the uploaded file
	 * 
	 * @param fileType
	 * @param file
	 * @return
	 * @throws FileParsingException
	 */
	public DesignImportData parseFile(Integer fileType, MultipartFile file) throws FileParsingException {

		DesignImportData designImportData = null;

		if (fileType == FILE_TYPE_CSV) {
			designImportData = this.csvParser.parseFile(file);
		} else if (fileType == FILE_TYPE_EXCEL) {
			designImportData = this.excelParser.parseFile(file, null);
		}

		return designImportData;
	}

	/**
	 * Parses the file from the file system
	 * 
	 * @param fileType
	 * @param absoluteFilename
	 * @return
	 * @throws FileParsingException
	 */
	public DesignImportData parseFile(Integer fileType, String absoluteFilename) throws FileParsingException {

		DesignImportData designImportData = null;

		if (fileType == FILE_TYPE_CSV) {
			designImportData = this.csvParser.parseFile(absoluteFilename);
		} else if (fileType == FILE_TYPE_EXCEL) {
			// FIXME : DanV to fix
			//designImportData = this.excelParser.parseFile(absoluteFilename, null);
		}

		return designImportData;
	}

	@Override
	public DesignImportData parseCsvMap(Map<Integer, List<String>> csvMap) throws FileParsingException {
		// TODO Auto-generated method stub
		return null;
	}

}