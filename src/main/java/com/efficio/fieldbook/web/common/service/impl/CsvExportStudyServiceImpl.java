package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.service.ExportService;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.common.service.CsvExportStudyService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.ZipUtil;

@Service
public class CsvExportStudyServiceImpl implements CsvExportStudyService {

	private static final Logger LOG = LoggerFactory
			.getLogger(CsvExportStudyServiceImpl.class);

	@Resource
	private FieldbookProperties fieldbookProperties;

	@Resource
	private OntologyService ontologyService;

	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Resource
	private ExportService exportService;
	
	private static Integer[] REQUIRED_COLUMNS = {TermId.PLOT_NO.getId(), TermId.ENTRY_NO.getId(), TermId.DESIG.getId()};

	@Override
	public String export(Workbook workbook, String filename,
			List<Integer> instances) {
		return export(workbook, filename, instances, null);
	}

	@Override
	public String export(Workbook workbook, String filename,
			List<Integer> instances, List<Integer> visibleColumns) {

		FileOutputStream fos = null;
		List<String> filenameList = new ArrayList<String>();
		String outputFilename = null;

		for (Integer index : instances) {
			List<Integer> indexes = new ArrayList<Integer>();
			indexes.add(index);

			List<MeasurementRow> observations = getApplicableObservations(workbook, indexes);

			try {

				String filenamePath = getFileNamePath(index, workbook
						.getTrialObservations().get(index - 1), instances,
						filename, workbook.isNursery());

				List<ExportColumnHeader> exportColumnHeaders = getExportColumnHeaders(
						visibleColumns,
						workbook.getMeasurementDatasetVariables());
				List<Map<Integer, ExportColumnValue>> exportColumnValues = getExportColumnValues(
						exportColumnHeaders,
						workbook.getMeasurementDatasetVariables(), observations);

				exportService.generateCSVFile(exportColumnValues,
						exportColumnHeaders, filenamePath);

				outputFilename = filenamePath;
				filenameList.add(filenamePath);

			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
					}
				}
			}
		}

		if (instances != null && instances.size() > 1) {
			outputFilename = fieldbookProperties.getUploadDirectory()
					+ File.separator
					+ filename.replaceAll(
							AppConstants.EXPORT_XLS_SUFFIX.getString(), "")
					+ AppConstants.ZIP_FILE_SUFFIX.getString();
			ZipUtil.zipIt(outputFilename, filenameList);
		}

		return outputFilename;

	}

	protected List<MeasurementRow> getApplicableObservations(Workbook workbook,
			List<Integer> indexes) {
		return ExportImportStudyUtil
				.getApplicableObservations(workbook,
						workbook.getExportArrangedObservations(), indexes);
	}

	protected String getFileNamePath(int index,
			MeasurementRow trialObservation, List<Integer> instances,
			String filename, boolean isNursery) throws MiddlewareQueryException {
		String filenamePath = fieldbookProperties.getUploadDirectory()
				+ File.separator + SettingsUtil.cleanSheetAndFileName(filename);
		if (instances != null && (instances.size() > 1 || !isNursery)) {
			int fileExtensionIndex = filenamePath.lastIndexOf(".");
			if (instances.size() > 1) {
				return filenamePath.substring(0, fileExtensionIndex)
						+ "-"
						+ index
						+ SettingsUtil
								.cleanSheetAndFileName(getSiteNameOfTrialInstance(trialObservation))
						+ filenamePath.substring(fileExtensionIndex);
			} else {
				return filename.substring(0, filename.lastIndexOf("."))
						+ "-"
						+ index
						+ SettingsUtil
								.cleanSheetAndFileName(getSiteNameOfTrialInstance(trialObservation))
						+ filenamePath.substring(fileExtensionIndex);
			}
		}
		return filenamePath;
	}

	protected String getSiteNameOfTrialInstance(MeasurementRow trialObservation)
			throws MiddlewareQueryException {
		if (trialObservation != null && trialObservation.getMeasurementVariables() != null) {
			for (MeasurementData data : trialObservation.getDataList()) {
				if (data.getMeasurementVariable().getTermId() == TermId.TRIAL_LOCATION.getId()) {
					return "_" + data.getValue();
				} else if (data.getMeasurementVariable().getTermId() == TermId.LOCATION_ID.getId()) {
					if (data.getValue() != null && !data.getValue().isEmpty() && NumberUtils.isNumber(data.getValue())) {
						return "_" + fieldbookMiddlewareService.getLocationById(Integer.parseInt(data.getValue())).getLname();
					} else {
						return "";
					}
				}
			}
		}
		return "";
	}

	protected List<ExportColumnHeader> getExportColumnHeaders(
			List<Integer> visibleColumns, List<MeasurementVariable> variables) {
		
		List<ExportColumnHeader> exportColumnHeaders = new ArrayList<>();

		if (variables != null && !variables.isEmpty()) {
			for (MeasurementVariable variable : variables) {
				if(visibleColumns == null){
					exportColumnHeaders.add(new ExportColumnHeader(variable
							.getTermId(), variable.getName(), true));
				}
				else{
					exportColumnHeaders.add(getColumnsBasedOnVisibility(visibleColumns,variable));			
				}
				
			}
		}

		return exportColumnHeaders;
	}

	private ExportColumnHeader getColumnsBasedOnVisibility(
			List<Integer> visibleColumns, MeasurementVariable variable) {
		if (visibleColumns.contains(variable.getTermId()) || partOfRequiredColumns(variable.getTermId())) {
			return new ExportColumnHeader(variable.getTermId(), variable.getName(), true);
		} else {
			return new ExportColumnHeader(variable.getTermId(), variable.getName(), false);
		}
	}

	protected boolean partOfRequiredColumns(int termId) {
		for(int id : REQUIRED_COLUMNS){
			if(termId == id){
				return true;
			}
		}
		return false;
	}

	protected List<Map<Integer, ExportColumnValue>> getExportColumnValues(
			List<ExportColumnHeader> columns,
			List<MeasurementVariable> variables,
			List<MeasurementRow> observations) {

		List<Map<Integer, ExportColumnValue>> exportColumnValues = new ArrayList<>();

		for (MeasurementRow dataRow : observations) {
			exportColumnValues.add(getColumnValueMap(columns, dataRow));
		}

		return exportColumnValues;
	}

	protected Map<Integer, ExportColumnValue> getColumnValueMap(
			List<ExportColumnHeader> columns, MeasurementRow dataRow) {
		Map<Integer, ExportColumnValue> columnValueMap = new HashMap<>();

		for (ExportColumnHeader column : columns) {
			Integer termId = column.getId();
			MeasurementData dataCell = dataRow.getMeasurementData(termId);

			if (column.isDisplay() && dataCell != null) {
				if (dataCell.getMeasurementVariable() != null
						&& dataCell.getMeasurementVariable().getTermId() == TermId.TRIAL_INSTANCE_FACTOR
								.getId()) {
					continue;
				}

				columnValueMap.put(termId, getColumnValue(dataCell, termId));

			}
		}

		return columnValueMap;
	}

	protected ExportColumnValue getColumnValue(MeasurementData dataCell,
			Integer termId) {
		ExportColumnValue columnValue = null;

		if (measurementVariableHasValue(dataCell)
				&& !dataCell.getMeasurementVariable().getPossibleValues()
						.isEmpty()
				&& dataCell.getMeasurementVariable().getTermId() != TermId.BREEDING_METHOD_VARIATE
						.getId()
				&& dataCell.getMeasurementVariable().getTermId() != TermId.BREEDING_METHOD_VARIATE_CODE
						.getId()
				&& !dataCell.getMeasurementVariable().getProperty()
						.equals(getPropertyName())) {

			String value = getCategoricalCellValue(dataCell);
			columnValue = new ExportColumnValue(termId, value);

		} else {

			if (AppConstants.NUMERIC_DATA_TYPE.getString().equalsIgnoreCase(
					dataCell.getDataType())) {

				if (dataCell.getValue() != null
						&& !"".equalsIgnoreCase(dataCell.getValue())) {
					columnValue = new ExportColumnValue(termId, Double.valueOf(
							dataCell.getValue()).toString());
				}

			} else {
				columnValue = new ExportColumnValue(termId, dataCell.getValue());
			}

		}
		return columnValue;
	}

	protected String getCategoricalCellValue(MeasurementData dataCell) {
		return ExportImportStudyUtil.getCategoricalCellValue(
				dataCell.getValue(), dataCell.getMeasurementVariable()
						.getPossibleValues());
	}

	protected String getPropertyName() {
		String propertyName = "";
		try {
			propertyName = ontologyService.getProperty(TermId.BREEDING_METHOD_PROP.getId())
					.getTerm()
					.getName();
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
		return propertyName;
	}

	protected boolean measurementVariableHasValue(MeasurementData dataCell) {
		return dataCell.getMeasurementVariable() != null
				&& dataCell.getMeasurementVariable().getPossibleValues() != null;
	}

	public void setOntologyService(OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}

	
	public void setFieldbookProperties(FieldbookProperties fieldbookProperties) {
		this.fieldbookProperties = fieldbookProperties;
	}

	
	public void setExportService(ExportService exportService) {
		this.exportService = exportService;
	}
	
	
}
