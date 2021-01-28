/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package com.efficio.fieldbook.web.fieldmap.controller;

import com.efficio.fieldbook.service.api.ExportFieldmapService;
import com.efficio.fieldbook.service.api.FieldMapService;
import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.SelectedFieldmapList;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;
import com.efficio.fieldbook.web.label.printing.service.FieldPlotLayoutIterator;
import com.efficio.fieldbook.web.trial.controller.ManageTrialController;
import org.generationcp.commons.constant.AppConstants;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.commons.util.StringUtil;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.generationcp.middleware.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class GenerateFieldmapController.
 * <p/>
 * Generates the final fieldmap for the step 3.
 */
@Controller @RequestMapping({GenerateFieldmapController.URL})
public class GenerateFieldmapController extends AbstractBaseFieldbookController {

	/**
	 * The Constant URL.
	 */
	public static final String URL = "/Fieldmap/generateFieldmapView";
	public static final String REDIRECT = "redirect:";
	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(GenerateFieldmapController.class);
	/**
	 * The user fieldmap.
	 */
	@Resource
	private UserFieldmap userFieldmap;
	/**
	 * The fieldmap service.
	 */
	@Resource
	private FieldMapService fieldmapService;

	@Resource
	private FieldPlotLayoutIterator horizontalFieldMapLayoutIterator;
	/**
	 * The fieldbook middleware service.
	 */
	@Resource
	private FieldbookService fieldbookMiddlewareService;
	/**
	 * The export excel service.
	 */
	@Resource
	private ExportFieldmapService exportFieldmapService;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	private final InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();

	/**
	 * Show generated fieldmap.
	 *
	 * @param form the form
	 * @param model the model
	 * @return the string
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String showGeneratedFieldmap(@ModelAttribute("fieldmapForm") final FieldmapForm form, final Model model) {

		form.setUserFieldmap(this.userFieldmap);

		return super.show(model);
	}

	/**
	 * View fieldmap.
	 *
	 * @param form the form
	 * @param model the model
	 * @param datasetId the dataset id
	 * @param instanceId the instance id
	 * @param studyType the study type
	 * @return the string
	 */
	@RequestMapping(value = "/viewFieldmap/{studyType}/{datasetId}/{instanceId}", method = RequestMethod.GET)
	public String viewFieldmap(@ModelAttribute("fieldmapForm") final FieldmapForm form, final Model model, @PathVariable final Integer datasetId,
			@PathVariable final Integer instanceId, @PathVariable final String studyType) {
		try {

			this.userFieldmap.setSelectedDatasetId(datasetId);
			this.userFieldmap.setSelectedInstanceId(instanceId);

			this.userFieldmap.setSelectedFieldMaps(this.fieldbookMiddlewareService
					.getAllFieldMapsInBlockByTrialInstanceId(datasetId, instanceId, this.crossExpansionProperties));

			final FieldMapTrialInstanceInfo trialInfo =
					this.userFieldmap.getSelectedTrialInstanceByDatasetIdAndEnvironmentId(datasetId, instanceId);
			if (trialInfo != null) {
				this.userFieldmap.setNumberOfRangesInBlock(Util.getIntValue(trialInfo.getRangesInBlock()));
				this.userFieldmap.setNumberOfRowsInBlock(Util.getIntValue(trialInfo.getRowsInBlock()));
				this.userFieldmap.setNumberOfEntries((long) this.userFieldmap.getAllSelectedFieldMapLabels(false).size());
				this.userFieldmap.setNumberOfRowsPerPlot(Util.getIntValue(trialInfo.getRowsPerPlot()));
				this.userFieldmap.setPlantingOrder(Util.getIntValue(trialInfo.getPlantingOrder()));
				this.userFieldmap.setBlockName(trialInfo.getBlockName());
				this.userFieldmap.setFieldName(trialInfo.getFieldName());
				this.userFieldmap.setLocationName(trialInfo.getLocationName());
				this.userFieldmap.setFieldMapLabels(this.userFieldmap.getAllSelectedFieldMapLabels(false));
				this.userFieldmap.setMachineRowCapacity(trialInfo.getMachineRowCapacity());
				this.userFieldmap.setBlockId(trialInfo.getBlockId());
				this.userFieldmap.setHasOverlappingCoordinates(trialInfo.isHasOverlappingCoordinates());

				final FieldPlotLayoutIterator plotIterator = this.horizontalFieldMapLayoutIterator;
				this.userFieldmap.setFieldmap(
						this.fieldmapService.generateFieldmap(this.userFieldmap, plotIterator, false, trialInfo.getDeletedPlots()));
			}
			this.userFieldmap.setSelectedFieldmapList(new SelectedFieldmapList(this.userFieldmap.getSelectedFieldMaps()));
			this.userFieldmap.setGenerated(false);
			form.setUserFieldmap(this.userFieldmap);

		} catch (final MiddlewareQueryException e) {
			GenerateFieldmapController.LOG.error(e.getMessage(), e);
		}
		return super.show(model);
	}

