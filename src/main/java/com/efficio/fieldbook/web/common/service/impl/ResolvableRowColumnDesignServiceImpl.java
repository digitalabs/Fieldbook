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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

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
	 
	@Override
	public  List<MeasurementRow> generateDesign(List<ImportedGermplasm> germplasmList,
			ExpDesignParameterUi parameter, List<MeasurementVariable> factors,
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
				
				
				MainDesign mainDesign = ExpDesignUtil.createResolvableRowColDesign(Integer.toString(nTreatments),
						replicates, rows, cols, stdvarTreatment.getName(), stdvarRep.getName(), 
						stdvarRows.getName(),stdvarCols.getName(),stdvarPlot.getName(),
						"0", "0", "", "1", "");
				
				measurementRowList = ExpDesignUtil.generateExpDesignMeasurements(environments, 
						factors, nonTrialFactors, variates, treatmentVariables, reqVarList, germplasmList, 
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
					
					if(replicationCount <= 1 || replicationCount >= 11){
						output = new ExpDesignValidationOutput(false, messageSource.getMessage(
			                    "experiment.design.replication.count.resolvable.error", null, locale));
					}else if( size != (rowsPerReplication * colsPerReplication) ){
						output = new ExpDesignValidationOutput(false, messageSource.getMessage(
			                    "experiment.design.resolvable.incorrect.row.and.col.product.to.germplasm.size", null, locale));
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
