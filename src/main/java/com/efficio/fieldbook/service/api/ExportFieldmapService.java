/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.service.api;

import java.io.FileOutputStream;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;

/**
 * The Interface ExcelExportService. Handles the exporting needs to Excel file format.
 */
public interface ExportFieldmapService {

	/**
	 * Exports a fieldmap to an Excel file.
	 * 
	 * @param fileName The file name of the Excel file to generate
	 * @param userFieldMap The data to export
	 * @return
	 * @throws FieldbookException
	 */
	FileOutputStream exportFieldMapToExcel(String fileName, UserFieldmap userFieldMap) throws FieldbookException;

}
