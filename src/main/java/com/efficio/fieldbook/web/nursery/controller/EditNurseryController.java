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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.TreeViewUtil;

/**
 * The Class CreateNurseryController.
 */
@Controller
@RequestMapping(EditNurseryController.URL)
public class EditNurseryController extends SettingsController {
	
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EditNurseryController.class);

    /** The Constant URL. */
    public static final String URL = "/NurseryManager/editNursery";
    
    /** The Constant URL_SETTINGS. */
    public static final String URL_SETTINGS = "/NurseryManager/ver2.0/chooseSettings";
    
    @Resource
    private OntologyService ontologyService;
	
   
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
	return "NurseryManager/ver2.0/editNursery";
    }

    /**
     * Use existing nursery.
     *
     * @param form the form
     * @param nurseryId the nursery id
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/{nurseryId}", method = RequestMethod.GET)
    public String useExistingNursery(@ModelAttribute("createNurseryForm") CreateNurseryForm form, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form2, 
            @PathVariable int nurseryId, Model model, HttpSession session) throws MiddlewareQueryException{
        session.invalidate();
        if(nurseryId != 0){     
            //settings part
            Workbook workbook = fieldbookMiddlewareService.getNurseryDataSet(nurseryId);
            
            Dataset dataset = (Dataset)SettingsUtil.convertWorkbookToXmlDataset(workbook);
            SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId());
            
            //nursery-level
            List<SettingDetail> nurseryLevelConditions = updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()), 
                    buildRequiredVariablesLabel(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), true), 
                    buildRequiredVariablesFlag(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()), 
                    userSelection.getStudyLevelConditions(), true);
            List<SettingDetail> basicDetails = getBasicDetails(nurseryLevelConditions);
            
            removeBasicDetailsVariables(nurseryLevelConditions);
            
            //plot-level
            List<SettingDetail> plotLevelConditions = updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()), 
                    buildRequiredVariablesLabel(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), false), 
                    buildRequiredVariablesFlag(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()), 
                    userSelection.getPlotsLevelList(), false);
            
            userSelection.setBasicDetails(basicDetails);
            userSelection.setStudyLevelConditions(nurseryLevelConditions);
            userSelection.setPlotsLevelList(plotLevelConditions);
            form.setStudyId(nurseryId);
            form.setBasicDetails(userSelection.getBasicDetails());
            form.setStudyLevelVariables(userSelection.getStudyLevelConditions());
            form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
            form.setSelectionVariatesVariables(userSelection.getSelectionVariates());
            
            form.setNurseryConditions(userSelection.getNurseryConditions());
            //form.setSelectedSettingId(1);
            form.setLoadSettings("1");
            form.setRequiredFields(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString() + "," + AppConstants.FIXED_NURSERY_VARIABLES.getString());
            form.setProjectId(this.getCurrentProjectId());
            form.setIdNameVariables(AppConstants.ID_NAME_COMBINATION.getString());
            
            //measurements part
            if (workbook != null) {
                form.setFolderId(Integer.valueOf((int)workbook.getStudyDetails().getParentFolderId()));
                form.setFolderName(fieldbookMiddlewareService.getFolderNameById(form.getFolderId()));
                userSelection.setMeasurementRowList(workbook.getObservations());
                form.setMeasurementRowList(userSelection.getMeasurementRowList());
                form.setMeasurementVariables(workbook.getMeasurementDatasetVariables());
                form.setStudyName(workbook.getStudyDetails().getStudyName());
                form.changePage(1);
                userSelection.setCurrentPage(form.getCurrentPage());
                userSelection.setWorkbook(workbook);
            }
            
            if (userSelection.getMeasurementRowList() != null && userSelection.getMeasurementRowList().size() > 0) {
                for (SettingDetail setting : userSelection.getPlotsLevelList()) {
                    setting.setDeletable(false);
                }
            }
            
            form.setPlotLevelVariables(userSelection.getPlotsLevelList());
        }
        setFormStaticData(form);
        model.addAttribute("createNurseryForm", form);
        model.addAttribute("settingsList", getNurserySettingsList());
        model.addAttribute("nurseryList", getNurseryList());
        //setupFormData(form);
        return super.show(model);
    }
    
    private List<SettingDetail> getBasicDetails(List<SettingDetail> nurseryLevelConditions) {
        List<SettingDetail> basicDetails = new ArrayList<SettingDetail>();
        for (SettingDetail setting : nurseryLevelConditions) {
            if (inFixedNurseryList(setting.getVariable().getCvTermId())) {
                basicDetails.add(setting);
            }
        }
        return basicDetails;
    }
    
    private void removeBasicDetailsVariables(List<SettingDetail> nurseryLevelConditions) {
        Iterator<SettingDetail> iter = nurseryLevelConditions.iterator();
        while (iter.hasNext()) {
            if (inFixedNurseryList(iter.next().getVariable().getCvTermId())) {
                iter.remove();
            }
        }
    }
    
    private boolean inFixedNurseryList(int propertyId) {
        StringTokenizer token = new StringTokenizer(AppConstants.FIXED_NURSERY_VARIABLES.getString(), ",");
        while(token.hasMoreTokens()){
            if (Integer.parseInt(token.nextToken()) == propertyId) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Show.
     *
     * @param form the form
     * @param form2 the form2
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("createNurseryForm") CreateNurseryForm form, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form2, Model model, HttpSession session) throws MiddlewareQueryException{
    	session.invalidate();
    	form.setProjectId(this.getCurrentProjectId());
    	form.setRequiredFields(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString() + "," + AppConstants.FIXED_NURSERY_VARIABLES.getString());
    	form.setIdNameVariables(AppConstants.ID_NAME_COMBINATION.getString());
    	setFormStaticData(form);
    	assignDefaultValues(form);
    	return super.show(model);
    }
    
    /**
     * Assign default values.
     *
     * @param form the form
     * @throws MiddlewareQueryException the middleware query exception
     */
    private void assignDefaultValues(CreateNurseryForm form) throws MiddlewareQueryException {
        List<SettingDetail> basicDetails = new ArrayList<SettingDetail>();
        List<SettingDetail> nurseryDefaults = new ArrayList<SettingDetail>();
        List<SettingDetail> plotDefaults = new ArrayList<SettingDetail>();
        List<SettingDetail> baselineTraitsList = new ArrayList<SettingDetail>();
        List<SettingDetail> nurseryConditions = new ArrayList<SettingDetail>();
        
        basicDetails = buildDefaultVariables(basicDetails, AppConstants.FIXED_NURSERY_VARIABLES.getString(), buildRequiredVariablesLabel(AppConstants.FIXED_NURSERY_VARIABLES.getString(), false));
        form.setBasicDetails(basicDetails);
        form.setStudyLevelVariables(nurseryDefaults);
        form.setPlotLevelVariables(plotDefaults);
        nurseryDefaults = buildDefaultVariables(nurseryDefaults, AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), buildRequiredVariablesLabel(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), true));
        plotDefaults = buildDefaultVariables(plotDefaults, AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), buildRequiredVariablesLabel(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), false));
        
        this.userSelection.setBasicDetails(basicDetails);
        this.userSelection.setStudyLevelConditions(nurseryDefaults);
        this.userSelection.setPlotsLevelList(plotDefaults);
        this.userSelection.setBaselineTraitsList(baselineTraitsList);
        this.userSelection.setNurseryConditions(nurseryConditions);
    }    

    /**
     * Submit.
     *
     * @param form the form
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public Map<String, String> submit(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) throws MiddlewareQueryException {
    	String name = null;
    	for (SettingDetail nvar : form.getBasicDetails()) {
    		if (nvar.getVariable() != null && nvar.getVariable().getCvTermId() != null && nvar.getVariable().getCvTermId().equals(TermId.STUDY_NAME.getId())) {
    			name = nvar.getValue();
    			break;
    		}
    	}
    	
    	List<SettingDetail> studyLevelVariables = new ArrayList<SettingDetail>();
    	if (form.getStudyLevelVariables() != null && !form.getStudyLevelVariables().isEmpty()) {
    		studyLevelVariables.addAll(form.getStudyLevelVariables());
    	}
    	studyLevelVariables.addAll(form.getBasicDetails());
    	 
    	List<SettingDetail> studyLevelVariablesSession = userSelection.getBasicDetails();
    	userSelection.getStudyLevelConditions().addAll(studyLevelVariablesSession);
    	
    	List<SettingDetail> baselineTraits = form.getBaselineTraitVariables();
    	List<SettingDetail> baselineTraitsSession = userSelection.getSelectionVariates();
    	if (baselineTraits == null) {
    	    baselineTraits = form.getSelectionVariatesVariables();
    	    userSelection.getBaselineTraitsList().addAll(baselineTraitsSession);
    	} else if (form.getSelectionVariatesVariables() != null) {
    	    baselineTraits.addAll(form.getSelectionVariatesVariables());
    	    userSelection.getBaselineTraitsList().addAll(baselineTraitsSession);
    	}
    	
    	//include deleted list if measurements are available
    	if (userSelection.getMeasurementRowList() != null && userSelection.getMeasurementRowList().size() > 0) {
    	    addDeletedSettingsList(studyLevelVariables, userSelection.getDeletedStudyLevelConditions(), 
    	            userSelection.getStudyLevelConditions());
    	    addDeletedSettingsList(baselineTraits, userSelection.getDeletedStudyLevelConditions(), 
    	        userSelection.getBaselineTraitsList());
    	    addDeletedSettingsList(form.getNurseryConditions(), userSelection.getDeletedNurseryConditions(), 
                userSelection.getNurseryConditions());
    	}
    	    
    	Dataset dataset = (Dataset)SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, studyLevelVariables, 
    	        form.getPlotLevelVariables(), baselineTraits, userSelection, form.getNurseryConditions());
    	Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset);
    	    	
    	createStudyDetails(workbook, form.getBasicDetails(), form.getFolderId(), form.getStudyId());
    	userSelection.setWorkbook(workbook);
    	
    	Map<String, String> resultMap = new HashMap<String, String>();
    	//saving of measurement rows
    	if (userSelection.getMeasurementRowList() != null && userSelection.getMeasurementRowList().size() > 0) {
            try {
                int previewPageNum = userSelection.getCurrentPage();
                copyDataFromFormToUserSelection(form, previewPageNum);
                form.setMeasurementRowList(userSelection.getMeasurementRowList());
                form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariables());
                workbook.setObservations(form.getMeasurementRowList());
                
                userSelection.setWorkbook(workbook);
                validationService.validateObservationValues(workbook);
                fieldbookMiddlewareService.saveMeasurementRows(workbook);
                
                resultMap.put("status", "1");
            } catch (MiddlewareQueryException e) {
                LOG.error(e.getMessage());
                resultMap.put("status", "-1");
                resultMap.put("errorMessage", e.getMessage());
            }
            return resultMap;
    	} else {
    	    resultMap.put("status", "1");
    	    return resultMap;
    	}
    	
    }
    
    private void addDeletedSettingsList(List<SettingDetail> formList, List<SettingDetail> deletedList, List<SettingDetail> sessionList) {
        if (deletedList != null) {
            for (SettingDetail setting : deletedList) {
                setting.getVariable().setOperation(Operation.DELETE);
            }
            formList.addAll(deletedList);
            sessionList.addAll(deletedList);
        }
    }
    
    private void copyDataFromFormToUserSelection(CreateNurseryForm form, int previewPageNum){
        if (form.getPaginatedMeasurementRowList() != null) {
            for(int i = 0 ; i < form.getPaginatedMeasurementRowList().size() ; i++){
                    MeasurementRow measurementRow = form.getPaginatedMeasurementRowList().get(i);
                    int realIndex = ((previewPageNum - 1) * form.getResultPerPage()) + i;
                    for(int index = 0 ; index < measurementRow.getDataList().size() ; index++){
                            MeasurementData measurementData =  measurementRow.getDataList().get(index);
                            MeasurementData sessionMeasurementData = userSelection.getMeasurementRowList().get(realIndex).getDataList().get(index);
                            if(sessionMeasurementData.isEditable())
                                    sessionMeasurementData.setValue(measurementData.getValue());                            
                    }
                    //getUserSelection().getMeasurementRowList().set(realIndex, measurementRow);
            }
        }
    }
    
    /**
     * Creates the study details.
     *
     * @param workbook the workbook
     * @param conditions the conditions
     * @param folderId the folder id
     */
    private void createStudyDetails(Workbook workbook, List<SettingDetail> conditions, Integer folderId, Integer studyId) {
        if (workbook.getStudyDetails() == null) {
            workbook.setStudyDetails(new StudyDetails());
        }
        StudyDetails studyDetails = workbook.getStudyDetails();

        if (conditions != null && !conditions.isEmpty()) {
                studyDetails.setId(studyId);
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
    
    /**
     * Gets the setting detail value.
     *
     * @param details the details
     * @param termId the term id
     * @return the setting detail value
     */
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
    
    /**
     * Sets the form static data.
     *
     * @param form the new form static data
     */
    private void setFormStaticData(CreateNurseryForm form){
        form.setBreedingMethodId(AppConstants.BREEDING_METHOD_ID.getString());
        form.setLocationId(AppConstants.LOCATION_ID.getString());
        form.setBreedingMethodUrl(AppConstants.BREEDING_METHOD_URL.getString());
        form.setLocationUrl(AppConstants.LOCATION_URL.getString());
        form.setProjectId(this.getCurrentProjectId());
        form.setImportLocationUrl(AppConstants.IMPORT_GERMPLASM_URL.getString());
        form.setStudyNameTermId(AppConstants.STUDY_NAME_ID.getString());
        form.setStartDateId(AppConstants.START_DATE_ID.getString());
    	form.setEndDateId(AppConstants.END_DATE_ID.getString());
    	form.setOpenGermplasmUrl(AppConstants.GERMPLASM_DETAILS_URL.getString());
    }
    
    /**
     * Gets the setting detail list.
     *
     * @param mode the mode
     * @return the setting detail list
     */
    private List<SettingDetail> getSettingDetailList(int mode) {
        if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
            return userSelection.getStudyLevelConditions();
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
            return userSelection.getPlotsLevelList();
        } else if (mode == AppConstants.SEGMENT_TRAITS.getInt() || mode == AppConstants.SEGMENT_NURSERY_CONDITIONS.getInt()) {
            List<SettingDetail> newList = new ArrayList<SettingDetail>();
            
            for (SettingDetail setting : userSelection.getBaselineTraitsList()) {
                newList.add(setting);
            }
            
            for (SettingDetail setting : userSelection.getNurseryConditions()) {
                newList.add(setting);
            }
                
            return newList;
        } else if (mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()) {
            return userSelection.getSelectionVariates();
        }
        return null;
    }
    
    /**
     * Displays the Add Setting popup.
     *
     * @param mode the mode
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "displayAddSetting/{mode}", method = RequestMethod.GET)
    public Map<String, Object> showAddSettingPopup(@PathVariable int mode) {
                Map<String, Object> result = new HashMap<String, Object>();
        try {

                List<StandardVariableReference> standardVariableList = 
                                fieldbookService.filterStandardVariablesForSetting(mode, getSettingDetailList(mode));
                
                try{
                        if(userSelection.getTraitRefList() == null){
                                List<TraitClassReference> traitRefList = (List<TraitClassReference>) 
                                ontologyService.getAllTraitGroupsHierarchy(true);
                                userSelection.setTraitRefList(traitRefList);
                        }
                                List<TraitClassReference> traitRefList = userSelection.getTraitRefList();
                                //we convert it to map so that it would be easier to chekc if there is a record or not
                                HashMap<String, StandardVariableReference> mapVariableRef = new HashMap<String, StandardVariableReference>();
                                if(standardVariableList != null && !standardVariableList.isEmpty()){
                                        for(StandardVariableReference varRef: standardVariableList){
                                                mapVariableRef.put(varRef.getId().toString(), varRef);
                                        }
                                }
                                
                                String treeData = TreeViewUtil.convertOntologyTraitsToJson(traitRefList, mapVariableRef);
                        String searchTreeData = TreeViewUtil.convertOntologyTraitsToSearchSingleLevelJson(traitRefList, mapVariableRef);
                        result.put("treeData", treeData);
                        result.put("searchTreeData", searchTreeData);                
                }catch(Exception e){
                        LOG.error(e.getMessage());
                }
        } catch(Exception e) {
                LOG.error(e.getMessage(), e);
        }
        
        //return "[]";
        return result;
    }
    
    /**
     * Show variable details.
     *
     * @param id the id
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value="showVariableDetails/{id}", method = RequestMethod.GET)
    public String showVariableDetails(@PathVariable int id) {
        try {

                SettingVariable svar = getSettingVariable(id);
                if (svar != null) {
                        ObjectMapper om = new ObjectMapper();
                        return om.writeValueAsString(svar);
                }
                
        } catch(Exception e) {
                LOG.error(e.getMessage(), e);
        }
        return "[]";
    }
    
    /**
     * Adds the settings.
     *
     * @param form the form
     * @param model the model
     * @param mode the mode
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/addSettings/{mode}", method = RequestMethod.POST)
    public String addSettings(@ModelAttribute("createNurseryForm") CreateNurseryForm form, 
            Model model, @PathVariable int mode) {
        List<SettingDetail> newSettings = new ArrayList<SettingDetail>();
        try {
                List<SettingVariable> selectedVariables = form.getSelectedVariables();
                if (selectedVariables != null && !selectedVariables.isEmpty()) {
                        for (SettingVariable var : selectedVariables) {
                                populateSettingVariable(var);
                                        List<ValueReference> possibleValues = 
                                                fieldbookService.getAllPossibleValues(var.getCvTermId());
                                        SettingDetail newSetting = new SettingDetail(var, possibleValues, null, true);
                                        List<ValueReference> possibleValuesFavorite = fieldbookService.getAllPossibleValuesFavorite(var.getCvTermId(), this.getCurrentProjectId());
                                        newSetting.setPossibleValuesFavorite(possibleValuesFavorite);
                                        newSetting.setPossibleValuesToJson(possibleValues);
                                        newSetting.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
                                        newSettings.add(newSetting);
                        }
                }
                
                if (newSettings != null && !newSettings.isEmpty()) {
                        return addNewSettingDetails(form, mode, newSettings);
                }
                
        } catch(Exception e) {
                LOG.error(e.getMessage(), e);
        }
        
        return "[]";
    }
    
    /**
     * Clear settings.
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(value = "/clearSettings", method = RequestMethod.GET)
    public String clearSettings(@ModelAttribute("createNurseryForm") CreateNurseryForm form,
                Model model, HttpSession session) {
        
        try {
            form.setProjectId(this.getCurrentProjectId());
            form.setRequiredFields(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString() + "," + AppConstants.FIXED_NURSERY_VARIABLES.getString());
            form.setIdNameVariables(AppConstants.ID_NAME_COMBINATION.getString());
            setFormStaticData(form);
            assignDefaultValues(form);
        } catch(Exception e) {
                LOG.error(e.getMessage(), e);
        }
        
        model.addAttribute("createNurseryForm", form);
        model.addAttribute("settingsList", getNurserySettingsList());
        model.addAttribute("nurseryList", getNurseryList());
        
        return super.showAjaxPage(model, URL_SETTINGS);
    }
    
    /**
     * Adds the new setting details.
     *
     * @param form the form
     * @param mode the mode
     * @param newDetails the new details
     * @return the string
     * @throws Exception the exception
     */
    private String addNewSettingDetails(CreateNurseryForm form, int mode
            , List<SettingDetail> newDetails) throws Exception {
        if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
            if (form.getStudyLevelVariables() == null) {
                form.setStudyLevelVariables(newDetails);
            }
            else {
                form.getStudyLevelVariables().addAll(newDetails);
            }
            if (userSelection.getStudyLevelConditions() == null) {
                userSelection.setStudyLevelConditions(newDetails);
            }
            else {
                userSelection.getStudyLevelConditions().addAll(newDetails);
            }
            
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
            if (form.getPlotLevelVariables() == null) {
                form.setPlotLevelVariables(newDetails);
            }
            else {
                form.getPlotLevelVariables().addAll(newDetails);
            }
            if (userSelection.getPlotsLevelList() == null) {
                userSelection.setPlotsLevelList(newDetails);
            }
            else {
                userSelection.getPlotsLevelList().addAll(newDetails);
            }
        } else if (mode == AppConstants.SEGMENT_TRAITS.getInt()){
            if (form.getBaselineTraitVariables() == null) {
                form.setBaselineTraitVariables(newDetails);
            }
            else {
                form.getBaselineTraitVariables().addAll(newDetails);
            }
            if (userSelection.getBaselineTraitsList() == null) {
                userSelection.setBaselineTraitsList(newDetails);
            }
            else {
                userSelection.getBaselineTraitsList().addAll(newDetails);
            }
        } else if (mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()) {
            if (form.getSelectionVariatesVariables() == null) {
                form.setSelectionVariatesVariables(newDetails);
            } else {
                form.getSelectionVariatesVariables().addAll(newDetails);
            }
            if (userSelection.getSelectionVariates() == null) {
                userSelection.setSelectionVariates(newDetails);
            } else {
                userSelection.getSelectionVariates().addAll(newDetails);
            }
        } else {
            if (form.getNurseryConditions() == null) {
                form.setNurseryConditions(newDetails);
            } else {
                form.getNurseryConditions().addAll(newDetails);
            }
            if (userSelection.getNurseryConditions() == null) {
                userSelection.setNurseryConditions(newDetails);
            } else {
                userSelection.getNurseryConditions().addAll(newDetails);
            }
        }
        ObjectMapper om = new ObjectMapper();
        return om.writeValueAsString(newDetails);
    }
    
    @ResponseBody
    @RequestMapping(value = "/deleteVariable/{mode}/{variableId}", method = RequestMethod.POST)
    public String deleteVariable(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model, 
            @PathVariable int mode, @PathVariable int variableId) {
        if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
            //form.getNurseryLevelVariables()
            deleteVariableInSession(userSelection.getStudyLevelConditions(), variableId);
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
            deleteVariableInSession(userSelection.getPlotsLevelList(), variableId);
        } else if (mode == AppConstants.SEGMENT_TRAITS.getInt()){
            deleteVariableInSession(userSelection.getBaselineTraitsList(), variableId);
        } else if (mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()){
            deleteVariableInSession(userSelection.getSelectionVariates(), variableId);
        } else {
            deleteVariableInSession(userSelection.getNurseryConditions(), variableId);
        }
        return "";
    }
    
    private void deleteVariableInSession(List<SettingDetail> variableList, int variableId) {
        Iterator<SettingDetail> iter = variableList.iterator();
        while (iter.hasNext()) {
            if (iter.next().getVariable().getCvTermId().equals(Integer.valueOf(variableId))) {
                iter.remove();
            }
        }
    }
}
