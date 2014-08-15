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
import com.efficio.fieldbook.web.common.service.ResolvableRowColumnDesignService;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;

@Service
public class ResolvableRowColumnDesignServiceImpl implements
		ResolvableRowColumnDesignService {

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
	public  List<MeasurementRow> generateDesign(List<ImportedGermplasm> germplasmList,
			ExpDesignParameterUi parameter, List<MeasurementVariable> trialVariables, List<MeasurementVariable> factors,
			List<MeasurementVariable> nonTrialFactors, List<MeasurementVariable> variates, 
			List<TreatmentVariable> treatmentVariables) throws BVDesignException {
			
			List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();
			int nTreatments = germplasmList.size();
			String rows = parameter.getRowsPerReplications();
			String cols = parameter.getColsPerReplications();
			String replicates = parameter.getReplicationsCount();
			int environments = Integer.valueOf(parameter.getNoOfEnvironments());					
			
			try {
				
				StandardVariable stdvarTreatment = fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId());
				StandardVariable stdvarRep = null;				
				StandardVariable stdvarPlot = null;
				StandardVariable stdvarRows = null;
				StandardVariable stdvarCols = null;
				
				List<StandardVariable> reqVarList = getRequiredVariable();
				
				for(StandardVariable var : reqVarList){
					if(var.getId() == TermId.REP_NO.getId()){
						stdvarRep = var;
					}else if(var.getId() == TermId.ROW.getId()){
						stdvarRows = var;
					}else if(var.getId() == TermId.COL.getId()){
						stdvarCols = var;
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
				MainDesign mainDesign = ExpDesignUtil.createResolvableRowColDesign(Integer.toString(nTreatments),
						replicates, rows, cols, stdvarTreatment.getName(), stdvarRep.getName(), 
						stdvarRows.getName(),stdvarCols.getName(),stdvarPlot.getName(),
						parameter.getNrlatin(), parameter.getNclatin(), parameter.getReplatinGroups(), "1", "", parameter.getUseLatenized());
				
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
			//StandardVariable stdvarTreatment = fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId());
			StandardVariable stdvarRep = fieldbookMiddlewareService.getStandardVariable(TermId.REP_NO.getId());				
			StandardVariable stdvarPlot = fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId());
			StandardVariable stdvarRows = fieldbookMiddlewareService.getStandardVariable(TermId.ROW.getId());
			StandardVariable stdvarCols = fieldbookMiddlewareService.getStandardVariable(TermId.COL.getId());
			
			varList.add(stdvarRep);
			varList.add(stdvarPlot);
			varList.add(stdvarRows);
			varList.add(stdvarCols);
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
				int size = germplasmList.size();
				if(!NumberUtils.isNumber(expDesignParameter.getRowsPerReplications())){
					output = new ExpDesignValidationOutput(false, messageSource.getMessage(
		                    "experiment.design.rows.per.replication.should.be.a.number", null, locale));
				}else if(!NumberUtils.isNumber(expDesignParameter.getColsPerReplications())){
					output = new ExpDesignValidationOutput(false, messageSource.getMessage(
		                    "experiment.design.cols.per.replication.should.be.a.number", null, locale));
				}else if(!NumberUtils.isNumber(expDesignParameter.getReplicationsCount())){
					output = new ExpDesignValidationOutput(false, messageSource.getMessage(
		                    "experiment.design.replication.count.should.be.a.number", null, locale));
				}else{
					int rowsPerReplication = Integer.valueOf(expDesignParameter.getRowsPerReplications());
					int colsPerReplication = Integer.valueOf(expDesignParameter.getColsPerReplications());
					int replicationCount = Integer.valueOf(expDesignParameter.getReplicationsCount());
					
					if(replicationCount <= 1 || replicationCount >= 13){
						output = new ExpDesignValidationOutput(false, messageSource.getMessage(
			                    "experiment.design.replication.count.resolvable.error", null, locale));
					}else if( size != (rowsPerReplication * colsPerReplication) ){
						output = new ExpDesignValidationOutput(false, messageSource.getMessage(
			                    "experiment.design.resolvable.incorrect.row.and.col.product.to.germplasm.size", null, locale));
					}else if(expDesignParameter.getUseLatenized() != null && expDesignParameter.getUseLatenized().booleanValue()){
						//we add validation for latinize
						Integer nrLatin = Integer.parseInt(expDesignParameter.getNrlatin());
						Integer ncLatin = Integer.parseInt(expDesignParameter.getNclatin());
						/*
"nrows" and "ncolumns" are indeed the factors of the "ntreatments" value. Equation: nrows x ncolumns = ntreatments.
"nrlatin" parameter value should be a positive integer less than the "nrows" value set
"nclatin" parameter value should be a positive integer less than the "ncolumns" value set
The sum of the values set for "replatingroups" should always be equal to the "nreplicates" value specified by the plant breeder.
nrlatin somehow cannot exceed the nreplicates value specified.  A technical error is thrown with this unclear message: "Error from CycDesigN: output parameters 13, 0, 0, 0." This might be a possible bug.

						 */				
						//nrlatin and nclatin validation
						if(nrLatin >= rowsPerReplication){
							output = new ExpDesignValidationOutput(false, messageSource.getMessage(
				                    "experiment.design.nrlatin.should.be.less.than.rows.per.replication", null, locale));
						}else if(nrLatin >= replicationCount){
							output = new ExpDesignValidationOutput(false, messageSource.getMessage(
				                    "experiment.design.nrlatin.should.not.be.greater.than.the.replication.count", null, locale));
						}else if(ncLatin >= colsPerReplication){
							output = new ExpDesignValidationOutput(false, messageSource.getMessage(
				                    "experiment.design.nclatin.should.be.less.than.cols.per.replication", null, locale));
						}else if(ncLatin >= replicationCount){
							output = new ExpDesignValidationOutput(false, messageSource.getMessage(
				                    "experiment.design.nclatin.should.not.be.greater.than.the.replication.count", null, locale));
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
			return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), 
				TermId.NO_OF_ROWS_IN_REPS.getId(), TermId.NO_OF_COLS_IN_REPS.getId(),
				TermId.NO_OF_CROWS_LATINIZE.getId(), TermId.NO_OF_CCOLS_LATINIZE.getId(), TermId.REPLICATIONS_MAP.getId(),   
				TermId.NO_OF_REPS_IN_COLS.getId());
		}
		else {
			return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), 
					TermId.NO_OF_ROWS_IN_REPS.getId(), TermId.NO_OF_COLS_IN_REPS.getId());
		}
	}

}
