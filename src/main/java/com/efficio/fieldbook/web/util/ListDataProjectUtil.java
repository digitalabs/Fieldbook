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
	    
}
