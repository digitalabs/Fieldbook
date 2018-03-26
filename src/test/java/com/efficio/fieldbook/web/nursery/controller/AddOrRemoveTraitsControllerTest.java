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

package com.efficio.fieldbook.web.nursery.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;

@Deprecated
public class AddOrRemoveTraitsControllerTest extends AbstractBaseIntegrationTest {

	/** The Constant log. */
	private static final Logger LOG = LoggerFactory.getLogger(AddOrRemoveTraitsControllerTest.class);

	/** The file service. */

	/** The fieldbook service. */
	@Autowired
	FieldbookService fieldbookService;

	@Resource
	private MeasurementsGeneratorService measurementsGeneratorService;

	/** The import germplasm file service. */
	@Autowired
	private ImportGermplasmFileService importGermplasmFileService;

	/** The workbook advance. */
	private org.apache.poi.ss.usermodel.Workbook workbookAdvance;

	/**
	 * Sets the up.
	 */
	@Override
	@Before
	public void setUp() {
		try {
			final InputStream inp = this.getClass().getClassLoader().getResourceAsStream("GermplasmImportTemplate-Advanced-rev4.xls");

			this.workbookAdvance = WorkbookFactory.create(inp);

		} catch (final Exception e) {
			AddOrRemoveTraitsControllerTest.LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Test valid nursery workbook.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testAddOrRemoveTraitTest() throws Exception {
		final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		try {
			this.importGermplasmFileService.doProcessNow(this.workbookAdvance, mainInfo);
			form.setImportedGermplasmMainInfo(mainInfo);
			form.setImportedGermplasm(mainInfo.getImportedGermplasmList().getImportedGermplasms());
		} catch (final Exception e) {
			AddOrRemoveTraitsControllerTest.LOG.error(e.getMessage(), e);
		}
		final UserSelection userSelection = new UserSelection();
		userSelection.setWorkbook(new org.generationcp.middleware.domain.etl.Workbook());
		final StudyDetails details = new StudyDetails();
		details.setStudyType(new StudyTypeDto("N"));
		userSelection.getWorkbook().setStudyDetails(details);
		final List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
		final MeasurementVariable checkVariable =
				new MeasurementVariable("CHECK", "TYPE OF ENTRY", "CODE", "ASSIGNED", "CHECK", "C", "", "ENTRY");
		factors.add(checkVariable);
		userSelection.getWorkbook().setFactors(factors);
		userSelection.getWorkbook().setVariates(new ArrayList<MeasurementVariable>());
		userSelection.setImportedGermplasmMainInfo(mainInfo);
		final MeasurementsGeneratorService service = Mockito.mock(MeasurementsGeneratorService.class);
		final List<MeasurementRow> measurementRowsTemp = new ArrayList<MeasurementRow>();
		for (int i = 0; i < 29; i++) {
			measurementRowsTemp.add(new MeasurementRow());
		}
		Mockito.when(service.generateRealMeasurementRows(userSelection)).thenReturn(measurementRowsTemp);
		final List<MeasurementRow> measurementRows = service.generateRealMeasurementRows(userSelection);
		Assert.assertEquals(29, measurementRows.size());
	}

}
