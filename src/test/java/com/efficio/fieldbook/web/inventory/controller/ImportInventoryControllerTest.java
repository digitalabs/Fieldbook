package com.efficio.fieldbook.web.inventory.controller;

import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.exception.FileParsingException;
import com.efficio.fieldbook.web.common.service.ImportInventoryService;
import com.efficio.fieldbook.web.inventory.bean.SeedSelection;
import com.efficio.fieldbook.web.inventory.form.SeedStoreForm;
import com.efficio.fieldbook.web.nursery.bean.ImportedInventoryList;
import com.efficio.fieldbook.web.nursery.form.ImportInventoryForm;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.InventoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 3/12/2015
 * Time: 3:18 PM
 */

@RunWith(MockitoJUnitRunner.class)
public class ImportInventoryControllerTest {
	@Mock
	private ImportInventoryService importInventoryService;

	@Mock
	private MessageSource messageSource;

	@Mock
	private InventoryService inventoryService;

	@Mock
	private SeedSelection seedSelection;

	@Mock
	private PaginationListSelection paginationListSelection;

	@InjectMocks
	private ImportInventoryController dut = spy(new ImportInventoryController());

	@Test
	public void testListTypeSwitchingAdvance()
			throws MiddlewareQueryException, FileParsingException {
		when(importInventoryService.mergeImportedData(anyListOf(InventoryDetails.class), any(
				ImportedInventoryList.class))).thenReturn(false);
		List<InventoryDetails> currentList = new ArrayList<>();

		when(inventoryService.getInventoryDetailsByGermplasmList(anyInt(), anyString()))
				.thenReturn(currentList);
		ImportInventoryForm form = new ImportInventoryForm();
		form.setImportSource(ImportInventoryController.ADVANCE_IMPORT_SOURCE);

		Map<String, Object> results = dut.importFile(form);

		ArgumentCaptor<String> listType = ArgumentCaptor.forClass(String.class);
		verify(inventoryService).getInventoryDetailsByGermplasmList(anyInt(), listType.capture());

		assertEquals(GermplasmListType.ADVANCED.name(), listType.getValue());
	}

	@Test
	public void testListTypeSwitchingCross() throws MiddlewareQueryException, FileParsingException {
		when(importInventoryService.mergeImportedData(anyListOf(InventoryDetails.class), any(
				ImportedInventoryList.class))).thenReturn(false);
		List<InventoryDetails> currentList = new ArrayList<>();

		when(inventoryService.getInventoryDetailsByGermplasmList(anyInt(), anyString()))
				.thenReturn(currentList);
		ImportInventoryForm form = new ImportInventoryForm();
		form.setImportSource(ImportInventoryController.CROSSES_IMPORT_SOURCE);

		Map<String, Object> results = dut.importFile(form);

		ArgumentCaptor<String> listType = ArgumentCaptor.forClass(String.class);
		verify(inventoryService).getInventoryDetailsByGermplasmList(anyInt(), listType.capture());

		assertEquals(GermplasmListType.CROSSES.name(), listType.getValue());
	}

	@Test
	public void testImportFileNoParsingErrorNoOverwrite() throws MiddlewareQueryException,
			FileParsingException {
		when(importInventoryService.mergeImportedData(anyListOf(InventoryDetails.class), any(
				ImportedInventoryList.class))).thenReturn(false);
		List<InventoryDetails> currentList = new ArrayList<>();

		when(inventoryService.getInventoryDetailsByGermplasmList(anyInt(), anyString()))
				.thenReturn(currentList);
		ImportInventoryForm form = new ImportInventoryForm();
		form.setImportSource(ImportInventoryController.ADVANCE_IMPORT_SOURCE);

		Map<String, Object> results = dut.importFile(form);

		assertEquals("Proper result not generated", 1,
				results.get(ImportInventoryController.IS_SUCCESS));
		assertNull("Overwrite improperly triggered",
				results.get(ImportInventoryController.IS_OVERWRITE));
	}

