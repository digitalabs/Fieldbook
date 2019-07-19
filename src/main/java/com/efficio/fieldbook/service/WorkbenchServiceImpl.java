/*******************************************************************************
 * Copyright (c) 2014, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.service;

import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.Tool;

import com.efficio.fieldbook.service.api.WorkbenchService;

/**
 * The Class WorkbenchServiceImpl.
 *
 * @author Joyce Avestro
 */
public class WorkbenchServiceImpl implements WorkbenchService {

	/**
	 * The workbench data manager.
	 */
	@Resource
	private WorkbenchDataManager workbenchDataManager;

	@Override
	public Tool getToolWithName(String toolName) {
		return this.workbenchDataManager.getToolWithName(toolName);
	}

	@Override
	public List<StandardPreset> getAllStandardPresets() {
		return this.workbenchDataManager.getStandardPresetDAO().getAll();
	}

	@Override
	public List<StandardPreset> getStandardPresetByCrop(String cropName) {
		return this.workbenchDataManager.getStandardPresetFromCropAndTool(cropName, this.getFieldbookWebTool().getToolId().intValue());
	}

	@Override
	public List<StandardPreset> getStandardPresetByCrop(int toolId, String cropName, String toolSection) {
		return this.workbenchDataManager.getStandardPresetFromCropAndTool(cropName, toolId, toolSection);
	}

	@Override
	public List<StandardPreset> getStandardPresetByCropAndPresetName(String presetName, int toolId, String cropName, String toolSection) {
		return this.workbenchDataManager.getStandardPresetFromCropAndToolByName(presetName, cropName, toolId, toolSection);
	}

	@Override
	public Tool getFieldbookWebTool() {
		return this.workbenchDataManager.getToolWithName("fieldbook_web");
	}

	@Override
	public StandardPreset getStandardPresetById(Integer id) {
		return this.workbenchDataManager.getStandardPresetDAO().getById(id);
	}

	@Override
	public StandardPreset saveOrUpdateStandardPreset(StandardPreset preset) {
		return this.workbenchDataManager.saveOrUpdateStandardPreset(preset);
	}

	@Override
	public Integer getCurrentIbdbUserId(Long projectId, Integer workbenchUserId) {
		return this.workbenchDataManager.getCurrentIbdbUserId(projectId, workbenchUserId);
	}

	@Override
	public Project getProjectById(Long projectId) {
		return this.workbenchDataManager.getProjectById(projectId);
	}

}
