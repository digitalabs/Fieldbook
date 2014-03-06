/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.nursery.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.nursery.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.bean.SettingVariable;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

@Controller
@RequestMapping(CreateNurseryController.URL)
public class CreateNurseryController extends AbstractBaseFieldbookController {
	
    private static final Logger LOG = LoggerFactory.getLogger(CreateNurseryController.class);

    public static final String URL = "/NurseryManager/createNursery";
    public static final String URL_SETTINGS = "/NurseryManager/chooseSettings";
	
    @Resource
    private UserSelection userSelection;
    
	@Resource
	private WorkbenchService workbenchService;
	
	@Resource
	private FieldbookService fieldbookService;
	
	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	
	@Resource
	private MeasurementsGeneratorService measurementsGeneratorService;
	
	@Resource
	private ValidationService validationService;
	
	@Resource
	private DataImportService dataImportService;
	

	@Override
	public String getContentName() {
		return "NurseryManager/createNursery";
	}

    @ModelAttribute("settingsList")
    public List<TemplateSetting> getSettingsList() {
        try {
        	TemplateSetting templateSettingFilter = new TemplateSetting(null, Integer.valueOf(getCurrentProjectId()), null, getNurseryTool(), null, null);
        	templateSettingFilter.setIsDefaultToNull();
            List<TemplateSetting> templateSettingsList = workbenchService.getTemplateSettings(templateSettingFilter);
            templateSettingsList.add(0, new TemplateSetting(Integer.valueOf(0), Integer.valueOf(getCurrentProjectId()), "", null, "", false));
            return templateSettingsList;

        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
		
        return null;
    }
    
    /**
     * Gets the settings list.
     *
     * @return the settings list
     */
    @ModelAttribute("nurseryList")
    public List<StudyDetails> getNurseryList() {
        try {
            List<StudyDetails> nurseries = fieldbookMiddlewareService.getAllLocalNurseryDetails();
            return nurseries;
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
                
        return null;
    }

    @RequestMapping(value="/nursery/{nurseryId}", method = RequestMethod.GET)
    public String useExistingNursery(@ModelAttribute("manageSettingsForm") CreateNurseryForm form, @PathVariable int nurseryId
            , Model model, HttpSession session) throws MiddlewareQueryException{
        if(nurseryId != 0){     
            Workbook workbook = fieldbookMiddlewareService.getNurseryDataSet(nurseryId);
            Dataset dataset = SettingsUtil.convertWorkbookToXmlDataset(workbook);
            SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId());
            List<Integer> requiredFactors = buildRequiredFactors();
            List<String> requiredFactorsLabel = buildRequiredFactorsLabel();
            boolean[] requiredFactorsFlag = buildRequiredFactorsFlag();
            List<SettingDetail> nurseryLevelConditions = userSelection.getNurseryLevelConditions();
                    
            for(SettingDetail nurseryLevelCondition : nurseryLevelConditions){
                Integer  stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(nurseryLevelCondition.getVariable().getProperty(), 
                        nurseryLevelCondition.getVariable().getScale(), nurseryLevelCondition.getVariable().getMethod(), 
                        PhenotypicType.valueOf(nurseryLevelCondition.getVariable().getRole()));
                
                //mark required factors that are already in the list
                int ctr = 0;
                for (Integer requiredFactor: requiredFactors) {
                    if (requiredFactor.equals(stdVar)) {
                        requiredFactorsFlag[ctr] = true;
                    }
                    ctr++;
                }
            }
            
            
            //add required factors that are not in existing nursery
            for (int i = 0; i < requiredFactorsFlag.length; i++) {
                if (!requiredFactorsFlag[i]) {
                    nurseryLevelConditions.add(createSettingDetail(requiredFactors.get(i), requiredFactorsLabel.get(i)));
                }
            }
            
            userSelection.setNurseryLevelConditions(nurseryLevelConditions);
            form.setNurseryLevelVariables(userSelection.getNurseryLevelConditions());
            form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
            form.setPlotLevelVariables(userSelection.getPlotsLevelList());
            form.setSelectedSettingId(0);
            form.setRequiredFields(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString());
        }
        
        model.addAttribute("manageSettingsForm", form);
        model.addAttribute("settingsList", getSettingsList());
        model.addAttribute("nurseryList", getNurseryList());
        //setupFormData(form);
        return super.showAjaxPage(model, getContentName() );
    }
    
    /**
     * Creates the setting detail.
     *
     * @param id the id
     * @return the setting detail
     * @throws MiddlewareQueryException the middleware query exception
     */
    private SettingDetail createSettingDetail(int id, String name) throws MiddlewareQueryException {
            String variableName = "";
            StandardVariable stdVar = getStandardVariable(id);
            if (name != null) {
                variableName = name;
            } else {
                variableName = stdVar.getName();
            }
            if (stdVar != null) {
            SettingVariable svar = new SettingVariable(
                    variableName, stdVar.getDescription(), stdVar.getProperty().getName(),
                                        stdVar.getScale().getName(), stdVar.getMethod().getName(), stdVar.getStoredIn().getName(), 
                                        stdVar.getDataType().getName(), stdVar.getDataType().getId(), 
                                        stdVar.getConstraints() != null && stdVar.getConstraints().getMinValue() != null ? stdVar.getConstraints().getMinValue() : null,
                                        stdVar.getConstraints() != null && stdVar.getConstraints().getMaxValue() != null ? stdVar.getConstraints().getMaxValue() : null);
                        svar.setCvTermId(stdVar.getId());
                        svar.setCropOntologyId(stdVar.getCropOntologyId() != null ? stdVar.getCropOntologyId() : "");
                        svar.setTraitClass(stdVar.getIsA() != null ? stdVar.getIsA().getName() : "");

                        List<ValueReference> possibleValues = fieldbookService.getAllPossibleValues(id);
                        SettingDetail settingDetail = new SettingDetail(svar, possibleValues, null, false);
                        settingDetail.setPossibleValuesToJson(possibleValues);
                        List<ValueReference> possibleValuesFavorite = fieldbookService.getAllPossibleValuesFavorite(id, this.getCurrentProjectId());
                        settingDetail.setPossibleValuesFavorite(possibleValuesFavorite);
                        settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
                        return settingDetail;
                }
                return new SettingDetail();
    }
    
    /**
     * Get standard variable.
     * @param id
     * @return
     * @throws MiddlewareQueryException
     */
    private StandardVariable getStandardVariable(int id) throws MiddlewareQueryException {
        StandardVariable variable = userSelection.getCacheStandardVariable(id);
        if (variable == null) {
                variable = fieldbookMiddlewareService.getStandardVariable(id);
                if (variable != null) {
                        userSelection.putStandardVariableInCache(variable);
                }
        }
        
        return variable;
    }
    
    private List<Integer> buildRequiredFactors() {
        List<Integer> requiredFactors = new ArrayList<Integer>();
        String createNurseryRequiredFields = AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString();
        StringTokenizer token = new StringTokenizer(createNurseryRequiredFields, ",");
        while(token.hasMoreTokens()){
        	requiredFactors.add(Integer.valueOf(token.nextToken()));
        }        
        return requiredFactors;
    }
    
    private List<String> buildRequiredFactorsLabel() {
    	
        List<String> requiredFactors = new ArrayList<String>();
        /*
        requiredFactors.add(AppConstants.LOCATION.getString());
        requiredFactors.add(AppConstants.PRINCIPAL_INVESTIGATOR.getString());
        requiredFactors.add(AppConstants.STUDY_NAME.getString());
        requiredFactors.add(AppConstants.STUDY_TITLE.getString());
        requiredFactors.add(AppConstants.OBJECTIVE.getString());
        */
        String createNurseryRequiredFields = AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString();
        StringTokenizer token = new StringTokenizer(createNurseryRequiredFields, ",");
        while(token.hasMoreTokens()){
        	requiredFactors.add(AppConstants.getString(token.nextToken() + AppConstants.LABEL.getString()));
        }        
        
        return requiredFactors;
    }

    private boolean[] buildRequiredFactorsFlag() {
        boolean[] requiredFactorsFlag = new boolean[5];
        
        for (int i = 0; i < requiredFactorsFlag.length; i++) {
            requiredFactorsFlag[i] = false;
        }
        return requiredFactorsFlag;
    } 
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("createNurseryForm") CreateNurseryForm form, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form2, Model model, HttpSession session) throws MiddlewareQueryException{
    	session.invalidate();
    	form.setProjectId(this.getCurrentProjectId());
    	form.setRequiredFields(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString());
    	return super.show(model);
    }

