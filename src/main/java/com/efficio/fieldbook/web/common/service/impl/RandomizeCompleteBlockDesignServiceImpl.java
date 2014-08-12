package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.RandomizeCompleteBlockDesignService;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.SettingsUtil;

@Service
public class RandomizeCompleteBlockDesignServiceImpl implements RandomizeCompleteBlockDesignService{

	@Resource
	public org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	@Resource
	protected WorkbenchService workbenchService;
	@Resource
	protected FieldbookProperties fieldbookProperties;
	@Resource
    private ResourceBundleMessageSource messageSource;
	@Resource
	public FieldbookService fieldbookService;
	@Resource
    private UserSelection userSelection;
	
	@Override
	public  List<MeasurementRow> generateDesign(List<ImportedGermplasm> germplasmList,
			ExpDesignParameterUi parameter, List<MeasurementVariable> trialVariables, List<MeasurementVariable> factors,
			List<MeasurementVariable> nonTrialFactors, List<MeasurementVariable> variates, 
			List<TreatmentVariable> treatmentVariables) throws BVDesignException {
		
		
		List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();
		String block = parameter.getReplicationsCount();	
		int environments = Integer.valueOf(parameter.getNoOfEnvironments());				
		
		try {
			
			List<String> treatmentFactor = new ArrayList<String>();
			List<String> levels = new ArrayList<String>();
			
			Map<String, List<String>> treatmentFactorValues = new HashMap<String, List<String>>(); //Key - CVTerm ID , List of values
			List<TreatmentVariable> treatmentVarList = new ArrayList<TreatmentVariable>();
			Map treatmentFactorsData = parameter.getTreatmentFactorsData();
			
			List<SettingDetail> treatmentFactorList = userSelection.getTreatmentFactors();
			
			if(treatmentFactorsData != null){
				Iterator keySetIter = treatmentFactorsData.keySet().iterator();
				while(keySetIter.hasNext()){
					String key = (String) keySetIter.next();
					Map treatmentData = (Map) treatmentFactorsData.get(key);						
					treatmentFactorValues.put(key, (List)treatmentData.get("labels"));
					//add the treatment variables
					Object pairVarObj = (Object)treatmentData.get("variableId");
							
					String pairVar = "";
					if(pairVarObj instanceof String){
						pairVar = (String) pairVarObj;
					}else{
						pairVar = ((Integer) pairVarObj).toString();
					}
					if(key != null && NumberUtils.isNumber(key) && pairVar != null && NumberUtils.isNumber(pairVar)){
						int treatmentPair1 = Integer.parseInt(key);
						int treatmentPair2 = Integer.parseInt(pairVar);
						StandardVariable stdVar1 = fieldbookMiddlewareService.getStandardVariable(treatmentPair1);
						StandardVariable stdVar2 =  fieldbookMiddlewareService.getStandardVariable(treatmentPair2);
						TreatmentVariable treatmentVar = new TreatmentVariable();
						MeasurementVariable measureVar1 = ExpDesignUtil.convertStandardVariableToMeasurementVariable(stdVar1, Operation.ADD, fieldbookService);
						MeasurementVariable measureVar2 = ExpDesignUtil.convertStandardVariableToMeasurementVariable(stdVar2, Operation.ADD, fieldbookService);
						measureVar1.setFactor(true);
						measureVar2.setFactor(true);
						
						SettingsUtil.findAndUpdateVariableName(treatmentFactorList, measureVar1);
						
						measureVar1.setTreatmentLabel(measureVar1.getName());
						measureVar2.setTreatmentLabel(measureVar1.getName());
						
						treatmentVar.setLevelVariable(measureVar1);
						treatmentVar.setValueVariable(measureVar2);
						treatmentVariables.add(treatmentVar);
					}

				}
			}
			
			
			
			if(treatmentFactorValues != null){
				Set<String> keySet = treatmentFactorValues.keySet();
				for(String key : keySet){
					int level = treatmentFactorValues.get(key).size();
					treatmentFactor.add(ExpDesignUtil.cleanBVDesingKey(key));										
					levels.add(Integer.toString(level));
				}
			}
			
			StandardVariable stdvarTreatment = fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId());
			
			treatmentFactorValues.put(stdvarTreatment.getName(), Arrays.asList(Integer.toString(germplasmList.size())));
			treatmentFactor.add(stdvarTreatment.getName());
			levels.add(Integer.toString(germplasmList.size()));
			
			StandardVariable stdvarBlock = null;				
			StandardVariable stdvarPlot = null;
			
			List<StandardVariable> reqVarList = getRequiredVariable();
			
			for(StandardVariable var : reqVarList){
				if(var.getId() == TermId.BLOCK_NO.getId()){
					stdvarBlock = var;
				}else if(var.getId() == TermId.PLOT_NO.getId()){
					stdvarPlot = var;
				}
			}
			
			
			MainDesign mainDesign = ExpDesignUtil.createRandomizedCompleteBlockDesign(block, 
					stdvarBlock.getName(), stdvarPlot.getName(),
				treatmentFactor, levels, "1", "");
			
			measurementRowList = ExpDesignUtil.generateExpDesignMeasurements(environments, trialVariables, factors,
					nonTrialFactors, variates, treatmentVariables, reqVarList, germplasmList, 
					mainDesign, workbenchService, fieldbookProperties, stdvarTreatment.getName(), treatmentFactorValues, fieldbookService);					
			
		}catch(BVDesignException e){
			throw e;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return measurementRowList;
	}
		
	
	@Override
	public List<StandardVariable> getRequiredVariable() {
		List<StandardVariable> varList = new ArrayList<StandardVariable>();
		try {		
			StandardVariable stdvarBlock = fieldbookMiddlewareService.getStandardVariable(TermId.BLOCK_NO.getId());				
			StandardVariable stdvarPlot = fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId());
			
			varList.add(stdvarBlock);
			varList.add(stdvarPlot);
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return varList;
	}
	
	@Override
	public ExpDesignValidationOutput validate(
			ExpDesignParameterUi expDesignParameter,
			List<ImportedGermplasm> germplasmList) {
		Locale locale = LocaleContextHolder.getLocale();
		ExpDesignValidationOutput output = new ExpDesignValidationOutput(true, "");
		try{
			if(expDesignParameter != null && germplasmList != null){
				if(!NumberUtils.isNumber(expDesignParameter.getReplicationsCount())){
					output = new ExpDesignValidationOutput(false, messageSource.getMessage(
		                    "experiment.design.replication.count.should.be.a.number", null, locale));
				}else{
					int replicationCount = Integer.valueOf(expDesignParameter.getReplicationsCount());
					
					if(replicationCount <= 0 || replicationCount >= 13){
						output = new ExpDesignValidationOutput(false, messageSource.getMessage(
			                    "experiment.design.replication.count.rcbd.error", null, locale));
					}
				}
			}
		}catch(Exception e){
			output = new ExpDesignValidationOutput(false, messageSource.getMessage(
                    "experiment.design.invalid.generic.error", null, locale));
		}
		
		return output;
	}

	public List<Integer> getExperimentalDesignVariables(ExpDesignParameterUi params) {
		return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NO_OF_REPS_IN_COLS.getId());
	}

}
