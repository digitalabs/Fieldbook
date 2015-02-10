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
package com.efficio.fieldbook.service.api;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.pojos.workbench.Tool;

import java.util.List;

/**
 * The Interface WorkbenchService.
 *
 * @author Joyce Avestro
 */
public interface WorkbenchService {

	/**
	 * Gets the tool with name.
	 *
	 * @param toolName the tool name
	 * @return the tool with name
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	Tool getToolWithName(String toolName) throws MiddlewareQueryException;

	/**
	 * Gets the template settings.
	 *
	 * @param templateSettingFilter the template setting filter
	 * @return the template settings
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	List<TemplateSetting> getTemplateSettings(TemplateSetting templateSettingFilter)
			throws MiddlewareQueryException;

	/**
	 * Return all standard presets
	 *
	 * @return
	 * @throws MiddlewareQueryException
	 */
	List<StandardPreset> getAllStandardPresets() throws MiddlewareQueryException;

	/**
	 * Return all standard preset specified by crop name
	 *
	 * @param cropName
	 * @return
	 * @throws MiddlewareQueryException
	 */
	List<StandardPreset> getStandardPresetByCrop(String cropName) throws MiddlewareQueryException;

	/**
	 * Returns all standard preset specified by crop,tool,and tool_section
	 *
	 * @param toolId
	 * @param cropName
	 * @param toolSection
	 * @return
	 * @throws MiddlewareQueryException
	 */
	List<StandardPreset> getStandardPresetByCrop(int toolId, String cropName, String toolSection)
			throws MiddlewareQueryException;

	List<StandardPreset> getStandardPresetByCropAndPresetName(String presetName, int toolId,
			String cropName,
			String toolSection) throws MiddlewareQueryException;

	/**
	 * Returns the specific fieldbook web tool
	 *
	 * @return
	 * @throws MiddlewareQueryException
	 */
	Tool getFieldbookWebTool() throws MiddlewareQueryException;

	/**
	 * Returns the standard preset specified by id
	 *
	 * @param id
	 * @return
	 * @throws MiddlewareQueryException
	 */
	StandardPreset getStandardPresetById(Integer id) throws MiddlewareQueryException;

	/**
	 * Save new or update existing standard preset
	 *
	 * @param preset
	 * @return
	 * @throws MiddlewareQueryException
	 */
	StandardPreset saveOrUpdateStandardPreset(StandardPreset preset)
			throws MiddlewareQueryException;

	/**
	 * Adds the template setting.
	 *
	 * @param templateSetting the template setting
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	Integer addTemplateSetting(TemplateSetting templateSetting) throws MiddlewareQueryException;

	/**
	 * Update template setting.
	 *
	 * @param templateSetting the template setting
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	void updateTemplateSetting(TemplateSetting templateSetting) throws MiddlewareQueryException;

	/**
	 * Delete template setting.
	 *
	 * @param templateSettingId the template setting id
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	void deleteTemplateSetting(Integer templateSettingId) throws MiddlewareQueryException;

	/**
	 * Gets the current ibdb user id.
	 *
	 * @param projectId       the project id
	 * @param workbenchUserId the current workbench user id
	 * @return the current ibdb user id
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	Integer getCurrentIbdbUserId(Long projectId, Integer workbenchUserId)
			throws MiddlewareQueryException;

	/**
	 * Gets the project by id.
	 *
	 * @param projectId the project id
	 * @return the project by id
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	Project getProjectById(Long projectId) throws MiddlewareQueryException;
}
