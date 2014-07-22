package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import org.thymeleaf.expression.Lists;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.RandomizeCompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableIncompleteBlockDesignService;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.WorkbookUtil;

@Service
public class ResolvableIncompleteBlockDesignServiceImpl implements ResolvableIncompleteBlockDesignService{

	@Resource
	public org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	@Resource
	protected WorkbenchService workbenchService;
	@Resource
	protected FieldbookProperties fieldbookProperties;
	@Resource
    private ResourceBundleMessageSource messageSource;
	
	
	@Override
	public List<MeasurementRow> generateDesign(List<ImportedGermplasm> germplasmList,
			ExpDesignParameterUi parameter, List<MeasurementVariable> trialVariables, List<MeasurementVariable> factors, 
			List<MeasurementVariable> nonTrialFactors, List<MeasurementVariable> variates, 
			List<TreatmentVariable> treatmentVariables) throws BVDesignException {
		
		List<MeasurementRow> measurementRowList = new ArrayList();

		int nTreatments = germplasmList.size();
		String blockSize = parameter.getBlockSize();
		String replicates = parameter.getReplicationsCount();	    	
		int environments = Integer.valueOf(parameter.getNoOfEnvironments());
		//we need to add the 4 vars
		try {
			
			StandardVariable stdvarTreatment = fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId());
			StandardVariable stdvarRep = null;
			StandardVariable stdvarBlock = null;		
			StandardVariable stdvarPlot = null;
			
			List<StandardVariable> reqVarList = getRequiredVariable();
			
			for(StandardVariable var : reqVarList){
				if(var.getId() == TermId.REP_NO.getId()){
					stdvarRep = var;
				}else if(var.getId() == TermId.BLOCK_NO.getId()){
					stdvarBlock = var;
				}else if(var.getId() == TermId.PLOT_NO.getId()){
					stdvarPlot = var;
				}
			}
			
			
			MainDesign mainDesign = ExpDesignUtil.createResolvableIncompleteBlockDesign(blockSize,
				Integer.toString(nTreatments), replicates, 
				stdvarTreatment.getName(), stdvarRep.getName(), stdvarBlock.getName(), stdvarPlot.getName(), 
				"0", "", "1", "");
			
			measurementRowList = ExpDesignUtil.generateExpDesignMeasurements(environments, 
					trialVariables, factors, nonTrialFactors, variates, treatmentVariables, reqVarList, germplasmList, 
					mainDesign, workbenchService, fieldbookProperties, stdvarTreatment.getName(), null);					
			
		}catch(BVDesignException e){
			throw e;
		}catch(Exception e){
			e.printStackTrace();
		}
		return measurementRowList;
	}

	@Override
	public List<StandardVariable> getRequiredVariable() {
		List<StandardVariable> varList = new ArrayList();
		try {		
			StandardVariable stdvarRep = fieldbookMiddlewareService.getStandardVariable(TermId.REP_NO.getId());
			StandardVariable stdvarBlock = fieldbookMiddlewareService.getStandardVariable(TermId.BLOCK_NO.getId());		
			StandardVariable stdvarPlot = fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId());
		
			varList.add(stdvarRep);
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
				if(!NumberUtils.isNumber(expDesignParameter.getBlockSize())){
					output = new ExpDesignValidationOutput(false, messageSource.getMessage(
		                    "experiment.design.block.size.should.be.a.number", null, locale));
				}else if(!NumberUtils.isNumber(expDesignParameter.getReplicationsCount())){
					output = new ExpDesignValidationOutput(false, messageSource.getMessage(
		                    "experiment.design.replication.count.should.be.a.number", null, locale));
				}else{
					int blockSize = Integer.valueOf(expDesignParameter.getBlockSize());
					int replicationCount = Integer.valueOf(expDesignParameter.getReplicationsCount());
					
					if(replicationCount <= 1 || replicationCount >= 11){
						output = new ExpDesignValidationOutput(false, messageSource.getMessage(
			                    "experiment.design.replication.count.resolvable.error", null, locale));
					}else if( blockSize < 1 ){
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
