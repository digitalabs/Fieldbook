package com.efficio.fieldbook.web.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
	
	private static final List<String> TRAIT_FILE_HEADERS = Arrays.asList("trait", "format", "defaultValue", 
			"minimum", "maximum", "details", "categories", "isVisible", "realPosition");
	
	private static final String NUMERIC_FORMAT = "numeric";
	private static final String TEXT_FORMAT = "text";
	
	
	private static final Map<Integer, String> idNameMap;
	
	static {
		idNameMap = new HashMap<Integer, String>();
		idNameMap.put(TERM_PLOT_ID, PLOT_ID);
		idNameMap.put(TERM_RANGE, RANGE);
		idNameMap.put(TERM_PLOT1, PLOT);
		idNameMap.put(TERM_PLOT2, PLOT);
	}
	
	public static List<List<String>> convertWorkbookData(List<MeasurementRow> observations, List<MeasurementVariable> variables) {
		List<List<String>> table = new ArrayList<List<String>>();
		
		if (observations != null && !observations.isEmpty()) {
			List<Integer> factorHeaders = getFactorHeaders(variables);
			
			//write header row
			table.add(getHeaderNames(variables, true));
			
			List<MeasurementVariable> labels = getMeasurementLabels(factorHeaders, variables);
			
			for (MeasurementRow row : observations) {
				List<String> dataRow = new ArrayList<String>();
				
				for (MeasurementVariable label : labels) {
					String value = null;
					if (label.getPossibleValues() != null && !label.getPossibleValues().isEmpty()) {
						value = ExportImportStudyUtil.getCategoricalCellValue(row.getMeasurementData(label.getName()).getValue(), label.getPossibleValues());
					}
					else {
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
				if (isFactor == null 
						|| (isFactor != null && (isFactor && header.isFactor() || !isFactor && !header.isFactor()))) {
					
					if (idNameMap.get(header.getTermId()) != null) {
						names.add(idNameMap.get(header.getTermId()));
					}
					else {
						names.add(header.getName());
					}
				}
			}
		}

		return names;
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
	
	public static void writeTraits(List<MeasurementVariable> traits, String filenamePath, FieldbookService fieldbookMiddlewareService, OntologyService ontologyService) {
		
        new File(filenamePath).exists();
        CsvWriter csvWriter = null;
        try {
        	List<List<String>> dataTable = convertTraitsData(traits, fieldbookMiddlewareService, ontologyService);
        	
            csvWriter = new CsvWriter(new FileWriter(filenamePath, false), ',');
            for (List<String> row : dataTable) {
            	for (String cell : row) {
            		csvWriter.write(cell);
            	}
            	csvWriter.endRecord();
            }

		} catch (IOException e) {
            LOG.error("ERROR in KSU CSV Export Study", e);

		} finally {
        	if (csvWriter != null) {
        		csvWriter.close();
        	}
        }		
	}
	
	private static List<List<String>> convertTraitsData(List<MeasurementVariable> traits, FieldbookService fieldbookMiddlewareService, OntologyService ontologyService) {
		List<List<String>> data = new ArrayList<List<String>>();
		
		data.add(TRAIT_FILE_HEADERS);
		
		//get name of breeding method property and get all methods 
        String propertyName = "";
        List<Method> methods = new ArrayList<Method>();
        try {
            methods = fieldbookMiddlewareService.getAllBreedingMethods(false);
            propertyName = ontologyService.getProperty(TermId.BREEDING_METHOD_PROP.getId()).getName(); 
        } catch(MiddlewareQueryException e) {
            e.printStackTrace();
        }
		
		int index = 1;
		for (MeasurementVariable trait : traits) {
			List<String> traitData = new ArrayList<String>();
			traitData.add(trait.getName());
			if (trait.getDataTypeDisplay().equals("C")) {
				traitData.add(TEXT_FORMAT);
			}
			else {
				traitData.add(NUMERIC_FORMAT);
			}
			traitData.add(""); //default value
			if (trait.getMinRange() != null) {
				traitData.add(trait.getMinRange().toString());
			}
			else {
				traitData.add("");
			}
			if (trait.getMaxRange() != null) {
				traitData.add(trait.getMaxRange().toString());
			}
			else {
				traitData.add("");
			}
			traitData.add(""); //details
			if (trait.getPossibleValues() != null && !trait.getPossibleValues().isEmpty() 
                    && !trait.getProperty().equals(propertyName)) {
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
			    //add code for breeding method properties
                for (Method method : methods) {
                    if (possibleValuesString.length() > 0) {
                        possibleValuesString.append("/");
                    }
                    possibleValuesString.append(method.getMcode());
                }
                traitData.add(possibleValuesString.toString());
			} else {
				traitData.add(""); //categories
			}
			traitData.add("TRUE");
			traitData.add(String.valueOf(index));
			index++;
			data.add(traitData);
		}
		
		return data;
	}
}
