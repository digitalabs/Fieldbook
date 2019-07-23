
package com.efficio.fieldbook.web.common.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportRow;
import org.generationcp.commons.pojo.GermplasmListExportInputValues;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.dao.GermplasmListDAO;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.interfaces.GermplasmExportSource;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.util.FieldbookListUtil;
import org.springframework.beans.factory.annotation.Configurable;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.controller.ExportGermplasmListController;
import com.efficio.fieldbook.web.common.service.ExportGermplasmListService;

@Configurable
public class ExportGermplasmListServiceImpl implements ExportGermplasmListService {

	@Resource
	private OntologyService ontologyService;

	@Resource
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private UserSelection userSelection;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private GermplasmListManager germplasmListManager;

	@Resource
	private GermplasmExportService germplasmExportService;

	@Resource
	private InventoryDataManager inventoryDataManager;

	public ExportGermplasmListServiceImpl() {

	}

	@Override
	public void exportGermplasmListXLS(final String fileNamePath, final int listId, final Map<String, Boolean> visibleColumns) throws GermplasmListExporterException {

		try {
			final GermplasmListExportInputValues input = this.setUpInput(fileNamePath, listId, visibleColumns);

			this.germplasmExportService.generateGermplasmListExcelFile(input);

		} catch (final MiddlewareQueryException e) {
			throw new GermplasmListExporterException("Error with exporting germplasm list to XLS.", e);
		}

	}

	GermplasmListExportInputValues setUpInput(final String fileNamePath, final int listId, final Map<String, Boolean> visibleColumns) {
		final GermplasmListExportInputValues input = new GermplasmListExportInputValues();
		input.setFileName(fileNamePath);
		GermplasmList germplasmList = this.fieldbookMiddlewareService.getGermplasmListById(listId);

		final int studyId = this.userSelection.getWorkbook().getStudyDetails().getId();

		// retrieval of germplasm list data from snapshot list
		final List<GermplasmList> germplasmLists = this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.STUDY);
		List<? extends GermplasmExportSource> germplasmlistData = new ArrayList<>();
		if (germplasmLists != null && !germplasmLists.isEmpty()) {
			germplasmList = germplasmLists.get(0);

			if (germplasmList != null && germplasmList.getListRef() != null) {
				// set the ImportedGermplasmListMainInfo to the List reference of the list, so that it still points to the original list
				this.userSelection.getImportedGermplasmMainInfo().setListId(germplasmList.getListRef());
			}
			final List<ListDataProject> listDataProjects = this.germplasmListManager.retrieveSnapshotListData(germplasmList.getId());
			FieldbookListUtil.populateStockIdInListDataProject(listDataProjects, inventoryDataManager);
			germplasmlistData = listDataProjects;
		}

		this.setExportListTypeFromOriginalGermplasm(germplasmList);

		final List<ValueReference> possibleValues =
				this.getPossibleValues(this.userSelection.getPlotsLevelList(), TermId.ENTRY_TYPE.getId());
		this.processEntryTypeCode(germplasmlistData, possibleValues);

		input.setGermplasmList(germplasmList);

		input.setListData(germplasmlistData);

		input.setOwnerName(this.fieldbookMiddlewareService.getOwnerListName(germplasmList.getUserId()));

		final Integer currentLocalIbdbUserId = this.contextUtil.getCurrentWorkbenchUserId();
		input.setCurrentLocalIbdbUserId(currentLocalIbdbUserId);
		input.setExporterName(this.fieldbookMiddlewareService.getOwnerListName(currentLocalIbdbUserId));
		input.setVisibleColumnMap(visibleColumns);

		// Get the variables that will be put into the Inventory Section
		input.setInventoryVariableMap(this.extractInventoryVariableMapFromVisibleColumns(visibleColumns));

		// We do not need the inventory variables in visibleColumns anymore so we have to remove them, since variables in Inventory Section will come from
		// GermplasmListExportInputValues.InventoryVariableMap.
		this.removeInventoryVariableMapFromVisibleColumns(visibleColumns);

