package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.service.ResolvableRowColumnDesignService;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
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
	
	@Override
	public  List<MeasurementRow> generateDesign(List<ImportedGermplasm> germplasmList,
			Map<String, String> parameterMap, 
			List<MeasurementVariable> nonTrialFactors, List<MeasurementVariable> variates, 
			List<TreatmentVariable> treatmentVariables, Map<String, List<String>> treatmentFactorValues) {
			
			List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();
			int nTreatments = germplasmList.size();
			String rows = parameterMap.get("rows");
			String cols = parameterMap.get("cols");
			String replicates = parameterMap.get("replicates");
			int environments = Integer.valueOf(parameterMap.get("environments"));					
			
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
					}else if(var.getId() == 8581){
						stdvarRows = var;
					}else if(var.getId() == 8582){
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
						nonTrialFactors, variates, treatmentVariables, reqVarList, germplasmList, 
						mainDesign, workbenchService, fieldbookProperties, stdvarTreatment, null);					
				
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
			StandardVariable stdvarRows = fieldbookMiddlewareService.getStandardVariable(8581);
			StandardVariable stdvarCols = fieldbookMiddlewareService.getStandardVariable(8582);
			
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

}
