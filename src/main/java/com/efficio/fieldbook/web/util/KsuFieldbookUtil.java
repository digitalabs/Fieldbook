
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

public class KsuFieldbookUtil {

	private static final Logger LOG = LoggerFactory.getLogger(KsuFieldbookUtil.class);

	private static final String PLOT_ID = "plot_id";
	private static final String RANGE = "range";
	private static final String PLOT = "plot";

	private static final int TERM_PLOT_ID = TermId.PLOT_CODE.getId();
	private static final int TERM_RANGE = TermId.RANGE_NO.getId();
	private static final int TERM_PLOT1 = TermId.PLOT_NO.getId();
	private static final int TERM_PLOT2 = TermId.PLOT_NNO.getId();

	private static final List<String> TRAIT_FILE_HEADERS = Arrays.asList("trait", "format", "defaultValue", "minimum", "maximum",
			"details", "categories", "isVisible", "realPosition");

	private static final String NUMERIC_FORMAT = "numeric";
	private static final String TEXT_FORMAT = "text";

	private static final Map<Integer, String> ID_NAME_MAP;

	static {
		ID_NAME_MAP = new HashMap<Integer, String>();
		KsuFieldbookUtil.ID_NAME_MAP.put(KsuFieldbookUtil.TERM_PLOT_ID, KsuFieldbookUtil.PLOT_ID);
		KsuFieldbookUtil.ID_NAME_MAP.put(KsuFieldbookUtil.TERM_RANGE, KsuFieldbookUtil.RANGE);
		KsuFieldbookUtil.ID_NAME_MAP.put(KsuFieldbookUtil.TERM_PLOT1, KsuFieldbookUtil.PLOT);
		KsuFieldbookUtil.ID_NAME_MAP.put(KsuFieldbookUtil.TERM_PLOT2, KsuFieldbookUtil.PLOT);
	}

	public enum KsuRequiredColumnEnum {
		ENTRY_NO(TermId.ENTRY_NO.getId(), "ENTRY_NO"), PLOT_NO(TermId.PLOT_NO.getId(), KsuFieldbookUtil.PLOT), GID(TermId.GID.getId(),
				"GID"), DESIGNATION(TermId.DESIG.getId(), "DESIGNATION");

		private final Integer id;
		private final String label;

		private static final Map<Integer, KsuRequiredColumnEnum> LOOK_UP = new HashMap<>();

		static {
			for (KsuRequiredColumnEnum cl : EnumSet.allOf(KsuRequiredColumnEnum.class)) {
				KsuRequiredColumnEnum.LOOK_UP.put(cl.getId(), cl);
			}
		}

		KsuRequiredColumnEnum(Integer id, String label) {
			this.id = id;
			this.label = label;
		}

		public Integer getId() {
			return this.id;
		}

		public String getLabel() {
			return this.label;
		}

		public static KsuRequiredColumnEnum get(Integer id) {
			return KsuRequiredColumnEnum.LOOK_UP.get(id);
		}
	}