		input.setColumnTermMap(this.generateColumnStandardVariableMap(visibleColumns));
		return input;
	}

	/**
	 * Extracts the inventory variables from the visibleColumns map.
	 *
	 * @param visibleColumns
	 * @return
	 */
	Map<Integer,Variable> extractInventoryVariableMapFromVisibleColumns(final Map<String, Boolean> visibleColumns) {

		final Map<Integer, Variable> inventontoryVariableMap = new HashMap<>();

		final Iterator<Map.Entry<String, Boolean>> iterator = visibleColumns.entrySet().iterator();

		while (iterator.hasNext()) {

			final Map.Entry<String, Boolean> entry = iterator.next();
			final String termId = entry.getKey();
			final Boolean isVisible = entry.getValue();
			if (isVisible && isInventoryVariable(termId)) {
				addVariableToMap(inventontoryVariableMap, Integer.valueOf(termId));
			}
		}
		return inventontoryVariableMap;

	}

	/**
	 * Removes inventory variables from the visibleColumns map.
	 *
	 * @param visibleColumns
	 * @return
	 */
	void removeInventoryVariableMapFromVisibleColumns(final Map<String, Boolean> visibleColumns) {

		final Iterator<Map.Entry<String, Boolean>> iterator = visibleColumns.entrySet().iterator();

		while (iterator.hasNext()) {

			final Map.Entry<String, Boolean> entry = iterator.next();
			final String termId = entry.getKey();
			if (isInventoryVariable(termId)) {
				iterator.remove();
			}
		}

	}

	boolean isInventoryVariable(final String termId) {
		return termId.equals(String.valueOf(TermId.STOCKID.getId())) || termId.equals(String.valueOf(TermId.SEED_AMOUNT_G.getId()));
	}

	void addVariableToMap(final Map<Integer, Variable> variableMap, final int termId) {

		final Variable variable =
				this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), termId, false);
		if (variable != null) {
			variableMap.put(variable.getId(), variable);
		}

	}

	private Map<Integer, Term> generateColumnStandardVariableMap(final Map<String, Boolean> visibleColumnMap) {

		final Map<Integer, Term> standardVariableMap = new HashMap<>();

		if (this.userSelection.getPlotsLevelList() != null) {
			for (final SettingDetail settingDetail : this.userSelection.getPlotsLevelList()) {
				final Boolean isVisible = visibleColumnMap.get(settingDetail.getVariable().getCvTermId().toString());
				if (!settingDetail.isHidden() && isVisible != null && isVisible) {
					final Integer variableId = settingDetail.getVariable().getCvTermId();
					final Variable variable =
							this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), variableId, false);
					standardVariableMap.put(variableId, variable);
				}
			}
		}


		return standardVariableMap;
	}

	protected List<ValueReference> getPossibleValues(final List<SettingDetail> settingDetails, final int termId) {

		for (final SettingDetail settingDetail : settingDetails) {
			if (Objects.equals(settingDetail.getVariable().getCvTermId(), termId)) {
				return settingDetail.getPossibleValues();
			}
		}

		return new ArrayList<>();

	}

	protected void processEntryTypeCode(final List<? extends GermplasmExportSource> listData, final List<ValueReference> possibleValues) {

		for (final GermplasmExportSource data : listData) {
			if (possibleValues != null && !possibleValues.isEmpty()) {
				for (final ValueReference possibleValue : possibleValues) {
					if (possibleValue.getId().equals(Integer.valueOf(data.getCheckType().toString()))) {
						((ListDataProject) data).setCheckTypeDescription(possibleValue.getName());
					}
				}
			} else {
				((ListDataProject) data).setCheckTypeDescription(String.valueOf(data.getCheckType()));
			}
		}
	}

	@Override
	public void exportGermplasmListCSV(final String fileNamePath, final Map<String, Boolean> visibleColumns) throws GermplasmListExporterException {

		final List<ExportRow> exportRows = this.getExportColumnValuesFromTable(visibleColumns);
		final List<ExportColumnHeader> exportColumnHeaders = this.getExportColumnHeadersFromTable(visibleColumns);

		try {

			this.germplasmExportService.generateCSVFile(exportRows, exportColumnHeaders, fileNamePath);

		} catch (final IOException e) {
			throw new GermplasmListExporterException("Error with exporting list to CSV File.", e);
		}

	}

	protected List<ExportColumnHeader> getExportColumnHeadersFromTable(final Map<String, Boolean> visibleColumns) {

		final List<ExportColumnHeader> exportColumnHeaders = new ArrayList<>();

		final List<SettingDetail> factorsList = this.userSelection.getPlotsLevelList();

		for (final SettingDetail settingDetail : factorsList) {
			final Boolean isExist = visibleColumns.get(settingDetail.getVariable().getCvTermId().toString());
			if (!settingDetail.isHidden() && isExist != null && isExist == Boolean.TRUE) {
				exportColumnHeaders.add(
						new ExportColumnHeader(settingDetail.getVariable().getCvTermId(), settingDetail.getVariable().getName(), true));
			}
		}

		return exportColumnHeaders;
	}

	protected List<ExportRow> getExportColumnValuesFromTable(final Map<String, Boolean> visibleColumns) {

		final List<ExportRow> exportColumnValues = new ArrayList<>();

		final List<SettingDetail> factorsList = this.userSelection.getPlotsLevelList();
		final List<ImportedGermplasm> listData = this.getImportedGermplasm();

		for (final ImportedGermplasm data : listData) {
			final ExportRow row = new ExportRow();

			for (final SettingDetail settingDetail : factorsList) {
				final Integer termId = settingDetail.getVariable().getCvTermId();
				row.addColumnValue(termId,
						this.getGermplasmInfo(settingDetail.getVariable().getCvTermId().toString(), data, settingDetail));
			}

			exportColumnValues.add(row);
		}

		return exportColumnValues;
	}

	protected List<ImportedGermplasm> getImportedGermplasm() {
		return this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
	}

	protected String getGermplasmInfo(final String termId, final ImportedGermplasm germplasm, final SettingDetail settingDetail) {
		String val = "";
		if (termId != null && NumberUtils.isNumber(termId)) {
			final Integer term = Integer.valueOf(termId);
			if (term.intValue() == TermId.GID.getId()) {
				val = germplasm.getGid().toString();
			} else if (term.intValue() == TermId.ENTRY_CODE.getId()) {
				val = germplasm.getEntryCode().toString();
			} else if (term.intValue() == TermId.ENTRY_NO.getId()) {
				val = germplasm.getEntryId().toString();
			} else if (term.intValue() == TermId.SOURCE.getId() || term.intValue() == TermId.GERMPLASM_SOURCE.getId()) {
				val = germplasm.getSource().toString();
			} else if (term.intValue() == TermId.CROSS.getId()) {
				val = germplasm.getCross().toString();
			} else if (term.intValue() == TermId.DESIG.getId()) {
				val = germplasm.getDesig().toString();
			} else if (term.intValue() == TermId.CHECK.getId()) {
				// get the code of ENTRY_TYPE - CATEGORICAL FACTOR
				val = this.getCategoricalCodeValue(germplasm, settingDetail);
			} else if (term == TermId.GROUPGID.getId()) {
				val = germplasm.getMgid().toString();
			} else if (term == TermId.STOCKID.getId()) {
				val = germplasm.getStockIDs().toString();
			}
		}
		return val;
	}

	protected String getCategoricalCodeValue(final ImportedGermplasm germplasm, final SettingDetail settingDetail) {
		String val = "";
		if (settingDetail.getPossibleValues() != null) {
			for (final ValueReference possibleValue : settingDetail.getPossibleValues()) {
				if (possibleValue.getId().equals(Integer.valueOf(germplasm.getEntryTypeValue().toString()))) {
					val = possibleValue.getName();
				}
			}
		} else {
			val = germplasm.getEntryTypeValue().toString();
		}

		return val;
	}

	protected void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	protected UserSelection getUserSelection() {
		return this.userSelection;
	}

	protected void setUserSelection(final UserSelection userSelection) {
		this.userSelection = userSelection;
	}

	protected FieldbookService getFieldbookMiddlewareService() {
		return this.fieldbookMiddlewareService;
	}

	protected void setFieldbookMiddlewareService(final FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	protected OntologyService getOntologyService() {
		return this.ontologyService;
	}

	protected void setOntologyService(final OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}

	protected void setExportListTypeFromOriginalGermplasm(final GermplasmList list) throws MiddlewareQueryException {
		if (list != null && list.getListRef() != null) {
			final GermplasmList origList = this.fieldbookMiddlewareService.getGermplasmListById(list.getListRef());

			if (origList != null) {
				if (origList.getStatus() != null && origList.getStatus().intValue() != GermplasmListDAO.STATUS_DELETED.intValue()) {
					list.setType(origList.getType());
				} else {
					list.setType(ExportGermplasmListController.GERPLASM_TYPE_LST);
				}
			}
		}
	}
}
