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

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.service.api.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.nursery.bean.SettingVariable;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.util.AppConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class SettingsController.
 */
public abstract class SettingsController extends AbstractBaseFieldbookController{

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(SettingsController.class);
	 
	/** The workbench service. */
	@Resource
	protected WorkbenchService workbenchService;
	
	/** The fieldbook service. */
	@Resource
	protected FieldbookService fieldbookService;
	
	/** The fieldbook middleware service. */
	@Resource
	protected org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	
	/** The user selection. */
    @Resource
    protected UserSelection userSelection;    
	
	/** The measurements generator service. */
	@Resource
	protected MeasurementsGeneratorService measurementsGeneratorService;
	
	/** The validation service. */
	@Resource
	protected ValidationService validationService;
	
	/** The data import service. */
	@Resource
	protected DataImportService dataImportService;
	

	
	/**
	 * Gets the settings list.
	 *
	 * @return the settings list
	 */
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
    
    /**
     * Builds the required factors.
     *
     * @return the list
     */
    protected List<Integer> buildRequiredFactors() {
        List<Integer> requiredFactors = new ArrayList<Integer>();
        String createNurseryRequiredFields = AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString();
        StringTokenizer token = new StringTokenizer(createNurseryRequiredFields, ",");
        while(token.hasMoreTokens()){
        	requiredFactors.add(Integer.valueOf(token.nextToken()));
        }        
        return requiredFactors;
    }
    
    /**
     * Builds the required factors label.
     *
     * @return the list
     */
    protected List<String> buildRequiredFactorsLabel() {
    	
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

    /**
     * Builds the required factors flag.
     *
     * @return the boolean[]
     */
    protected boolean[] buildRequiredFactorsFlag() {
        boolean[] requiredFactorsFlag = new boolean[5];
        
        for (int i = 0; i < requiredFactorsFlag.length; i++) {
            requiredFactorsFlag[i] = false;
        }
        return requiredFactorsFlag;
    } 
    
    /**
     * Populates Setting Variable.
     *
     * @param var the var
     * @throws MiddlewareQueryException the middleware query exception
     */
    protected void populateSettingVariable(SettingVariable var) throws MiddlewareQueryException {
    	StandardVariable  stdvar = getStandardVariable(var.getCvTermId());
    	if (stdvar != null) {
			var.setDescription(stdvar.getDescription());
			var.setProperty(stdvar.getProperty().getName());
			var.setScale(stdvar.getScale().getName());
			var.setMethod(stdvar.getMethod().getName());
			var.setDataType(stdvar.getDataType().getName());
			var.setRole(stdvar.getStoredIn().getName());
			var.setCropOntologyId(stdvar.getCropOntologyId() != null ? stdvar.getCropOntologyId() : "");
			var.setTraitClass(stdvar.getIsA() != null ? stdvar.getIsA().getName() : "");
			var.setDataTypeId(stdvar.getDataType().getId());
			var.setMinRange(stdvar.getConstraints() != null && stdvar.getConstraints().getMinValue() != null ? stdvar.getConstraints().getMinValue() : null);
			var.setMaxRange(stdvar.getConstraints() != null && stdvar.getConstraints().getMaxValue() != null ? stdvar.getConstraints().getMaxValue() : null);
			var.setWidgetType();
    	}
    }

    /**
     * Get setting variable.
     *
     * @param id the id
     * @return the setting variable
     * @throws MiddlewareQueryException the middleware query exception
     */
    protected SettingVariable getSettingVariable(int id) throws MiddlewareQueryException {
		StandardVariable stdVar = getStandardVariable(id);
		if (stdVar != null) {
			SettingVariable svar = new SettingVariable(stdVar.getName(), 
			        stdVar.getDescription(), stdVar.getProperty().getName(),
					stdVar.getScale().getName(), stdVar.getMethod().getName(), 
					stdVar.getStoredIn().getName(), 
					stdVar.getDataType().getName(), stdVar.getDataType().getId(),
					stdVar.getConstraints() != null && stdVar.getConstraints().getMinValue() != null ? stdVar.getConstraints().getMinValue() : null,
					stdVar.getConstraints() != null && stdVar.getConstraints().getMaxValue() != null ? stdVar.getConstraints().getMaxValue() : null);
			svar.setCvTermId(stdVar.getId());
			svar.setCropOntologyId(stdVar.getCropOntologyId() != null ? stdVar.getCropOntologyId() : "");
			svar.setTraitClass(stdVar.getIsA() != null ? stdVar.getIsA().getName() : "");
			return svar;
		}
		return null;
    }
    
    /**
     * Get standard variable.
     *
     * @param id the id
     * @return the standard variable
     * @throws MiddlewareQueryException the middleware query exception
     */
    protected StandardVariable getStandardVariable(int id) throws MiddlewareQueryException {
    	StandardVariable variable = userSelection.getCacheStandardVariable(id);
    	if (variable == null) {
    		variable = fieldbookMiddlewareService.getStandardVariable(id);
    		if (variable != null) {
    			userSelection.putStandardVariableInCache(variable);
    		}
    	}
    	
    	return variable;
    }
}
