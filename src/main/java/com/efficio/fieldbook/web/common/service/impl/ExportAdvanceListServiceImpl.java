package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;

import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.service.ExportService;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.efficio.fieldbook.web.common.service.ExportAdvanceListService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.ZipUtil;

public class ExportAdvanceListServiceImpl implements ExportAdvanceListService {

	private static final Logger LOG = LoggerFactory.getLogger(ExportAdvanceListServiceImpl.class);

	@Resource
	private FieldbookProperties fieldbookProperties;
	@Resource
	private InventoryService inventoryMiddlewareService;
	@Resource
	public MessageSource messageSource;
	@Resource
	private FieldbookService fieldbookMiddlewareService;

	private final String ZIP_DEFAULT_FILENAME = "AdvancedList";

	// Temporary Id that we use to map it for the generation of export file
	protected static final Integer INVENTORY_AMOUNT = 90001;
	protected static final Integer INVENTORY_SCALE = 90002;
	protected static final Integer INVENTORY_COMMENT = 90003;
	
	private static final String NO_FILE = "noFile";

	@Override
	public File exportAdvanceGermplasmList(String delimitedAdvanceGermplasmListIds,
			String studyName, ExportService exportServiceImpl, String type) {
		List<Integer> advanceGermplasmListIds = this
				.parseDelimitedAdvanceGermplasmListIds(delimitedAdvanceGermplasmListIds);
		List<String> filenameList = new ArrayList<String>();
		String outputFilename = NO_FILE;
		String suffix = AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString().equalsIgnoreCase(type) ? AppConstants.EXPORT_XLS_SUFFIX.getString() : AppConstants.EXPORT_CSV_SUFFIX.getString();
		
		for (Integer advanceGermpasmListId : advanceGermplasmListIds) {
			try {
				List<InventoryDetails> inventoryDetailList = this.inventoryMiddlewareService
						.getInventoryDetailsByGermplasmList(advanceGermpasmListId);
				GermplasmList germplasmList = this.fieldbookMiddlewareService
						.getGermplasmListById(advanceGermpasmListId);
				String advanceListName = germplasmList.getName();
				String filenamePath = this.getFileNamePath(advanceListName) + suffix;
				String sheetName = advanceListName;
				
				exportList(inventoryDetailList, filenamePath, sheetName, exportServiceImpl, type);
					
				outputFilename = filenamePath;
				filenameList.add(filenamePath);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			} catch (MiddlewareQueryException e) {
				LOG.error(e.getMessage(), e);
			}
		}

		if (filenameList.size() > 1) {
			outputFilename = getFileNamePath(studyName+"-"+this.ZIP_DEFAULT_FILENAME) + AppConstants.ZIP_FILE_SUFFIX.getString();
			this.zipFileNameList(outputFilename, filenameList);
		}

		return new File(outputFilename);
	}

	protected void exportList(List<InventoryDetails> inventoryDetailList, String filenamePath, String sheetName, ExportService exportServiceImpl, String type) throws IOException {
		if(AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString().equalsIgnoreCase(type)) {
			exportServiceImpl.generateExcelFileForSingleSheet(this.generateAdvanceListColumnValues(inventoryDetailList), this.generateAdvanceListColumnHeaders(), filenamePath, sheetName);
		} else {
			exportServiceImpl.generateCSVFile(this.generateAdvanceListColumnValues(inventoryDetailList), this.generateAdvanceListColumnHeaders(), filenamePath);
		}		
	}
	
	

	protected boolean zipFileNameList(String outputFilename, List<String> filenameList) {
		ZipUtil.zipIt(outputFilename, filenameList);
		return true;
	}

	protected List<Integer> parseDelimitedAdvanceGermplasmListIds(String advancedListIds) {
		List<Integer> advancedGermplasmListIds = new ArrayList<Integer>();
		StringTokenizer tokenizer = new StringTokenizer(advancedListIds, "|");
		while (tokenizer.hasMoreTokens()) {
			advancedGermplasmListIds.add(Integer.valueOf(tokenizer.nextToken()));
		}
		return advancedGermplasmListIds;
	}

	protected String getFileNamePath(String name) {
		String filenamePath = this.fieldbookProperties.getUploadDirectory() + File.separator
				+ SettingsUtil.cleanSheetAndFileName(name);
		return filenamePath;
	}