	public static List<List<String>> convertWorkbookData(List<MeasurementRow> observations, List<MeasurementVariable> variables) {
		List<List<String>> table = new ArrayList<List<String>>();

		if (observations != null && !observations.isEmpty()) {
			List<Integer> factorHeaders = KsuFieldbookUtil.getFactorHeaders(variables);

			// write header row
			table.add(KsuFieldbookUtil.getHeaderNames(variables, true));

			List<MeasurementVariable> labels = KsuFieldbookUtil.getMeasurementLabels(factorHeaders, variables);

			for (MeasurementRow row : observations) {
				List<String> dataRow = new ArrayList<String>();

				for (MeasurementVariable label : labels) {
					String value = null;
					if (label.getPossibleValues() != null && !label.getPossibleValues().isEmpty()) {
						value =
								ExportImportStudyUtil.getCategoricalCellValue(row.getMeasurementData(label.getName()).getValue(),
										label.getPossibleValues());
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

	private static List<Integer> getFactorHeaders(List<MeasurementVariable> headers) {
		List<Integer> factorHeaders = new ArrayList<Integer>();

		if (headers != null && !headers.isEmpty()) {
			for (MeasurementVariable header : headers) {
				if (header.isFactor()) {
					factorHeaders.add(header.getTermId());
				}
			}
		}

		return factorHeaders;
	}

	private static List<String> getHeaderNames(List<MeasurementVariable> headers, Boolean isFactor) {
		List<String> names = new ArrayList<String>();

		if (headers != null && !headers.isEmpty()) {
			for (MeasurementVariable header : headers) {
				if (isFactor == null || isFactor != null && (isFactor && header.isFactor() || !isFactor && !header.isFactor())) {

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

	public static boolean isValidHeaderNames(String[] headerNames) {
		Map<String, Boolean> requiredColumns = new HashMap<String, Boolean>();

		for (String headerName : headerNames) {
			if (KsuFieldbookUtil.isARequiredColumn(headerName)) {
				requiredColumns.put(headerName, true);
			}
		}

		// check if all required columns are present
		Integer numberOfRequiredColumnPreset = 0;
		for (Boolean isPresent : requiredColumns.values()) {
			if (isPresent) {
				numberOfRequiredColumnPreset++;
			}
		}
		return numberOfRequiredColumnPreset == 4;
	}

	protected static boolean isARequiredColumn(String headerName) {
		for (KsuRequiredColumnEnum column : KsuRequiredColumnEnum.values()) {
			if (column.getLabel().equalsIgnoreCase(headerName)) {
				return true;
			}
		}
		return false;
	}

	private static List<MeasurementVariable> getMeasurementLabels(List<Integer> factorIds, List<MeasurementVariable> variables) {
		List<MeasurementVariable> labels = new ArrayList<MeasurementVariable>();

		for (Integer factorId : factorIds) {
			for (MeasurementVariable factor : variables) {
				if (factor.isFactor() && factorId.equals(factor.getTermId())) {
					labels.add(factor);
					break;
				}
			}
		}

		return labels;
	}

	public static void writeTraits(List<MeasurementVariable> traits, String filenamePath, FieldbookService fieldbookMiddlewareService,
			OntologyService ontologyService) {

		new File(filenamePath).exists();
		CsvWriter csvWriter = null;
		try {
			List<List<String>> dataTable = KsuFieldbookUtil.convertTraitsData(traits, fieldbookMiddlewareService, ontologyService);

			csvWriter = new CsvWriter(new FileWriter(filenamePath, false), ',');
			for (List<String> row : dataTable) {
				for (String cell : row) {
					csvWriter.write(cell);
				}
				csvWriter.endRecord();
			}

		} catch (IOException e) {
			KsuFieldbookUtil.LOG.error("ERROR in KSU CSV Export Study", e);

		} finally {
			if (csvWriter != null) {
				csvWriter.close();
			}
		}
	}

	private static List<List<String>> convertTraitsData(List<MeasurementVariable> traits, FieldbookService fieldbookMiddlewareService,
			OntologyService ontologyService) {
		List<List<String>> data = new ArrayList<List<String>>();

		data.add(KsuFieldbookUtil.TRAIT_FILE_HEADERS);

		// get name of breeding method property and get all methods
		String propertyName = "";
		List<Method> methods = new ArrayList<Method>();
		try {
			methods = fieldbookMiddlewareService.getAllBreedingMethods(false);
			propertyName = ontologyService.getProperty(TermId.BREEDING_METHOD_PROP.getId()).getName();
		} catch (MiddlewareQueryException e) {
			KsuFieldbookUtil.LOG.error(e.getMessage(), e);
		}

		int index = 1;
		for (MeasurementVariable trait : traits) {
			List<String> traitData = new ArrayList<String>();
			traitData.add(trait.getName());
			if ("C".equalsIgnoreCase(trait.getDataTypeDisplay())) {
				traitData.add(KsuFieldbookUtil.TEXT_FORMAT);
			} else {
				traitData.add(KsuFieldbookUtil.NUMERIC_FORMAT);
			}
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
			if (trait.getPossibleValues() != null && !trait.getPossibleValues().isEmpty() && !trait.getProperty().equals(propertyName)) {
				StringBuilder possibleValuesString = new StringBuilder();
				for (ValueReference value : trait.getPossibleValues()) {
					if (possibleValuesString.length() > 0) {
						possibleValuesString.append("/");
					}
					possibleValuesString.append(value.getName());
				}

				traitData.add(possibleValuesString.toString());
			} else if (trait.getProperty().equals(propertyName)) {
				StringBuilder possibleValuesString = new StringBuilder();
				// add code for breeding method properties
				for (Method method : methods) {
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
}
