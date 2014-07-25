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
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
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
	private static String RANDOMIZED_COMPLETE_BLOCK_DESIGN = "RandomizedBlock";
	private static String RESOLVABLE_INCOMPLETE_BLOCK_DESIGN = "ResolvableIncompleteBlock";
	private static String RESOLVABLE_ROW_COL_DESIGN = "ResolvableRowColumn";
	
	
	private static final Logger LOG = LoggerFactory.getLogger(ExpDesignUtil.class);
	
	public static BVDesignOutput runBVDesign(WorkbenchService workbenchService, FieldbookProperties fieldbookProperties, MainDesign design) throws JAXBException, IOException{
		
		String bvDesignLocation = getBreedingViewExeLocation(workbenchService);
		int returnCode = -1;
		if(bvDesignLocation != null && design != null && design.getDesign() != null){
			 String outputFilePath = System.currentTimeMillis()+BV_PREFIX+CSV_EXTENSION; //generateBVFilePath(CSV_EXTENSION, fieldbookProperties);
			 
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
	
	public static MainDesign createRandomizedCompleteBlockDesign(String nBlock, String blockFactor, String plotFactor,
			List<String> treatmentFactor, List<String> levels, String timeLimit, String outputfile){
		
		timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();
		
		List<ExpDesignParameter> paramList = new ArrayList<ExpDesignParameter>();
		paramList.add(createExpDesignParameter("nblocks", nBlock, null));
		paramList.add(createExpDesignParameter("blockfactor", blockFactor, null));
		paramList.add(createExpDesignParameter("plotfactor", plotFactor, null));
		List<ListItem> itemsTreatmentFactor = new ArrayList<ListItem>();
		List<ListItem> itemsLevels = new ArrayList<ListItem>();
		if(treatmentFactor != null){
			for(String treatment : treatmentFactor){
				ListItem listItem = new ListItem(treatment);
				itemsTreatmentFactor.add(listItem);
			}
		}
		if(levels != null){
			for(String level : levels){
				ListItem listItem = new ListItem(level);
				itemsLevels.add(listItem);
			}
		}
		paramList.add(createExpDesignParameter("treatmentfactors", null, itemsTreatmentFactor));		
		paramList.add(createExpDesignParameter("levels", null,itemsLevels));
				
		paramList.add(createExpDesignParameter("timelimit", timeLimit, null));
		paramList.add(createExpDesignParameter("outputfile", outputfile, null));
		
		ExpDesign design = new ExpDesign(RANDOMIZED_COMPLETE_BLOCK_DESIGN, paramList);
		MainDesign mainDesign = new MainDesign(design);
		return mainDesign;
	}
	
	public static MainDesign createResolvableIncompleteBlockDesign(String blockSize, String nTreatments,
			String nReplicates, String treatmentFactor, String replicateFactor, String blockFactor,
			String plotFactor, String nBlatin, String replatingGroups, String timeLimit, String outputfile, boolean useLatinize){
		
		timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();
		
		List<ExpDesignParameter> paramList = new ArrayList<ExpDesignParameter>();
		paramList.add(createExpDesignParameter("blocksize", blockSize, null));
		paramList.add(createExpDesignParameter("ntreatments", nTreatments, null));
		paramList.add(createExpDesignParameter("nreplicates", nReplicates, null));
		paramList.add(createExpDesignParameter("treatmentfactor", treatmentFactor, null));
		paramList.add(createExpDesignParameter("replicatefactor", replicateFactor, null));
		paramList.add(createExpDesignParameter("blockfactor", blockFactor, null));
		paramList.add(createExpDesignParameter("plotfactor", plotFactor, null));
		if(useLatinize){
			paramList.add(createExpDesignParameter("nblatin", nBlatin, null));
			//we add the string tokenize replating groups
			//we tokenize the replating groups
			StringTokenizer tokenizer = new StringTokenizer(replatingGroups, ",");
			List<ListItem> replatingList = new ArrayList<ListItem>();
			while(tokenizer.hasMoreTokens()){
				replatingList.add(new ListItem(tokenizer.nextToken()));
			}
			paramList.add(createExpDesignParameter("replatingroups", null, replatingList));
		}else{
			paramList.add(createExpDesignParameter("nblatin", "0", null));
		}
		
		paramList.add(createExpDesignParameter("timelimit", timeLimit, null));
		paramList.add(createExpDesignParameter("outputfile", outputfile, null));
		
		ExpDesign design = new ExpDesign(RESOLVABLE_INCOMPLETE_BLOCK_DESIGN, paramList);
		MainDesign mainDesign = new MainDesign(design);
		return mainDesign;
	}
	
	public static MainDesign createResolvableRowColDesign(String nTreatments,
			String nReplicates, String nRows, String nColumns, String treatmentFactor, String replicateFactor, 
			String rowFactor, String columnFactor,String plotFactor,
			String nrLatin, String ncLatin, String replatingGroups, String timeLimit, String outputfile, Boolean useLatinize){
		//we override the timelimit from the propfile
		
		timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();
		
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
		if(useLatinize != null && useLatinize.booleanValue()){
			paramList.add(createExpDesignParameter("nrlatin", nrLatin, null));
			paramList.add(createExpDesignParameter("nclatin", ncLatin, null));
			//we tokenize the replating groups
			StringTokenizer tokenizer = new StringTokenizer(replatingGroups, ",");
			List<ListItem> replatingList = new ArrayList<ListItem>();
			while(tokenizer.hasMoreTokens()){
				replatingList.add(new ListItem(tokenizer.nextToken()));
			}
			paramList.add(createExpDesignParameter("replatingroups", null, replatingList));
		}else{
			paramList.add(createExpDesignParameter("nrlatin", "0", null));
			paramList.add(createExpDesignParameter("nclatin", "0", null));
		}
		paramList.add(createExpDesignParameter("timelimit", timeLimit, null));
		paramList.add(createExpDesignParameter("outputfile", outputfile, null));
		
		
	
		ExpDesign design = new ExpDesign(RESOLVABLE_ROW_COL_DESIGN, paramList);
		MainDesign mainDesign = new MainDesign(design);
		return mainDesign;
	}
	
	public static MeasurementVariable convertStandardVariableToMeasurementVariable(StandardVariable var, Operation operation) {
        MeasurementVariable mvar = new MeasurementVariable(
        		var.getName(), var.getDescription(), var.getScale().getName(), var.getMethod().getName(), var.getProperty().getName(), var.getDataType().getName(), null,
        		var.getPhenotypicType().getLabelList().get(0));
        mvar.setFactor(true);
        mvar.setOperation(operation);
        mvar.setStoredIn(var.getStoredIn().getId());
        mvar.setTermId(var.getId());        
        return mvar;
    }

	
	public static MeasurementRow createMeasurementRow(List<MeasurementVariable> headerVariable, 
			ImportedGermplasm germplasm, Map<String, String> bvEntryMap, Map<String, List<String>> treatmentFactorValues, 
			List<MeasurementVariable> trialVariables, int trialNo, List<MeasurementVariable> factors)
	throws MiddlewareQueryException {
		MeasurementRow measurementRow = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<MeasurementData>();
		MeasurementData treatmentLevelData = null;
		MeasurementData measurementData = null;		
        
        MeasurementVariable trialInstanceVar = 
                WorkbookUtil.getMeasurementVariable(trialVariables, TermId.TRIAL_INSTANCE_FACTOR.getId());
        measurementData = new MeasurementData(trialInstanceVar.getName(), Integer.toString(trialNo), false, 
                trialInstanceVar.getDataType(), trialInstanceVar);
        dataList.add(measurementData);
	
		
		for(MeasurementVariable var : headerVariable){
			
				measurementData =null;
				
				
				Integer termId = var.getTermId();											
				
				if(termId.intValue() == TermId.ENTRY_NO.getId())
					measurementData = new MeasurementData(var.getName(), bvEntryMap.get(var.getName()), false, var.getDataType(), var);
				else if(termId.intValue() == TermId.SOURCE.getId())
					measurementData = new MeasurementData(var.getName(), "", false, var.getDataType(), var);
				else if(termId.intValue() == TermId.CROSS.getId())	
					measurementData = new MeasurementData(var.getName(), germplasm.getCross(), false, var.getDataType(), var);
				else if(termId.intValue() == TermId.DESIG.getId())	
					measurementData = new MeasurementData(var.getName(), germplasm.getDesig(), false, var.getDataType(), var);
				else if(termId.intValue() == TermId.GID.getId()){	    					
					measurementData = new MeasurementData(var.getName(), germplasm.getGid(), false, var.getDataType(), var);
				}else if(termId.intValue() == TermId.ENTRY_CODE.getId())	    					
					measurementData = new MeasurementData(var.getName(), germplasm.getEntryCode(), false, var.getDataType(), var);
				else if(termId.intValue() == TermId.PLOT_NO.getId())
					measurementData = new MeasurementData(var.getName(), bvEntryMap.get(var.getName()), false, var.getDataType(), var);
				else if(termId.intValue() == TermId.CHECK.getId()){
					measurementData = new MeasurementData(var.getName(), Integer.toString(germplasm.getCheckId()), 
			    							false, var.getDataType(), germplasm.getCheckId(), var);
					
				} else if (termId.intValue() == TermId.REP_NO.getId()) {
					measurementData = new MeasurementData(var.getName(), bvEntryMap.get(var.getName()), false, var.getDataType(), var);
					
				} else if (termId.intValue() == TermId.BLOCK_NO.getId()) {
					measurementData = new MeasurementData(var.getName(), bvEntryMap.get(var.getName()), false, var.getDataType(), var);
					
				} else if (termId.intValue() == TermId.ROW.getId()) {
					measurementData = new MeasurementData(var.getName(), bvEntryMap.get(var.getName()), false, var.getDataType(), var);
					
				} else if (termId.intValue() == TermId.COL.getId()) {
					measurementData = new MeasurementData(var.getName(), bvEntryMap.get(var.getName()), false, var.getDataType(), var);
					
				} else if (termId.intValue() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					measurementData = new MeasurementData(var.getName(), Integer.toString(trialNo), false, 
							var.getDataType(), var);
					
				} else if (var.getTreatmentLabel() != null && !"".equals(var.getTreatmentLabel())) {
					if (treatmentLevelData == null){
						measurementData = new MeasurementData(var.getName(), bvEntryMap.get(cleanBVDesingKey(Integer.toString(var.getTermId()))), false, var.getDataType(), var);
						treatmentLevelData = measurementData;
					} else {
						String level = treatmentLevelData.getValue();
						if (NumberUtils.isNumber(level)) {
							int index = Integer.valueOf(level) - 1;
							if (treatmentFactorValues != null && treatmentFactorValues.containsKey(String.valueOf(treatmentLevelData.getMeasurementVariable().getTermId()))) {
								String value = treatmentFactorValues.get(String.valueOf(treatmentLevelData.getMeasurementVariable().getTermId())).get(index);
								measurementData = new MeasurementData(var.getName(), value, false, var.getDataType(), var);
							}
						}
						treatmentLevelData = null;
					}
					
				}else{
					//meaning non factor
                	measurementData = new MeasurementData(var.getName(), "", true, var.getDataType(), var);
                	var.setFactor(false);
				}
				
				
				dataList.add(measurementData); 
		}
		measurementRow.setDataList(dataList);
		return measurementRow;
	}
	
	public static List<MeasurementRow> generateExpDesignMeasurements(int environments, 
			List<MeasurementVariable> trialVariables, List<MeasurementVariable> factors, List<MeasurementVariable> nonTrialFactors, List<MeasurementVariable> variates, 
			List<TreatmentVariable> treatmentVariables, List<StandardVariable> requiredExpDesignVariable, 
			List<ImportedGermplasm> germplasmList, MainDesign mainDesign, WorkbenchService workbenchService, 
			FieldbookProperties fieldbookProperties, String entryNumberIdentifier, Map<String, List<String>> treatmentFactorValues) 
					throws JAXBException, IOException, MiddlewareQueryException, BVDesignException{
		List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();
		List<MeasurementVariable> varList = new ArrayList<MeasurementVariable>();			
		varList.addAll(nonTrialFactors);
		for(StandardVariable var : requiredExpDesignVariable){
			if(WorkbookUtil.getMeasurementVariable(nonTrialFactors, var.getId()) == null){
				MeasurementVariable measureVar = ExpDesignUtil.convertStandardVariableToMeasurementVariable(var, Operation.ADD);
				varList.add(measureVar);
				if(WorkbookUtil.getMeasurementVariable(factors, var.getId()) == null){
					factors.add(measureVar);
				}
			}		
		}
		
		if(treatmentVariables != null){
			for(int i = 0 ; i < treatmentVariables.size() ; i++){
				varList.add(treatmentVariables.get(i).getLevelVariable());
				varList.add(treatmentVariables.get(i).getValueVariable());
			}
		}
		varList.addAll(variates);
		
		
		for(int i = 1 ; i <= environments ; i++){
			int trialNo = i;
			BVDesignOutput bvOutput = ExpDesignUtil.runBVDesign(workbenchService, fieldbookProperties, mainDesign);
			
			if(bvOutput.isSuccess()){
				for(int counter = 0 ; counter < bvOutput.getBvResultList().size() ; counter++){
					String entryNo = bvOutput.getEntryValue(entryNumberIdentifier, counter);
					if(NumberUtils.isNumber(entryNo)){
						int germplasmIndex = Integer.valueOf(entryNo) - 1;
						if(germplasmIndex >= 0 && germplasmIndex < germplasmList.size()){
							ImportedGermplasm importedGermplasm = germplasmList.get(germplasmIndex);
							MeasurementRow row = createMeasurementRow(varList, importedGermplasm, bvOutput.getEntryMap(counter), treatmentFactorValues, trialVariables, trialNo, factors);
							measurementRowList.add(row);
						}
					}
				}
			}else{
				throw new BVDesignException("experiment.design.generate.generic.error");
			}
			
		}
		return measurementRowList;
	}
	
	public static String cleanBVDesingKey(String key){
		if(key != null){
			return "_"+key.replace("-", "_");			
		}
		return key;
	}
}
