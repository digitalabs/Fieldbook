package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.service.DesignImportService;
import com.efficio.fieldbook.web.nursery.controller.DesignImportController;
import com.efficio.fieldbook.web.trial.bean.Environment;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import com.mysql.jdbc.StringUtils;


public class DesignImportServiceImpl implements DesignImportService {
	
	private static final Logger LOG = LoggerFactory.getLogger(DesignImportServiceImpl.class);

	@Resource
    private UserSelection userSelection;
	
	@Resource
    private FieldbookService fieldbookService;
	
	@Resource
    private OntologyService ontologyService;

	
	@Resource
	private OntologyDataManager ontologyDataManager;
	
	@Resource
    private MessageSource messageSource;
	
	
	@Override
	public List<MeasurementRow> generateDesign(Workbook workbook, DesignImportData designImportData, EnvironmentData environmentData) throws DesignValidationException {
		
		Set<String> generatedTrialInstancesFromUI = extractTrialInstancesFromEnvironmentData(environmentData);
		
		List<ImportedGermplasm> importedGermplasm = userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
		
		Map<Integer, List<String>> csvData = designImportData.getCsvData();
		Map<Integer, StandardVariable> germplasmStandardVariables = convertToStandardVariables(
				workbook.getGermplasmFactors());
		
		List<MeasurementRow> measurements = new ArrayList<>();
		
		Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();
		
		//row counter starts at index = 1 because zero index is the header
		int rowCounter = 1;

		while(rowCounter <= csvData.size() - 1){
			MeasurementRow measurementRow = createMeasurementRow(workbook,mappedHeaders, csvData.get(rowCounter), importedGermplasm, germplasmStandardVariables, generatedTrialInstancesFromUI);
			if (measurementRow != null){
				measurements.add(measurementRow);
			}
			rowCounter++;
			
		}

		// add trait data to the list of <easurementRow
		addVariatesToMeasurementRows(workbook, measurements);
		
		return measurements;
	}
	
