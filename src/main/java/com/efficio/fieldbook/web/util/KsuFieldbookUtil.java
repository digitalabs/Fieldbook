
package com.efficio.fieldbook.web.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvWriter;
import com.google.common.collect.ImmutableMap;

public class KsuFieldbookUtil {

	private static final Logger LOG = LoggerFactory.getLogger(KsuFieldbookUtil.class);

	private static final String OBS_UNIT_ID = "obs_unit_id";
	private static final String RANGE = "range";
	public static final String PLOT = "PLOT_NO";

	private static final int TERM_PLOT_CODE = TermId.PLOT_CODE.getId();
	private static final int TERM_RANGE = TermId.RANGE_NO.getId();
	private static final int TERM_PLOT1 = TermId.PLOT_NO.getId();
	private static final int TERM_PLOT2 = TermId.PLOT_NNO.getId();

	private static final List<String> TRAIT_FILE_HEADERS = Arrays.asList("trait", "format", "defaultValue", "minimum",
			"maximum", "details", "categories", "isVisible", "realPosition");

	private static final List<Integer> dataTypeList = Arrays.asList(TermId.NUMERIC_VARIABLE.getId(),
			TermId.CATEGORICAL_VARIABLE.getId(), TermId.DATE_VARIABLE.getId(), TermId.CHARACTER_VARIABLE.getId());

	private static final Map<Integer, String> ID_NAME_MAP;

	static {
		ID_NAME_MAP = new HashMap<>();
		KsuFieldbookUtil.ID_NAME_MAP.put(KsuFieldbookUtil.TERM_PLOT_CODE, KsuFieldbookUtil.OBS_UNIT_ID);
		KsuFieldbookUtil.ID_NAME_MAP.put(KsuFieldbookUtil.TERM_RANGE, KsuFieldbookUtil.RANGE);
		KsuFieldbookUtil.ID_NAME_MAP.put(KsuFieldbookUtil.TERM_PLOT1, KsuFieldbookUtil.PLOT);
		KsuFieldbookUtil.ID_NAME_MAP.put(KsuFieldbookUtil.TERM_PLOT2, KsuFieldbookUtil.PLOT);
	}

	public enum KsuRequiredColumnEnum {
		ENTRY_NO(TermId.ENTRY_NO.getId(), "ENTRY_NO"), PLOT_NO(TermId.PLOT_NO.getId(), KsuFieldbookUtil.PLOT), GID(
				TermId.GID.getId(),
				"GID"), DESIGNATION(TermId.DESIG.getId(), "DESIGNATION"), OBS_UNIT_ID(TermId.OBS_UNIT_ID.getId(), "OBS_UNIT_ID");

		private final Integer id;
		private final String label;

		private static final Map<Integer, KsuRequiredColumnEnum> LOOK_UP = new HashMap<>();

		static {
			for (final KsuRequiredColumnEnum cl : EnumSet.allOf(KsuRequiredColumnEnum.class)) {
				KsuRequiredColumnEnum.LOOK_UP.put(cl.getId(), cl);
			}
		}

		KsuRequiredColumnEnum(final Integer id, final String label) {
			this.id = id;
			this.label = label;
		}

		public Integer getId() {
			return this.id;
		}

		public String getLabel() {
			return this.label;
		}

		public static KsuRequiredColumnEnum get(final Integer id) {
			return KsuRequiredColumnEnum.LOOK_UP.get(id);
		}
	}

	private static final ImmutableMap<Integer, String> dataTypeFormats = ImmutableMap.<Integer, String> builder()
			.put(TermId.CATEGORICAL_VARIABLE.getId(), "categorical").put(TermId.NUMERIC_VARIABLE.getId(), "numeric")
			.put(TermId.DATE_VARIABLE.getId(), "date").put(TermId.CHARACTER_VARIABLE.getId(), "text")
			.put(0, "unrecognized").build();

	public static List<List<String>> convertWorkbookData(final List<MeasurementRow> observations,
			final List<MeasurementVariable> variables) {

		final List<List<String>> table = new ArrayList<>();

		if (observations != null && !observations.isEmpty()) {

			// write header row
			table.add(KsuFieldbookUtil.getHeaderNames(variables));

			final List<MeasurementVariable> labels = KsuFieldbookUtil.getMeasurementLabels(variables);

			for (final MeasurementRow row : observations) {
				final List<String> dataRow = new ArrayList<>();

				for (final MeasurementVariable label : labels) {
					String value = null;
					if (label.getPossibleValues() != null && !label.getPossibleValues().isEmpty()) {
						value = ExportImportStudyUtil.getCategoricalCellValue(
								row.getMeasurementData(label.getName()).getValue(), label.getPossibleValues());
					} else {
						value = row.getMeasurementData(label.getName()).getValue();
					}
					dataRow.add(value);
				}
				table.add(dataRow);
			}
		}

		return table;
	}

