/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/
package com.efficio.fieldbook.utils.test;

import java.io.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * The Class SettingsUtil.
 */
public class ExcelImportUtil {

	public static Workbook parseFile(String filename) throws Exception {
		Workbook readWorkbook = null;
		try {
			HSSFWorkbook xlsBook = new HSSFWorkbook(new FileInputStream(new File(filename)));
			readWorkbook = xlsBook;
		} catch (OfficeXmlFileException officeException) {
			XSSFWorkbook xlsxBook = new XSSFWorkbook(new FileInputStream(new File(filename)));
			readWorkbook = xlsxBook;
		}
		return readWorkbook;
	}
}
