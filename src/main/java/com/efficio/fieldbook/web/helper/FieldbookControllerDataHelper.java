package com.efficio.fieldbook.web.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;

import com.efficio.fieldbook.web.common.controller.ExportStudyController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to remove any duplicate code in Controllers
 */
public class FieldbookControllerDataHelper {

	private static final Logger LOG = LoggerFactory.getLogger(FieldbookControllerDataHelper.class);


	public static void writeXlsToOutputStream(File xls, final HttpServletResponse response) {
		try {
			FileInputStream in = new FileInputStream(xls);
			final OutputStream out = response.getOutputStream();

			final byte[] buffer = new byte[ExportStudyController.BUFFER_SIZE];
			int length = 0;

			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			in.close();
			out.close();
		} catch(IOException e) {
			FieldbookControllerDataHelper.LOG.error(e.getMessage(), e);
		}
	}
}
