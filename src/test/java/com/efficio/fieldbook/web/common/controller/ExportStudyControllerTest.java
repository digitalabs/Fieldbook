/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.common.controller;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.junit.Test;

import com.efficio.fieldbook.web.AbstractBaseControllerTest;

public class ExportStudyControllerTest extends AbstractBaseControllerTest {

	@Resource
	private ExportStudyController exportStudyController;

	@Test
	public void testGetOutputFileNameValueChanged() {
		String fileName = this.exportStudyController.getOutputFileName(true, "trial-test_1.xls",
				"trial-test.xls");
		Assert.assertEquals("Expected trial-test.xls but got trial-test_1.xls", "trial-test.xls",
				fileName);
	}

	@Test
	public void testGetOutputFileNameValueRetained() {
		String fileName = this.exportStudyController.getOutputFileName(false, "trial-test_1.xls",
				"trial-test.xls");
		Assert.assertEquals("Expected trial-test_1.xls but got trial-test.xls", "trial-test_1.xls",
				fileName);
	}
}