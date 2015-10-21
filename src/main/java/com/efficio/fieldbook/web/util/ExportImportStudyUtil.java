
package com.efficio.fieldbook.web.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportImportStudyUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ExportImportStudyUtil.class);

	private static final Integer[] REQUIRED_COLUMNS = {TermId.PLOT_NO.getId(), TermId.ENTRY_NO.getId(), TermId.DESIG.getId()};

	private ExportImportStudyUtil() {
		// do nothing
	}

	public static String getCategoricalCellValue(final String idValue, final List<ValueReference> possibleValues) {
		// With the New Data Table, the idValue will contain the long text instead of the id.
		if (idValue != null && possibleValues != null && !possibleValues.isEmpty()) {
			for (final ValueReference possibleValue : possibleValues) {
				if (idValue.equalsIgnoreCase(possibleValue.getDescription())) {
					return possibleValue.getName();
				}
			}
		}
		// just in case an id was passed, but this won't be the case most of the time
		if (idValue != null && NumberUtils.isNumber(idValue)) {
			for (final ValueReference ref : possibleValues) {
				if (ref.getId().equals(Integer.valueOf(idValue))) {
					return ref.getName();
				}
			}
		}
		return idValue;
	}

	public static String getCategoricalIdCellValue(final String description, final List<ValueReference> possibleValues) {
		return ExportImportStudyUtil.getCategoricalIdCellValue(description, possibleValues, false);
	}

	public static String getCategoricalIdCellValue(final String description, final List<ValueReference> possibleValues,
			final boolean isReturnOriginalValue) {
		if (description != null) {
			for (final ValueReference possibleValue : possibleValues) {
				if (description.equalsIgnoreCase(possibleValue.getName())) {
					return possibleValue.getId().toString();
				}
			}
		}
		return isReturnOriginalValue ? description : "";
	}

	public static List<Integer> getLocationIdsFromTrialInstances(final Workbook workbook, final List<Integer> instances) {
		final List<Integer> locationIds = new ArrayList<Integer>();

		final List<MeasurementVariable> trialVariables = workbook.getTrialVariables();
		String label = null;
		final List<MeasurementRow> trialObservations = workbook.getTrialObservations();
		if (trialVariables != null && instances != null && !instances.isEmpty()) {
			for (final MeasurementVariable trialVariable : trialVariables) {
				if (trialVariable.getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					label = trialVariable.getName();
					break;
				}
			}
			if (trialObservations != null && label != null) {
				for (final MeasurementRow trialObservation : trialObservations) {
					final String trialInstanceString = trialObservation.getMeasurementDataValue(label);
					if (trialInstanceString != null && NumberUtils.isNumber(trialInstanceString)) {
						final int trialInstanceNumber = Double.valueOf(trialInstanceString).intValue();
						if (instances != null && instances.indexOf(Integer.valueOf(trialInstanceNumber)) != -1) {
							locationIds.add((int) trialObservation.getLocationId());
						}
					}
				}
			}
		}

		if (locationIds.isEmpty() && trialObservations != null) {
			for (final MeasurementRow trialObservation : trialObservations) {
				locationIds.add((int) trialObservation.getLocationId());
			}
		}

		return locationIds;
	}

	public static List<MeasurementRow> getApplicableObservations(final Workbook workbook, final List<MeasurementRow> observations,
			final List<Integer> instances) {
		List<MeasurementRow> rows = null;
		if (instances != null && !instances.isEmpty()) {
			rows = new ArrayList<MeasurementRow>();
			final List<Integer> locationIds = ExportImportStudyUtil.getLocationIdsFromTrialInstances(workbook, instances);
			for (final MeasurementRow row : observations) {
				if (locationIds.contains((int) row.getLocationId())) {
					rows.add(row);
				}
			}
		} else {
			rows = workbook.getObservations();
		}
		return rows;
	}

	public static String getSiteNameOfTrialInstance(final MeasurementRow trialObservation,
			final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) throws MiddlewareQueryException {
		if (trialObservation != null && trialObservation.getMeasurementVariables() != null) {
			for (final MeasurementData data : trialObservation.getDataList()) {
				if (data.getMeasurementVariable().getTermId() == TermId.TRIAL_LOCATION.getId()) {
					return "_" + data.getValue();
				} else if (data.getMeasurementVariable().getTermId() == TermId.LOCATION_ID.getId()) {
					return ExportImportStudyUtil.getSiteNameOfTrialInstanceBasedOnLocationID(fieldbookMiddlewareService, data);
				}
			}
		}
		return "";
	}

	private static String getSiteNameOfTrialInstanceBasedOnLocationID(
			final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, final MeasurementData data)
			throws MiddlewareQueryException {
		if (data.getValue() != null && !data.getValue().isEmpty() && NumberUtils.isNumber(data.getValue())) {
			return "_" + fieldbookMiddlewareService.getLocationById(Integer.parseInt(data.getValue())).getLname();
		} else {
			return "";
		}
	}

	public static boolean partOfRequiredColumns(final int termId) {
		for (final int id : ExportImportStudyUtil.REQUIRED_COLUMNS) {
			if (termId == id) {
				return true;
			}
		}
		return false;
	}

	public static String getPropertyName(final OntologyService ontologyService) {
		String propertyName = "";
		try {
			propertyName = ontologyService.getProperty(TermId.BREEDING_METHOD_PROP.getId()).getTerm().getName();
		} catch (final MiddlewareQueryException e) {
			ExportImportStudyUtil.LOG.error(e.getMessage(), e);
		}
		return propertyName;
	}

	public static boolean measurementVariableHasValue(final MeasurementData dataCell) {
		return dataCell.getMeasurementVariable() != null && dataCell.getMeasurementVariable().getPossibleValues() != null;
	}

	public static boolean isColumnVisible(final int termId, final List<Integer> visibleColumns) {
		if (visibleColumns == null) {
			return true;
		} else {
			return ExportImportStudyUtil.partOfRequiredColumns(termId) || visibleColumns.contains(termId);
		}
	}

	public static String getFileNamePath(final int trialInstanceNo, final MeasurementRow trialObservation, final List<Integer> instances,
			final String filename, final boolean isNursery, final FieldbookProperties fieldbookProperties,
			final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {

		String filenamePath = "";
		StringBuilder filenameBuilder = new StringBuilder();
		filenameBuilder.append(fieldbookProperties.getUploadDirectory());
		filenameBuilder.append(File.separator);
		filenameBuilder.append(SettingsUtil.cleanSheetAndFileName(filename));

		filenamePath = filenameBuilder.toString();

		if (isNursery) {
			return filenamePath;
		}

		// For Trial
		if (instances != null && !instances.isEmpty()) {

			final int fileExtensionIndex = filenamePath.lastIndexOf(".");
			final String siteName = ExportImportStudyUtil.getSiteNameOfTrialInstance(trialObservation, fieldbookMiddlewareService);

			filenameBuilder = new StringBuilder();
			if (instances.size() > 1) {
				filenameBuilder.append(filenamePath.substring(0, fileExtensionIndex));
			} else {
				filenameBuilder.append(filename.substring(0, filename.lastIndexOf(".")));
			}

			filenameBuilder.append("-");
			filenameBuilder.append(trialInstanceNo);
			filenameBuilder.append(SettingsUtil.cleanSheetAndFileName(siteName));
			filenameBuilder.append(filenamePath.substring(fileExtensionIndex));

			filenamePath = filenameBuilder.toString();
		}

		return filenamePath;
	}
}
