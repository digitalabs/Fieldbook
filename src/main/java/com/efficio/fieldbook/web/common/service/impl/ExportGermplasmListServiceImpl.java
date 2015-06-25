
package com.efficio.fieldbook.web.common.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.pojo.GermplasmListExportInputValues;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.efficio.fieldbook.service.GermplasmExportService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.ExportGermplasmListService;

@Configurable
public class ExportGermplasmListServiceImpl implements ExportGermplasmListService {

	private static final Logger LOG = LoggerFactory.getLogger(ExportGermplasmListServiceImpl.class);

	@Resource
	private OntologyService ontologyService;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private UserSelection userSelection;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private GermplasmListManager germplasmListManager;

	@Resource
	private ResourceBundleMessageSource messageSource;

	public ExportGermplasmListServiceImpl() {

	}

	@Override
	public void exportGermplasmListXLS(String fileNamePath, int listId, Map<String, Boolean> visibleColumns, Boolean isNursery)
			throws GermplasmListExporterException {

		GermplasmListExportInputValues input = new GermplasmListExportInputValues();
		input.setFileName(fileNamePath);

		try {

			GermplasmList germplasmList;

			germplasmList = this.fieldbookMiddlewareService.getGermplasmListById(listId);

			input.setGermplasmList(germplasmList);

			input.setOwnerName(this.fieldbookMiddlewareService.getOwnerListName(germplasmList.getUserId()));

			Integer currentLocalIbdbUserId = this.contextUtil.getCurrentUserLocalId();
			input.setCurrentLocalIbdbUserId(currentLocalIbdbUserId);

			input.setExporterName(this.fieldbookMiddlewareService.getOwnerListName(currentLocalIbdbUserId));

			input.setVisibleColumnMap(visibleColumns);

			GermplasmExportService exportService = this.getExportService(this.userSelection, isNursery);
			exportService.generateGermplasmListExcelFile(input);

		} catch (MiddlewareQueryException e) {
			throw new GermplasmListExporterException("Error with exporting germplasm list to XLS.", e);
		}

	}

	@Override
	public void exportGermplasmListCSV(String fileNamePath, Map<String, Boolean> visibleColumns, Boolean isNursery)
			throws GermplasmListExporterException {

		List<Map<Integer, ExportColumnValue>> exportColumnValues = this.getExportColumnValuesFromTable(visibleColumns, isNursery);
		List<ExportColumnHeader> exportColumnHeaders = this.getExportColumnHeadersFromTable(visibleColumns, isNursery);

		try {

			GermplasmExportService exportService = this.getExportService(this.userSelection, isNursery);
			exportService.generateCSVFile(exportColumnValues, exportColumnHeaders, fileNamePath);

		} catch (IOException e) {
			throw new GermplasmListExporterException("Error with exporting list to CSV File.", e);
		}

	}

	protected List<ExportColumnHeader> getExportColumnHeadersFromTable(Map<String, Boolean> visibleColumns, Boolean isNursery) {

		List<ExportColumnHeader> exportColumnHeaders = new ArrayList<>();

		List<SettingDetail> factorsList = this.userSelection.getPlotsLevelList();

		if (isNursery) {

			try {
				StandardVariable gid = this.ontologyService.getStandardVariable(TermId.GID.getId());
				exportColumnHeaders.add(new ExportColumnHeader(TermId.GID.getId(), gid.getName(), true));

				StandardVariable cross = this.ontologyService.getStandardVariable(TermId.CROSS.getId());
				exportColumnHeaders.add(new ExportColumnHeader(TermId.CROSS.getId(), cross.getName(), true));

				StandardVariable entryNo = this.ontologyService.getStandardVariable(TermId.ENTRY_NO.getId());
				exportColumnHeaders.add(new ExportColumnHeader(TermId.ENTRY_NO.getId(), entryNo.getName(), true));

				StandardVariable desig = this.ontologyService.getStandardVariable(TermId.DESIG.getId());
				exportColumnHeaders.add(new ExportColumnHeader(TermId.DESIG.getId(), desig.getName(), true));

				StandardVariable seedSource = this.ontologyService.getStandardVariable(TermId.SEED_SOURCE.getId());
				exportColumnHeaders.add(new ExportColumnHeader(TermId.SEED_SOURCE.getId(), seedSource.getName(), true));

				StandardVariable entryCode = this.ontologyService.getStandardVariable(TermId.ENTRY_CODE.getId());
				exportColumnHeaders.add(new ExportColumnHeader(TermId.ENTRY_CODE.getId(), entryCode.getName(), true));
			} catch (MiddlewareQueryException e) {
				ExportGermplasmListServiceImpl.LOG.error(e.getMessage(), e);
			}

		} else {

			for (SettingDetail settingDetail : factorsList) {
				Boolean isExist = visibleColumns.get(settingDetail.getVariable().getCvTermId().toString());
				if (!settingDetail.isHidden() && isExist != null && isExist == Boolean.TRUE) {
					exportColumnHeaders.add(new ExportColumnHeader(settingDetail.getVariable().getCvTermId(), settingDetail.getVariable()
							.getName(), true));
				}
			}

		}

		return exportColumnHeaders;
	}

