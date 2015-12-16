package com.efficio.fieldbook.web.trial.controller;

import javax.annotation.Resource;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.efficio.fieldbook.web.common.service.RandomizeCompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableIncompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableRowColumnDesignService;
import com.efficio.fieldbook.web.importdesign.service.DesignImportService;

@RunWith(MockitoJUnitRunner.class)
public class ExpDesignControllerTest {
	
	@Mock
	private RandomizeCompleteBlockDesignService randomizeCompleteBlockDesign;
	
	@Mock
	private ResolvableIncompleteBlockDesignService resolveIncompleteBlockDesign;
	
	@Mock
	private ResolvableRowColumnDesignService resolvableRowColumnDesign;
	
	@Mock
	private ResourceBundleMessageSource messageSource;
	
	@Mock
	private CrossExpansionProperties crossExpansionProperties;
	
	@Mock
	private ContextUtil contextUtil;
	
	@Mock
	private DesignImportService designImportService;
	
	@InjectMocks
	ExpDesignController expDesignController;
	
	@Before()
	public void init(){
		Mockito.doReturn("CIMMYT").when(this.crossExpansionProperties).getProfile();
		Project project = Mockito.mock(Project.class);
		Mockito.doReturn(project).when(this.contextUtil).getProjectInContext();
		Mockito.doReturn(new CropType("WHEAT")).when(project).getCropType();
	}
	
	@Test
	public void testRetrieveDesignTypes(){
		String designTypes = expDesignController.retrieveDesignTypes();
		//To do: convert to java object to test properly
	}
}
