package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.springframework.context.MessageSource;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.service.DesignImportService;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.mysql.jdbc.StringUtils;


public class DesignImportServiceImpl implements DesignImportService {

	@Resource
    private UserSelection userSelection;
	
	@Resource
    private FieldbookService fieldbookService;
	
	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	
	@Resource
    private MessageSource messageSource;
	
	@Override
	public List<MeasurementRow> generateDesign() throws DesignValidationException {
		
		DesignImportData designImportData = userSelection.getDesignImportData();
		List<ImportedGermplasm> importedGermplasm = userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
		Map<Integer, StandardVariable> germplasmStandardVariables = generateGermplasmStandardVariables();
		
		try {
			createTestMapping(designImportData);
		} catch (MiddlewareQueryException e) {
			//do nothing
		}
		
		validateIfTrialFactorExists(designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		validateEntryNumberExists(designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));
		validatePlotNumberExists(designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN));
		validatePlotNumberIsUnique(designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN), designImportData.getCsvData());
		validateEntryNoMustBeUniquePerInstance();
		isTrialInstanceSameAsTheSelectedEnvironments();
		
		List<MeasurementRow> measurements = new ArrayList<>();
		
		Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();
		Map<Integer, List<String>> csvData = designImportData.getCsvData();
		
		//row counter starts at index = 1 because zero index is the header
		int rowCounter = 1;

		while(rowCounter < csvData.size() - 1){
			MeasurementRow measurementRow = createMeasurementRow(mappedHeaders, csvData.get(rowCounter), importedGermplasm, germplasmStandardVariables);
			measurements.add(measurementRow);
			rowCounter++;
			
		}
		return measurements;
	}

	@Override
	public List<MeasurementVariable> getDesignMeasurementVariables() {
		
		List<MeasurementVariable> measurementVariables = new ArrayList<>();
		
		DesignImportData designImportData = userSelection.getDesignImportData();
		
		//Add the Germplasm Factors
		for (StandardVariable germplasmStandardVariable : generateGermplasmStandardVariables().values()){
			measurementVariables.add(createMeasurementVariable(germplasmStandardVariable));
		}
		
		for (Entry<PhenotypicType, List<DesignHeaderItem>> entry : designImportData.getMappedHeaders().entrySet()){
			for (DesignHeaderItem headerItem : entry.getValue()){
				
				MeasurementVariable measurementVariable = createMeasurementVariable(headerItem.getVariable());
				
				if (entry.getKey() == PhenotypicType.VARIATE){
					measurementVariable.setFactor(false);
				}else{
					measurementVariable.setFactor(true);
				}
				
				measurementVariables.add(measurementVariable);
				
			}
		}
		
		return measurementVariables;
	}
	
	protected MeasurementRow createMeasurementRow(Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders, List<String> rowValues, List<ImportedGermplasm> importedGermplasm, Map<Integer, StandardVariable> germplasmStandardVariables){
		MeasurementRow measurement = new MeasurementRow();

		List<MeasurementData> dataList = new ArrayList<>();
		
		for (Entry<PhenotypicType, List<DesignHeaderItem>> entry : mappedHeaders.entrySet()){
			for (DesignHeaderItem headerItem : entry.getValue()){
				
				if (headerItem.getVariable().getId() == TermId.ENTRY_NO.getId()){
					String value = rowValues.get(headerItem.getColumnIndex());
					Integer entryNo = Integer.parseInt(rowValues.get(headerItem.getColumnIndex()));
					ImportedGermplasm germplasmEntry = importedGermplasm.get(entryNo-1);
					
					//add first the entry_no factor
					dataList.add(createMeasurementData(headerItem.getVariable(), value)); 
					//then manually add the other germplasm details
					dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.GID.getId()), germplasmEntry.getGid()));
					dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.DESIG.getId()), germplasmEntry.getDesig()));
					dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.ENTRY_TYPE.getId()), germplasmEntry.getCheck()));
				}else{
					String value = rowValues.get(headerItem.getColumnIndex());
					dataList.add(createMeasurementData(headerItem.getVariable(), value));
				}
				
			}
		}
		
		measurement.setDataList(dataList);
		return measurement;
	}
	
	protected MeasurementData createMeasurementData(StandardVariable standardVariable, String value){ 
		MeasurementData data = new MeasurementData();
		data.setMeasurementVariable(createMeasurementVariable(standardVariable));
		data.setValue(value);
		return data;
	}
	
	protected MeasurementVariable createMeasurementVariable(StandardVariable standardVariable){
		MeasurementVariable variable = ExpDesignUtil.convertStandardVariableToMeasurementVariable(standardVariable, Operation.ADD, fieldbookService);
		return variable;
	}
	
	protected Map<Integer, StandardVariable> generateGermplasmStandardVariables() {
			
		Map<Integer, StandardVariable> map = new HashMap<>();

		try {
			
			map.put(TermId.GID.getId(), fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId()));
			map.put(TermId.DESIG.getId(), fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId()));
			map.put(TermId.ENTRY_TYPE.getId(), fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_TYPE.getId()));
			
		} catch (MiddlewareQueryException e) {
			//do nothing
		}
			
		return map;
	}
	
	protected void createTestMapping(DesignImportData designImportData) throws MiddlewareQueryException{
		
		List<DesignHeaderItem> trialEnv = new ArrayList<>();
		List<DesignHeaderItem> germplasm = new ArrayList<>();
		List<DesignHeaderItem> design = new ArrayList<>();
		List<DesignHeaderItem> variate = new ArrayList<>();
		
		for (DesignHeaderItem item : designImportData.getUnmappedHeaders()){
			StandardVariable stdVar = fieldbookMiddlewareService.getStandardVariableByName(item.getHeaderName());
			item.setVariable(stdVar);
			
			if (stdVar.getPhenotypicType() == PhenotypicType.TRIAL_ENVIRONMENT){
				trialEnv.add(item);
			}
			if (stdVar.getPhenotypicType() == PhenotypicType.GERMPLASM){
				germplasm.add(item);
			}
			if (stdVar.getPhenotypicType() == PhenotypicType.TRIAL_DESIGN){
				design.add(item);
			}
			if (stdVar.getPhenotypicType() == PhenotypicType.VARIATE){
				variate.add(item);
			}
		}
		
		designImportData.getMappedHeaders().put(PhenotypicType.TRIAL_ENVIRONMENT, trialEnv);
		designImportData.getMappedHeaders().put(PhenotypicType.GERMPLASM, germplasm);
		designImportData.getMappedHeaders().put(PhenotypicType.TRIAL_DESIGN, design);
		designImportData.getMappedHeaders().put(PhenotypicType.VARIATE, variate);
		
	}
	
	
	protected void validateIfTrialFactorExists(List<DesignHeaderItem> headerDesignItems) throws DesignValidationException {
		for (DesignHeaderItem headerDesignItem : headerDesignItems){
			if (headerDesignItem.getVariable().getId() == TermId.TRIAL_INSTANCE_FACTOR.getId()){
				return;
			}
		}
		throw new DesignValidationException(messageSource.getMessage("design.import.error.trial.is.required", null, Locale.ENGLISH));
	}
	
	protected void validateEntryNumberExists(List<DesignHeaderItem> headerDesignItems) throws DesignValidationException {
		for (DesignHeaderItem headerDesignItem : headerDesignItems){
			if (headerDesignItem.getVariable().getId() == TermId.ENTRY_NO.getId()){
				return;
			}
		}
		throw new DesignValidationException(messageSource.getMessage("design.import.error.entry.no.is.required", null, Locale.ENGLISH));
	}
	
	protected void validatePlotNumberExists(List<DesignHeaderItem> headerDesignItems) throws DesignValidationException {
		for (DesignHeaderItem headerDesignItem : headerDesignItems){
			if (headerDesignItem.getVariable().getId() == TermId.PLOT_NO.getId()){
				return;
			}
		}
		throw new DesignValidationException(messageSource.getMessage("design.import.error.plot.no.is.required", null, Locale.ENGLISH));
	}
	
	protected void validatePlotNumberIsUnique(List<DesignHeaderItem> headerDesignItems, Map<Integer, List<String>> csvMap) throws DesignValidationException {
		Set<String> set = new HashSet<String>();
		for (DesignHeaderItem headerDesignItem : headerDesignItems){
			if (headerDesignItem.getVariable().getId() == TermId.PLOT_NO.getId()){
				for (Entry<Integer, List<String>> entry : csvMap.entrySet()){
					String value = entry.getValue().get(headerDesignItem.getColumnIndex());
					if (StringUtils.isNullOrEmpty(value) && set.contains(value)){
						throw new DesignValidationException(messageSource.getMessage("design.import.error.plot.number.must.be.unique", null, Locale.ENGLISH));
					}else {
						set.add(value);
					}
				}
			}
		}
	}
	
	protected void validateEntryNoMustBeUniquePerInstance() throws DesignValidationException {
		
	}
	
	protected boolean isTrialInstanceSameAsTheSelectedEnvironments(){
		return false;
	}
	
	
	/**
	
			design.import.error.mismatch.count.of.germplasm.entries=The number of germplasm entries in the file does not match the number of germplasm entries selected for this Trial.
			design.import.error.entry.number.unique.per.instance=Entry number must be unique per Trial instance.

	**/
}
