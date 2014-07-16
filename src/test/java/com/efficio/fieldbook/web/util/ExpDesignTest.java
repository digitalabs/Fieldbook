package com.efficio.fieldbook.web.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.Assert;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
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
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesign;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesignParameter;
import com.efficio.fieldbook.web.trial.bean.xml.ListItem;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class ExpDesignTest extends AbstractJUnit4SpringContextTests{
	
	@Autowired
    private WorkbenchService workbenchService;
	@Autowired
    private FieldbookProperties fieldbookProperties;
	 
	public List<ExpDesignParameter> createResolvableIncompleteBlockParameterList(boolean hasReplatingGroup){
		/*
<Parameter name="blocksize" value="6"/>
<Parameter name="ntreatments" value="24"/>
<Parameter name="nreplicates" value="2"/>
<Parameter name="treatmentfactor" value="Treat"/>
<Parameter name="replicatefactor" value="Reps"/>
<Parameter name="blockfactor" value="Subblocks"/>
<Parameter name="plotwithinblockfactor" value="Plots"/>
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
<Parameter name="plotwithinblockfactor" value="plots"/>
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
		paramList.add(createExpDesignParameter("blocksize", "6", null));
		paramList.add(createExpDesignParameter("ntreatments", "24", null));
		paramList.add(createExpDesignParameter("nreplicates", "2", null));
		paramList.add(createExpDesignParameter("treatmentfactor", "Treat", null));
		paramList.add(createExpDesignParameter("replicatefactor", "Reps", null));
		paramList.add(createExpDesignParameter("blockfactor", "Subblocks", null));
		paramList.add(createExpDesignParameter("plotwithinblockfactor", "Plots", null));
		paramList.add(createExpDesignParameter("nblatin", "0", null));
		
		ExpDesignParameter param = createExpDesignParameter("replatingroups", "0", null);
		
		if(hasReplatingGroup){
			List<ListItem> items = new ArrayList();
			items.add(new ListItem("2"));
			items.add(new ListItem("1"));
			param = createExpDesignParameter("replatingroups", "0", items);
			param.setValue(null);
		}
		
		
		paramList.add(param);
		paramList.add(createExpDesignParameter("timelimit", "1", null));
		paramList.add(createExpDesignParameter("outputfile", "c:/documents/output.csv", null));
		return paramList;
	}
	
	private ExpDesignParameter createExpDesignParameter(String name, String value, List<ListItem> items){
		ExpDesignParameter designParam = new ExpDesignParameter(name, value);
		if(items != null && !items.isEmpty()){
			designParam.setListItem(items);
		}
		return designParam;
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
		paramList.add(createExpDesignParameter("nreplicates", "3", null));
		paramList.add(createExpDesignParameter("nrows", "6", null));
		paramList.add(createExpDesignParameter("ncolumns", "4", null));
		paramList.add(createExpDesignParameter("ntreatments", "24", null));
		paramList.add(createExpDesignParameter("replicatefactor", "replicates", null));
		paramList.add(createExpDesignParameter("rowfactor", "rows", null));
		paramList.add(createExpDesignParameter("columnfactor", "columns", null));
		paramList.add(createExpDesignParameter("treatmentfactor", "genotypes", null));
		paramList.add(createExpDesignParameter("nrlatin", "0", null));
		paramList.add(createExpDesignParameter("nclatin", "1", null));
		ExpDesignParameter param = createExpDesignParameter("replatingroups", "6", null);
		
		if(hasReplatingGroup){
			List<ListItem> items = new ArrayList();
			items.add(new ListItem("2"));
			items.add(new ListItem("1"));
			param = createExpDesignParameter("replatingroups", "0", items);
			param.setValue(null);
		}
		
		paramList.add(param);
		paramList.add(createExpDesignParameter("timelimit", "1", null));
		paramList.add(createExpDesignParameter("outputfile", "c:/documents/output.csv", null));
		
		return paramList;
	}
	
	@Test
	public void testResolvableIncompleteBlockExpDesignToXml() {
		
		ExpDesign design = new ExpDesign("ResolvableIncompleteBlock", createResolvableIncompleteBlockParameterList(false));
		MainDesign mainDesign = new MainDesign(design);
		
		String expectedWithoutLatinized = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"ResolvableIncompleteBlock\"><Parameter name=\"blocksize\" value=\"6\"/><Parameter name=\"ntreatments\" value=\"24\"/><Parameter name=\"nreplicates\" value=\"2\"/><Parameter name=\"treatmentfactor\" value=\"Treat\"/><Parameter name=\"replicatefactor\" value=\"Reps\"/><Parameter name=\"blockfactor\" value=\"Subblocks\"/><Parameter name=\"plotwithinblockfactor\" value=\"Plots\"/><Parameter name=\"nblatin\" value=\"0\"/><Parameter name=\"replatingroups\" value=\"0\"/><Parameter name=\"timelimit\" value=\"1\"/><Parameter name=\"outputfile\" value=\"c:/documents/output.csv\"/></Template></Templates>";
		String expectedWithLatinized = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"ResolvableIncompleteBlock\"><Parameter name=\"blocksize\" value=\"6\"/><Parameter name=\"ntreatments\" value=\"24\"/><Parameter name=\"nreplicates\" value=\"2\"/><Parameter name=\"treatmentfactor\" value=\"Treat\"/><Parameter name=\"replicatefactor\" value=\"Reps\"/><Parameter name=\"blockfactor\" value=\"Subblocks\"/><Parameter name=\"plotwithinblockfactor\" value=\"Plots\"/><Parameter name=\"nblatin\" value=\"0\"/><Parameter name=\"replatingroups\"><ListItem value=\"2\"/><ListItem value=\"1\"/></Parameter><Parameter name=\"timelimit\" value=\"1\"/><Parameter name=\"outputfile\" value=\"c:/documents/output.csv\"/></Template></Templates>";
		try {
			Assert.assertEquals(expectedWithoutLatinized, ExpDesignUtil.getXmlStringForSetting(mainDesign));
			
			design = new ExpDesign("ResolvableIncompleteBlock", createResolvableIncompleteBlockParameterList(true));
			mainDesign.setDesign(design);
			//System.out.println(ExpDesignUtil.getXmlStringForSetting(mainDesign));
			Assert.assertEquals(expectedWithLatinized, ExpDesignUtil.getXmlStringForSetting(mainDesign));
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testResolvableIncompleteBlockExpDesignRunToBvDesign() {
		
		ExpDesign design = new ExpDesign("ResolvableIncompleteBlock", createResolvableIncompleteBlockParameterList(false));
		MainDesign mainDesign = new MainDesign(design);
		
		try{
			ExpDesignUtil.runBVDesign(workbenchService, fieldbookProperties, mainDesign);
		}catch(Exception e){
			e.printStackTrace();
		}
	}	
	
	@Test
	public void testResolvableRowColumnExpDesignToXml() {
		
		ExpDesign design = new ExpDesign("ResolvableRowColumn", createResolvableRowColumnParameterList(false));
		MainDesign mainDesign = new MainDesign(design);
		String expectedWithoutLatinized = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"ResolvableRowColumn\"><Parameter name=\"nreplicates\" value=\"3\"/><Parameter name=\"nrows\" value=\"6\"/><Parameter name=\"ncolumns\" value=\"4\"/><Parameter name=\"ntreatments\" value=\"24\"/><Parameter name=\"replicatefactor\" value=\"replicates\"/><Parameter name=\"rowfactor\" value=\"rows\"/><Parameter name=\"columnfactor\" value=\"columns\"/><Parameter name=\"treatmentfactor\" value=\"genotypes\"/><Parameter name=\"nrlatin\" value=\"0\"/><Parameter name=\"nclatin\" value=\"1\"/><Parameter name=\"replatingroups\" value=\"6\"/><Parameter name=\"timelimit\" value=\"1\"/><Parameter name=\"outputfile\" value=\"c:/documents/output.csv\"/></Template></Templates>";
		String expectedWithLatinized = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"ResolvableRowColumn\"><Parameter name=\"nreplicates\" value=\"3\"/><Parameter name=\"nrows\" value=\"6\"/><Parameter name=\"ncolumns\" value=\"4\"/><Parameter name=\"ntreatments\" value=\"24\"/><Parameter name=\"replicatefactor\" value=\"replicates\"/><Parameter name=\"rowfactor\" value=\"rows\"/><Parameter name=\"columnfactor\" value=\"columns\"/><Parameter name=\"treatmentfactor\" value=\"genotypes\"/><Parameter name=\"nrlatin\" value=\"0\"/><Parameter name=\"nclatin\" value=\"1\"/><Parameter name=\"replatingroups\"><ListItem value=\"2\"/><ListItem value=\"1\"/></Parameter><Parameter name=\"timelimit\" value=\"1\"/><Parameter name=\"outputfile\" value=\"c:/documents/output.csv\"/></Template></Templates>";
		try {
			//System.out.println(ExpDesignUtil.getXmlStringForSetting(mainDesign));
			Assert.assertEquals(expectedWithoutLatinized, ExpDesignUtil.getXmlStringForSetting(mainDesign));
			
			design = new ExpDesign("ResolvableRowColumn", createResolvableRowColumnParameterList(true));
			mainDesign.setDesign(design);
			//System.out.println(ExpDesignUtil.getXmlStringForSetting(mainDesign));
			Assert.assertEquals(expectedWithLatinized, ExpDesignUtil.getXmlStringForSetting(mainDesign));
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