    @RequestMapping(value="/view/{templateSettingId}", method = RequestMethod.POST)
    public String viewSettings(@ModelAttribute("createNurseryForm") CreateNurseryForm form, @PathVariable int templateSettingId, 
    	Model model, HttpSession session) throws MiddlewareQueryException{
    	
    	if(templateSettingId != 0){    	
	    	TemplateSetting templateSettingFilter = new TemplateSetting(Integer.valueOf(templateSettingId), Integer.valueOf(getCurrentProjectId()), null, getNurseryTool(), null, null);
	    	templateSettingFilter.setIsDefaultToNull();
	    	List<TemplateSetting> templateSettings = workbenchService.getTemplateSettings(templateSettingFilter);
	    	TemplateSetting templateSetting = templateSettings.get(0); //always 1
	    	Dataset dataset = SettingsUtil.parseXmlToDatasetPojo(templateSetting.getConfiguration());
	    	userSelection.setDataset(dataset);
	    	SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId());
	    	form.setNurseryLevelVariables(userSelection.getNurseryLevelConditions());
	    	form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
	    	form.setPlotLevelVariables(userSelection.getPlotsLevelList());
//	    	form.setIsDefault(templateSetting.getIsDefault().intValue() == 1 ? true : false);
//	    	form.setSettingName(templateSetting.getName());
	    	form.setSelectedSettingId(templateSetting.getTemplateSettingId());
	    	form.setRequiredFields(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString());
//    	}else{
//    		assignDefaultValues(form);
    	}
//    	model.addAttribute("createNurseryForm", form);
//    	model.addAttribute("settingsList", getSettingsList());
        return super.showAjaxPage(model, URL_SETTINGS );
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public String submit(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) throws MiddlewareQueryException {
    	
    	String name = null;
    	for (SettingDetail nvar : form.getNurseryLevelVariables()) {
    		if (nvar.getVariable() != null && nvar.getVariable().getCvTermId() != null && nvar.getVariable().getCvTermId().equals(TermId.STUDY_NAME.getId())) {
    			name = nvar.getValue();
    			break;
    		}
    	}
    	System.out.println("NAME IS " + name);
    	
    	Dataset dataset = SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, form.getNurseryLevelVariables(), form.getPlotLevelVariables(), form.getBaselineTraitVariables(), userSelection);
//    	Dataset dataset = userSelection.getDataset();
    	Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset);
    	userSelection.setWorkbook(workbook);

    	
    	createStudyDetails(workbook, form.getNurseryLevelVariables(), form.getFolderId());
 
    	return "success";
    }
    
    private void createStudyDetails(Workbook workbook, List<SettingDetail> conditions, Integer folderId) {
        if (workbook.getStudyDetails() == null) {
            workbook.setStudyDetails(new StudyDetails());
        }
        StudyDetails studyDetails = workbook.getStudyDetails();

        if (conditions != null && !conditions.isEmpty()) {
	        studyDetails.setTitle(getSettingDetailValue(conditions, TermId.STUDY_TITLE.getId()));
	        studyDetails.setObjective(getSettingDetailValue(conditions, TermId.STUDY_OBJECTIVE.getId()));
	        studyDetails.setStudyName(getSettingDetailValue(conditions, TermId.STUDY_NAME.getId()));
	        studyDetails.setStudyType(StudyType.N);
	        
	        if (folderId != null) {
	        	studyDetails.setParentFolderId(folderId);
	        }
    	}
        studyDetails.print(1);
    }
    
    private String getSettingDetailValue(List<SettingDetail> details, int termId) {
    	String value = null;
    	
    	for (SettingDetail detail : details) {
    		if (detail.getVariable().getCvTermId().equals(termId)) {
    			value = detail.getValue();
    			break;
    		}
    	}
    	
    	return value;
    }
    
}
