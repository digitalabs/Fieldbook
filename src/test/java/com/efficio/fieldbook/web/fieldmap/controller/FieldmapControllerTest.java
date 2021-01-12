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

package com.efficio.fieldbook.web.fieldmap.controller;

import org.junit.Assert;

import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.commons.util.StringUtil;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.junit.Test;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

public class FieldmapControllerTest extends AbstractBaseIntegrationTest {

	@Test
	public void testGetReturnsCorrectModelAndView() {
		// TODO add tests for the core URLs that this controller handles.
		Assert.assertTrue(true);
	}


	@Test
	public void testValidateFieldMapDataSetInfoInvalid() {
		final List<FieldMapDatasetInfo> datasetList = this.getFieldMapDatasetInfo(false);
		final  FieldmapController controller = new FieldmapController();
		final boolean hasInvalidValue = controller.validateFieldMapDataSetInfo(datasetList);
		Assert.assertTrue("Has Invalid value",hasInvalidValue);
	}

	@Test
	public void testValidateFieldMapDataSetInfoValid() {
		final List<FieldMapDatasetInfo> datasetList = this.getFieldMapDatasetInfo(true);
		final  FieldmapController controller = new FieldmapController();
		final boolean hasInvalidValue = controller.validateFieldMapDataSetInfo(datasetList);
		Assert.assertFalse("No Invalid value.",hasInvalidValue);
	}

	private List<FieldMapDatasetInfo> getFieldMapDatasetInfo(final boolean isValid) {
		int randomId = Integer.parseInt(RandomStringUtils.randomNumeric(1));
		if (randomId <= 0) {
			randomId = 1;
		}
		int range = 1;
		final int column = 1;
		if (!isValid) {
			range = -1;
		}

		final List<FieldMapLabel> labels = new ArrayList<>();
		final FieldMapLabel label = new FieldMapLabel();
		label.setBlockNo(randomId);
		label.setColumn(column);
		label.setRange(range);
		label.setDatasetId(randomId);
		label.setEntryNumber(1);
		label.setExperimentId(randomId);
		label.setGermplasmName(RandomStringUtils.randomAlphanumeric(12));
		label.setGid(randomId);
		label.setInstanceId(randomId);
		labels.add(label);

		final List<FieldMapTrialInstanceInfo> fieldMapTrialInstanceInfos = new ArrayList<>();
		final FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = new FieldMapTrialInstanceInfo();
		fieldMapTrialInstanceInfo.setPlantingOrder(1);
		fieldMapTrialInstanceInfo.setMachineRowCapacity(1);
		fieldMapTrialInstanceInfo.setRowsInBlock(10);
		fieldMapTrialInstanceInfo.setRangesInBlock(10);
		fieldMapTrialInstanceInfo.setBlockId(randomId);
		fieldMapTrialInstanceInfo.setFieldId(randomId);
		fieldMapTrialInstanceInfo.setHasInValidValue(!isValid);
		fieldMapTrialInstanceInfos.add(fieldMapTrialInstanceInfo);
		fieldMapTrialInstanceInfo.setHasFieldMap(true);
		fieldMapTrialInstanceInfo.setFieldMapLabels(labels);

		final List<FieldMapDatasetInfo> datasetList = new ArrayList<>();
		final FieldMapDatasetInfo info = new FieldMapDatasetInfo();
		info.setDatasetId(randomId);
		info.setDatasetName(RandomStringUtils.randomAlphanumeric(10));
		info.setTrialInstances(fieldMapTrialInstanceInfos);
		datasetList.add(info);

		return datasetList;
	}
}