	/**
	 * Writes the Header Row to the CSV or Excel file. Omits designated columns.
	 *
	 * @param headers
	 *            : List of MeasurementVariables to filter, processing the name
	 *            of the Variable
	 * @return a list of Strings to print in appropriate format
	 *
	 */
	static List<String> getHeaderNames(final List<MeasurementVariable> headers) {
		final List<String> names = new ArrayList<>();

		if (headers != null && !headers.isEmpty()) {
			for (final MeasurementVariable header : headers) {
				if (header.isFactor()) {
					if (KsuFieldbookUtil.ID_NAME_MAP.get(header.getTermId()) != null) {
						names.add(KsuFieldbookUtil.ID_NAME_MAP.get(header.getTermId()));
					} else {
						names.add(header.getName());
					}
				}
			}
		}

		return names;
	}

	public static boolean isValidHeaderNames(final String[] headerNames) {
		final List<String> rowHeadersList = Arrays.asList(headerNames);
		for (final KsuRequiredColumnEnum column : KsuRequiredColumnEnum.values()) {
			if (!rowHeadersList.contains(column.getLabel().trim())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Collects the measurement data required to export. Processes omissions as
	 * required in {@link KsuFieldbookUtil}
	 *
	 * @param variables
	 *            : The variables to export
	 * @return
	 */
	private static List<MeasurementVariable> getMeasurementLabels(final List<MeasurementVariable> variables) {
		final List<MeasurementVariable> labels = new ArrayList<>();

		for (final MeasurementVariable factor : variables) {
			if (factor.isFactor()) {
				labels.add(factor);
			}
		}

		return labels;
	}

	public static void writeTraits(final List<MeasurementVariable> traits, final String filenamePath,
			final FieldbookService fieldbookMiddlewareService, final OntologyService ontologyService) {

		new File(filenamePath).exists();
		CsvWriter csvWriter = null;
		try {
			final List<List<String>> dataTable = KsuFieldbookUtil.convertTraitsData(traits, fieldbookMiddlewareService,
					ontologyService);

			csvWriter = new CsvWriter(new FileWriter(filenamePath, false), ',');
			for (final List<String> row : dataTable) {
				for (final String cell : row) {
					csvWriter.write(cell);
				}
				csvWriter.endRecord();
			}

		} catch (final IOException e) {
			KsuFieldbookUtil.LOG.error("ERROR in KSU CSV Export Study", e);

		} finally {
			if (csvWriter != null) {
				csvWriter.close();
			}
		}
	}

	protected static List<List<String>> convertTraitsData(final List<MeasurementVariable> traits,
			final FieldbookService fieldbookMiddlewareService, final OntologyService ontologyService) {
		final List<List<String>> data = new ArrayList<>();

		data.add(KsuFieldbookUtil.TRAIT_FILE_HEADERS);

		// get name of breeding method property and get all methods
		String propertyName = "";
		List<Method> methods = new ArrayList<>();
		try {
			methods = fieldbookMiddlewareService.getAllBreedingMethods(false);
			propertyName = ontologyService.getProperty(TermId.BREEDING_METHOD_PROP.getId()).getName();
		} catch (final MiddlewareQueryException e) {
			KsuFieldbookUtil.LOG.error(e.getMessage(), e);
		}

		int index = 1;
		for (final MeasurementVariable trait : traits) {
			final List<String> traitData = new ArrayList<>();
			traitData.add(trait.getName());
			traitData.add(KsuFieldbookUtil.getDataTypeDescription(trait));
			// default value
			traitData.add("");
			if (trait.getMinRange() != null) {
				traitData.add(trait.getMinRange().toString());
			} else {
				traitData.add("");
			}
			if (trait.getMaxRange() != null) {
				traitData.add(trait.getMaxRange().toString());
			} else {
				traitData.add("");
			}
			traitData.add(""); // details
			if (trait.getPossibleValues() != null && !trait.getPossibleValues().isEmpty()
					&& !trait.getProperty().equals(propertyName)) {
				final StringBuilder possibleValuesString = new StringBuilder();
				for (final ValueReference value : trait.getPossibleValues()) {
					if (possibleValuesString.length() > 0) {
						possibleValuesString.append("/");
					}
					possibleValuesString.append(value.getName());
				}

				traitData.add(possibleValuesString.toString());
			} else if (trait.getProperty().equals(propertyName)) {
				final StringBuilder possibleValuesString = new StringBuilder();
				// add code for breeding method properties
				for (final Method method : methods) {
					if (possibleValuesString.length() > 0) {
						possibleValuesString.append("/");
					}
					possibleValuesString.append(method.getMcode());
				}
				traitData.add(possibleValuesString.toString());
			} else {
				traitData.add(""); // categories
			}
			traitData.add("TRUE");
			traitData.add(String.valueOf(index));
			index++;
			data.add(traitData);
		}

		return data;
	}

	static String getDataTypeDescription(final MeasurementVariable trait) {
		final Integer dataType;
		if (trait.getDataTypeId() == null || !KsuFieldbookUtil.dataTypeList.contains(trait.getDataTypeId())) {
			dataType = 0;
		} else {
			dataType = trait.getDataTypeId();
		}
		return KsuFieldbookUtil.dataTypeFormats.get(dataType);
	}

	public static String getLabelFromKsuRequiredColumn(final MeasurementVariable variable) {
		String label = "";

		if (KsuRequiredColumnEnum.get(variable.getTermId()) != null) {
			label = KsuRequiredColumnEnum.get(variable.getTermId()).getLabel();
		}

		if (label.trim().length() > 0) {
			return label;
		}

		return variable.getName();
	}
}
