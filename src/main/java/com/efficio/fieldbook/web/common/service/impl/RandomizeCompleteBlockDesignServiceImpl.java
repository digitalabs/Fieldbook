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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.RandomizeCompleteBlockDesignService;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;

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
	
	@Override
	public  List<MeasurementRow> generateDesign(List<ImportedGermplasm> germplasmList,
			ExpDesignParameterUi parameter, List<MeasurementVariable> factors,
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
			if(treatmentFactorsData != null){
				Iterator keySetIter = treatmentFactorsData.keySet().iterator();
				while(keySetIter.hasNext()){
					String key = (String) keySetIter.next();
					Map treatmentData = (Map) treatmentFactorsData.get(key);						
					treatmentFactorValues.put(key, (List)treatmentData.get("labels"));
				}
			}
			treatmentFactorValues.put(Integer.toString(TermId.ENTRY_NO.getId()), Arrays.asList(Integer.toString(germplasmList.size())));
			
			
			if(treatmentFactorValues != null){
				Set<String> keySet = treatmentFactorValues.keySet();
				for(String key : keySet){
					int level = treatmentFactorValues.get(key).size();
					treatmentFactor.add(ExpDesignUtil.TREATMENT_PREFIX + key);										
					if(key != null && key.equalsIgnoreCase(Integer.toString(TermId.ENTRY_NO.getId()))){
						levels.add(treatmentFactorValues.get(key).get(0));	
					}else{
						levels.add(Integer.toString(level));
					}
				}
			}
			
			StandardVariable stdvarTreatment = fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId());
			StandardVariable stdvarRep = null;				
			StandardVariable stdvarPlot = null;
			
			List<StandardVariable> reqVarList = getRequiredVariable();
			
			for(StandardVariable var : reqVarList){
				if(var.getId() == TermId.REP_NO.getId()){
					stdvarRep = var;
				}else if(var.getId() == TermId.PLOT_NO.getId()){
					stdvarPlot = var;
				}
			}
			
			
			MainDesign mainDesign = ExpDesignUtil.createRandomizedCompleteBlockDesign(block, 
					stdvarRep.getName(), stdvarPlot.getName(),
				treatmentFactor, levels, "1", "");
			
			measurementRowList = ExpDesignUtil.generateExpDesignMeasurements(environments, 
					nonTrialFactors, variates, treatmentVariables, reqVarList, germplasmList, 
					mainDesign, workbenchService, fieldbookProperties, (ExpDesignUtil.TREATMENT_PREFIX + stdvarTreatment.getId()), treatmentFactorValues);					
			
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
			//StandardVariable stdvarTreatment = fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId());
			StandardVariable stdvarRep = fieldbookMiddlewareService.getStandardVariable(TermId.REP_NO.getId());				
			StandardVariable stdvarPlot = fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId());
			
			varList.add(stdvarRep);
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
				if(!NumberUtils.isNumber(expDesignParameter.getBlockSize())){
					output = new ExpDesignValidationOutput(false, messageSource.getMessage(
		                    "experiment.design.block.size.should.be.a.number", null, locale));
				}else{
					int blockSize = Integer.valueOf(expDesignParameter.getBlockSize());
					
					if( blockSize < 1 ){
						output = new ExpDesignValidationOutput(false, messageSource.getMessage(
			                    "experiment.design.block.size.should.be.a.greater.than.1", null, locale));
					}
				}
			}
		}catch(Exception e){
			output = new ExpDesignValidationOutput(false, messageSource.getMessage(
                    "experiment.design.invalid.generic.error", null, locale));
		}
		
		return output;
	}

	
}
