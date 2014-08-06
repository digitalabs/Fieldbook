package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProject;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;

public class ListDataProjectUtil {
	
	  public static List<ListDataProject> createListDataProject(List<ImportedGermplasm> importedGermplasmList){
	    	
    	List<ListDataProject> listDataProject = new ArrayList<ListDataProject>();
        if (importedGermplasmList != null && importedGermplasmList.size() > 0) {
        	//need a copy of the germplasm list and save
            for (ImportedGermplasm aData : importedGermplasmList) {
            	ListDataProject listDataProj = new ListDataProject();
            	//listDataProj.setListDataProjectId(listDataProjectId);            	
            	//listDataProj.setList(germplasmList);
            	listDataProj.setGermplasmId(Integer.valueOf(aData.getGid()));
            	if(aData.getCheckId() != null){
            		listDataProj.setCheckType(aData.getCheckId());
            	}else{
            		listDataProj.setCheckType(Integer.valueOf(0));
            	}
            	listDataProj.setDesignation(aData.getDesig());
            	listDataProj.setEntryId(aData.getEntryId());
            	listDataProj.setEntryCode(aData.getEntryCode());
            	listDataProj.setSeedSource(aData.getSource());
            	listDataProj.setGroupName(aData.getGroupName());
                               
                listDataProject.add(listDataProj);
            }
        }
        return listDataProject;
        
    }
	  public static List<ListDataProject> createListDataProjectFromGermplasmListData(List<GermplasmListData> germplasmListDatas){
	    	
	    	List<ListDataProject> listDataProject = new ArrayList<ListDataProject>();
	        if (germplasmListDatas != null && germplasmListDatas.size() > 0) {
	        	//need a copy of the germplasm list and save
	            for (GermplasmListData aData : germplasmListDatas) {
	            	ListDataProject listDataProj = new ListDataProject();
	            	//listDataProj.setListDataProjectId(listDataProjectId);            	
	            	//listDataProj.setList(germplasmList);
	            	listDataProj.setGermplasmId(Integer.valueOf(aData.getGid()));
	            	
	            	listDataProj.setDesignation(aData.getDesignation());
	            	listDataProj.setEntryId(aData.getEntryId());
	            	listDataProj.setEntryCode(aData.getEntryCode());
	            	listDataProj.setSeedSource(aData.getSeedSource());
	            	listDataProj.setGroupName(aData.getGroupName());
	                               
	                listDataProject.add(listDataProj);
	            }
	        }
	        return listDataProject;
	        
	    }
	  
  /**
     * Transform germplasm list data to imported germplasm.
     *
     * @param data the data
     * @param defaultCheckId the default check id
     * @return the list
     */
    public static List<ImportedGermplasm> transformListDataProjectToImportedGermplasm(List<ListDataProject> data) {
        List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
        int index = 1;
        if (data != null && data.size() > 0) {
            for (ListDataProject aData : data) {
                ImportedGermplasm germplasm = new ImportedGermplasm();
                germplasm.setCheck(aData.getCheckType().toString());
                germplasm.setCheckId(aData.getCheckType());
                germplasm.setCross(aData.getGroupName());
                germplasm.setDesig(aData.getDesignation());
                germplasm.setEntryCode(aData.getEntryCode());
                germplasm.setEntryId(aData.getEntryId());
                germplasm.setGid(aData.getGermplasmId().toString());
                germplasm.setSource(aData.getSeedSource());
                germplasm.setGroupName(aData.getGroupName());
                germplasm.setIndex(index++);
                
                list.add(germplasm);
            }
        }
        return list;
    }
}
