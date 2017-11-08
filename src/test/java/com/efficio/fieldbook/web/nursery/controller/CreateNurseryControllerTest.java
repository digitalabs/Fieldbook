
package com.efficio.fieldbook.web.nursery.controller;

import java.util.ArrayList;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.VariableConstraints;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.ExtendedModelMap;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.FieldbookProperties;

/**
 * Created by cyrus on 21/10/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateNurseryControllerTest {

	private final String TEST_PROG_UUID = "UUID_STRING";

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpSession session;

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
	private final CreateNurseryController controller = new CreateNurseryController();

	@Before
	public void setUp() throws Exception {
		final Project project = new Project();
		project.setProjectId(1L);
		final CropType cropType = new CropType();
		cropType.setCropName("Test");
		project.setCropType(cropType);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(project);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(this.TEST_PROG_UUID);
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(Matchers.anyInt(), Matchers.eq(this.TEST_PROG_UUID))).thenReturn(
				this.createTestVariable());
	}

	@Test
	public void testShowSetsUpModelAttributes() throws Exception {
		ExtendedModelMap model = new ExtendedModelMap();
		this.controller.show(new CreateNurseryForm(), new ImportGermplasmListForm(), model, this.session, this.request);
		SettingsControllerTest.checkVariableSecionIdModelAttributes(model);
	}

	@Test
	public void testUseExistingNurserySetsUpModelAttributes() throws Exception {
		ExtendedModelMap model = new ExtendedModelMap();
		this.controller.useExistingNursery(new CreateNurseryForm(), 0, model, this.session, this.request);
		SettingsControllerTest.checkVariableSecionIdModelAttributes(model);
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
		stdVariable.getEnumerations().add(new Enumeration(StudyType.N.getId(), StudyType.N.getName(),  StudyType.N.getLabel(), 1));
		stdVariable.getEnumerations().add(new Enumeration(StudyType.HB.getId(), StudyType.HB.getName(),  StudyType.HB.getLabel(), 2));
		stdVariable.getEnumerations().add(new Enumeration(StudyType.PN.getId(), StudyType.PN.getName(),  StudyType.PN.getLabel(), 3));
		stdVariable.setConstraints(new VariableConstraints(100.0, 999.0));
		stdVariable.setCropOntologyId("CROP-TEST");

		return stdVariable;
	}

}
