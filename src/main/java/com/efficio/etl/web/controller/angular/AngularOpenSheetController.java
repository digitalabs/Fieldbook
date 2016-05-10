
package com.efficio.etl.web.controller.angular;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
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

	public static final String URL = "/workbook/openSheet";

	public final static int ROW_COUNT_PER_SCREEN = 10;
	public final static int MAX_DISPLAY_CHARACTER_PER_ROW = 60;

	@Resource
	private ETLService etlService;

	@Resource(name = "etlUserSelection")
	private UserSelection userSelection;

	@Override
	@RequestMapping(method = RequestMethod.GET)
	public String show(Model model) {

		try {
			Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);

			model.addAttribute("displayedRows", AngularOpenSheetController.ROW_COUNT_PER_SCREEN);
			List<IndexValueDTO> columnHeaders =
					this.etlService.retrieveColumnInformation(workbook, this.userSelection.getSelectedSheet(),
							this.userSelection.getHeaderRowIndex());
			model.addAttribute("columnHeaders", columnHeaders);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return super.show(model);
	}

	@Override
	public String getContentName() {
		return "angular/angularOpenSheet";
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public Map<String, Object> processSelection(@RequestBody SelectRowsForm form, HttpServletRequest request) {

		this.userSelection.setHeaderRowIndex(form.getHeaderRow());
		this.userSelection.setContentRowIndex(form.getContentRow());
		this.userSelection.setObservationRows(form.getObservationRows());
		this.userSelection.setContentRowDisplayText(form.getContentRowDisplayText());

		return this.wrapFormResult(ImportObservationsController.URL, request);
	}

	@ResponseBody
	@RequestMapping("/observationCount")
	public Map<String, Object> getObservationRowsForColumn(@RequestParam("columnIndex") int columnIndex,
			@RequestParam("contentIndex") int contentIndex) {
		Map<String, Object> returnVal = new HashMap<String, Object>();
		try {
			Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);

			try {
				columnIndex = this.getValidIndexColumnIndex(workbook);// override columnIndex because the indexColumn is no longer displayed
																		// in the page
			} catch (Exception e) {
				e.printStackTrace();
			}
			Integer observationCount =
					this.etlService.calculateObservationRows(workbook, this.userSelection.getSelectedSheet(), contentIndex, columnIndex);

			returnVal.put("value", observationCount);
			returnVal.put("status", "success");

			return returnVal;
		} catch (IOException e) {
			e.printStackTrace();
			returnVal.put("status", "error");
			returnVal.put("message", e.getMessage());
		}

		return returnVal;
	}

	@ResponseBody
	@RequestMapping("/columnInfo/{rowIndex}")
	public List<IndexValueDTO> getUpdatedColumnInfo(@PathVariable int rowIndex) {
		try {
			Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);
			return this.etlService.retrieveColumnInformation(workbook, this.userSelection.getSelectedSheet(), rowIndex);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new ArrayList<IndexValueDTO>();
	}

	@ResponseBody
	@RequestMapping(value = "/displayRow", params = "list=true")
	public List<RowDTO> getUpdatedRowDisplayHTML(@RequestParam(value = "lastRowIndex") Integer lastRowIndex, @RequestParam(
			value = "startRowIndex", required = false) Integer startRow) {
		try {
			Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);

			if (startRow == null) {
				startRow = 0;
			}
			if (startRow <= this.userSelection.getHeaderRowIndex()) {
				startRow = this.userSelection.getHeaderRowIndex() + 1;
			}

			int count = this.etlService.getAvailableRowsForDisplay(workbook, this.userSelection.getSelectedSheet());

			// position of header row is subtracted from count to give
			if (lastRowIndex > count) {
				lastRowIndex = count;
			}

			List<RowDTO> rowList =
					this.etlService.retrieveRowInformation(workbook, this.userSelection.getSelectedSheet(), startRow, lastRowIndex,
							AngularOpenSheetController.MAX_DISPLAY_CHARACTER_PER_ROW);

			return rowList;

		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<RowDTO>();
		}
	}

	@ResponseBody
	@RequestMapping(value = "/displayRow", params = "count=true")
	public Map<String, Object> getMaximumRowDisplayCount() {
		Map<String, Object> returnValue = new HashMap<String, Object>();
		try {
			Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);
			Integer count = this.etlService.getAvailableRowsForDisplay(workbook, this.userSelection);
			returnValue.put("value", count);
			returnValue.put("status", "ok");
		} catch (IOException e) {
			e.printStackTrace();
			returnValue.put("status", "error");
		}

		return returnValue;
	}

	@ModelAttribute("selectRowsForm")
	public SelectRowsForm getSelectRowsForm() {
		SelectRowsForm selectRowsForm = new SelectRowsForm();
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

	public void setEtlService(ETLService etlService) {
		this.etlService = etlService;
	}

	public void setUserSelection(UserSelection userSelection) {
		this.userSelection = userSelection;
	}

	@Override
	public UserSelection getUserSelection() {
		return this.userSelection;
	}

	public int getValidIndexColumnIndex(Workbook workbook) throws Exception {
		boolean isMeansDataImport =
				this.userSelection.getDatasetType() != null
						&& this.userSelection.getDatasetType().intValue() == DataSetType.MEANS_DATA.getId();
		org.generationcp.middleware.domain.etl.Workbook importData =
				this.etlService.retrieveAndSetProjectOntology(this.userSelection, isMeansDataImport);
		List<String> fileHeaders = this.etlService.retrieveColumnHeaders(workbook, this.userSelection);
		List<MeasurementVariable> studyHeaders = importData.getAllVariables();
		return this.etlService.getIndexColumnIndex(fileHeaders, studyHeaders);
	}
}
