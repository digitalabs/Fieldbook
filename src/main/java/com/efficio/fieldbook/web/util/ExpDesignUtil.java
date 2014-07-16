package com.efficio.fieldbook.web.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.controller.ImportStudyController;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;

public class ExpDesignUtil {
	private static String XML_EXTENSION = ".xml";
	private static String CSV_EXTENSION = ".csv";
	private static String BV_PREFIX = "-bv";
	private static String BREEDING_VIEW_EXE = "BreedingView.exe";
	private static String BVDESIGN_EXE = "BVDesign.exe";
	
	private static final Logger LOG = LoggerFactory.getLogger(ExpDesignUtil.class);
	
	public static void runBVDesign(WorkbenchService workbenchService, FieldbookProperties fieldbookProperties, MainDesign design) throws JAXBException, IOException{
		
		String bvDesignLocation = getBreedingViewExeLocation(workbenchService);
		if(bvDesignLocation != null && design != null && design.getDesign() != null){
			 String outputFilePath =  generateBVFilePath(CSV_EXTENSION, fieldbookProperties);
			 
			 design.getDesign().setParameterValue("outputfile", outputFilePath);
			 String xml = getXmlStringForSetting(design);
			 String filepath = writeToFile(xml, fieldbookProperties);

			 
			 ProcessBuilder pb = new ProcessBuilder(bvDesignLocation, "-i"+filepath);
	         Process p = pb.start();
	         try {
	        	InputStreamReader isr = new  InputStreamReader(p.getInputStream());
        	    BufferedReader br = new BufferedReader(isr);

        	    String lineRead;
        	    while ((lineRead = br.readLine()) != null) {
        	        // swallow the line, or print it out - System.out.println(lineRead);
        	    	LOG.debug(lineRead);
        	    }

        	    int rc = p.waitFor();
        	    //add here the code to parse the csv file
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				  try {
					  if(p != null){	
						    //missing these was causing the mass amounts of open 'files'
						    p.getInputStream().close();
						    p.getOutputStream().close();
						    p.getErrorStream().close(); 
					  }
				  } catch (Exception ioe) {
				    LOG.error(ioe.getMessage());
				  }
				}
		}
	}
	private static String getBreedingViewExeLocation(WorkbenchService workbenchService){
		String bvDesignLocation = null;
		Tool bvTool = null;
		try {
			bvTool = workbenchService.getToolWithName(AppConstants.TOOL_NAME_BREEDING_VIEW.getString());
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
		if(bvTool != null){
			 //write xml to temp file
			 File absoluteToolFile = new File(bvTool.getPath()).getAbsoluteFile();
			 bvDesignLocation = absoluteToolFile.getAbsolutePath().replaceAll(BREEDING_VIEW_EXE, BVDESIGN_EXE);
		}
		return bvDesignLocation;
	}
	private static String generatBVFileName(String extensionFileName){
		String filename = System.currentTimeMillis()+BV_PREFIX+extensionFileName;
		return filename;
	}
	private static String generateBVFilePath(String extensionFilename, FieldbookProperties fieldbookProperties){
		String filename = generatBVFileName(extensionFilename);
    	String filenamePath = fieldbookProperties.getUploadDirectory() + File.separator + filename;
    	File f = new File(filenamePath);
    	return f.getAbsolutePath();
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
           e.printStackTrace();
        }
        return filenamePath;
	}
	
	public static String getXmlStringForSetting(MainDesign mainDesign) throws JAXBException{
		JAXBContext context = JAXBContext.newInstance(MainDesign.class);
        Marshaller marshaller = context.createMarshaller();
        StringWriter writer = new StringWriter();
        marshaller.marshal(mainDesign, writer);
        return writer.toString();
    }
	
	public static MainDesign readXmlStringForSetting(String xmlString) throws JAXBException{
		JAXBContext context = JAXBContext.newInstance(MainDesign.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		MainDesign mainDesign = (MainDesign) unmarshaller.unmarshal(new StringReader(xmlString));
        return mainDesign;
	}
}
