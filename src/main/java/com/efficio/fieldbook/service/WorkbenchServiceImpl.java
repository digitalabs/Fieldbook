/*******************************************************************************
 * Copyright (c) 2014, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.service;

import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.*;

import com.efficio.fieldbook.service.api.WorkbenchService;

/**
 * The Class WorkbenchServiceImpl.
 *  
 *  @author Joyce Avestro
 *  
 */
public class WorkbenchServiceImpl implements WorkbenchService{
	
    /** The workbench data manager. */
    @Resource
    private WorkbenchDataManager workbenchDataManager;    
    
    @Override
    public Tool getToolWithName(String toolName) throws MiddlewareQueryException{    	
        return workbenchDataManager.getToolWithName(toolName);
    }
    
    @Override
    public List<TemplateSetting> getTemplateSettings(TemplateSetting templateSettingFilter) 
            throws MiddlewareQueryException{
        return workbenchDataManager.getTemplateSettings(templateSettingFilter);
    }

    @Override
    public List<StandardPreset> getAllStandardPresets() throws MiddlewareQueryException {
        return workbenchDataManager.getStandardPresetDAO().getAll();
    }

    @Override
    public List<StandardPreset> getStandardPresetByCrop(String cropName) throws MiddlewareQueryException {
        return workbenchDataManager.getStandardPresetFromCropAndTool(cropName,this.getFieldbookWebTool().getToolId().intValue());
    }

    @Override
    public Tool getFieldbookWebTool() throws MiddlewareQueryException {
        return workbenchDataManager.getToolWithName("fieldbook_web");
    }

    @Override
    public StandardPreset getStandardPresetById(Integer id) throws MiddlewareQueryException {
        return workbenchDataManager.getStandardPresetDAO().getById(id);
    }

    @Override
    public StandardPreset saveOrUpdateStandardPresets(StandardPreset preset) throws MiddlewareQueryException{
        return  workbenchDataManager.addStandardPreset(preset);
    }
    
    @Override
    public Integer addTemplateSetting(TemplateSetting templateSetting) throws MiddlewareQueryException{
        return workbenchDataManager.addTemplateSetting(templateSetting);
    }
    
    @Override
    public void updateTemplateSetting(TemplateSetting templateSetting) throws MiddlewareQueryException{
        workbenchDataManager.updateTemplateSetting(templateSetting);
    }

    @Override
    public void deleteTemplateSetting(Integer templateSettingId) throws MiddlewareQueryException{
        workbenchDataManager.deleteTemplateSetting(templateSettingId);
    }
    
    @Override
    public Integer getCurrentIbdbUserId(Long projectId, Integer workbenchUserId) throws MiddlewareQueryException {
    	Integer ibdbUserId = null;
        IbdbUserMap userMapEntry = workbenchDataManager.getIbdbUserMap(workbenchUserId, projectId);
        if (userMapEntry != null) {
            ibdbUserId = userMapEntry.getIbdbUserId();
        }
        return ibdbUserId;
    }

	@Override
	public Project getProjectById(Long projectId)
			throws MiddlewareQueryException {
		return workbenchDataManager.getProjectById(projectId);
	}
    
    
}