	protected List<ExportColumnHeader> generateAdvanceListColumnHeaders() {
		List<ExportColumnHeader> exportColumnHeaders = new ArrayList<ExportColumnHeader>();
		Locale locale = LocaleContextHolder.getLocale();

		exportColumnHeaders.add(new ExportColumnHeader(TermId.ENTRY_NO.getId(), this.messageSource
				.getMessage("seed.entry.number", null, locale), true));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.DESIG.getId(), this.messageSource
				.getMessage("seed.entry.designation", null, locale), true));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.CROSS.getId(), this.messageSource
				.getMessage("seed.entry.parentage", null, locale), true));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.GID.getId(), this.messageSource
				.getMessage("seed.inventory.gid", null, locale), true));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.SOURCE.getId(), this.messageSource
				.getMessage("seed.inventory.source", null, locale), true));
		exportColumnHeaders
				.add(new ExportColumnHeader(TermId.LOCATION_ID.getId(), this.messageSource
						.getMessage("seed.inventory.table.location", null, locale), true));
		exportColumnHeaders.add(new ExportColumnHeader(
				ExportAdvanceListServiceImpl.INVENTORY_AMOUNT, this.messageSource.getMessage(
						"seed.inventory.amount", null, locale), true));
		exportColumnHeaders.add(new ExportColumnHeader(
				ExportAdvanceListServiceImpl.INVENTORY_SCALE, this.messageSource.getMessage(
						"seed.inventory.table.scale", null, locale), true));
		exportColumnHeaders.add(new ExportColumnHeader(
				ExportAdvanceListServiceImpl.INVENTORY_COMMENT, this.messageSource.getMessage(
						"seed.inventory.comment", null, locale), true));

		return exportColumnHeaders;
	}

	protected List<Map<Integer, ExportColumnValue>> generateAdvanceListColumnValues(
			List<InventoryDetails> inventoryDetailList) {
		List<Map<Integer, ExportColumnValue>> exportColumnValues = new ArrayList<Map<Integer, ExportColumnValue>>();
		for (InventoryDetails inventoryDetails : inventoryDetailList) {
			Map<Integer, ExportColumnValue> dataMap = new HashMap<Integer, ExportColumnValue>();
			dataMap.put(TermId.ENTRY_NO.getId(), new ExportColumnValue(TermId.ENTRY_NO.getId(),
					inventoryDetails.getEntryId().toString()));
			dataMap.put(TermId.DESIG.getId(), new ExportColumnValue(TermId.DESIG.getId(),
					inventoryDetails.getGermplasmName()));
			dataMap.put(TermId.CROSS.getId(), new ExportColumnValue(TermId.CROSS.getId(),
					inventoryDetails.getParentage()));
			dataMap.put(TermId.GID.getId(), new ExportColumnValue(TermId.GID.getId(),
					inventoryDetails.getGid().toString()));
			dataMap.put(TermId.SOURCE.getId(), new ExportColumnValue(TermId.SOURCE.getId(),
					inventoryDetails.getSource()));
			dataMap.put(
					TermId.LOCATION_ID.getId(),
					new ExportColumnValue(TermId.LOCATION_ID.getId(), inventoryDetails
							.getLocationName()));
			String amount = getInventoryAmount(inventoryDetails);
			String scaleName = getInventoryScale(inventoryDetails);
			String comment = getInventoryComment(inventoryDetails);
			dataMap.put(ExportAdvanceListServiceImpl.INVENTORY_AMOUNT, new ExportColumnValue(
					ExportAdvanceListServiceImpl.INVENTORY_AMOUNT, amount));
			dataMap.put(ExportAdvanceListServiceImpl.INVENTORY_SCALE, new ExportColumnValue(
					ExportAdvanceListServiceImpl.INVENTORY_SCALE, scaleName));
			dataMap.put(ExportAdvanceListServiceImpl.INVENTORY_COMMENT, new ExportColumnValue(
					ExportAdvanceListServiceImpl.INVENTORY_COMMENT, comment));
			exportColumnValues.add(dataMap);
		}
		return exportColumnValues;
	}

	protected String getInventoryAmount(InventoryDetails inventoryDetails){
		return inventoryDetails.getAmount() != null ? inventoryDetails.getAmount()
				.toString() : "";
	}
	
	protected String getInventoryScale(InventoryDetails inventoryDetails){
		return inventoryDetails.getScaleName() != null ? inventoryDetails
				.getScaleName() : "";
	}
	
	protected String getInventoryComment(InventoryDetails inventoryDetails){
		return  inventoryDetails.getComment() != null ? inventoryDetails.getComment()
				: "";
	}
	
	public void setFieldbookProperties(FieldbookProperties fieldbookProperties) {
		this.fieldbookProperties = fieldbookProperties;
	}

	public void setInventoryMiddlewareService(InventoryService inventoryMiddlewareService) {
		this.inventoryMiddlewareService = inventoryMiddlewareService;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setFieldbookMiddlewareService(FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

}
