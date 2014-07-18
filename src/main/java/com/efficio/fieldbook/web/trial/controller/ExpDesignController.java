package com.efficio.fieldbook.web.trial.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.service.RandomizeCompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableIncompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableRowColumnDesignService;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
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
    public String showMeasurements(Model model) {
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
        
    	int designType = 0;
    	List<ImportedGermplasm> germplasmList = userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
		Map<String, String> parameterMap = new HashMap<String, String>();
		Map<String, List<String>> treatmentFactorValues = new HashMap<String, List<String>>(); //Key - CVTerm ID , List of values
    	try{
	    	if(designType == 0){
	    		randomizeCompleteBlockDesign.generateDesign(germplasmList, parameterMap, workbook.getGermplasmFactors(), workbook.getVariates(), workbook.getTreatmentFactors(), treatmentFactorValues);
	    	}else if(designType == 1){
	    		resolveIncompleteBlockDesign.generateDesign(germplasmList, parameterMap, workbook.getGermplasmFactors(), workbook.getVariates(), null, null);
	    	}else if(designType == 2){
	    		resolvableRowColumnDesign.generateDesign(germplasmList, parameterMap, workbook.getGermplasmFactors(), workbook.getVariates(), null, null);
	    	}
	    		
    	}catch(Exception e){
    		e.printStackTrace();
    	}
        
        return showAjaxPage(model, URL_MEASUREMENT);
    }

}
