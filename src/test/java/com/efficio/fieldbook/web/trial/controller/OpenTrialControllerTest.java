package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
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
    public void testOpenTrialRedirectForIncompatibleStudy() throws Exception {
        final OpenTrialController moleOpenTrialController = spy(openTrialController);
        doNothing().when(moleOpenTrialController).clearSessionData(session);
        when(fieldbookService.getTrialDataSet(TRIAL_ID)).thenThrow(MiddlewareQueryException.class);

        String out = moleOpenTrialController.openTrial(createTrialForm, TRIAL_ID, model, session, redirectAttributes);

        assertEquals("should redirect to manage trial page", "redirect:" + ManageTrialController.URL, out);

        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);

        verify(redirectAttributes).addFlashAttribute(arg1.capture(), arg2.capture());
        assertEquals("value should be redirectErrorMessage", "redirectErrorMessage", arg1.getValue());


    }
}