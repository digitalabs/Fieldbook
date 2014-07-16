package com.efficio.fieldbook.web.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.controller.ImportStudyController;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesign;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesignParameter;
import com.efficio.fieldbook.web.trial.bean.xml.ListItem;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;

public class ExpDesignUtil {
	private static String XML_EXTENSION = ".xml";
	private static String CSV_EXTENSION = ".csv";
	private static String BV_PREFIX = "-bv";
	private static String BREEDING_VIEW_EXE = "BreedingView.exe";
	private static String BVDESIGN_EXE = "BVDesign.exe";
	private static String OUTPUT_FILE_PARAMETER_NAME = "outputfile";
	private static String RESOLVABLE_INCOMPLETE_BLOCK_DESIGN = "ResolvableIncompleteBlock";
	private static String RESOLVABLE_ROW_COL_DESIGN = "ResolvableRowColumn";
	
	private static final Logger LOG = LoggerFactory.getLogger(ExpDesignUtil.class);
	
	public static BVDesignOutput runBVDesign(WorkbenchService workbenchService, FieldbookProperties fieldbookProperties, MainDesign design) throws JAXBException, IOException{
		
		String bvDesignLocation = getBreedingViewExeLocation(workbenchService);
		int returnCode = -1;
		if(bvDesignLocation != null && design != null && design.getDesign() != null){
			 String outputFilePath =  generateBVFilePath(CSV_EXTENSION, fieldbookProperties);
			 
			 design.getDesign().setParameterValue(OUTPUT_FILE_PARAMETER_NAME, outputFilePath);
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

        	    returnCode = p.waitFor();        	    
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
		BVDesignOutput output = new BVDesignOutput(returnCode);
		if(returnCode == 0){
			 CSVReader reader = new CSVReader(new FileReader(design.getDesign().getParameterValue(OUTPUT_FILE_PARAMETER_NAME)));
			 List myEntries = reader.readAll();			 			 
			 output.setResults(myEntries);			
		}
		return output;
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
	
	public static ExpDesignParameter createExpDesignParameter(String name, String value, List<ListItem> items){
		ExpDesignParameter designParam = new ExpDesignParameter(name, value);
		if(items != null && !items.isEmpty()){
			designParam.setListItem(items);
		}
		return designParam;
	}
	public static MainDesign createResolvableIncompleteBlockDesign(String blockSize, String nTreatments,
			String nReplicates, String treatmentFactor, String replicateFactor, String blockFactor,
			String plotFactor, String nBlatin, String replatingGroups, String timeLimit, String outputfile){
		
		List<ExpDesignParameter> paramList = new ArrayList<ExpDesignParameter>();
		paramList.add(createExpDesignParameter("blocksize", blockSize, null));
		paramList.add(createExpDesignParameter("ntreatments", nTreatments, null));
		paramList.add(createExpDesignParameter("nreplicates", nReplicates, null));
		paramList.add(createExpDesignParameter("treatmentfactor", treatmentFactor, null));
		paramList.add(createExpDesignParameter("replicatefactor", replicateFactor, null));
		paramList.add(createExpDesignParameter("blockfactor", blockFactor, null));
		paramList.add(createExpDesignParameter("plotfactor", plotFactor, null));				
		paramList.add(createExpDesignParameter("nblatin", nBlatin, null));		
		paramList.add(createExpDesignParameter("timelimit", timeLimit, null));
		paramList.add(createExpDesignParameter("outputfile", outputfile, null));
		
		ExpDesign design = new ExpDesign(RESOLVABLE_INCOMPLETE_BLOCK_DESIGN, paramList);
		MainDesign mainDesign = new MainDesign(design);
		return mainDesign;
	}
	
	public static MainDesign createResolvableRowColDesign(String nTreatments,
			String nReplicates, String nRows, String nColumns, String treatmentFactor, String replicateFactor, 
			String rowFactor, String columnFactor,String plotFactor,
			String nrLatin, String ncLatin, String replatingGroups, String timeLimit, String outputfile){
		
		List<ExpDesignParameter> paramList = new ArrayList<ExpDesignParameter>();
		paramList.add(createExpDesignParameter("ntreatments", nTreatments, null));
		paramList.add(createExpDesignParameter("nreplicates", nReplicates, null));
		paramList.add(createExpDesignParameter("nrows", nRows, null));
		paramList.add(createExpDesignParameter("ncolumns", nColumns, null));
		paramList.add(createExpDesignParameter("treatmentfactor", treatmentFactor, null));
		paramList.add(createExpDesignParameter("replicatefactor", replicateFactor, null));
		paramList.add(createExpDesignParameter("rowfactor", rowFactor, null));
		paramList.add(createExpDesignParameter("columnfactor", columnFactor, null));
		paramList.add(createExpDesignParameter("plotfactor", plotFactor, null));
		paramList.add(createExpDesignParameter("nrlatin", nrLatin, null));
		paramList.add(createExpDesignParameter("nclatin", ncLatin, null));
		paramList.add(createExpDesignParameter("timelimit", timeLimit, null));
		paramList.add(createExpDesignParameter("outputfile", outputfile, null));
		
		
	
		ExpDesign design = new ExpDesign(RESOLVABLE_ROW_COL_DESIGN, paramList);
		MainDesign mainDesign = new MainDesign(design);
		return mainDesign;
	}
}
