package com.efficio.fieldbook.web.util;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.Assert;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.workbench.settings.Condition;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.pojos.workbench.settings.Factor;
import org.generationcp.middleware.pojos.workbench.settings.Variate;
import org.generationcp.middleware.util.Debug;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.service.RandomizeCompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableIncompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableRowColumnDesignService;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesign;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesignParameter;
import com.efficio.fieldbook.web.trial.bean.xml.ListItem;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.mchange.util.AssertException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class ExpDesignTest extends AbstractJUnit4SpringContextTests{
	
	@Autowired
    private WorkbenchService workbenchService;
	@Autowired
    private FieldbookProperties fieldbookProperties;
	@Autowired
    private ResolvableIncompleteBlockDesignService resolveIncompleteBlockDesign;
	@Autowired
    private ResolvableRowColumnDesignService resolveRowColumn;
	@Autowired
    private RandomizeCompleteBlockDesignService randomizeBlockDesign;
	@Resource
	public org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	
	public List<ExpDesignParameter> createResolvableIncompleteBlockParameterList(boolean hasReplatingGroup){
		/*
<Parameter name="blocksize" value="6"/>
<Parameter name="ntreatments" value="24"/>
<Parameter name="nreplicates" value="2"/>
<Parameter name="treatmentfactor" value="Treat"/>
<Parameter name="replicatefactor" value="Reps"/>
<Parameter name="blockfactor" value="Subblocks"/>
<Parameter name="plotfactor" value="Plots"/>
<Parameter name="nblatin" value="0"/>
<Parameter name="replatingroups" value="0"/>
<Parameter name="timelimit" value="1"/>
<Parameter name="outputfile" value="c:/documents/output.csv"/>

<Templates>
<Template name=" ResolvableIncompleteBlock">
<Parameter name="seed" value="146207"/>
<Parameter name="nreplicates" value="3"/>
<Parameter name="blocksize" value="4"/>
<Parameter name="ntreatments" value="24"/>
<Parameter name="replicatefactor" value="replicates"/>
<Parameter name="blockfactor" value="blocks"/>
<Parameter name="plotfactor" value="Plots"/>
<Parameter name="treatmentfactor" value="genotypes"/>
<Parameter name="nblatin" value="1"/>
<Parameter name="replatingroups">
<ListItem value="2"/>
<ListItem value="1"/>
</Parameter>
<Parameter name="outputfile" value="c:/documents/output.csv"/>
</Template>
</Templates>
		 */
		List<ExpDesignParameter> paramList = new ArrayList();
		paramList.add(ExpDesignUtil.createExpDesignParameter("blocksize", "6", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter("ntreatments", "24", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter("nreplicates", "2", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter("treatmentfactor", "Treat", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter("replicatefactor", "Reps", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter("blockfactor", "Subblocks", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter("plotfactor", "Plots", null));
		
		//paramList.add(createExpDesignParameter("plotwithinblockfactor", "Plots", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter("nblatin", "0", null));
		
		/*
		ExpDesignParameter param = createExpDesignParameter("replatingroups", "0", null);
		
		if(hasReplatingGroup){
			List<ListItem> items = new ArrayList();
			items.add(new ListItem("2"));
			items.add(new ListItem("1"));
			param = createExpDesignParameter("replatingroups", "0", items);
			param.setValue(null);
		}
		
		
		paramList.add(param);
		*/
		paramList.add(ExpDesignUtil.createExpDesignParameter("timelimit", "1", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter("outputfile", "c:/documents/output.csv", null));
		return paramList;
	}
	
	
	public List<ExpDesignParameter> createResolvableRowColumnParameterList(boolean hasReplatingGroup){
		/*
<Templates>
<Template name="ResolvableRowColumn">
<Parameter name="ntreatments" value="25"/>
<Parameter name="nreplicates" value="2"/>
<Parameter name="nrows" value="5"/>
<Parameter name="ncolumns" value="10"/>
<Parameter name="treatmentfactor" value="Treat"/>
<Parameter name="replicatefactor" value="Reps"/>
<Parameter name="rowfactor" value="Rows"/>
<Parameter name="columnfactor" value="Columns"/>
<Parameter name="nrlatin" value="0"/>
<Parameter name="nclatin" value="0"/>
<Parameter name="replatingroups" value="0"/>
<Parameter name="timelimit" value="1"/>
<Parameter name="outputfile" value="c:/documents/output.csv"/>
</Template>
</Templates> 

<Templates>
<Template name="ResolvableRowColumn">
<Parameter name="nreplicates" value="3"/>
<Parameter name="nrows" value="6"/>
<Parameter name="ncolumns" value="4"/>
<Parameter name="ntreatments" value="24"/>
<Parameter name="replicatefactor" value="replicates"/>
<Parameter name="rowfactor" value="rows"/>
<Parameter name="columnfactor" value="columns"/>
<Parameter name="treatmentfactor" value="genotypes"/>
<Parameter name="nrlatin" value="0"/>
<Parameter name="nclatin" value="1"/>
<Parameter name="replatingroups">
<ListItem value="1"/>
<ListItem value="2"/>
</Parameter>
<Parameter name="timelimit" value="1"/>
<Parameter name="outputfile" value="c:/documents/output.csv"/>
</Template>
</Templates>
		 */
		List<ExpDesignParameter> paramList = new ArrayList();
		paramList.add(ExpDesignUtil.createExpDesignParameter("nreplicates", "3", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter("nrows", "6", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter("ncolumns", "4", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter("ntreatments", "24", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter("replicatefactor", "replicates", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter("rowfactor", "rows", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter("columnfactor", "columns", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter("treatmentfactor", "genotypes", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter("nrlatin", "0", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter("nclatin", "1", null));
		
		/*
		ExpDesignParameter param = createExpDesignParameter("replatingroups", "6", null);
		
		if(hasReplatingGroup){
			List<ListItem> items = new ArrayList();
			items.add(new ListItem("2"));
			items.add(new ListItem("1"));
			param = createExpDesignParameter("replatingroups", "0", items);
			param.setValue(null);
		}
		
		paramList.add(param);
		*/
		paramList.add(ExpDesignUtil.createExpDesignParameter("timelimit", "1", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter("outputfile", "c:/documents/output.csv", null));
		
		return paramList;
	}
	
	@Test
	public void testResolvableIncompleteBlockExpDesignToXml() {
		
		MainDesign design = ExpDesignUtil.createResolvableIncompleteBlockDesign("6", "24", 
				"2", "Treat", "Reps", "Subblocks", 
				"Plots", "0", "", "1", "");
		
		try {
					
			System.out.println(ExpDesignUtil.getXmlStringForSetting(design));
			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testResolvableIncompleteBlockExpDesignRunToBvDesign() {
		
		MainDesign mainDesign = ExpDesignUtil.createResolvableIncompleteBlockDesign("6", "24", 
				"2", "Treat", "Reps", "Subblocks", 
				"Plots", "0", "", "1", "");
		
		try{
			ExpDesignUtil.runBVDesign(workbenchService, fieldbookProperties, mainDesign);
		}catch(Exception e){
			e.printStackTrace();
		}
	}	
	
	@Test
	public void testResolvableRowColExpDesignRunToBvDesign() {
		
		MainDesign mainDesign = ExpDesignUtil.createResolvableRowColDesign("50",
				"2", "5", "10", "Treat", "Reps", 
				"Rows", "Columns","Plots",
				"0", "0", "", "1", "");
		
		try{
			BVDesignOutput output = ExpDesignUtil.runBVDesign(workbenchService, fieldbookProperties, mainDesign);
			assertEquals(output.isSuccess(), true);
		}catch(Exception e){
			e.printStackTrace();
		}
	}	
	
	@Test
	public void testRandomizeCompleteBlockDesignExpDesignRunToBvDesign() {
		
		List<String> treatmentFactor = new ArrayList<String>();
		treatmentFactor.add("ENTRY_NO");
		treatmentFactor.add("FERTILIZER");
		
		List<String> levels = new ArrayList<String>();
		levels.add("24");
		levels.add("3");
		
		MainDesign mainDesign = ExpDesignUtil.createRandomizedCompleteBlockDesign("6", "Reps", "Plots",
				treatmentFactor, levels, "1", "");
		
		try{
			BVDesignOutput output = ExpDesignUtil.runBVDesign(workbenchService, fieldbookProperties, mainDesign);
			assertEquals(output.isSuccess(), true);
		}catch(Exception e){
			e.printStackTrace();
		}
	}	
	
	
	@Test
	public void testResolvableRowColumnExpDesignToXml() {
			
		MainDesign design = ExpDesignUtil.createResolvableRowColDesign("50",
				"2", "5", "10", "Treat", "Reps", 
				"Rows", "Columns","Plots",
				"0", "0", "", "1", "");
		
		try {
			System.out.println(ExpDesignUtil.getXmlStringForSetting(design));
			
			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableIncompleteBlockDesignService() {
			
		
		try {
			List<ImportedGermplasm> germplasmList = createGermplasmList("Test", 24);
			Map<String, String> parameterMap = new HashMap();
			parameterMap.put("blockSize", "6");
			parameterMap.put("replicates", "2");	    	
			parameterMap.put("environments", "1");
			List<MeasurementVariable> factors = new ArrayList();		
	        factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId()), Operation.ADD));
	        factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId()), Operation.ADD));
	        factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId()), Operation.ADD));
	        
	        List<MeasurementVariable> variates = new ArrayList();		
	        variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(fieldbookMiddlewareService.getStandardVariable(20368), Operation.ADD));        
        		
	        List<MeasurementRow> measurementRowList = resolveIncompleteBlockDesign.generateDesign(germplasmList, parameterMap, factors, 
					variates, null, null);
			for(MeasurementRow measurementRow : measurementRowList){
				System.out.println(measurementRow.toString());
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testResolvableRowColumnDesignService() {
			
		
		try {
			List<ImportedGermplasm> germplasmList = createGermplasmList("Test", 200);
			Map<String, String> parameterMap = new HashMap();
			parameterMap.put("rows", "2");
			parameterMap.put("cols", "100");	    	
			parameterMap.put("replicates", "2");
			parameterMap.put("environments", "2");			
			
			//number of replicates should be 2 or more
			
			List<MeasurementVariable> factors = new ArrayList();		
	        factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId()), Operation.ADD));
	        factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId()), Operation.ADD));
	        factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId()), Operation.ADD));
	        
	        List<MeasurementVariable> variates = new ArrayList();		
	        variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(fieldbookMiddlewareService.getStandardVariable(20368), Operation.ADD));        
        		
	        List<MeasurementRow> measurementRowList = resolveRowColumn.generateDesign(germplasmList, parameterMap, factors, 
					variates, null, null);
			for(MeasurementRow measurementRow : measurementRowList){
				System.out.println(measurementRow.toString());
			}
			
			assertEquals(800, measurementRowList.size());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRandomizeCompleteBlockDesignService() {
			
		
		try {
			List<ImportedGermplasm> germplasmList = createGermplasmList("Test", 200);
			Map<String, String> parameterMap = new HashMap();    	
			parameterMap.put("block", "2");
			parameterMap.put("environments", "2");			
			Map<String, List<String>> treatmentFactorValues = new HashMap();
			List<TreatmentVariable> treatmentVarList = new ArrayList();
			
			
			treatmentFactorValues.put("8230", Arrays.asList("200"));
			treatmentFactorValues.put("8284", Arrays.asList("100", "200", "300"));
			treatmentFactorValues.put("8377", Arrays.asList("100", "200", "300"));
			
			TreatmentVariable treatmentVar1 = new TreatmentVariable();
			treatmentVar1.setLevelVariable(ExpDesignUtil.convertStandardVariableToMeasurementVariable(fieldbookMiddlewareService.getStandardVariable(8284), Operation.ADD));
			treatmentVar1.setValueVariable(ExpDesignUtil.convertStandardVariableToMeasurementVariable(fieldbookMiddlewareService.getStandardVariable(8282), Operation.ADD));

			
			TreatmentVariable treatmentVar2 = new TreatmentVariable();
			treatmentVar2.setLevelVariable(ExpDesignUtil.convertStandardVariableToMeasurementVariable(fieldbookMiddlewareService.getStandardVariable(8377), Operation.ADD));
			treatmentVar2.setValueVariable(ExpDesignUtil.convertStandardVariableToMeasurementVariable(fieldbookMiddlewareService.getStandardVariable(8263), Operation.ADD));

			treatmentVarList.add(treatmentVar2);

			
			//number of replicates should be 2 or more
			
			List<MeasurementVariable> factors = new ArrayList();		
	        factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId()), Operation.ADD));
	        factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId()), Operation.ADD));
	        factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId()), Operation.ADD));
	        
	        List<MeasurementVariable> variates = new ArrayList();		
	        variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(fieldbookMiddlewareService.getStandardVariable(20368), Operation.ADD));        
        		
	        List<MeasurementRow> measurementRowList = randomizeBlockDesign.generateDesign(germplasmList, parameterMap, factors, 
					variates, treatmentVarList, treatmentFactorValues);
			for(MeasurementRow measurementRow : measurementRowList){
				System.out.println(measurementRow.toString());
			}
			
			assertEquals(7200, measurementRowList.size());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private List<ImportedGermplasm> createGermplasmList(String prefix, int size) {
		List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
		
		for (int i = 0; i < size; i++) {
			ImportedGermplasm germplasm = new ImportedGermplasm(i+1, prefix + (i+1), null);
			list.add(germplasm);
		}
		
		return list;
	}
	
}
