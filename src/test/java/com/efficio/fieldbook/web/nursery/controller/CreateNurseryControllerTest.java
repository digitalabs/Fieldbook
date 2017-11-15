
package com.efficio.fieldbook.web.nursery.controller;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
			StandardVariableTestDataInitializer.createStandardVariable());
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
}
