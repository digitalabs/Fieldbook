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

import org.generationcp.middleware.domain.oms.StudyType;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

public class ManageNurseriesControllerTest extends AbstractBaseIntegrationTest {

	@Test
	public void testGet() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get(ManageNurseriesController.URL))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.model().attribute("type", StudyType.N.getName()))
				.andExpect(MockMvcResultMatchers.model().attributeExists("preloadSummaryId"))
				.andExpect(MockMvcResultMatchers.model().attributeExists("preloadSummaryName"))
				.andExpect(MockMvcResultMatchers.model().attribute(AbstractBaseFieldbookController.TEMPLATE_NAME_ATTRIBUTE, "Common/manageStudy"));
	}

}
