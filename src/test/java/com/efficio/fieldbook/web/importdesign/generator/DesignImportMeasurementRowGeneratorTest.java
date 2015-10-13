
package com.efficio.fieldbook.web.importdesign.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.data.initializer.DesignImportDataInitializer;
import com.efficio.fieldbook.web.data.initializer.ImportedGermplasmMainInfoInitializer;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;

@RunWith(MockitoJUnitRunner.class)
public class DesignImportMeasurementRowGeneratorTest {

	private static final int GW100_G = 51496;

	private static final String PROGRAM_UUID = "789c6438-5a94-11e5-885d-feff819cdc9f";

	@Mock
	private UserSelection userSelection;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private DesignImportMeasurementRowGenerator generator;

	@Before
	public void setUp() {
		WorkbookDataUtil.setTestWorkbook(null);

		Mockito.doReturn(this.createProperty(TermId.BREEDING_METHOD_PROP.getId())).when(this.ontologyService)
				.getProperty(Mockito.anyString());
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(DesignImportMeasurementRowGeneratorTest.PROGRAM_UUID);

	}

	private Property createProperty(final int id) {
		final Term term = new Term();
		term.setId(id);
		final Property property = new Property();
		property.setTerm(term);
		return property;
	}

	@Test
	public void testAddGermplasmDetailsToDataList() throws FileParsingException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final List<ImportedGermplasm> importedGermplasm = ImportedGermplasmMainInfoInitializer.createImportedGermplasmList();
		final Map<Integer, StandardVariable> germplasmStandardVariables =
				DesignImportDataInitializer.getStandardVariables(PhenotypicType.GERMPLASM, workbook.getFactors());
		final List<MeasurementData> dataList = new ArrayList<>();

		this.generator.setWorkbook(workbook);
		this.generator.addGermplasmDetailsToDataList(importedGermplasm, germplasmStandardVariables, dataList, 1, this.fieldbookService);

		Assert.assertEquals("The added MeasurementData should Match the germplasm Standard Variables", germplasmStandardVariables.size(),
				dataList.size());

		final ImportedGermplasm germplasmEntry = importedGermplasm.get(0);

		for (final MeasurementData measurementData : dataList) {

			if (TermId.ENTRY_NO.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value : " + TermId.ENTRY_NO.toString(),
						measurementData.getValue().toString(), germplasmEntry.getEntryId().toString());
			}
			if (TermId.GID.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value : " + TermId.GID.toString(),
						measurementData.getValue().toString(), germplasmEntry.getGid().toString());
			}
			if (TermId.DESIG.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value : " + TermId.DESIG.toString(),
						measurementData.getValue().toString(), germplasmEntry.getDesig().toString());
			}
			if (TermId.ENTRY_TYPE.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value : " + TermId.ENTRY_TYPE.toString(),
						measurementData.getValue().toString(), germplasmEntry.getCheck().toString());
			}
			if (TermId.CROSS.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value : " + TermId.CROSS.toString(),
						measurementData.getValue().toString(), germplasmEntry.getCross().toString());
			}
			if (TermId.ENTRY_CODE.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value : " + TermId.ENTRY_CODE.toString(),
						measurementData.getValue().toString(), germplasmEntry.getEntryCode().toString());
			}
			if (TermId.GERMPLASM_SOURCE.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals(
						"The value of MeasurementData should match the germplasm value : " + TermId.GERMPLASM_SOURCE.toString(),
						measurementData.getValue().toString(), germplasmEntry.getSource().toString());
			}
			if (TermId.SEED_SOURCE.getId() == measurementData.getMeasurementVariable().getTermId()) {
				Assert.assertEquals("The value of MeasurementData should match the germplasm value : " + TermId.SEED_SOURCE.toString(),
						measurementData.getValue().toString(), germplasmEntry.getSource().toString());
			}

		}
	}

	@Test
	public void testAddVariatesToMeasurementRows() throws DesignValidationException {

		Mockito.doReturn(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo()).when(this.userSelection)
				.getImportedGermplasmMainInfo();

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);
		final EnvironmentData environmentData = DesignImportDataInitializer.createEnvironmentData(1);
		DesignImportDataInitializer.processEnvironmentData(environmentData);
		final List<MeasurementRow> measurements = workbook.getObservations();

		final MeasurementVariable variate = new MeasurementVariable();
		variate.setTermId(GW100_G);
		variate.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		variate.setName("GW100_g");

		final List<MeasurementVariable> variates = workbook.getVariates();
		variates.add(variate);

		final Integer beforeSize = measurements.get(0).getDataList().size();

		// trigger the addition of variates by setting the Operation to 'ADD' or
		// 'UPDATE'
		for (final MeasurementVariable measurementVariable : workbook.getVariates()) {
			measurementVariable.setOperation(Operation.ADD);
		}

		this.generator.setWorkbook(workbook);
		this.generator.addVariatesToMeasurementRows(measurements, this.userSelection, this.fieldbookService, this.ontologyService,
				this.contextUtil);

		final Integer actualSize = measurements.get(0).getDataList().size();
		final Integer noOfAddedVariates = 1;
		final Integer expectedSize = beforeSize + noOfAddedVariates;
		Assert.assertEquals("The size of the data list should be " + expectedSize + " since " + noOfAddedVariates + " variates are added",
				expectedSize, actualSize);

	}

	@Test
	public void testCreateMeasurementData() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final MeasurementVariable measurementVariable = workbook.getFactors().get(0);
		this.generator.setWorkbook(workbook);
		final MeasurementData data = this.generator.createMeasurementData(measurementVariable, "1");

		Assert.assertEquals("1", data.getValue());
		Assert.assertEquals(measurementVariable, data.getMeasurementVariable());

	}

}
