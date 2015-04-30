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

import org.apache.poi.ss.util.WorkbookUtil;
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
	
	private static final String NO_FILE = "noFile";
	
	private static String ADVANCE_LIST_SHEET_NAME = "Advance List";

	private static final String STOCK_LIST_EXPORT_SHEET_NAME = "Observation";

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
				String filenamePath = getFileNamePath(advanceListName) + suffix;
				String sheetName =  WorkbookUtil.createSafeSheetName(ADVANCE_LIST_SHEET_NAME);
				
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
			outputFilename = getFileNamePath(studyName+"-"+AppConstants.ADVANCE_ZIP_DEFAULT_FILENAME.getString()) + AppConstants.ZIP_FILE_SUFFIX.getString();
			this.zipFileNameList(outputFilename, filenameList);
		}

		return new File(outputFilename);
	}
	
	@Override
	public File exportStockList(Integer stockListId, ExportService exportServiceImpl) {

		List<String> filenameList = new ArrayList<String>();
		String outputFilename = NO_FILE;
		String suffix = AppConstants.EXPORT_XLS_SUFFIX.getString();
		
			try {
				List<InventoryDetails> inventoryDetailList = this.inventoryMiddlewareService
						.getInventoryDetailsByGermplasmList(stockListId);
				GermplasmList germplasmList = this.fieldbookMiddlewareService
						.getGermplasmListById(stockListId);
				String advanceListName = germplasmList.getName();
				String filenamePath = getFileNamePath(advanceListName) + suffix;
				String sheetName =  org.apache.poi.ss.util.WorkbookUtil.createSafeSheetName(STOCK_LIST_EXPORT_SHEET_NAME);
				
				exportList(inventoryDetailList, filenamePath, sheetName, exportServiceImpl, AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString());
					
				outputFilename = filenamePath;
				filenameList.add(filenamePath);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			} catch (MiddlewareQueryException e) {
				LOG.error(e.getMessage(), e);
			}


		return new File(outputFilename);
	}

	
	protected String getFileNamePath(String name) {
			String filenamePath = this.fieldbookProperties.getUploadDirectory() + File.separator
					+ SettingsUtil.cleanSheetAndFileName(name);
			return filenamePath;
	}
	 
	protected void exportList(List<InventoryDetails> inventoryDetailList, String filenamePath, String sheetName, ExportService exportServiceImpl, String type) throws IOException {
		List<ExportColumnHeader> exportColumnHeaders = this.generateAdvanceListColumnHeaders();
		if(AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString().equalsIgnoreCase(type)) {
			exportServiceImpl.generateExcelFileForSingleSheet(this.generateAdvanceListColumnValues(inventoryDetailList, exportColumnHeaders), exportColumnHeaders, filenamePath, sheetName);
		} else {
			exportServiceImpl.generateCSVFile(this.generateAdvanceListColumnValues(inventoryDetailList, exportColumnHeaders), exportColumnHeaders, filenamePath);
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

	

	protected List<ExportColumnHeader> generateAdvanceListColumnHeaders() {
		List<ExportColumnHeader> exportColumnHeaders = new ArrayList<ExportColumnHeader>();
		Locale locale = LocaleContextHolder.getLocale();

		exportColumnHeaders.add(new ExportColumnHeader(TermId.ENTRY_NO.getId(), this.messageSource
				.getMessage("seed.entry.number", null, locale), true, ExportColumnHeader.GREEN));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.DESIG.getId(), this.messageSource
				.getMessage("seed.entry.designation", null, locale), true, ExportColumnHeader.GREEN));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.CROSS.getId(), this.messageSource
				.getMessage("seed.entry.parentage", null, locale), true, ExportColumnHeader.GREEN));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.GID.getId(), this.messageSource
				.getMessage("seed.inventory.gid", null, locale), true, ExportColumnHeader.GREEN));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.SOURCE.getId(), this.messageSource
				.getMessage("seed.inventory.source", null, locale), true, ExportColumnHeader.GREEN));
		exportColumnHeaders
				.add(new ExportColumnHeader(TermId.LOCATION_ID.getId(), this.messageSource
						.getMessage("seed.inventory.table.location", null, locale), true, ExportColumnHeader.BLUE));
		exportColumnHeaders.add(new ExportColumnHeader(
				AppConstants.TEMPORARY_INVENTORY_AMOUNT.getInt(), this.messageSource.getMessage(
						"seed.inventory.amount", null, locale), true, ExportColumnHeader.BLUE));
		exportColumnHeaders.add(new ExportColumnHeader(
				AppConstants.TEMPORARY_INVENTORY_SCALE.getInt(), this.messageSource.getMessage(
						"seed.inventory.table.scale", null, locale), true, ExportColumnHeader.BLUE));
		exportColumnHeaders.add(new ExportColumnHeader(
				AppConstants.TEMPORARY_INVENTORY_COMMENT.getInt(), this.messageSource.getMessage(
						"seed.inventory.comment", null, locale), true, ExportColumnHeader.BLUE));

		return exportColumnHeaders;
	}

	protected List<Map<Integer, ExportColumnValue>> generateAdvanceListColumnValues(
			List<InventoryDetails> inventoryDetailList, List<ExportColumnHeader> exportColumnHeaders) {
		List<Map<Integer, ExportColumnValue>> exportColumnValues = new ArrayList<Map<Integer, ExportColumnValue>>();
		for (InventoryDetails inventoryDetails : inventoryDetailList) {
			Map<Integer, ExportColumnValue> dataMap = new HashMap<Integer, ExportColumnValue>();						
			for(ExportColumnHeader columnHeaders : exportColumnHeaders){				
				dataMap.put(columnHeaders.getId(), new ExportColumnValue(columnHeaders.getId(),
						getInventoryDetailValueInfo(inventoryDetails, columnHeaders.getId())));
			}
			exportColumnValues.add(dataMap);
		}
		return exportColumnValues;
	}
	
	protected String getInventoryDetailValueInfo(InventoryDetails inventoryDetails, int columnHeaderId){
		String val = "";
		if(columnHeaderId == TermId.ENTRY_NO.getId()) {
				val = inventoryDetails.getEntryId().toString();
		} else if(columnHeaderId == TermId.DESIG.getId()) {
				val = inventoryDetails.getGermplasmName();
		} else if(columnHeaderId == TermId.CROSS.getId() ){ 
				val = inventoryDetails.getParentage();
		} else if(columnHeaderId == TermId.GID.getId()) {
				val = inventoryDetails.getGid().toString();
		} else if(columnHeaderId == TermId.SOURCE.getId()) { 
				val = inventoryDetails.getSource();
		} else if(columnHeaderId == TermId.LOCATION_ID.getId()) {
			// in preparation for BMS-143. Export the abbreviation instead of the whole name
				val = inventoryDetails.getLocationAbbr();
		} else if(columnHeaderId == AppConstants.TEMPORARY_INVENTORY_AMOUNT.getInt()) { 
				val = getInventoryAmount(inventoryDetails);
		} else if(columnHeaderId == AppConstants.TEMPORARY_INVENTORY_SCALE.getInt()) { 
				val = getInventoryValue(inventoryDetails.getScaleName());
		} else if(columnHeaderId == AppConstants.TEMPORARY_INVENTORY_COMMENT.getInt()) { 
				val = getInventoryValue(inventoryDetails.getComment());
		}
		return val;		
	}

	protected String getInventoryValue(String inventoryValue){
		return inventoryValue != null ? inventoryValue : "";
	}
	
	protected String getInventoryAmount(InventoryDetails inventoryDetails){
		return inventoryDetails.getAmount() != null ? inventoryDetails.getAmount()
				.toString() : "";
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
