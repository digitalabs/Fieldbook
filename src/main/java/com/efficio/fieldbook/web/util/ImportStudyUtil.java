package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.WorkbookParserException;

public class ImportStudyUtil {
	
	public static Map<String, MeasurementRow> createMeasurementRowsMap(final List<MeasurementRow> observations, final String instanceNumber,
			final boolean isNursery) {
		final Map<String, MeasurementRow> map = new HashMap<String, MeasurementRow>();
		List<MeasurementRow> newObservations = new ArrayList<MeasurementRow>();
		if (!isNursery) {
			if (instanceNumber != null && !"".equalsIgnoreCase(instanceNumber)) {
				newObservations = WorkbookUtil.filterObservationsByTrialInstance(observations, instanceNumber);
			}
		} else {
			newObservations = observations;
		}

		if (newObservations != null && !newObservations.isEmpty()) {
			for (final MeasurementRow row : newObservations) {
				map.put(row.getKeyIdentifier(), row);
			}
		}
		return map;
	}
	
	public static String getTrialInstanceNo(Workbook workbook, String filename) throws WorkbookParserException {
		String trialInstanceNumber = workbook != null && workbook.isNursery() ? "1" : ImportStudyUtil.getTrialInstanceNoFromFileName(filename);
		if (trialInstanceNumber == null || "".equalsIgnoreCase(trialInstanceNumber)) {
			throw new WorkbookParserException("error.workbook.import.missing.trial.instance");
		}
		return trialInstanceNumber;
	}
	
	public static String getTrialInstanceNoFromFileName(String filename) throws WorkbookParserException {
		String trialInstanceNumber = "";

		String pattern = "(.+)[-](\\d+)";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(filename);

		if (m.find()) {
			trialInstanceNumber = m.group(m.groupCount());
		}

		if (!NumberUtils.isNumber(trialInstanceNumber)) {
			throw new WorkbookParserException("error.workbook.import.missing.trial.instance");
		}

		return trialInstanceNumber;
	}
}
