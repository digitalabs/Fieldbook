
package com.efficio.etl.web.controller.angular;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.pojos.dms.DatasetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.AbstractBaseETLController;
import com.efficio.etl.web.ImportObservationsController;
import com.efficio.etl.web.bean.IndexValueDTO;
import com.efficio.etl.web.bean.RowDTO;
import com.efficio.etl.web.bean.SelectRowsForm;
import com.efficio.etl.web.bean.UserSelection;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

@Controller
@RequestMapping(AngularOpenSheetController.URL)
public class AngularOpenSheetController extends AbstractBaseETLController {

	private static final String MESSAGE = "message";
	private static final String VALUE = "value";
	private static final String STATUS = "status";
	public static final String URL = "/etl/workbook/openSheet";

	public static final int ROW_COUNT_PER_SCREEN = 10;
	public static final int MAX_DISPLAY_CHARACTER_PER_ROW = 60;

	private static final Logger LOG = LoggerFactory.getLogger(AngularOpenSheetController.class);

	@Resource
	private ETLService etlService;

	@Resource(name = "etlUserSelection")
	private UserSelection userSelection;

	@Override
	@RequestMapping(method = RequestMethod.GET)
	public String show(final Model model) {

		try {
			final Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);

			model.addAttribute("displayedRows", AngularOpenSheetController.ROW_COUNT_PER_SCREEN);
			final List<IndexValueDTO> columnHeaders =
					this.etlService.retrieveColumnInformation(workbook, this.userSelection.getSelectedSheet(),
							this.userSelection.getHeaderRowIndex());
			model.addAttribute("columnHeaders", columnHeaders);

		} catch (final IOException e) {
			AngularOpenSheetController.LOG.error(e.getMessage(), e);
		}