	@RequestMapping(value = "/exportExcel", method = RequestMethod.GET)
	public ResponseEntity<FileSystemResource> exportExcel(final HttpServletRequest request) throws FieldbookException {

		final FileExportInfo exportInfo;
		try {
			// changed selected name to block name for now
			final String fileName = StringUtil.isEmpty(this.userFieldmap.getBlockName()) ? this.userFieldmap.getLocationName() : this.userFieldmap.getBlockName();
			exportInfo = this.makeSafeFileName(fileName);
			this.exportFieldmapService.exportFieldMapToExcel(exportInfo.getFilePath(), this.userFieldmap);

			return FieldbookUtil.createResponseEntityForFileDownload(exportInfo.getFilePath(), exportInfo.getDownloadFileName());
		} catch (final IOException e) {
			throw new FieldbookException(e.getMessage(), e);
		}

	}

	FileExportInfo makeSafeFileName(final String filename) throws IOException {
		final String cleanFilename = FileNameGenerator.generateFileName(filename.replace(" ", ""), AppConstants.EXPORT_XLS_SUFFIX
			.getString(), false);
		final String outputFilepath = this.installationDirectoryUtil.getTempFileInOutputDirectoryForProjectAndTool(cleanFilename, AppConstants.EXPORT_XLS_SUFFIX.getString(), this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		return new FileExportInfo(outputFilepath, cleanFilename + AppConstants.EXPORT_XLS_SUFFIX.getString());
	}

	/**
	 * Submits the details.
	 *
	 * @param form the form
	 * @param model the model
	 * @return the string
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String submitDetails(@ModelAttribute("FieldmapForm") final FieldmapForm form, final Model model) {

		this.userFieldmap.setStartingColumn(form.getUserFieldmap().getStartingColumn());
		this.userFieldmap.setStartingRange(form.getUserFieldmap().getStartingRange());
		this.userFieldmap.setPlantingOrder(form.getUserFieldmap().getPlantingOrder());
		this.userFieldmap.setMachineRowCapacity(form.getUserFieldmap().getMachineRowCapacity());

		final int startRange = this.userFieldmap.getStartingRange() - 1;
		final int startCol = this.userFieldmap.getStartingColumn() - 1;
		final int rows = this.userFieldmap.getNumberOfRowsInBlock();
		final int ranges = this.userFieldmap.getNumberOfRangesInBlock();
		final int rowsPerPlot = this.userFieldmap.getNumberOfRowsPerPlot();
		final boolean isSerpentine = this.userFieldmap.getPlantingOrder() == 2;

		final int col = rows / rowsPerPlot;
		// should list here the deleted plot in col-range format
		final Map<String, String> deletedPlot = new HashMap<>();
		if (form.getMarkedCells() != null && !form.getMarkedCells().isEmpty()) {
			final List<String> markedCells = Arrays.asList(form.getMarkedCells().split(","));

			for (final String markedCell : markedCells) {
				deletedPlot.put(markedCell, markedCell);
			}
		}

		this.markDeletedPlots(form.getMarkedCells());

		final List<FieldMapLabel> labels = this.userFieldmap.getAllSelectedFieldMapLabelsToBeAdded(true);

		// we can add logic here to decide if its vertical or horizontal
		final FieldPlotLayoutIterator plotIterator = this.horizontalFieldMapLayoutIterator;
		final Plot[][] plots = plotIterator
			.createFieldMap(col, ranges, startRange, startCol, isSerpentine, deletedPlot, labels, this.userFieldmap.getFieldmap());
		this.userFieldmap.setFieldmap(plots);
		form.setUserFieldmap(this.userFieldmap);

		this.userFieldmap.setGenerated(true);

		return GenerateFieldmapController.REDIRECT + GenerateFieldmapController.URL;
	}

	/**
	 * Redirect to main screen.
	 *
	 * @param form the form
	 * @param model the model
	 * @return the string
	 */
	@RequestMapping(value = "/showMainPage", method = RequestMethod.GET)// TODO ESTE REDIRECT DEBE SER PARA ABRIR EL ESTUDIO DENUEVO VERIFICAR
	public String redirectToMainScreen(@ModelAttribute("fieldmapForm") final FieldmapForm form, final Model model) {
		return GenerateFieldmapController.REDIRECT + ManageTrialController.URL;
	}

	/**
	 * Gets the user fieldmap.
	 *
	 * @return the user fieldmap
	 */
	public UserFieldmap getUserFieldmap() {
		return this.userFieldmap;
	}

	/**
	 * Sets the user fieldmap.
	 *
	 * @param userFieldmap the new user fieldmap
	 */
	public void setUserFieldmap(final UserFieldmap userFieldmap) {
		this.userFieldmap = userFieldmap;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
	 */
	@Override
	public String getContentName() {
		return "Fieldmap/generateFieldmapView";
	}

	private void markDeletedPlots(final String deletedPlots) {
		final List<String> dpform = new ArrayList<>();
		if (deletedPlots != null) {
			final String[] dps = deletedPlots.split(",");
			for (final String deletedPlot : dps) {
				final String[] coordinates = deletedPlot.split("_");
				if (coordinates.length == 2 && NumberUtils.isNumber(coordinates[0]) && NumberUtils.isNumber(coordinates[1])) {
					dpform.add(coordinates[0] + "," + coordinates[1]);
				}
			}
		}
		this.userFieldmap.setDeletedPlots(dpform);
	}
}
