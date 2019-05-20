
package com.efficio.fieldbook.web.common.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportRow;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.service.api.OntologyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.web.common.service.CsvExportStudyService;
import org.generationcp.commons.constant.AppConstants;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;

@Service
@Transactional
public class CsvExportStudyServiceImpl extends BaseExportStudyServiceImpl implements CsvExportStudyService {

	@Resource
	private OntologyService ontologyService;

	@Resource
	private GermplasmExportService germplasmExportService;
	
	@Override
	void writeOutputFile(Workbook workbook, List<Integer> visibleColumns, MeasurementRow instanceLevelObservation,
			List<MeasurementRow> plotLevelObservations, String fileNamePath) throws IOException {
		final List<ExportColumnHeader> exportColumnHeaders =
				this.getExportColumnHeaders(visibleColumns, workbook.getMeasurementDatasetVariables());
		final List<ExportRow> exportRows =
				this.getExportColumnValues(exportColumnHeaders, plotLevelObservations);

		this.germplasmExportService.generateCSVFile(exportRows, exportColumnHeaders, fileNamePath);
	}

	@Override
	String getFileExtension() {
		return AppConstants.EXPORT_CSV_SUFFIX.getString();
	}
	
	protected List<MeasurementRow> getApplicableObservations(final Workbook workbook, final List<Integer> indexes) {
		return ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getExportArrangedObservations(), indexes);
	}

	protected List<ExportColumnHeader> getExportColumnHeaders(final List<Integer> visibleColumns, final List<MeasurementVariable> variables) {

		final List<ExportColumnHeader> exportColumnHeaders = new ArrayList<>();

		if (variables != null && !variables.isEmpty()) {
			for (final MeasurementVariable variable : variables) {
				if (visibleColumns == null) {
					exportColumnHeaders.add(new ExportColumnHeader(variable.getTermId(), variable.getName(), true));
				} else {
					exportColumnHeaders.add(this.getColumnsBasedOnVisibility(visibleColumns, variable));
				}

			}
		}

		return exportColumnHeaders;
	}

	protected ExportColumnHeader getColumnsBasedOnVisibility(final List<Integer> visibleColumns, final MeasurementVariable variable) {
		if (visibleColumns.contains(variable.getTermId()) || ExportImportStudyUtil.partOfRequiredColumns(variable.getTermId())) {
			return new ExportColumnHeader(variable.getTermId(), variable.getName(), true);
		} else {
			return new ExportColumnHeader(variable.getTermId(), variable.getName(), false);
		}
	}

	protected List<ExportRow> getExportColumnValues(final List<ExportColumnHeader> columns,
			final List<MeasurementRow> observations) {

		final List<ExportRow> exportRows = new ArrayList<>();

		for (final MeasurementRow dataRow : observations) {
			exportRows.add(this.getColumnValueMap(columns, dataRow));
		}

		return exportRows;
	}

	protected ExportRow getColumnValueMap(final List<ExportColumnHeader> columns, final MeasurementRow dataRow) {
		final ExportRow row = new ExportRow();

		for (final ExportColumnHeader column : columns) {
			final Integer termId = column.getId();
			final MeasurementData dataCell = dataRow.getMeasurementData(termId);

			if (column.isDisplay() && dataCell != null) {
				if (dataCell.getMeasurementVariable() != null
						&& dataCell.getMeasurementVariable().getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					continue;
				}
				
				// FIXME : DB visit IN LOOP
				row.addColumnValue(termId, this.getColumnValue(dataCell, termId));

			}
		}

		return row;
	}

	protected String getColumnValue(final MeasurementData dataCell, final Integer termId) {
		String columnValue = null;

		if (ExportImportStudyUtil.measurementVariableHasValue(dataCell) && !dataCell.getMeasurementVariable().getPossibleValues().isEmpty()
				&& dataCell.getMeasurementVariable().getTermId() != TermId.BREEDING_METHOD_VARIATE.getId()
				&& dataCell.getMeasurementVariable().getTermId() != TermId.BREEDING_METHOD_VARIATE_CODE.getId()
				&& !dataCell.getMeasurementVariable().getProperty().equals(ExportImportStudyUtil.getPropertyName(this.ontologyService))) {

			columnValue = this.getCategoricalCellValue(dataCell);

		} else {

			if (AppConstants.NUMERIC_DATA_TYPE.getString().equalsIgnoreCase(dataCell.getDataType())) {

				columnValue = this.getNumericColumnValue(dataCell, termId);

			} else {
				columnValue = dataCell.getValue();
			}

		}
		return columnValue;
	}

	protected String getNumericColumnValue(final MeasurementData dataCell, final Integer termId) {
		String columnValue = null;
		String cellVal = "";

		if (dataCell.getValue() != null && !"".equalsIgnoreCase(dataCell.getValue())) {
			if (MeasurementData.MISSING_VALUE.equalsIgnoreCase(dataCell.getValue())) {
				columnValue = dataCell.getValue();
			} else {
				columnValue = Double.valueOf(dataCell.getValue()).toString();
			}
		}
		return columnValue;
	}

	protected String getCategoricalCellValue(final MeasurementData dataCell) {
		return ExportImportStudyUtil.getCategoricalCellValue(dataCell.getValue(), dataCell.getMeasurementVariable().getPossibleValues());
	}

	public void setOntologyService(final OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}

	public void setGermplasmExportService(final GermplasmExportService germplasmExportService) {
		this.germplasmExportService = germplasmExportService;
	}
	
}
