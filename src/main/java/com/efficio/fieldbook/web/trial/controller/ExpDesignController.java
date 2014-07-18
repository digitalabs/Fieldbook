package com.efficio.fieldbook.web.trial.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.service.RandomizeCompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableIncompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableRowColumnDesignService;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.util.SettingsUtil;

@Controller
@RequestMapping(ExpDesignController.URL)
public class ExpDesignController extends
        BaseTrialController {

    private static final Logger LOG = LoggerFactory.getLogger(ExpDesignController.class);
    public static final String URL = "/TrialManager/experimental/design";
    
    @Resource
    private OntologyService ontologyService;
    
    @Resource
    private RandomizeCompleteBlockDesignService randomizeCompleteBlockDesign;
    @Resource
    private ResolvableIncompleteBlockDesignService resolveIncompleteBlockDesign;
    @Resource
    private ResolvableRowColumnDesignService resolvableRowColumnDesign;
    
    @Override
    public String getContentName() {
        return "TrialManager/openTrial";
    }
   

    @RequestMapping(value = "/generate", method = RequestMethod.POST)
    public String showMeasurements(Model model, @RequestBody ExpDesignParameterUi expDesign) {
    	/*
			0 - Resolvable Complete Block Design
			1 - Resolvable Incomplete Block Design
			2 - Resolvable Row Col
    	 */
    	//we do the conversion
         List<SettingDetail> studyLevelConditions = userSelection.getStudyLevelConditions();
         List<SettingDetail> basicDetails = userSelection.getBasicDetails();
         // transfer over data from user input into the list of setting details stored in the session
    	 List<SettingDetail> combinedList = new ArrayList<SettingDetail>();
         combinedList.addAll(basicDetails);

         if (studyLevelConditions != null) {             
             combinedList.addAll(studyLevelConditions);
         }

         String name = "";

         
    	Dataset dataset = (Dataset) SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, combinedList,
                userSelection.getPlotsLevelList(), userSelection.getBaselineTraitsList(), userSelection, userSelection.getTrialLevelVariableList(),
                userSelection.getTreatmentFactors(), null, null, userSelection.getNurseryConditions(), false);

        Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, false);
        userSelection.setTemporaryWorkbook(workbook);
        
    	int designType = expDesign.getDesignType();
    	List<ImportedGermplasm> germplasmList = userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
		Map<String, String> parameterMap = new HashMap<String, String>();
		
		
		//{designType=0, replicationsCount=4, treatmentFactors={8284={variable={cvTermId=8284, name=DAY_OBS, description=Day of observation - Not specified (Day), property=Observation time, scale=Day (dd), method=Not specified, role=Trial design information, dataType=Numeric variable, traitClass=Trial environment, cropOntologyId=, dataTypeId=1110, minRange=null, maxRange=null, widgetType=NTEXT, operation=ADD, storedInId=null}, possibleValues=[], possibleValuesFavorite=null, possibleValuesJson=null, possibleValuesFavoriteJson=null, value=null, order=0, group=null, pairedVariable=null, hidden=false, deletable=true, favorite=false}}, treatmentFactorsData={8284={levels=4, labels=[100, 200, 300, 400], pairCvTermId=8282}}}
		
    	try{
    		parameterMap.put("environments", expDesign.getNoOfEnvironments());
	    	if(designType == 0){
	    		
				parameterMap.put("block", expDesign.getReplicationsCount());
				Map<String, List<String>> treatmentFactorValues = new HashMap<String, List<String>>(); //Key - CVTerm ID , List of values
				List<TreatmentVariable> treatmentVarList = new ArrayList();
				Map treatmentFactorsData = expDesign.getTreatmentFactorsData();
				if(treatmentFactorsData != null){
					Iterator keySetIter = treatmentFactorsData.keySet().iterator();
					while(keySetIter.hasNext()){
						String key = (String) keySetIter.next();
						Map treatmentData = (Map) treatmentFactorsData.get(key);						
						treatmentFactorValues.put(key, (List)treatmentData.get("labels"));
					}
				}
				treatmentFactorValues.put(Integer.toString(TermId.ENTRY_NO.getId()), Arrays.asList(germplasmList.size()));
				//TODO : still need to entry no
	    		randomizeCompleteBlockDesign.generateDesign(germplasmList, parameterMap, workbook.getGermplasmFactors(), workbook.getVariates(), workbook.getTreatmentFactors(), treatmentFactorValues);
	    	}else if(designType == 1){
	    		parameterMap.put("blockSize", expDesign.getBlockSize());
	    		parameterMap.put("replicates", expDesign.getReplicationsCount());	    	

	    		
	    		resolveIncompleteBlockDesign.generateDesign(germplasmList, parameterMap, workbook.getGermplasmFactors(), workbook.getVariates(), null, null);
	    	}else if(designType == 2){
	    		parameterMap.put("rows", expDesign.getRowsPerReplications());
				parameterMap.put("cols", expDesign.getColsPerReplications());
				parameterMap.put("replicates", expDesign.getReplicationsCount());
	    		resolvableRowColumnDesign.generateDesign(germplasmList, parameterMap, workbook.getGermplasmFactors(), workbook.getVariates(), null, null);
	    	}
	    		
    	}catch(Exception e){
    		e.printStackTrace();
    	}
        
        return showAjaxPage(model, URL_MEASUREMENT);
    }

}
