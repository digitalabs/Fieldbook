package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OpenTrialControllerTest {
    private static final int TRIAL_ID = 1;
    @Mock
    private UserSelection userSelection;

    @Mock
    private HttpSession session;

    @Mock
    private CreateTrialForm createTrialForm;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private FieldbookService fieldbookService;

    @Mock
    private ErrorHandlerService errorHandlerService;

    @InjectMocks
    private OpenTrialController openTrialController;

    @Test
    public void testOpenTrialNoRedirect() throws Exception {
        final OpenTrialController moleOpenTrialController = setupOpenTrialController();
        Workbook workbook = mock(Workbook.class);
        when(fieldbookService.getTrialDataSet(TRIAL_ID)).thenReturn(workbook);

        doNothing().when(moleOpenTrialController).setModelAttributes(createTrialForm,TRIAL_ID,model,workbook);

        String out = moleOpenTrialController.openTrial(createTrialForm,TRIAL_ID,model,session,redirectAttributes);

        verify(fieldbookService).getTrialDataSet(TRIAL_ID);
        assertEquals("should return the base angular template",OpenTrialController.ANGULAR_BASE_TEMPLATE_NAME,out);
    }

    @Test
    public void testOpenTrialRedirectForIncompatibleStudy() throws Exception {
        final OpenTrialController moleOpenTrialController = setupOpenTrialController();

        when(fieldbookService.getTrialDataSet(TRIAL_ID)).thenThrow(MiddlewareQueryException.class);

        String out = moleOpenTrialController.openTrial(createTrialForm, TRIAL_ID, model, session, redirectAttributes);

        assertEquals("should redirect to manage trial page", "redirect:" + ManageTrialController.URL, out);

        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);

        verify(redirectAttributes).addFlashAttribute(arg1.capture(), arg2.capture());
        assertEquals("value should be redirectErrorMessage", "redirectErrorMessage", arg1.getValue());


    }

    protected OpenTrialController setupOpenTrialController() {
        final OpenTrialController moleOpenTrialController = spy(openTrialController);
        doNothing().when(moleOpenTrialController).clearSessionData(session);
        return moleOpenTrialController;
    }
}