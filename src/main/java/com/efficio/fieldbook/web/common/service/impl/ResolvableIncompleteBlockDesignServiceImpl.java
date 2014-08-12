package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

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

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.ResolvableIncompleteBlockDesignService;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;

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
	@Resource
	public FieldbookService fieldbookService;
	
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
			
			if(parameter.getUseLatenized() != null && parameter.getUseLatenized().booleanValue()){
				if(parameter.getReplicationsArrangement() != null){
					if(parameter.getReplicationsArrangement().intValue() == 1){
						//column
						parameter.setReplatinGroups(parameter.getReplicationsCount());
					}else if(parameter.getReplicationsArrangement().intValue() == 2){
						//rows
						String rowReplatingGroup = "";
						for(int i = 0 ; i < Integer.parseInt(parameter.getReplicationsCount()) ; i++){
							if(rowReplatingGroup != null && !rowReplatingGroup.equalsIgnoreCase("")){
								rowReplatingGroup += ",";
							}
							rowReplatingGroup += "1";
						}
						parameter.setReplatinGroups(rowReplatingGroup);
					}
				}
			}
			
			MainDesign mainDesign = ExpDesignUtil.createResolvableIncompleteBlockDesign(blockSize,
				Integer.toString(nTreatments), replicates, 
				stdvarTreatment.getName(), stdvarRep.getName(), stdvarBlock.getName(), stdvarPlot.getName(), 
				parameter.getNblatin(), parameter.getReplatinGroups(), "1", "", parameter.getUseLatenized());
			
			measurementRowList = ExpDesignUtil.generateExpDesignMeasurements(environments, 
					trialVariables, factors, nonTrialFactors, variates, treatmentVariables, reqVarList, germplasmList, 
					mainDesign, workbenchService, fieldbookProperties, stdvarTreatment.getName(), null, fieldbookService);					
			
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
					int treatmentSize = germplasmList.size();
					
					if(replicationCount <= 1 || replicationCount >= 13){
						output = new ExpDesignValidationOutput(false, messageSource.getMessage(
			                    "experiment.design.replication.count.resolvable.error", null, locale));
					}else if( blockSize <= 1 ){
						output = new ExpDesignValidationOutput(false, messageSource.getMessage(
			                    "experiment.design.block.size.should.be.a.greater.than.1", null, locale));
					}else if( treatmentSize % blockSize != 0 ){
						output = new ExpDesignValidationOutput(false, messageSource.getMessage(
			                    "experiment.design.block.size.not.a.factor.of.treatment.size", null, locale));
					}else if(expDesignParameter.getUseLatenized() != null && expDesignParameter.getUseLatenized().booleanValue()){
						//we add validation for latinize
						Integer nbLatin = Integer.parseInt(expDesignParameter.getNblatin());
						/*
The value set for "nblatin" xml parameter cannot be value higher than or equal the block level value. To get the block levels, we just need to divide the "ntreatments" value by the "blocksize" value. This means the BVDesign tool works to any value you specify in the "nblatin" parameter as long as it does not exceed the computed block levels value. As mentioned in the requirements, an "nblatin" parameter with value 0 means there is no latinization that will take place.
The sum of the values set for "replatingroups" should always be equal to the "nreplicates" value specified by the plant breeder.
						 */
						int blockLevel = treatmentSize / blockSize;						
						//nbLatin should be less than the block level
						if(nbLatin >= blockLevel){
							output = new ExpDesignValidationOutput(false, messageSource.getMessage(
				                    "experiment.design.nblatin.should.not.be.greater.than.block.level", null, locale));
						}else if(nbLatin >= replicationCount){
							output = new ExpDesignValidationOutput(false, messageSource.getMessage(
				                    "experiment.design.nblatin.should.not.be.greater.than.the.replication.count", null, locale));
						}else if(expDesignParameter.getReplicationsArrangement() != null && expDesignParameter.getReplicationsArrangement().intValue() == 3){
							//meaning adjacent
							StringTokenizer tokenizer = new StringTokenizer(expDesignParameter.getReplatinGroups(), ",");
							int totalReplatingGroup = 0;
							
							while(tokenizer.hasMoreTokens()){
								totalReplatingGroup += Integer.parseInt(tokenizer.nextToken());
							}
							if(totalReplatingGroup != replicationCount){
								output = new ExpDesignValidationOutput(false, messageSource.getMessage(
					                    "experiment.design.replating.groups.not.equal.to.replicates", null, locale));
							}
						}
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
		if (params.getUseLatenized() != null && params.getUseLatenized()) {
			return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.BLOCK_SIZE.getId(), TermId.NO_OF_REPS_IN_COLS.getId(),
					TermId.REPLICATIONS_MAP.getId(), TermId.NO_OF_CBLKS_LATINIZE.getId());
		}
		else {
			return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.BLOCK_SIZE.getId(), TermId.NO_OF_REPS_IN_COLS.getId());
		}
	}
}
