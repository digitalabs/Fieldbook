package com.efficio.fieldbook.web.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.efficio.fieldbook.web.common.controller.ExportStudyController;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import org.generationcp.middleware.domain.oms.TermId;
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

	public static void processEnvironmentData(final EnvironmentData data) {
		for (int i = 0; i < data.getEnvironments().size(); i++) {
			final Map<String, String> values = data.getEnvironments().get(i).getManagementDetailValues();
			if (!values.containsKey(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()))) {
				values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
			} else if (values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())) == null
					|| values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())).isEmpty()) {
				values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
			}
		}
	}
}