	protected List<Map<Integer, ExportColumnValue>> getExportColumnValuesFromTable(Map<String, Boolean> visibleColumns, Boolean isNursery) {

		List<Map<Integer, ExportColumnValue>> exportColumnValues = new ArrayList<>();

		List<SettingDetail> factorsList = this.userSelection.getPlotsLevelList();
		List<ImportedGermplasm> listData = this.getImportedGermplasm();

		for (ImportedGermplasm data : listData) {

			Map<Integer, ExportColumnValue> row = new HashMap<>();

			if (isNursery) {

				row.put(TermId.GID.getId(),
						new ExportColumnValue(TermId.GID.getId(), this.getGermplasmInfo(String.valueOf(TermId.GID.getId()), data, null)));

				row.put(TermId.CROSS.getId(),
						new ExportColumnValue(TermId.CROSS.getId(), this.getGermplasmInfo(String.valueOf(TermId.CROSS.getId()), data, null)));

				row.put(TermId.ENTRY_NO.getId(),
						new ExportColumnValue(TermId.ENTRY_NO.getId(), this.getGermplasmInfo(String.valueOf(TermId.ENTRY_NO.getId()), data,
								null)));

				row.put(TermId.DESIG.getId(),
						new ExportColumnValue(TermId.DESIG.getId(), this.getGermplasmInfo(String.valueOf(TermId.DESIG.getId()), data, null)));

				row.put(TermId.SEED_SOURCE.getId(),
						new ExportColumnValue(TermId.SEED_SOURCE.getId(), this.getGermplasmInfo(String.valueOf(TermId.SEED_SOURCE.getId()),
								data, null)));

				row.put(TermId.ENTRY_CODE.getId(),
						new ExportColumnValue(TermId.ENTRY_CODE.getId(), this.getGermplasmInfo(String.valueOf(TermId.ENTRY_CODE.getId()),
								data, null)));

			} else {
				for (SettingDetail settingDetail : factorsList) {
					Integer termId = settingDetail.getVariable().getCvTermId();
					row.put(termId,
							new ExportColumnValue(termId, this.getGermplasmInfo(settingDetail.getVariable().getCvTermId().toString(), data,
									settingDetail)));
				}
			}

			exportColumnValues.add(row);
		}

		return exportColumnValues;
	}

	protected List<ImportedGermplasm> getImportedGermplasm() {
		return this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
	}

	protected String getGermplasmInfo(String termId, ImportedGermplasm germplasm, SettingDetail settingDetail) {
		String val = "";
		if (termId != null && NumberUtils.isNumber(termId)) {
			Integer term = Integer.valueOf(termId);
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
			}
		}
		return val;
	}

	protected String getCategoricalCodeValue(ImportedGermplasm germplasm, SettingDetail settingDetail) {
		String val = "";
		if (settingDetail.getPossibleValues() != null) {
			for (ValueReference possibleValue : settingDetail.getPossibleValues()) {
				if (possibleValue.getId().equals(Integer.valueOf(germplasm.getCheck().toString()))) {
					val = possibleValue.getName();
				}
			}
		} else {
			val = germplasm.getCheck().toString();
		}

		return val;
	}

	protected GermplasmExportService getExportService(UserSelection userSelection, Boolean isNursery) {

		return new GermplasmExportService(this.ontologyService, userSelection, isNursery);
	}

	protected void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	protected void setGermplasmListManager(GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	protected UserSelection getUserSelection() {
		return this.userSelection;
	}

	protected void setUserSelection(UserSelection userSelection) {
		this.userSelection = userSelection;
	}

	protected FieldbookService getFieldbookMiddlewareService() {
		return this.fieldbookMiddlewareService;
	}

	protected void setFieldbookMiddlewareService(FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	protected OntologyService getOntologyService() {
		return this.ontologyService;
	}

	protected void setOntologyService(OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}

}
