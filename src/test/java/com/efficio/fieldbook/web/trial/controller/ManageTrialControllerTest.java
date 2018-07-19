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

package com.efficio.fieldbook.web.trial.controller;

import org.generationcp.middleware.manager.api.StudyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

public class ManageTrialControllerTest {
	
	@Mock
	private StudyDataManager studyDataManager;
	
	@InjectMocks
	private ManageTrialController controller;
	
	private MockMvc mockMvc;
	
	@Before
	public void setup() {
        MockitoAnnotations.initMocks(this);
        // Use standalone setup in order to mock StudyDataManager (there is error in
        // in CI server when creating bean StudyDataManager since there's no program in DB)
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	public void testGet() throws Exception {

		this.mockMvc.perform(MockMvcRequestBuilders.get(ManageTrialController.URL))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.model().attributeExists("preloadSummaryId"))
				.andExpect(MockMvcResultMatchers.model().attributeExists("preloadSummaryName"))
				.andExpect(MockMvcResultMatchers.model().attributeExists("studyTypes"))
				.andExpect(MockMvcResultMatchers.model().attribute(AbstractBaseFieldbookController.TEMPLATE_NAME_ATTRIBUTE, "Common/manageStudy"));
	}
}
