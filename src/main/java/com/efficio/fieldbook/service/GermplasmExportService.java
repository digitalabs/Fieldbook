package com.efficio.fieldbook.service;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.pojo.GermplasmListExportInputValues;
import org.generationcp.commons.service.impl.ExportServiceImpl;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class GermplasmExportService extends ExportServiceImpl {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmExportService.class);
	
    private OntologyService ontologyService;
	private UserSelection userSelection;
	private Boolean isNursery;
	
	
	public GermplasmExportService(OntologyService ontologyService, UserSelection userSelection, Boolean isNursery){
		this.ontologyService = ontologyService;
		this.userSelection = userSelection;
		this.isNursery = isNursery;
	}
	
	
	@Override
	public void writeListFactorSection(Map<String, CellStyle> styles,
			HSSFSheet descriptionSheet, int startingRow,
			GermplasmListExportInputValues input) {
		writeListFactorSection(styles, descriptionSheet, startingRow, input.getVisibleColumnMap());
	}
	
	public void writeListFactorSection(Map<String, CellStyle> styles, HSSFSheet descriptionSheet, 
    		int startingRow, Map<String, Boolean> visibleColumnMap) {
    	
        int actualRow = startingRow - 1;
        
        HSSFRow factorDetailsHeader = descriptionSheet.createRow(actualRow);
        Cell factorCell = factorDetailsHeader.createCell(0);
        factorCell.setCellValue(FACTOR);
        factorCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell descriptionCell = factorDetailsHeader.createCell(1);
        descriptionCell.setCellValue(DESCRIPTION);
        descriptionCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell propertyCell = factorDetailsHeader.createCell(2);
        propertyCell.setCellValue(PROPERTY);
        propertyCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell scaleCell = factorDetailsHeader.createCell(3);
        scaleCell.setCellValue(SCALE);
        scaleCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell methodCell = factorDetailsHeader.createCell(4);
        methodCell.setCellValue(METHOD);
        methodCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell dataTypeCell = factorDetailsHeader.createCell(5);
        dataTypeCell.setCellValue(DATA_TYPE);
        dataTypeCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell spaceCell = factorDetailsHeader.createCell(6);
        spaceCell.setCellValue(NESTED_IN);
        spaceCell.setCellStyle(styles.get(HEADING_STYLE));
        
        
        try{

        	
        	if (isNursery){
        		
        		StandardVariable entryNo = ontologyService.getStandardVariable(TermId.ENTRY_NO.getId());
				addToDescriptionSheet(++actualRow, descriptionSheet, entryNo);
				
				StandardVariable desig = ontologyService.getStandardVariable(TermId.DESIG.getId());
				addToDescriptionSheet(++actualRow, descriptionSheet, desig);
				
				StandardVariable gid = ontologyService.getStandardVariable(TermId.GID.getId());
				addToDescriptionSheet(++actualRow, descriptionSheet, gid);
				
				StandardVariable cross = ontologyService.getStandardVariable(TermId.CROSS.getId());
				addToDescriptionSheet(++actualRow, descriptionSheet, cross);
        		
				StandardVariable seedSource = ontologyService.getStandardVariable(TermId.SEED_SOURCE.getId());
				addToDescriptionSheet(++actualRow, descriptionSheet, seedSource);
        	
        		StandardVariable entryCode = ontologyService.getStandardVariable(TermId.ENTRY_CODE.getId());
				addToDescriptionSheet(++actualRow, descriptionSheet, entryCode);
				
        	}else{
        		if (userSelection.getPlotsLevelList() != null){
                	for (SettingDetail settingDetail : userSelection.getPlotsLevelList()){
                		Boolean isVisible = visibleColumnMap.get(settingDetail.getVariable().getCvTermId().toString());
                		if (!settingDetail.isHidden() && isVisible != null && isVisible){
                			addToDescriptionSheet(++actualRow, descriptionSheet, settingDetail);
                		}
                		
                    }
                }
        	}
    	}catch(MiddlewareQueryException e){
    		LOG.error(e.getMessage(), e);
    	}
        
        
        
    }
	
	protected void addToDescriptionSheet(int rowIndex, HSSFSheet descriptionSheet, StandardVariable stdVar){
		HSSFRow entryIdRow = descriptionSheet.createRow(rowIndex);
        entryIdRow.createCell(0).setCellValue(stdVar.getName());
        entryIdRow.createCell(1).setCellValue(stdVar.getDescription());
        entryIdRow.createCell(2).setCellValue(stdVar.getProperty().getName().toUpperCase());
        entryIdRow.createCell(3).setCellValue(stdVar.getScale().getName().toUpperCase());
        entryIdRow.createCell(4).setCellValue(stdVar.getMethod().getName().toUpperCase());
        entryIdRow.createCell(5).setCellValue(stdVar.getDataType().getName().substring(0, 1));
        entryIdRow.createCell(6).setCellValue("");
	}
	
	protected void addToDescriptionSheet(int rowIndex, HSSFSheet descriptionSheet, SettingDetail settingDetail){
		HSSFRow entryIdRow = descriptionSheet.createRow(rowIndex);
        entryIdRow.createCell(0).setCellValue(settingDetail.getVariable().getName());
        entryIdRow.createCell(1).setCellValue(settingDetail.getVariable().getDescription());
        entryIdRow.createCell(2).setCellValue(settingDetail.getVariable().getProperty().toUpperCase());
        entryIdRow.createCell(3).setCellValue(settingDetail.getVariable().getScale().toUpperCase());
        entryIdRow.createCell(4).setCellValue(settingDetail.getVariable().getMethod().toUpperCase());
        entryIdRow.createCell(5).setCellValue(settingDetail.getVariable().getDataType().substring(0, 1));
        entryIdRow.createCell(6).setCellValue("");
	}
	
	@Override
	public void writeObservationSheet(Map<String, CellStyle> styles, HSSFSheet observationSheet, 
				GermplasmListExportInputValues input) throws GermplasmListExporterException {
		
		Map<String, Boolean> visibleColumnMap = input.getVisibleColumnMap();
		
	    createListEntriesHeaderRow(styles, observationSheet, visibleColumnMap);
	    
	    int i = 1;
	    
	    List<SettingDetail> factorsList = userSelection.getPlotsLevelList();
	    List<ImportedGermplasm> importedGermplasms = getImportedGermplasms();
	    
			for (ImportedGermplasm listData : importedGermplasms) {
		    	
		        HSSFRow listEntry = observationSheet.createRow(i);

		        int j = 0;
		        
		        
		        if (isNursery){
		        	
		        	listEntry.createCell(j).setCellValue(getGermplasmData(String.valueOf(TermId.ENTRY_NO.getId()), listData, null));
					j++;
				
					listEntry.createCell(j).setCellValue(getGermplasmData(String.valueOf(TermId.DESIG.getId()), listData, null));
					j++;
					
					listEntry.createCell(j).setCellValue(getGermplasmData(String.valueOf(TermId.GID.getId()), listData, null));
					j++;
				
					listEntry.createCell(j).setCellValue(getGermplasmData(String.valueOf(TermId.CROSS.getId()), listData, null));
					j++;
					
					listEntry.createCell(j).setCellValue(getGermplasmData(String.valueOf(TermId.SEED_SOURCE.getId()), listData, null));
					j++;
				
					listEntry.createCell(j).setCellValue(getGermplasmData(String.valueOf(TermId.ENTRY_CODE.getId()), listData, null));
					j++;
				
				
				}else{
					for (SettingDetail settingDetail: factorsList){
						Boolean isVisible = visibleColumnMap.get(settingDetail.getVariable().getCvTermId().toString());
						if (!settingDetail.isHidden() && isVisible != null && isVisible) {
							listEntry.createCell(j).setCellValue(getGermplasmData(settingDetail.getVariable().getCvTermId().toString(), listData, settingDetail));
							j++;
						}
					}
				}
		        
		     
		        i+=1;
		    }
	    
	}
	
	public List<ImportedGermplasm> getImportedGermplasms() {
		return userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
	}

	protected String getGermplasmData(String termId, ImportedGermplasm germplasm, SettingDetail settingDetail){
    	String val = "";
    	if(termId != null && NumberUtils.isNumber(termId)){
    		Integer term = Integer.valueOf(termId);
    		if(term.intValue() == TermId.GID.getId()){
    			val =  germplasm.getGid().toString(); 
    		}else if(term.intValue() == TermId.ENTRY_CODE.getId()){
    			val = germplasm.getEntryCode().toString();
    		}else if(term.intValue() == TermId.ENTRY_NO.getId()){
    			 val = germplasm.getEntryId().toString();
    		}else if(term.intValue() == TermId.SOURCE.getId() || term.intValue() == TermId.GERMPLASM_SOURCE.getId()){
    			val = germplasm.getSource().toString();
    		}else if(term.intValue() == TermId.CROSS.getId()){
    			val = germplasm.getCross().toString();
    		}else if(term.intValue() == TermId.DESIG.getId()){
    			val = germplasm.getDesig().toString(); 
    		}else if(term.intValue() == TermId.CHECK.getId()){
    			val = getCategoricalCodeValue(germplasm, settingDetail);
    		}     		    			    		
    	}
    	return val;
    }
	
	protected String getCategoricalCodeValue(ImportedGermplasm germplasm, SettingDetail settingDetail){
		String val = "";
		if (settingDetail.getPossibleValues() != null){
			for(ValueReference possibleValue : settingDetail.getPossibleValues()){
				if (possibleValue.getId().equals(Integer.valueOf(germplasm.getCheck().toString()))){
					val = possibleValue.getName();
				}
			}
		}else{
			val = germplasm.getCheck().toString(); 
		}
		
		return val;
	}
	
	@Override
	public void createListEntriesHeaderRow(Map<String, CellStyle> styles,
			HSSFSheet observationSheet, GermplasmListExportInputValues input) {
		createListEntriesHeaderRow(styles, observationSheet, input.getVisibleColumnMap());
	}

	public void createListEntriesHeaderRow(Map<String, CellStyle> styles,
			HSSFSheet observationSheet, Map<String, Boolean> visibleColumnMap) {
		HSSFRow listEntriesHeader = observationSheet.createRow(0);
	    
	    int columnIndex = 0;
	    
	    if (userSelection.getPlotsLevelList() != null){
        	
        	
        	if (isNursery){
        		
        		try{
        			
        			StandardVariable entryNo = ontologyService.getStandardVariable(TermId.ENTRY_NO.getId());
            		Cell entryIdCell1 = listEntriesHeader.createCell(columnIndex);
            		entryIdCell1.setCellValue(entryNo.getName());
            		entryIdCell1.setCellStyle(styles.get(HEADING_STYLE));
        	        columnIndex++;
    				
    				StandardVariable desig = ontologyService.getStandardVariable(TermId.DESIG.getId());
    				Cell entryIdCell2 = listEntriesHeader.createCell(columnIndex);
    				entryIdCell2.setCellValue(desig.getName());
    				entryIdCell2.setCellStyle(styles.get(HEADING_STYLE));
        	        columnIndex++;
    				
    				StandardVariable gid = ontologyService.getStandardVariable(TermId.GID.getId());
    				Cell entryIdCell3 = listEntriesHeader.createCell(columnIndex);
    				entryIdCell3.setCellValue(gid.getName());
    				entryIdCell3.setCellStyle(styles.get(HEADING_STYLE));
        	        columnIndex++;
    				
    				StandardVariable cross = ontologyService.getStandardVariable(TermId.CROSS.getId());
    				Cell entryIdCell4 = listEntriesHeader.createCell(columnIndex);
    				entryIdCell4.setCellValue(cross.getName());
    				entryIdCell4.setCellStyle(styles.get(HEADING_STYLE));
        	        columnIndex++;
        
    				StandardVariable seedSource = ontologyService.getStandardVariable(TermId.SEED_SOURCE.getId());
    				Cell entryIdCell5 = listEntriesHeader.createCell(columnIndex);
    				entryIdCell5.setCellValue(seedSource.getName());
    				entryIdCell5.setCellStyle(styles.get(HEADING_STYLE));
        	        columnIndex++;

                	StandardVariable entryCode = ontologyService.getStandardVariable(TermId.ENTRY_CODE.getId());
    				Cell entryIdCell6 = listEntriesHeader.createCell(columnIndex);
    				entryIdCell6.setCellValue(entryCode.getName());
    				entryIdCell6.setCellStyle(styles.get(HEADING_STYLE));
        	        columnIndex++;
        	        
        		}catch(MiddlewareQueryException e){
        			LOG.error(e.getMessage(), e);
        		}
    			
    		}else{
    			for (SettingDetail settingDetail : userSelection.getPlotsLevelList()){
            		Boolean isVisible = visibleColumnMap.get(settingDetail.getVariable().getCvTermId().toString());
            		if (!settingDetail.isHidden() && isVisible != null && isVisible){
            			Cell entryIdCell = listEntriesHeader.createCell(columnIndex);
            	        entryIdCell.setCellValue(settingDetail.getVariable().getName());
            	        entryIdCell.setCellStyle(styles.get(HEADING_STYLE));
            	        columnIndex++;
            		}
            		
                }
    		}
        }
	    
	}
	
	public UserSelection getUserSelection() {
		return userSelection;
	}

	public void setUserSelection(UserSelection userSelection) {
		this.userSelection = userSelection;
	}
}