		return super.show(model);
	}

	@Override
	public String getContentName() {
		return "etl/angular/angularOpenSheet";
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public Map<String, Object> processSelection(@RequestBody final SelectRowsForm form, final HttpServletRequest request) {

		this.userSelection.setHeaderRowIndex(form.getHeaderRow());
		this.userSelection.setContentRowIndex(form.getContentRow());
		this.userSelection.setObservationRows(form.getObservationRows());
		this.userSelection.setContentRowDisplayText(form.getContentRowDisplayText());

		Map<String, Object> result = null;
		try {

			result = this.wrapFormResult(ImportObservationsController.URL, request);
			result.put("hasOutOfBoundsData", this.etlService.checkOutOfBoundsData(this.userSelection));

		} catch (final IOException e) {

			LOG.error(e.getMessage(), e);

			result = new HashMap<String, Object>();
			result.put("success", false);
			result.put(MESSAGE, "An error occurred while reading the file.");
		}

		return result;
	}

	@ResponseBody
	@RequestMapping("/observationCount")
	public Map<String, Object> getObservationRowsForColumn(@RequestParam("columnIndex") final int columnIndex,
			@RequestParam("contentIndex") final int contentIndex) {
		final Map<String, Object> returnVal = new HashMap<String, Object>();
		final Workbook workbook;

		try {
			workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);

			returnVal.put(STATUS, "error");
			returnVal.put(MESSAGE, e.getMessage());

			return returnVal;
		}

		// override columnIndex because the indexColumn is no longer
		// displayed in the page
		int validColumnIndex = columnIndex;

		try {
			validColumnIndex = this.getValidIndexColumnIndex(workbook);
		} catch (final MiddlewareException e) {
			LOG.error(e.getMessage(), e);
		}

		final Integer observationCount =
				this.etlService.calculateObservationRows(workbook, this.userSelection.getSelectedSheet(), contentIndex, validColumnIndex);

		returnVal.put(VALUE, observationCount);
		returnVal.put(STATUS, "success");

		return returnVal;
	}

	@ResponseBody
	@RequestMapping("/columnInfo/{rowIndex}")
	public List<IndexValueDTO> getUpdatedColumnInfo(@PathVariable final int rowIndex) {
		try {
			final Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);
			return this.etlService.retrieveColumnInformation(workbook, this.userSelection.getSelectedSheet(), rowIndex);
		} catch (final IOException e) {
			AngularOpenSheetController.LOG.error(e.getMessage(), e);
		}

		return new ArrayList<IndexValueDTO>();
	}

	@ResponseBody
	@RequestMapping(value = "/displayRow", params = "list=true")
	public List<RowDTO> getUpdatedRowDisplayHTML(@RequestParam(value = "lastRowIndex") final Integer lastRowIndex, @RequestParam(
			value = "startRowIndex", required = false) final Integer startRow) {

		Integer lastRowIndexLocal = lastRowIndex;
		Integer startRowIndexLocal = startRow;

		try {

			final Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);

			if (startRowIndexLocal == null) {
				startRowIndexLocal = 0;
			}
			if (startRowIndexLocal <= this.userSelection.getHeaderRowIndex()) {
				startRowIndexLocal = this.userSelection.getHeaderRowIndex() + 1;
			}

			final int count = this.etlService.getAvailableRowsForDisplay(workbook, this.userSelection.getSelectedSheet());

			// position of header row is subtracted from count to give
			if (lastRowIndexLocal > count) {
				lastRowIndexLocal = count;
			}

			return this.etlService.retrieveRowInformation(workbook, this.userSelection.getSelectedSheet(), startRowIndexLocal,
					lastRowIndexLocal, AngularOpenSheetController.MAX_DISPLAY_CHARACTER_PER_ROW);

		} catch (final IOException e) {
			AngularOpenSheetController.LOG.error(e.getMessage(), e);
			return new ArrayList<RowDTO>();
		}
	}

	@ResponseBody
	@RequestMapping(value = "/displayRow", params = "count=true")
	public Map<String, Object> getMaximumRowDisplayCount() {
		final Map<String, Object> returnValue = new HashMap<String, Object>();
		try {
			final Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);
			final Integer count = this.etlService.getAvailableRowsForDisplay(workbook, this.userSelection);
			returnValue.put(VALUE, count);
			returnValue.put(STATUS, "ok");
		} catch (final IOException e) {
			AngularOpenSheetController.LOG.error(e.getMessage(), e);LOG.error(e.getMessage(), e);
			returnValue.put(STATUS, "error");
		}

		return returnValue;
	}

	@ModelAttribute("selectRowsForm")
	public SelectRowsForm getSelectRowsForm() {
		final SelectRowsForm selectRowsForm = new SelectRowsForm();
		selectRowsForm.setHeaderRow(this.userSelection.getHeaderRowIndex());
		selectRowsForm.setContentRow(this.userSelection.getContentRowIndex());
		selectRowsForm.setObservationRows(this.userSelection.getObservationRows());
		selectRowsForm.setHeaderRowDisplayText(this.userSelection.getHeaderRowDisplayText());
		selectRowsForm.setContentRowDisplayText(this.userSelection.getContentRowDisplayText());

		// if no value of index column available, but with observation count, assume user used manual input
		if (this.userSelection.getObservationRows() != null && this.userSelection.getObservationRows() > 0) {
			selectRowsForm.setNoObservationComputation(true);
		}

		return selectRowsForm;
	}

	public void setEtlService(final ETLService etlService) {
		this.etlService = etlService;
	}

	public void setUserSelection(final UserSelection userSelection) {
		this.userSelection = userSelection;
	}

	@Override
	public UserSelection getUserSelection() {
		return this.userSelection;
	}

	public int getValidIndexColumnIndex(final Workbook workbook) {
		final boolean isMeansDataImport =
				this.userSelection.getDatasetType() != null
						&& this.userSelection.getDatasetType().intValue() == DatasetType.MEANS_DATA;
		final org.generationcp.middleware.domain.etl.Workbook importData =
				this.etlService.retrieveAndSetProjectOntology(this.userSelection, isMeansDataImport);

		final List<String> fileHeaders =
				this.etlService.retrieveColumnHeaders(workbook, this.userSelection, this.etlService.headersContainsObsUnitId(importData));
		final List<MeasurementVariable> studyHeaders = importData.getAllVariables();
		return this.etlService.getIndexColumnIndex(fileHeaders, studyHeaders);
	}

}
