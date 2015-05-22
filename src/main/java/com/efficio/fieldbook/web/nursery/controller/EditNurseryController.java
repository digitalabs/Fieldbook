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
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.util.ContextUtil;
import org.generationcp.middleware.domain.etl.ExperimentalDesignVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.WebUtils;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

/**
 * The Class CreateNurseryController.
 */
@Controller
@RequestMapping(EditNurseryController.URL)
public class EditNurseryController extends SettingsController {

    /**
     * The Constant URL.
     */
    public static final String URL = "/NurseryManager/editNursery";
    /**
     * The Constant URL_SETTINGS.
     */
    public static final String URL_SETTINGS = "/NurseryManager/addOrRemoveTraits";
    public static final String STATUS = "status";
    public static final String HAS_MEASUREMENT_DATA_STR = "hasMeasurementData";
    public static final String ERROR = "-1";
    public static final String NO_MEASUREMENT = "0";
    public static final String SUCCESS = "1";

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EditNurseryController.class);
    /**
     * The ontology service.
     */
    @Resource
    private OntologyService ontologyService;

    /**
     * The fieldbook service.
     */
    @Resource
    private FieldbookService fieldbookService;

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */

    @Resource
    private ErrorHandlerService errorHandlerService;


    @Override
    public String getContentName() {
        return "NurseryManager/editNursery";
    }

    /**
     * Use existing nursery.
     *
     * @param form      the form
     * @param form2     the form2
     * @param nurseryId the nursery id
     * @param model     the model
     * @param session   the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value = "/{nurseryId}", method = RequestMethod.GET)
    public String useExistingNursery(@ModelAttribute("createNurseryForm") CreateNurseryForm form,
                                     @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form2,                                     
                                     @PathVariable int nurseryId, @RequestParam(required = false) String isAjax,
                                     Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) throws MiddlewareQueryException {

        final String contextParams = retrieveContextInfo(request);

        this.clearSessionData(request.getSession());

        try {
            Workbook workbook = null;
            if (nurseryId != 0) {
                //settings part
                workbook = fieldbookMiddlewareService.getNurseryDataSet(nurseryId);

                userSelection.setConstantsWithLabels(workbook.getConstants());

                form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(workbook.getVariates())));

                convertToXmlDatasetPojo(workbook);

                //nursery-level
                List<SettingDetail> nurseryLevelConditions = updateRequiredFields(buildVariableIDList(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()),
                        buildRequiredVariablesLabel(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), true),
                        buildRequiredVariablesFlag(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()),
                        userSelection.getStudyLevelConditions(), false, AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());

                List<SettingDetail> basicDetails = getSettingDetailsOfSection(nurseryLevelConditions, form,
                        AppConstants.FIXED_NURSERY_VARIABLES.getString());

                setCheckVariables(userSelection.getRemovedConditions(), form2, form);

                removeBasicDetailsVariables(nurseryLevelConditions);

                userSelection.setBasicDetails(basicDetails);
                form.setStudyId(nurseryId);
                form.setBasicDetails(userSelection.getBasicDetails());
                form.setStudyLevelVariables(userSelection.getStudyLevelConditions());
                form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
                form.setSelectionVariatesVariables(userSelection.getSelectionVariates());

                form.setNurseryConditions(userSelection.getNurseryConditions());
                form.setLoadSettings(SUCCESS);
                form.setFolderId(Integer.valueOf((int) workbook.getStudyDetails().getParentFolderId()));

                form.setFolderName(getNurseryFolderName(form.getFolderId()));

                //measurements part
                SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbook.getObservations(), false, ontologyService);
                setMeasurementsData(form, workbook);

                //make factors uneditable if experiments exist already
                if (form.isMeasurementDataExisting()) {
                    for (SettingDetail setting : userSelection.getPlotsLevelList()) {
                        setting.setDeletable(false);
                    }
                }

                form.setPlotLevelVariables(userSelection.getPlotsLevelList());

                List<GermplasmList> germplasmList = fieldbookMiddlewareService.getGermplasmListsByProjectId(Integer.valueOf(nurseryId), GermplasmListType.ADVANCED);
                List<GermplasmList> germplasmCrossesList = fieldbookMiddlewareService.getGermplasmListsByProjectId(Integer.valueOf(nurseryId), GermplasmListType.CROSSES);
                model.addAttribute("advancedList", germplasmList);
                model.addAttribute("crossesList", germplasmCrossesList);
            }

            setFormStaticData(form, contextParams, workbook);
            model.addAttribute("createNurseryForm", form);

            if (isAjax != null && SUCCESS.equalsIgnoreCase(isAjax)) {
                return super.showAjaxPage(model, getContentName());
            }

            return super.show(model);

        } catch (MiddlewareQueryException e) {
            LOG.debug(e.getMessage(), e);
            redirectAttributes.addFlashAttribute("redirectErrorMessage", errorHandlerService.getErrorMessagesAsString(e.getCode(), new String[]{AppConstants.NURSERY.getString(), StringUtils.capitalize(AppConstants.NURSERY.getString()), AppConstants.NURSERY.getString()}, "\n"));
            return "redirect:" + ManageNurseriesController.URL;
        }

    }

    protected String getNurseryFolderName(int folderId) throws MiddlewareQueryException {
        if (folderId == 1) {
            return AppConstants.NURSERIES.getString();
        }
        return fieldbookMiddlewareService.getFolderNameById(folderId);
    }

    protected void removeBasicDetailsVariables(List<SettingDetail> nurseryLevelConditions) {
        SettingsUtil.removeBasicDetailsVariables(nurseryLevelConditions);
    }

    protected void convertToXmlDatasetPojo(Workbook workbook) throws MiddlewareQueryException {
        Dataset dataset = (Dataset) SettingsUtil.convertWorkbookToXmlDataset(workbook);

        SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProject().getUniqueID(), false, false);
    }

    protected void clearSessionData(HttpSession session) {
        SessionUtility.clearSessionData(session, new String[]{SessionUtility.USER_SELECTION_SESSION_NAME, SessionUtility.POSSIBLE_VALUES_SESSION_NAME, SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
    }


    protected void setCheckVariables(List<SettingDetail> removedConditions,
                                     ImportGermplasmListForm form2, CreateNurseryForm form) {
        //set check variables
        List<SettingDetail> checkVariables = getCheckVariables(removedConditions, form);
        form2.setCheckVariables(checkVariables);
    }

    protected String retrieveContextInfo(HttpServletRequest request) {
        ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO);
        return ContextUtil.getContextParameterString(contextInfo);
    }

    /**
     * Sets the measurements data.
     *
     * @param form     the form
     * @param workbook the workbook
     */
    private void setMeasurementsData(CreateNurseryForm form, Workbook workbook) {
        userSelection.setMeasurementRowList(workbook.getObservations());
        form.setMeasurementRowList(userSelection.getMeasurementRowList());
        form.setMeasurementVariables(workbook.getMeasurementDatasetVariables());
        form.setStudyName(workbook.getStudyDetails().getStudyName());
        form.changePage(1);
        userSelection.setCurrentPage(form.getCurrentPage());
        userSelection.setWorkbook(workbook);
        userSelection.setTemporaryWorkbook(null);
    }

    /**
     * Show.
     *
     * @param form    the form
     * @param form2   the form2
     * @param model   the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("createNurseryForm") CreateNurseryForm form,
                       @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form2,
                       Model model, HttpServletRequest req, HttpSession session, HttpServletRequest request) throws MiddlewareQueryException {

        final String contextParams = retrieveContextInfo(request);
        clearSessionData(session);
        setFormStaticData(form, contextParams, new Workbook());
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
     * @param form  the form
     * @param model the model
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public Map<String, String> submit(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) throws MiddlewareQueryException {
        //get the name of the nursery
    	
    
        String name = null;
        for (SettingDetail nvar : form.getBasicDetails()) {
            if (nvar.getVariable() != null && nvar.getVariable().getCvTermId() != null && nvar.getVariable().getCvTermId().equals(TermId.STUDY_NAME.getId())) {
                name = nvar.getValue();
                break;
            }
        }
        //combine all study conditions (basic details and management details and hidden variables)
        List<SettingDetail> studyLevelVariables = new ArrayList<SettingDetail>();
        if (form.getStudyLevelVariables() != null && !form.getStudyLevelVariables().isEmpty()) {
            studyLevelVariables.addAll(form.getStudyLevelVariables());
        }
        studyLevelVariables.addAll(form.getBasicDetails());
 
        List<SettingDetail> studyLevelVariablesSession = userSelection.getBasicDetails();
        userSelection.getStudyLevelConditions().addAll(studyLevelVariablesSession);
        if (userSelection.getRemovedConditions() != null) {
            studyLevelVariables.addAll(userSelection.getRemovedConditions());
            userSelection.getStudyLevelConditions().addAll(userSelection.getRemovedConditions());
        }
        
        addNurseryTypeFromDesignImport(studyLevelVariables);
        addExperimentalDesignTypeFromDesignImport(studyLevelVariables);

        //add hidden variables like OCC in factors list
        if (userSelection.getRemovedFactors() != null) {
            form.getPlotLevelVariables().addAll(userSelection.getRemovedFactors());
            userSelection.getPlotsLevelList().addAll(userSelection.getRemovedFactors());
        }

        //combine all variates (traits and selection variates)
        List<SettingDetail> baselineTraits = form.getBaselineTraitVariables();
        List<SettingDetail> baselineTraitsSession = userSelection.getSelectionVariates();
        if (baselineTraits == null) {
            baselineTraits = form.getSelectionVariatesVariables();
            userSelection.getBaselineTraitsList().addAll(baselineTraitsSession);
        } else if (form.getSelectionVariatesVariables() != null) {
            baselineTraits.addAll(form.getSelectionVariatesVariables());
            userSelection.getBaselineTraitsList().addAll(baselineTraitsSession);
        }

        if (form.getPlotLevelVariables() == null) {
            form.setPlotLevelVariables(new ArrayList<SettingDetail>());
        }
        if (baselineTraits == null) {
            baselineTraits = new ArrayList<SettingDetail>();
        }
        if (form.getNurseryConditions() == null) {
            form.setNurseryConditions(new ArrayList<SettingDetail>());
        }

        //include deleted list if measurements are available
        SettingsUtil.addDeletedSettingsList(studyLevelVariables, userSelection.getDeletedStudyLevelConditions(),
                userSelection.getStudyLevelConditions());
        SettingsUtil.addDeletedSettingsList(form.getPlotLevelVariables(), userSelection.getDeletedPlotLevelList(),
                userSelection.getPlotsLevelList());
        SettingsUtil.addDeletedSettingsList(baselineTraits, userSelection.getDeletedBaselineTraitsList(),
                userSelection.getBaselineTraitsList());
        SettingsUtil.addDeletedSettingsList(form.getNurseryConditions(), userSelection.getDeletedNurseryConditions(),
                userSelection.getNurseryConditions());

        int trialDatasetId = userSelection.getWorkbook().getTrialDatasetId();
        //retain measurement dataset id
        int measurementDatasetId = userSelection.getWorkbook().getMeasurementDatesetId();

        Dataset dataset = (Dataset) SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, studyLevelVariables,
                form.getPlotLevelVariables(), baselineTraits, userSelection, form.getNurseryConditions());

        SettingsUtil.setConstantLabels(dataset, userSelection.getConstantsWithLabels());

        Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, true);
        workbook.setOriginalObservations(userSelection.getWorkbook().getOriginalObservations());
        workbook.setTrialDatasetId(trialDatasetId);
        workbook.setMeasurementDatesetId(measurementDatasetId);
        workbook.setTrialObservations(userSelection.getWorkbook().getTrialObservations());
        setTrialObservationsFromVariables(workbook);

        this.createStudyDetails(workbook, form.getBasicDetails(), form.getFolderId(), form.getStudyId());
        userSelection.setWorkbook(workbook);

        Map<String, String> resultMap = new HashMap<String, String>();
        //saving of measurement rows
        if (userSelection.getMeasurementRowList() != null && !userSelection.getMeasurementRowList().isEmpty()) {
            try {
                WorkbookUtil.addMeasurementDataToRows(workbook.getFactors(), false, userSelection, ontologyService, fieldbookService);
                WorkbookUtil.addMeasurementDataToRows(workbook.getVariates(), true, userSelection, ontologyService, fieldbookService);

                workbook.setMeasurementDatasetVariables(null);
                form.setMeasurementRowList(userSelection.getMeasurementRowList());
                form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariables());
                workbook.setObservations(form.getMeasurementRowList());

                userSelection.setWorkbook(workbook);

                fieldbookService.createIdCodeNameVariablePairs(userSelection.getWorkbook(), AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
                fieldbookService.createIdNameVariablePairs(userSelection.getWorkbook(), userSelection.getRemovedConditions(), AppConstants.ID_NAME_COMBINATION.getString(), true);
                fieldbookMiddlewareService.saveMeasurementRows(workbook);
                workbook.setTrialObservations(
                        fieldbookMiddlewareService.buildTrialObservations(trialDatasetId, workbook.getTrialConditions(), workbook.getTrialConstants()));
                workbook.setOriginalObservations(workbook.getObservations());
                	                
                fieldbookService.saveStudyImportedCrosses(userSelection.getImportedCrossesId(), form.getStudyId());
                resultMap.put(STATUS, SUCCESS);
                resultMap.put(HAS_MEASUREMENT_DATA_STR, String.valueOf(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(workbook.getVariates()))));
                
                fieldbookService.saveStudyColumnOrdering(form.getStudyId(), workbook.getStudyName(), form.getColumnOrders(), workbook);
            } catch (MiddlewareQueryException e) {
                resultMap.put(STATUS, ERROR);
                resultMap.put("errorMessage", e.getMessage());

                LOG.error(e.getMessage(), e);
            }
            return resultMap;
        } else {
            resultMap.put(STATUS, SUCCESS);
            return resultMap;
        }

    }
    
    private void addNurseryTypeFromDesignImport(List<SettingDetail> studyLevelVariables) {
		
    	SettingDetail nurseryTypeSettingDetail = new SettingDetail();
    	SettingVariable nurseryTypeSettingVariable = new SettingVariable();
    	
    	Integer nurseryTypeValue = userSelection.getNurseryTypeForDesign();
    	
    	nurseryTypeSettingDetail.setValue(String.valueOf(nurseryTypeValue));
    	nurseryTypeSettingVariable.setCvTermId(TermId.NURSERY_TYPE.getId());
    	nurseryTypeSettingVariable.setName("NURSERY_TYPE");
    	nurseryTypeSettingVariable.setOperation(Operation.ADD);
    	nurseryTypeSettingDetail.setVariable(nurseryTypeSettingVariable);
    	
    	if (userSelection.getNurseryTypeForDesign() != null && nurseryTypeValue != null){
   
    		for (SettingDetail settingDetail : studyLevelVariables){
        		if (settingDetail.getVariable().getCvTermId() == TermId.NURSERY_TYPE.getId()){
        			settingDetail.setValue(String.valueOf(nurseryTypeValue));
        			settingDetail.getVariable().setName("NURSERY_TYPE");
        			userSelection.setNurseryTypeForDesign(null);
        			return;
        		}
        	}
    		
        	studyLevelVariables.add(nurseryTypeSettingDetail);
    	}
    	
    	userSelection.setNurseryTypeForDesign(null);
    	
	}
    
    private void addExperimentalDesignTypeFromDesignImport(List<SettingDetail> studyLevelVariables) {
		
    	SettingDetail nurseryTypeSettingDetail = new SettingDetail();
    	SettingVariable nurseryTypeSettingVariable = new SettingVariable();
    	
    	
    	nurseryTypeSettingDetail.setValue(String.valueOf(TermId.OTHER_DESIGN.getId()));
    	nurseryTypeSettingVariable.setCvTermId(TermId.EXPERIMENT_DESIGN_FACTOR.getId());
    	nurseryTypeSettingVariable.setName("EXPERIMENT_DESIGN");
    	nurseryTypeSettingVariable.setOperation(Operation.ADD);
    	nurseryTypeSettingDetail.setVariable(nurseryTypeSettingVariable);
    	
    	if (userSelection.getExpDesignVariables() != null && !userSelection.getExpDesignVariables().isEmpty()){
   
    		for (SettingDetail settingDetail : studyLevelVariables){
        		if (settingDetail.getVariable().getCvTermId() == TermId.EXPERIMENT_DESIGN_FACTOR.getId()){
        			settingDetail.setValue(String.valueOf(TermId.OTHER_DESIGN.getId()));
        			settingDetail.getVariable().setName("EXPERIMENT_DESIGN");
        			return;
        		}
        	}

        	studyLevelVariables.add(nurseryTypeSettingDetail);
    	}
    	
	}

    private void setTrialObservationsFromVariables(Workbook workbook) {
        if (workbook.getTrialObservations() != null && !workbook.getTrialObservations().isEmpty() &&
                workbook.getTrialConditions() != null && !workbook.getTrialConditions().isEmpty()) {
            for (MeasurementVariable condition : workbook.getTrialConditions()) {
                for (MeasurementData data : workbook.getTrialObservations().get(0).getDataList()) {
                    if (data.getMeasurementVariable().getTermId() == condition.getTermId()) {
                        data.setValue(condition.getValue());
                    }
                }
            }
        }
        if (workbook.getTrialObservations() != null && !workbook.getTrialObservations().isEmpty() &&
                workbook.getTrialConstants() != null && !workbook.getTrialConstants().isEmpty()) {
            for (MeasurementVariable constant : workbook.getTrialConstants()) {
                for (MeasurementData data : workbook.getTrialObservations().get(0).getDataList()) {
                    if (data.getMeasurementVariable().getTermId() == constant.getTermId()) {
                        data.setValue(constant.getValue());

                        if (constant.getStoredIn() == TermId.CATEGORICAL_VARIATE.getId() && constant.getValue() != null
                                && NumberUtils.isNumber(constant.getValue())) {
                            data.setcValueId(constant.getValue());
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets the form static data.
     *
     * @param form the new form static data
     */
    protected void setFormStaticData(CreateNurseryForm form, String contextParams, Workbook workbook) {
    	
    	ExperimentalDesignVariable expDesignVar = workbook.getExperimentalDesignVariables();
    	if (expDesignVar != null && expDesignVar.getExperimentalDesign() != null){
    		form.setExperimentTypeId(expDesignVar.getExperimentalDesign().getValue());
    	}
    	
        form.setBreedingMethodId(AppConstants.BREEDING_METHOD_ID.getString());
        form.setLocationId(AppConstants.LOCATION_ID.getString());
        form.setBreedingMethodUrl(fieldbookProperties.getProgramBreedingMethodsUrl());
        form.setLocationUrl(fieldbookProperties.getProgramLocationsUrl());
        form.setProjectId(this.getCurrentProjectId());
        form.setImportLocationUrl(fieldbookProperties.getGermplasmImportUrl() + "?" + contextParams);
        form.setStudyNameTermId(AppConstants.STUDY_NAME_ID.getString());
        form.setStartDateId(AppConstants.START_DATE_ID.getString());
        form.setEndDateId(AppConstants.END_DATE_ID.getString());
        form.setOpenGermplasmUrl(fieldbookProperties.getGermplasmDetailsUrl());
        form.setBaselineTraitsSegment(AppConstants.SEGMENT_TRAITS.getString());
        form.setSelectionVariatesSegment(AppConstants.SEGMENT_SELECTION_VARIATES.getString());
        form.setCharLimit(Integer.parseInt(AppConstants.CHAR_LIMIT.getString()));
        form.setRequiredFields(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString() + "," + AppConstants.FIXED_NURSERY_VARIABLES.getString());
        form.setProjectId(this.getCurrentProjectId());
        form.setIdNameVariables(AppConstants.ID_NAME_COMBINATION.getString());
        form.setBreedingMethodCode(AppConstants.BREEDING_METHOD_CODE.getString());
        Integer datasetId = workbook.getMeasurementDatesetId();
        try {
            if (datasetId == null) {
                datasetId = fieldbookMiddlewareService.getMeasurementDatasetId(workbook.getStudyId(), workbook.getStudyName());
            }
            form.setHasFieldmap(fieldbookMiddlewareService.hasFieldMap(datasetId));
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Check measurement data.
     *
     * @param form       the form
     * @param model      the model
     * @param mode       the mode
     * @param variableId the variable id
     * @return the map
     */
    @ResponseBody
    @RequestMapping(value = "/checkMeasurementData/{mode}/{variableIds}", method = RequestMethod.GET)
    public Map<String, String> checkMeasurementData(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model,
                                                    @PathVariable int mode, @PathVariable String variableIds) {
        Map<String, String> resultMap = new HashMap<String, String>();

        //if there are measurement rows, check if values are already entered
        if (userSelection.getMeasurementRowList() != null && !userSelection.getMeasurementRowList().isEmpty() &&
        		hasMeasurementDataEnteredForVariables(SettingsUtil.parseVariableIds(variableIds), userSelection)) {
            resultMap.put(HAS_MEASUREMENT_DATA_STR, SUCCESS);
        } else {
            resultMap.put(HAS_MEASUREMENT_DATA_STR, NO_MEASUREMENT);
        }

        return resultMap;
    }

    /**
     * Reset session variables after save.
     *
     * @param form    the form
     * @param model   the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value = "/recreate/session/variables", method = RequestMethod.GET)
    public String resetSessionVariablesAfterSave(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model,
                                                 HttpSession session, HttpServletRequest request) throws MiddlewareQueryException {

        final String contextParams = retrieveContextInfo(request);

        Workbook workbook = userSelection.getWorkbook();
        form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(workbook.getVariates())));
        fieldbookMiddlewareService.setOrderVariableByRank(workbook);
        resetSessionVariablesAfterSave(workbook, true);

        //set measurement session variables to form
        setMeasurementsData(form, workbook);
        setFormStaticData(form, contextParams, workbook);
        model.addAttribute("createNurseryForm", form);

        return super.showAjaxPage(model, URL_SETTINGS);
    }

    /**
     * Show variable details.
     *
     * @param id the id
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/showVariableDetails/{id}", method = RequestMethod.GET)
    public String showVariableDetails(@PathVariable int id) {
        try {

            SettingVariable svar = getSettingVariable(id);
            if (svar != null) {
                ObjectMapper om = new ObjectMapper();
                return om.writeValueAsString(svar);
            }

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "[]";
    }

    @ResponseBody
    @RequestMapping(value = "/deleteMeasurementRows", method = RequestMethod.POST)
    public Map<String, String> deleteMeasurementRows() {
        Map<String, String> resultMap = new HashMap<String, String>();

        try {
            fieldbookMiddlewareService.deleteObservationsOfStudy(userSelection.getWorkbook().getMeasurementDatesetId());
            resultMap.put(STATUS, SUCCESS);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            resultMap.put(STATUS, ERROR);
            resultMap.put("errorMessage", e.getMessage());
        }

        userSelection.setMeasurementRowList(null);
        userSelection.getWorkbook().setOriginalObservations(null);
        userSelection.getWorkbook().setObservations(null);
        return resultMap;
    }

    @ModelAttribute("nameTypes")
    public List<UserDefinedField> getNameTypes() {
        try {
            return fieldbookMiddlewareService.getGermplasmNameTypes();
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }

        return new ArrayList<UserDefinedField>();
    }

    @ModelAttribute("programLocationURL")
    public String getProgramLocation() {
        return fieldbookProperties.getProgramLocationsUrl();
    }

    @ModelAttribute("programMethodURL")
    public String getProgramMethod() {
        return fieldbookProperties.getProgramBreedingMethodsUrl();
    }

    @ModelAttribute("projectID")
    public String getProgramID() {
        return getCurrentProjectId();
    }
}
