package com.efficio.fieldbook.web.common.service.impl;

import au.com.bytecode.opencsv.CSVWriter;
import com.efficio.fieldbook.web.common.service.CsvExportSampleListService;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CsvExportSampleListServiceImpl implements CsvExportSampleListService {

	private static final Logger LOG = LoggerFactory.getLogger(CsvExportSampleListServiceImpl.class);

	@Resource private FieldbookProperties fieldbookProperties;

	@Override
	public String export(final List<SampleDetailsDTO> sampleDetailsDTOs, final String filename, final List<String> visibleColumns)
		throws IOException {

		final FileOutputStream fos = null;
		final List<String> filenameList = new ArrayList<>();
		String outputFilename = null;
		try {

			final List<ExportColumnHeader> exportColumnHeaders = this.getExportColumnHeaders(visibleColumns);
			final List<Map<Integer, ExportColumnValue>> exportColumnValues =
				this.getExportColumnValues(exportColumnHeaders, sampleDetailsDTOs);

			final String filenamePath = ExportImportStudyUtil.getFileNamePath(filename, this.fieldbookProperties);
			this.generateCSVFile(exportColumnValues, exportColumnHeaders, filenamePath);

			outputFilename = filenamePath;
			filenameList.add(filenamePath);

		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		return outputFilename;

	}

	private List<Map<Integer, ExportColumnValue>> getExportColumnValues(List<ExportColumnHeader> columnHeaders,
		List<SampleDetailsDTO> sampleDetailsDTOs) {
		final List<Map<Integer, ExportColumnValue>> exportColumnValues = new ArrayList<>();
		int i = 1;
		for (final SampleDetailsDTO sampleDetailsDTO : sampleDetailsDTOs) {
			sampleDetailsDTO.setEntryNo(i);
			exportColumnValues.add(this.getColumnValueMap(columnHeaders, sampleDetailsDTO));
			i++;
		}

		return exportColumnValues;

	}

	private List<ExportColumnHeader> getExportColumnHeaders(List<String> visibleColumns) {
		final List<ExportColumnHeader> exportColumnHeaders = new ArrayList<>();

		int i = 0;
		for (final String header : visibleColumns) {
			exportColumnHeaders.add(new ExportColumnHeader(i, header, true));
			i++;
		}

		return exportColumnHeaders;
	}

	protected Map<Integer, ExportColumnValue> getColumnValueMap(final List<ExportColumnHeader> columns,
		final SampleDetailsDTO sampleDetailsDTO) {
		final Map<Integer, ExportColumnValue> columnValueMap = new HashMap<>();

		for (final ExportColumnHeader column : columns) {
			final Integer id = column.getId();
			columnValueMap.put(id, this.getColumnValue(sampleDetailsDTO, column));
		}

		return columnValueMap;
	}

	protected ExportColumnValue getColumnValue(final SampleDetailsDTO sampleDetailsDTO, final ExportColumnHeader column) {
		ExportColumnValue columnValue = null;

		switch (column.getName()) {
			case "ENTRY_NO":
				columnValue = new ExportColumnValue(column.getId(), sampleDetailsDTO.getEntryNo().toString());
				break;
			case "DESIGNATION":
				columnValue = new ExportColumnValue(column.getId(), sampleDetailsDTO.getDesignation());
				break;
			case "PLOT_NO":
				columnValue = new ExportColumnValue(column.getId(), sampleDetailsDTO.getPlotNumber().toString());
				break;
			case "PLANT_NO":
				columnValue = new ExportColumnValue(column.getId(), sampleDetailsDTO.getPlantNo().toString());
				break;
			case "SAMPLE_NAME":
				columnValue = new ExportColumnValue(column.getId(), sampleDetailsDTO.getSampleName());
				break;
			case "TAKEN_BY":
				columnValue = new ExportColumnValue(column.getId(), sampleDetailsDTO.getTakenBy());
				break;
			case "SAMPLING_DATE":
				if (null != sampleDetailsDTO.getSampleDate()) {
					final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
					final String convertedCurrentDate = sdf.format(sampleDetailsDTO.getSampleDate());

					columnValue = new ExportColumnValue(column.getId(), convertedCurrentDate);
				} else {
					columnValue = new ExportColumnValue(column.getId(), "-");
				}
				break;
			case "SAMPLE_UID":
				columnValue = new ExportColumnValue(column.getId(), sampleDetailsDTO.getSampleBusinessKey());
				break;
			case "PLANT_UID":
				columnValue = new ExportColumnValue(column.getId(), sampleDetailsDTO.getPlantBusinessKey());
				break;
			case "PLOT_ID":
				columnValue = new ExportColumnValue(column.getId(), sampleDetailsDTO.getPlotId());
				break;
			default:
				break;
		}
		return columnValue;
	}

	public File generateCSVFile(final List<Map<Integer, ExportColumnValue>> exportColumnValues,
		final List<ExportColumnHeader> exportColumnHeaders, final String fileNameFullPath) throws IOException {
		return this.generateCSVFile(exportColumnValues, exportColumnHeaders, fileNameFullPath, true);
	}

	public File generateCSVFile(final List<Map<Integer, ExportColumnValue>> exportColumnValues,
		final List<ExportColumnHeader> exportColumnHeaders, final String fileNameFullPath, final boolean includeHeader) throws IOException {
		final File newFile = new File(fileNameFullPath);

		final CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileNameFullPath), "UTF-8"), ',');

		// feed in your array (or convert your data to an array)
		final List<String[]> rowValues = new ArrayList<>();
		if (includeHeader) {
			rowValues.add(this.getColumnHeaderNames(exportColumnHeaders));
		}
		for (final Map<Integer, ExportColumnValue> exportColumnValue : exportColumnValues) {
			rowValues.add(this.getColumnValues(exportColumnValue, exportColumnHeaders));
		}
		writer.writeAll(rowValues);
		writer.close();
		return newFile;
	}

	protected String[] getColumnValues(final Map<Integer, ExportColumnValue> exportColumnMap,
		final List<ExportColumnHeader> exportColumnHeaders) {
		final List<String> values = new ArrayList<>();
		for (final ExportColumnHeader exportColumnHeader : exportColumnHeaders) {
			if (exportColumnHeader.isDisplay()) {
				final ExportColumnValue exportColumnValue = exportColumnMap.get(exportColumnHeader.getId());
				String colName = "";
				if (exportColumnValue != null) {
					colName = exportColumnValue.getValue();
				}
				values.add(colName);
			}
		}
		return values.toArray(new String[values.size()]);
	}

	protected String[] getColumnHeaderNames(final List<ExportColumnHeader> exportColumnHeaders) {
		final List<String> values = new ArrayList<>();
		for (final ExportColumnHeader exportColumnHeader : exportColumnHeaders) {
			if (exportColumnHeader.isDisplay()) {
				values.add(exportColumnHeader.getName());
			}
		}
		return values.toArray(new String[values.size()]);
	}

	public FieldbookProperties getFieldbookProperties() {
		return fieldbookProperties;
	}

	public void setFieldbookProperties(FieldbookProperties fieldbookProperties) {
		this.fieldbookProperties = fieldbookProperties;
	}
}
