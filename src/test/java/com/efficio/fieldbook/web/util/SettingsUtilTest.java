package com.efficio.fieldbook.web.util;

import java.util.ArrayList;

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

public class SettingsUtilTest {

	@Test
	public void testConvertXmlDatasetToWorkbookAndBack() {
		Dataset dataset = new Dataset();
		
		dataset.setConditions(new ArrayList<Condition>());
		dataset.setFactors(new ArrayList<Factor>());
		dataset.setVariates(new ArrayList<Variate>());
		
		dataset.getConditions().add(new Condition("CONDITION1", "CONDITION1", "PERSON", "DBCV", "ASSIGNED", PhenotypicType.STUDY.toString(), "C", "Meeh", null, null, null));
		dataset.getFactors().add(new Factor("FACTOR1", "FACTOR1", "GERMPLASM ENTRY", "NUMBER", "ENUMERATED", PhenotypicType.GERMPLASM.toString(), "N", 0));
		dataset.getVariates().add(new Variate("VARIATE1", "VARIATE1", "YIELD (GRAIN)", "Kg/ha", "Paddy Rice", PhenotypicType.VARIATE.toString(), "N", TermId.NUMERIC_VARIABLE.getId(), new ArrayList<ValueReference>(), 0.0, 0.0));
		
		Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, true);
		Debug.println(0, workbook);
		
		Dataset newDataset = (Dataset)SettingsUtil.convertWorkbookToXmlDataset(workbook);
		Assert.assertEquals(dataset.getConditions().get(0).getName(), newDataset.getConditions().get(0).getName());
		Assert.assertEquals(dataset.getConditions().get(0).getDescription(), newDataset.getConditions().get(0).getDescription());
		Assert.assertEquals(dataset.getConditions().get(0).getProperty(), newDataset.getConditions().get(0).getProperty());
		Assert.assertEquals(dataset.getConditions().get(0).getScale(), newDataset.getConditions().get(0).getScale());
		Assert.assertEquals(dataset.getConditions().get(0).getMethod(), newDataset.getConditions().get(0).getMethod());
		Assert.assertEquals(dataset.getConditions().get(0).getRole(), newDataset.getConditions().get(0).getRole());
		Assert.assertEquals(dataset.getConditions().get(0).getDatatype(), newDataset.getConditions().get(0).getDatatype());

		Assert.assertEquals(dataset.getFactors().get(0).getName(), newDataset.getFactors().get(0).getName());
		Assert.assertEquals(dataset.getFactors().get(0).getDescription(), newDataset.getFactors().get(0).getDescription());
		Assert.assertEquals(dataset.getFactors().get(0).getProperty(), newDataset.getFactors().get(0).getProperty());
		Assert.assertEquals(dataset.getFactors().get(0).getScale(), newDataset.getFactors().get(0).getScale());
		Assert.assertEquals(dataset.getFactors().get(0).getMethod(), newDataset.getFactors().get(0).getMethod());
		Assert.assertEquals(dataset.getFactors().get(0).getRole(), newDataset.getFactors().get(0).getRole());
		Assert.assertEquals(dataset.getFactors().get(0).getDatatype(), newDataset.getFactors().get(0).getDatatype());

		Assert.assertEquals(dataset.getVariates().get(0).getName(), newDataset.getVariates().get(0).getName());
		Assert.assertEquals(dataset.getVariates().get(0).getDescription(), newDataset.getVariates().get(0).getDescription());
		Assert.assertEquals(dataset.getVariates().get(0).getProperty(), newDataset.getVariates().get(0).getProperty());
		Assert.assertEquals(dataset.getVariates().get(0).getScale(), newDataset.getVariates().get(0).getScale());
		Assert.assertEquals(dataset.getVariates().get(0).getMethod(), newDataset.getVariates().get(0).getMethod());
		Assert.assertEquals(dataset.getVariates().get(0).getRole(), newDataset.getVariates().get(0).getRole());
		Assert.assertEquals(dataset.getVariates().get(0).getDatatype(), newDataset.getVariates().get(0).getDatatype());

	}
}
