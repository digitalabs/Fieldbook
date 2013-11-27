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
package com.efficio.fieldbook.web.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.generationcp.commons.util.StringUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolType;
import org.generationcp.middleware.pojos.workbench.WorkbenchSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

import com.efficio.fieldbook.web.trial.controller.ManageTrialController;

public class ToolUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ToolUtil.class);
   
    /**
     * Launch the specified native tool.
     * 
     * @param tool
     * @return the {@link Process} object created when the tool was launched
     * @throws IOException
     *             if an I/O error occurs while trying to launch the tool
     * @throws IllegalArgumentException
     *             if the specified Tool's type is not {@link ToolType#NATIVE}
     */
    public Process launchNativeTool(String abolutePath, String parameter) throws IOException {
        /*
        if (tool.getToolType() != ToolType.NATIVE) {
            throw new IllegalArgumentException("Tool must be a native tool");
        }
        */
        //we close the app first
        closeNativeTool(abolutePath);               
        
        File absoluteToolFile = new File(abolutePath).getAbsoluteFile();
        /*
        String parameter = "";
        if (!StringUtil.isEmpty(tool.getParameter())) {
            parameter = tool.getParameter();
        }
        */
        ProcessBuilder pb = new ProcessBuilder(absoluteToolFile.getAbsolutePath(), parameter);
        pb.directory(absoluteToolFile.getParentFile());
        return pb.start();
    }   
    
    public void closeNativeTool(String abolutePath) throws IOException {
        /*
        if (tool.getToolType() != ToolType.NATIVE) {
            throw new IllegalArgumentException("Tool must be a native tool");
        }*/

        File absoluteToolFile = new File(abolutePath).getAbsoluteFile();
        String[] pathTokens = absoluteToolFile.getAbsolutePath().split(
                                                                       "\\" + File.separator);

        String executableName = pathTokens[pathTokens.length - 1];

        // taskkill /T /F /IM <exe name>
        ProcessBuilder pb = new ProcessBuilder("taskkill", "/T", "/F", "/IM",
                                               executableName);
        pb.directory(absoluteToolFile.getParentFile());

        Process process = pb.start();
        try {
            process.waitFor();
        }
        catch (InterruptedException e) {
            LOG.error("Interrupted while waiting for " + abolutePath + " to stop.");
        }
    }
}
