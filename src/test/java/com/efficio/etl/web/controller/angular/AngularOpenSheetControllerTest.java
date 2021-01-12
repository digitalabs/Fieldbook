
package com.efficio.etl.web.controller.angular;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.ImportObservationsController;
import com.efficio.etl.web.bean.SelectRowsForm;
import com.efficio.etl.web.bean.UserSelection;

@RunWith(MockitoJUnitRunner.class)
public class AngularOpenSheetControllerTest {

	private static final String CONTEXT_PATH = "http://localhost:48080/DatasetImporter/";

	@Mock
	private HttpServletRequest request;

	@Mock
	private ETLService etlService;

	@Mock
	private UserSelection userSelection;

	@InjectMocks
	private AngularOpenSheetController controller;

	@Before
	public void init() {
		Mockito.when(this.request.getContextPath()).thenReturn(CONTEXT_PATH);
	}

	@Test
	public void testProcessSelectionSuccess() throws IOException {

		Mockito.when(this.etlService.checkOutOfBoundsData(this.userSelection)).thenReturn(true);

		SelectRowsForm form = new SelectRowsForm();
		form.setHeaderRow(0);
		form.setContentRow(1);
		form.setObservationRows(100);
		form.setContentRowDisplayText("TEST");

		Map<String, Object> result = this.controller.processSelection(form, this.request);

		Assert.assertEquals(true, result.get("success"));
		Assert.assertEquals(CONTEXT_PATH + ImportObservationsController.URL, result.get("redirectUrl"));
		Assert.assertEquals(true, result.get("hasOutOfBoundsData"));
	}

	@Test
	public void testProcessSelectionFail() throws IOException {

		Mockito.when(this.etlService.checkOutOfBoundsData(this.userSelection)).thenThrow(new IOException());

		SelectRowsForm form = new SelectRowsForm();
		form.setHeaderRow(0);
		form.setContentRow(1);
		form.setObservationRows(100);
		form.setContentRowDisplayText("TEST");

		Map<String, Object> result = this.controller.processSelection(form, this.request);

		Assert.assertEquals(false, result.get("success"));
		Assert.assertEquals("An error occurred while reading the file.", result.get("message"));
	}

}
