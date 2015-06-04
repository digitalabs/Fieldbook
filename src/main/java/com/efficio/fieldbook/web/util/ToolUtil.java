/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.util;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ToolUtil.
 */
public class ToolUtil {

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(ToolUtil.class);

	/**
	 * Launch the specified native tool.
	 *
	 * @param abolutePath the abolute path
	 * @param parameter the parameter
	 * @return the {@link Process} object created when the tool was launched
	 * @throws IOException if an I/O error occurs while trying to launch the tool
	 */
	public Process launchNativeTool(String abolutePath, String parameter) throws IOException {
		// we close the app first
		this.closeNativeTool(abolutePath);

		File absoluteToolFile = new File(abolutePath).getAbsoluteFile();
		ProcessBuilder pb = new ProcessBuilder(absoluteToolFile.getAbsolutePath(), parameter);
		pb.directory(absoluteToolFile.getParentFile());
		return pb.start();
	}

	/**
	 * Close native tool.
	 *
	 * @param abolutePath the abolute path
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void closeNativeTool(String abolutePath) throws IOException {
		File absoluteToolFile = new File(abolutePath).getAbsoluteFile();
		String[] pathTokens = absoluteToolFile.getAbsolutePath().split("\\" + File.separator);

		String executableName = pathTokens[pathTokens.length - 1];

		// taskkill /T /F /IM <exe name>
		ProcessBuilder pb = new ProcessBuilder("taskkill", "/T", "/F", "/IM", executableName);
		pb.directory(absoluteToolFile.getParentFile());

		Process process = pb.start();
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			ToolUtil.LOG.error("Interrupted while waiting for " + abolutePath + " to stop.");
		}
	}
}
