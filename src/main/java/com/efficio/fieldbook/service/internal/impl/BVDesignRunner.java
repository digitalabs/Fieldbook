package com.efficio.fieldbook.service.internal.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.service.internal.DesignRunner;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;

//@Component("BVDesignRunner")
public class BVDesignRunner implements DesignRunner {
	
	private static final Logger LOG = LoggerFactory.getLogger(BVDesignRunner.class);
	
    private static String XML_EXTENSION = ".xml";
    private static String CSV_EXTENSION = ".csv";
	private static String BV_PREFIX = "-bv";
	private static String BREEDING_VIEW_EXE = "BreedingView.exe";
	private static String BVDESIGN_EXE = "BVDesign.exe";
	private static String OUTPUT_FILE_PARAMETER_NAME = "outputfile";
	
	@Override
	public BVDesignOutput runBVDesign(WorkbenchService workbenchService, FieldbookProperties fieldbookProperties, MainDesign design) throws IOException{
		
		String bvDesignLocation = getBreedingViewExeLocation(workbenchService);
		int returnCode = -1;
		if(bvDesignLocation != null && design != null && design.getDesign() != null){
			 String outputFilePath = System.currentTimeMillis()+BV_PREFIX+CSV_EXTENSION;
			 
			 design.getDesign().setParameterValue(OUTPUT_FILE_PARAMETER_NAME, outputFilePath);
			 
			 String xml = "";
			 try {
				 xml = ExpDesignUtil.getXmlStringForSetting(design);
			 } catch (JAXBException e ) {
				 LOG.error(e.getMessage(), e);
			 }
			 
			 String filepath = writeToFile(xml, fieldbookProperties);

			 
			 ProcessBuilder pb = new ProcessBuilder(bvDesignLocation, "-i" + filepath);
	         Process p = pb.start();
	         try {
	        	InputStreamReader isr = new  InputStreamReader(p.getInputStream());
        	    BufferedReader br = new BufferedReader(isr);

        	    String lineRead;
        	    while ((lineRead = br.readLine()) != null) {
        	    	LOG.debug(lineRead);
        	    }

        	    returnCode = p.waitFor();        	    
        	    //add here the code to parse the csv file
			} catch (InterruptedException e) {
				LOG.error(e.getMessage(), e);
			}finally {
				  if(p != null){	
					    //missing these was causing the mass amounts of open 'files'
					    p.getInputStream().close();
					    p.getOutputStream().close();
					    p.getErrorStream().close(); 
				  }
			}
		}
		BVDesignOutput output = new BVDesignOutput(returnCode);
		if(returnCode == 0){
			 
			 File outputFile = new File(design.getDesign().getParameterValue(OUTPUT_FILE_PARAMETER_NAME));
			 FileReader fileReader = new FileReader(outputFile);
			 CSVReader reader = new CSVReader(fileReader);
			 List<String[]> myEntries = reader.readAll();			 			 
			 output.setResults(myEntries);	
			 fileReader.close();
			 reader.close();
			 outputFile.delete();
			 
		}
		return output;
	}
	
	private static String getBreedingViewExeLocation(WorkbenchService workbenchService){
		String bvDesignLocation = null;
		Tool bvTool = null;
		try {
			bvTool = workbenchService.getToolWithName(AppConstants.TOOL_NAME_BREEDING_VIEW.getString());
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
		if(bvTool != null){
			 //write xml to temp file
			 File absoluteToolFile = new File(bvTool.getPath()).getAbsoluteFile();
			 bvDesignLocation = absoluteToolFile.getAbsolutePath().replaceAll(BREEDING_VIEW_EXE, BVDESIGN_EXE);
		}
		return bvDesignLocation;
	}
	
	private static String writeToFile(String xml, FieldbookProperties fieldbookProperties){
    	String filenamePath = generateBVFilePath(XML_EXTENSION, fieldbookProperties);
        try {
        	
          File file = new File(filenamePath);
          BufferedWriter output = new BufferedWriter(new FileWriter(file));
          output.write(xml);
          output.close();
          filenamePath = file.getAbsolutePath();
        } catch ( IOException e ) {
           LOG.error(e.getMessage(), e);
        }
        return filenamePath;
	}
	
	private static String generateBVFilePath(String extensionFilename, FieldbookProperties fieldbookProperties){
		String filename = generateBVFileName(extensionFilename);
    	String filenamePath = fieldbookProperties.getUploadDirectory() + File.separator + filename;
    	File f = new File(filenamePath);
    	return f.getAbsolutePath();
	}
	
	private static String generateBVFileName(String extensionFileName){
		return System.currentTimeMillis()+BV_PREFIX+extensionFileName;
	}

}
