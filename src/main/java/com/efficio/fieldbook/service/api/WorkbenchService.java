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

package com.efficio.fieldbook.service.api;

import java.util.List;

import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.Tool;

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
	 */
	Tool getToolWithName(String toolName);

	/**
	 * Return all standard presets
	 *
	 * @return
	 */
	List<StandardPreset> getAllStandardPresets();

	/**
	 * Return all standard preset specified by crop name
	 *
	 * @param cropName
	 * @return
	 */
	List<StandardPreset> getStandardPresetByCrop(String cropName);

	/**
	 * Returns all standard preset specified by crop,tool,and tool_section
	 *
	 * @param toolId
	 * @param cropName
	 * @param toolSection
	 * @return
	 */
	List<StandardPreset> getStandardPresetByCrop(int toolId, String cropName, String toolSection);

	List<StandardPreset> getStandardPresetByCropAndPresetName(String presetName, int toolId, String cropName, String toolSection);

	/**
	 * Returns the specific fieldbook web tool
	 *
	 * @return
	 */
	Tool getFieldbookWebTool();

	/**
	 * Returns the standard preset specified by id
	 *
	 * @param id
	 * @return
	 */
	StandardPreset getStandardPresetById(Integer id);

	/**
	 * Save new or update existing standard preset
	 *
	 * @param preset
	 * @return
	 */
	StandardPreset saveOrUpdateStandardPreset(StandardPreset preset);

	/**
	 * Gets the current ibdb user id.
	 *
	 * @param projectId the project id
	 * @param workbenchUserId the current workbench user id
	 * @return the current ibdb user id
	 */
	Integer getCurrentIbdbUserId(Long projectId, Integer workbenchUserId);

	/**
	 * Gets the project by id.
	 *
	 * @param projectId the project id
	 * @return the project by id
	 */
	Project getProjectById(Long projectId);

}
