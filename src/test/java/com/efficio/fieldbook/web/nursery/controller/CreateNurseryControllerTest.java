package com.efficio.fieldbook.web.nursery.controller;

import java.util.ArrayList;
import java.util.Random;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import junit.framework.Assert;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.VariableConstraints;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Created by cyrus on 21/10/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateNurseryControllerTest {
	private MockHttpServletRequest request;
	private MockHttpSession session;

	@Mock
	private CreateNurseryForm createNurseryForm;

	@Mock
	private ImportGermplasmListForm importGermplasmListForm;

	@Mock
	private Model model;

	@Mock
	private ContextInfo contextInfo;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private UserSelection userSelection;

	@Mock
	private WorkbenchService workbenchService;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	protected FieldbookProperties fieldbookProperties;

	@InjectMocks
	private CreateNurseryController controller = new CreateNurseryController();

	@Before
	public void setUp() throws Exception {
		request = new MockHttpServletRequest();
		session = (MockHttpSession)request.getSession();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		Project project = new Project();
		project.setProjectId(1L);
		CropType cropType = new CropType();
		cropType.setCropName("Test");
		project.setCropType(cropType);
		Mockito.when(contextUtil.getProjectInContext()).thenReturn(project);
		session.setAttribute(ContextConstants.SESSION_ATTR_CONTEXT_INFO,contextInfo);

		Mockito.when(userSelection.getCacheStandardVariable(Mockito.anyInt())).thenReturn(createTestVariable());
	}

	@Test
	public void testShow() throws Exception {
		controller.setContextUtil(contextUtil);
		controller.show(createNurseryForm,importGermplasmListForm,model,session,request);

		ArgumentCaptor<Integer> traitArg = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> selectionMethodArg = ArgumentCaptor.forClass(Integer.class);

		// make sure we have set the model attributes correctly
		Mockito.verify(model,Mockito.times(1)).addAttribute(Mockito.eq("baselineTraitsSegment"),traitArg.capture());
		Mockito.verify(model,Mockito.times(1)).addAttribute(Mockito.eq("selectionVariatesSegment"),selectionMethodArg.capture());
		Assert.assertEquals(VariableType.TRAIT.getId(),traitArg.getValue());
		Assert.assertEquals(VariableType.SELECTION_METHOD.getId(),selectionMethodArg.getValue());

	}

	private StandardVariable createTestVariable() {
		final StandardVariable stdVariable = new StandardVariable();
		stdVariable.setName("variable name " + new Random().nextInt(10000));
		stdVariable.setDescription("variable description");
		stdVariable.setProperty(new Term(2002, "User", "Database user"));
		stdVariable.setMethod(new Term(4030, "Assigned", "Term, name or id assigned"));
		stdVariable.setScale(new Term(61220, "DBCV", "Controlled vocabulary from a database"));
		stdVariable.setDataType(new Term(1120, "Character variable", "variable with char values"));
		stdVariable.setIsA(new Term(1050, "Study condition", "Study condition class"));
		stdVariable.setEnumerations(new ArrayList<Enumeration>());
		stdVariable.getEnumerations().add(new Enumeration(10000, "N", "Nursery", 1));
		stdVariable.getEnumerations().add(new Enumeration(10001, "HB", "Hybridization nursery", 2));
		stdVariable.getEnumerations().add(new Enumeration(10002, "PN", "Pedigree nursery", 3));
		stdVariable.setConstraints(new VariableConstraints(100.0, 999.0));
		stdVariable.setCropOntologyId("CROP-TEST");

		return stdVariable;
	}

}