	@Test
	public void testImportFileNoParsingErrorWithOverwrite() throws MiddlewareQueryException,
			FileParsingException {
		when(importInventoryService.mergeImportedData(anyListOf(InventoryDetails.class), any(
				ImportedInventoryList.class))).thenReturn(true);
		List<InventoryDetails> currentList = new ArrayList<>();

		when(inventoryService.getInventoryDetailsByGermplasmList(anyInt(), anyString()))
				.thenReturn(currentList);

		ImportInventoryForm form = new ImportInventoryForm();
		form.setImportSource(ImportInventoryController.ADVANCE_IMPORT_SOURCE);

		Map<String, Object> results = dut.importFile(form);

		assertEquals("Proper result not generated", 1,
				results.get(ImportInventoryController.IS_SUCCESS));
		assertEquals("Overwrite improperly triggered", 1,
				results.get(ImportInventoryController.IS_OVERWRITE));
	}

	@Test
	public void testImportFileParsingError() throws MiddlewareQueryException,
			FileParsingException {
		when(importInventoryService.parseFile(any(MultipartFile.class)))
				.thenThrow(new FileParsingException("dummy message"));
		when(messageSource.getMessage(anyString(), any(Object[].class),
				any(Locale.class))).thenReturn(
				"ERROR MESSAGE");

		ImportInventoryForm form = new ImportInventoryForm();
		form.setImportSource(ImportInventoryController.ADVANCE_IMPORT_SOURCE);

		Map<String, Object> results = dut.importFile(form);

		assertEquals("Proper result not generated", 0,
				results.get(ImportInventoryController.IS_SUCCESS));
		assertEquals("ERROR MESSAGE", results.get("error"));
	}

	@Test
	public void testImportFileMWError() throws MiddlewareQueryException,
			FileParsingException {
		when(inventoryService.getInventoryDetailsByGermplasmList(anyInt(), anyString()))
				.thenThrow(MiddlewareQueryException.class);

		ImportInventoryForm form = new ImportInventoryForm();
		form.setImportSource(ImportInventoryController.ADVANCE_IMPORT_SOURCE);

		Map<String, Object> results = dut.importFile(form);

		assertEquals("Proper result not generated", 0,
				results.get(ImportInventoryController.IS_SUCCESS));
	}

	@Test
	public void testDisplayAdvanceGermplasmDetails() {
		List<InventoryDetails> detailList = new ArrayList<>();
		when(seedSelection.getInventoryList()).thenReturn(detailList);
		SeedStoreForm form = mock(SeedStoreForm.class);

		String result = dut.displayAdvanceGermplasmDetails(1, form, mock(Model.class));

		verify(paginationListSelection).addFinalAdvancedList("1", detailList);
		verify(form).setGidList("1");
		verify(form).setListId(1);
		verify(form).setCurrentPage(1);
		verify(form).setInventoryList(detailList);

		assertEquals("/NurseryManager/saveAdvanceInventoryImport", result);
	}

	@Test
	public void testSaveSuccess() throws MiddlewareQueryException {
		List<InventoryDetails> inventoryDetails = new ArrayList<>();
		ContextInfo info = mock(ContextInfo.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(seedSelection.getInventoryList()).thenReturn(inventoryDetails);

		doReturn(info).when(dut).getContextInfo(request);
		when(info.getloggedInUserId()).thenReturn(1);

		Map<String, Object> results = dut
				.saveInventoryImport(mock(ImportInventoryForm.class), request);

		assertEquals("Proper result not generated", 1,
				results.get(ImportInventoryController.IS_SUCCESS));

		verify(importInventoryService).saveUpdatedInventoryDetails(eq(inventoryDetails), eq(1),
				anyInt());
	}

	@Test
	public void testSaveFailure() throws MiddlewareQueryException {
		List<InventoryDetails> inventoryDetails = new ArrayList<>();
		ContextInfo info = mock(ContextInfo.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(seedSelection.getInventoryList()).thenReturn(inventoryDetails);

		when(importInventoryService.saveUpdatedInventoryDetails(anyList(),anyInt(), anyInt())).thenThrow(MiddlewareQueryException.class);

		doReturn(info).when(dut).getContextInfo(request);
		when(info.getloggedInUserId()).thenReturn(1);

		Map<String, Object> results = dut
				.saveInventoryImport(mock(ImportInventoryForm.class), request);

		assertEquals("Proper result not generated", 0,
				results.get(ImportInventoryController.IS_SUCCESS));
	}
}