	@Override
	public void validateDesignData(DesignImportData designImportData) throws DesignValidationException {
		
		Map<Integer, List<String>> csvData = designImportData.getCsvData();
		
		DesignHeaderItem trialInstanceDesignHeaderItem = validateIfTrialFactorExists(
				designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		DesignHeaderItem entryNoDesignHeaderItem = validateIfEntryNumberExists(
				designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));
		validateIfPlotNumberExists(
				designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN));
		validateIfPlotNumberIsUnique(
				designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN),
				designImportData.getCsvData());
		
		 Map<String, Map<Integer, List<String>>> csvMap = groupCsvRowsIntoTrialInstance(trialInstanceDesignHeaderItem, csvData);
		
		validateEntryNoMustBeUniquePerInstance(entryNoDesignHeaderItem, csvMap);
		
	}

	@Override
	public Set<MeasurementVariable> getDesignMeasurementVariables(Workbook workbook, DesignImportData designImportData) {
		
		Set<MeasurementVariable> measurementVariables = new LinkedHashSet<>();
		Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();
		
		//Add the trial environments first
		measurementVariables.addAll(this.extractMeasurementVariable(PhenotypicType.TRIAL_ENVIRONMENT, mappedHeaders));
		
		//Add the germplasm factors that exist from csv file header
		measurementVariables.addAll(this.extractMeasurementVariable(PhenotypicType.GERMPLASM, mappedHeaders));
		
		//Add the germplasm factors from the selected germplasm in workbook
		measurementVariables.addAll(workbook.getGermplasmFactors());
		
		
		//Add the design factors that exists from csv file header
		measurementVariables.addAll(
				this.extractMeasurementVariable(PhenotypicType.TRIAL_DESIGN, mappedHeaders));
		
		//Add the variates that exist from csv file header
		measurementVariables.addAll(
				this.extractMeasurementVariable(PhenotypicType.VARIATE, mappedHeaders));
		
		//Add the variates from the added traits in workbook
		measurementVariables.addAll(workbook.getVariates());
		
		if (workbook.getStudyDetails().getStudyType() == StudyType.N){
			
			measurementVariables.addAll(workbook.getFactors());
			
			Iterator<MeasurementVariable> iterator = measurementVariables.iterator();
			while(iterator.hasNext()){
				MeasurementVariable temp = iterator.next();
				if (temp.getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()){
					iterator.remove();
					break;
				}
			}
		}
		
		
		return measurementVariables;
	}
	
	@Override
	public Set<StandardVariable> getDesignRequiredStandardVariables(Workbook workbook,
			DesignImportData designImportData) {
		
		Set<StandardVariable> standardVariables = new LinkedHashSet<>();
		Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();
		
		for (DesignHeaderItem designHeaderItem : mappedHeaders.get(PhenotypicType.TRIAL_DESIGN)){
			standardVariables.add(designHeaderItem.getVariable());
		}
		
		return standardVariables;
	}
	
	@Override
	public Set<MeasurementVariable> getDesignRequiredMeasurementVariable(Workbook workbook,
			DesignImportData designImportData) {
		
		Set<MeasurementVariable> measurementVariables = new LinkedHashSet<>();
		Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();
		
		measurementVariables.addAll(extractMeasurementVariable(PhenotypicType.TRIAL_DESIGN, mappedHeaders));
		
		return measurementVariables;
		
	}
	
	@Override
	public boolean areTrialInstancesMatchTheSelectedEnvironments(Integer noOfEnvironments, DesignImportData designImportData){
		
		
			DesignHeaderItem trialInstanceDesignHeaderItem = filterDesignHeaderItemsByTermId(
					TermId.TRIAL_INSTANCE_FACTOR,
					designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
			
			if (trialInstanceDesignHeaderItem != null){
				Map<String, Map<Integer, List<String>>> csvMap = groupCsvRowsIntoTrialInstance(trialInstanceDesignHeaderItem, designImportData.getCsvData());
				if (noOfEnvironments == csvMap.size()){
					return true;
				}
			}
		
		
		return false;
	}

	@Override
	public Map<PhenotypicType,List<DesignHeaderItem>> categorizeHeadersByPhenotype(
			List<DesignHeaderItem> designHeaders)
			throws MiddlewareQueryException {
		List<String> headers = new ArrayList<>();
		// get headers as string list
		for (DesignHeaderItem item : designHeaders) {
			headers.add(item.getName());
		}

		// create groups for the Design Headers
		Map<PhenotypicType,List<DesignHeaderItem>> mappedDesignHeaders  = new HashMap<>();

		// note: the null key here is for unmapped headers
		mappedDesignHeaders.put(null,new ArrayList<DesignHeaderItem>());
		mappedDesignHeaders.put(PhenotypicType.TRIAL_ENVIRONMENT,new ArrayList<DesignHeaderItem>());
		mappedDesignHeaders.put(PhenotypicType.TRIAL_DESIGN,new ArrayList<DesignHeaderItem>());
		mappedDesignHeaders.put(PhenotypicType.GERMPLASM,new ArrayList<DesignHeaderItem>());
		mappedDesignHeaders.put(PhenotypicType.VARIATE,new ArrayList<DesignHeaderItem>());

		Map<String, List<StandardVariable>> variables = ontologyDataManager.getStandardVariablesInProjects(headers);

		for (DesignHeaderItem item : designHeaders) {
			List<StandardVariable> match = variables.get(item.getName());

			if (null != match && !match.isEmpty() && null != mappedDesignHeaders.get(
					match.get(0).getPhenotypicType())) {
				StandardVariable standardVariable = match.get(0);
				item.setVariable(standardVariable);

				// let set required if ff condition is true
				if (TermId.PLOT_NO.getId() == standardVariable.getId() ||
						TermId.ENTRY_NO.getId() == standardVariable.getId() ||
						TermId.TRIAL_INSTANCE_FACTOR.getId() == standardVariable.getId()) {
					item.setRequired(true);
				}

				mappedDesignHeaders.get(standardVariable.getPhenotypicType()).add(item);
			} else {
				mappedDesignHeaders.get(null).add(item);
			}

		}

		return mappedDesignHeaders;
	}
	
	protected DesignHeaderItem validateIfTrialFactorExists(List<DesignHeaderItem> headerDesignItems) throws DesignValidationException {
		DesignHeaderItem headerItem = filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR,headerDesignItems);
		if (headerItem == null){
			throw new DesignValidationException(messageSource.getMessage(
					"design.import.error.trial.is.required", null, Locale.ENGLISH));
		}else{
			return headerItem;
		}
	}
	
	protected DesignHeaderItem validateIfEntryNumberExists(List<DesignHeaderItem> headerDesignItems) throws DesignValidationException {
		DesignHeaderItem headerItem = filterDesignHeaderItemsByTermId(TermId.ENTRY_NO, headerDesignItems);
		if (headerItem == null){
			throw new DesignValidationException(messageSource.getMessage(
					"design.import.error.entry.no.is.required", null, Locale.ENGLISH));
		}else{
			return headerItem;
		}
	}
	
	protected void validateIfPlotNumberExists(List<DesignHeaderItem> headerDesignItems) throws DesignValidationException {
		for (DesignHeaderItem headerDesignItem : headerDesignItems){
			if (headerDesignItem.getVariable().getId() == TermId.PLOT_NO.getId()){
				return;
			}
		}
		throw new DesignValidationException(messageSource.getMessage(
				"design.import.error.plot.no.is.required", null, Locale.ENGLISH));
	}
	
	protected void validateIfPlotNumberIsUnique(List<DesignHeaderItem> headerDesignItems, Map<Integer, List<String>> csvMap) throws DesignValidationException {
		Set<String> set = new HashSet<String>();
		for (DesignHeaderItem headerDesignItem : headerDesignItems){
			if (headerDesignItem.getVariable().getId() == TermId.PLOT_NO.getId()){
				for (Entry<Integer, List<String>> entry : csvMap.entrySet()){
					String value = entry.getValue().get(headerDesignItem.getColumnIndex());
					if (!StringUtils.isNullOrEmpty(value)){
						if (set.contains(value)){
							throw new DesignValidationException(messageSource.getMessage(
									"design.import.error.plot.number.must.be.unique", null,
									Locale.ENGLISH));
						}else {
							set.add(value);
						}
					}
				}
			}
		}
	}
	
	protected void validateEntryNoMustBeUniquePerInstance(DesignHeaderItem entryNoHeaderItem ,Map<String, Map<Integer, List<String>>> csvMapGrouped) throws DesignValidationException {
		
		for (Entry<String,Map<Integer, List<String>>> entry : csvMapGrouped.entrySet()){
			validateEntryNumberMustBeUnique(entryNoHeaderItem, entry.getValue());
			
		}
		
	}
	
	protected void validateEntryNumberMustBeUnique(DesignHeaderItem entryNoHeaderItem, Map<Integer, List<String>> csvMap) throws DesignValidationException {
		Set<String> set = new HashSet<String>();
		
		Iterator<Entry<Integer, List<String>>> iterator = csvMap.entrySet().iterator();
		while(iterator.hasNext()){
			String value = iterator.next().getValue().get(entryNoHeaderItem.getColumnIndex());
			if (StringUtils.isNullOrEmpty(value) && set.contains(value)){
				throw new DesignValidationException(messageSource.getMessage(
						"design.import.error.entry.number.unique.per.instance", null,
						Locale.ENGLISH));
			}else {
				set.add(value);
			}
		}
		validateGermplasmEntriesFromShouldMatchTheGermplasmList(set);
	}
	
	protected void validateGermplasmEntriesFromShouldMatchTheGermplasmList(Set<String> entryNumbers) throws DesignValidationException {
		
		List<ImportedGermplasm> importedGermplasmList = userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
		for (ImportedGermplasm importedGermplasm : importedGermplasmList){
			if (!entryNumbers.contains(importedGermplasm.getEntryId().toString())){
				throw new DesignValidationException(messageSource.getMessage(
						"design.import.error.mismatch.count.of.germplasm.entries", null,
						Locale.ENGLISH));
			}
		}
		if (importedGermplasmList.size() != entryNumbers.size()){
			throw new DesignValidationException(messageSource.getMessage(
					"design.import.error.mismatch.count.of.germplasm.entries", null, Locale.ENGLISH));
		}
	}

	
	protected Map<String, Map<Integer, List<String>>> groupCsvRowsIntoTrialInstance(DesignHeaderItem trialInstanceHeaderItem, Map<Integer, List<String>> csvMap){
		
		Map<String, Map<Integer, List<String>>> csvMapGrouped = new HashMap<>();
		
		Iterator<Entry<Integer, List<String>>> iterator = csvMap.entrySet().iterator();
		//skip the header row
		iterator.next();
		while(iterator.hasNext()){
			Entry<Integer, List<String>> entry = iterator.next();
			String trialInstance = entry.getValue().get(trialInstanceHeaderItem.getColumnIndex());
			if (!csvMapGrouped.containsKey(trialInstance)){
				csvMapGrouped.put(trialInstance, new HashMap<Integer, List<String>>());
			}
			csvMapGrouped.get(trialInstance).put(entry.getKey(), entry.getValue());
		}
		return csvMapGrouped;
		
	}
	
	protected DesignHeaderItem filterDesignHeaderItemsByTermId(TermId termId, List<DesignHeaderItem> headerDesignItems){
		for (DesignHeaderItem headerDesignItem : headerDesignItems){
			if (headerDesignItem.getVariable().getId() == termId.getId()){
				return headerDesignItem;
			}
		}
		return null;
	}

	@Override
	public Set<MeasurementVariable> extractMeasurementVariable(PhenotypicType phenotypicType, Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders){
		
		Set<MeasurementVariable> measurementVariables = new HashSet<>();
		
		for (DesignHeaderItem designHeaderItem : mappedHeaders.get(phenotypicType)){
			MeasurementVariable measurementVariable = createMeasurementVariable(designHeaderItem.getVariable());
			measurementVariables.add(measurementVariable);
		}
		
		return measurementVariables;
	}
	
	protected MeasurementRow createMeasurementRow(Workbook workbook, Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders, List<String> rowValues, List<ImportedGermplasm> importedGermplasm, Map<Integer, StandardVariable> germplasmStandardVariables, Set<String> trialInstancesFromUI){
		
		MeasurementRow measurement = new MeasurementRow();

		List<MeasurementData> dataList = new ArrayList<>();
		
		for (Entry<PhenotypicType, List<DesignHeaderItem>> entry : mappedHeaders.entrySet()){
			for (DesignHeaderItem headerItem : entry.getValue()){
				
				//do not add the trial instance record from file if it is not selected in environment tab
				if (headerItem.getVariable().getId() == TermId.TRIAL_INSTANCE_FACTOR.getId() && !trialInstancesFromUI.contains(rowValues.get(headerItem.getColumnIndex()))){
					return null;
				}
				
				if (headerItem.getVariable().getId() == TermId.TRIAL_INSTANCE_FACTOR.getId() && workbook.getStudyDetails().getStudyType() == StudyType.N){
					
					// do not add the trial instance to measurement data list if the workbook is Nursery
					
				}else if (headerItem.getVariable().getId() == TermId.ENTRY_NO.getId()){
					
					Integer entryNo = Integer.parseInt(rowValues.get(headerItem.getColumnIndex()));
					
					addGermplasmDetailsToDataList(importedGermplasm, germplasmStandardVariables,
							dataList, entryNo);
					
				} else if (headerItem.getVariable().getPhenotypicType() != PhenotypicType.GERMPLASM) {
					
					String value = rowValues.get(headerItem.getColumnIndex());
					dataList.add(createMeasurementData(headerItem.getVariable(), value));
					
				}
				
			}
		}
		
		if (workbook.getStudyDetails().getStudyType() == StudyType.N){
			for (MeasurementVariable factor : workbook.getFactors()){
				addFactorToDataListIfNecessary(factor, dataList);
			}
		}
		
		measurement.setDataList(dataList);
		return measurement;
	}
	
	protected void addFactorToDataListIfNecessary(MeasurementVariable factor, List<MeasurementData> dataList){
		for (MeasurementData data : dataList){
			if (data.getMeasurementVariable().equals(factor)){
				return;
			}
		}
		
		dataList.add(createMeasurementData(factor, ""));
		
	}
	
	protected MeasurementData createMeasurementData(StandardVariable standardVariable, String value){ 
		MeasurementData data = new MeasurementData();
		data.setMeasurementVariable(createMeasurementVariable(standardVariable));
		data.setValue(value);
		data.setLabel(data.getMeasurementVariable().getName());
		data.setDataType(data.getMeasurementVariable().getDataType());
		return data;
	}
	
	protected MeasurementData createMeasurementData(MeasurementVariable measurementVariable, String value){ 
		MeasurementData data = new MeasurementData();
		data.setMeasurementVariable(measurementVariable);
		data.setValue(value);
		data.setLabel(data.getMeasurementVariable().getName());
		data.setDataType(data.getMeasurementVariable().getDataType());
		return data;
	}
	
	protected MeasurementVariable createMeasurementVariable(StandardVariable standardVariable){
		MeasurementVariable variable = ExpDesignUtil.convertStandardVariableToMeasurementVariable(
				standardVariable, Operation.ADD, fieldbookService);
		return variable;
	}
	
	protected Map<Integer, StandardVariable> convertToStandardVariables(List<MeasurementVariable> list) {
			
		Map<Integer, StandardVariable> map = new HashMap<>();

		for (MeasurementVariable measurementVariable : list){
			try {
				map.put(measurementVariable.getTermId(), ontologyService.getStandardVariable(
						measurementVariable.getTermId()));
			} catch (MiddlewareQueryException e) {
				LOG.error(e.getMessage(), e);
			}
		}

		return map;
	}
	
	protected void addGermplasmDetailsToDataList(List<ImportedGermplasm> importedGermplasm,
			Map<Integer, StandardVariable> germplasmStandardVariables,
			List<MeasurementData> dataList, Integer entryNo) {
		
		ImportedGermplasm germplasmEntry = importedGermplasm.get(entryNo-1);

		if (germplasmStandardVariables.get(TermId.ENTRY_NO.getId()) != null){
			dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.ENTRY_NO.getId()), germplasmEntry.getEntryId().toString()));
		}
		if (germplasmStandardVariables.get(TermId.GID.getId()) != null){
			dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.GID.getId()), germplasmEntry.getGid()));
		}
		if (germplasmStandardVariables.get(TermId.DESIG.getId()) != null){
			dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.DESIG.getId()), germplasmEntry.getDesig()));
		}
		if (germplasmStandardVariables.get(TermId.ENTRY_TYPE.getId()) != null){
			dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.ENTRY_TYPE.getId()), germplasmEntry.getCheck()));
		}
		if (germplasmStandardVariables.get(TermId.CROSS.getId()) != null){
			dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.CROSS.getId()), germplasmEntry.getCross()));
		}
		if (germplasmStandardVariables.get(TermId.ENTRY_CODE.getId()) != null){
			dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.ENTRY_CODE.getId()), germplasmEntry.getEntryCode()));
		}
		if (germplasmStandardVariables.get(TermId.GERMPLASM_SOURCE.getId()) != null){
			dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.GERMPLASM_SOURCE.getId()), germplasmEntry.getSource()));
		}
		if (germplasmStandardVariables.get(TermId.SEED_SOURCE.getId()) != null){
			dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.SEED_SOURCE.getId()), germplasmEntry.getSource()));
		}
	}
	
	protected void addVariatesToMeasurementRows(Workbook workbook, List<MeasurementRow> measurements) {
		try {
			Set<MeasurementVariable> temporaryList = new HashSet<>();
			for (MeasurementVariable mvar : workbook.getVariates()){
				if (mvar.getOperation() == Operation.ADD || mvar.getOperation() == Operation.UPDATE){
					MeasurementVariable copy = mvar.copy();
					copy.setOperation(Operation.ADD);
					temporaryList.add(copy);
				}
			}
			WorkbookUtil.addMeasurementDataToRows(new ArrayList<MeasurementVariable>(temporaryList), measurements, true, userSelection, ontologyService, fieldbookService);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	protected Set<String> extractTrialInstancesFromEnvironmentData(EnvironmentData environmentData){
		Set<String> generatedTrialInstancesFromUI = new HashSet<>();
		for (Environment env : environmentData.getEnvironments()){
			generatedTrialInstancesFromUI.add(env.getManagementDetailValues().get(String.valueOf(TermId.TRIAL_INSTANCE_FACTOR.getId())));
		}
		return generatedTrialInstancesFromUI;
	}

	
}
