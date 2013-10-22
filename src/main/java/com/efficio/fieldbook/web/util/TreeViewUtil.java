/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.oms.PropertyReference;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TraitReference;
import org.generationcp.middleware.pojos.GermplasmList;

import com.efficio.pojos.treeview.TreeNode;

public class TreeViewUtil {

	public static String convertReferencesToJson(List<Reference> references) throws Exception {
		List<TreeNode> treeNodes = convertReferencesToTreeView(references);
		return convertTreeViewToJson(treeNodes);
	}
	
	public static String convertFolderReferencesToJson(List<FolderReference> references) throws Exception {
		List<TreeNode> treeNodes = convertFolderReferencesToTreeView(references);
		return convertTreeViewToJson(treeNodes);
	}
	
    public static String convertDatasetReferencesToJson(List<DatasetReference> references) throws Exception {
        List<TreeNode> treeNodes = convertDatasetReferencesToTreeView(references);
        return convertTreeViewToJson(treeNodes);
    }
    
    public static String convertGermplasmListToJson(List<GermplasmList> germplasmLists) throws Exception {
        List<TreeNode> treeNodes = convertGermplasmListToTreeView(germplasmLists);
        return convertTreeViewToJson(treeNodes);
    }
    
	private static List<TreeNode> convertReferencesToTreeView(List<Reference> references) {
		List<TreeNode> treeNodes = new ArrayList<TreeNode>();
		if (references != null && !references.isEmpty()) {
			for (Reference reference : references) {
				treeNodes.add(convertReferenceToTreeNode(reference));
			}
		}
		return treeNodes;
	}
	
	private static List<TreeNode> convertFolderReferencesToTreeView(List<FolderReference> references) {
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
		if (references != null && !references.isEmpty()) {
			for (FolderReference reference : references) {
				treeNodes.add(convertReferenceToTreeNode(reference));
			}
		}
		return treeNodes;
	}

    private static List<TreeNode> convertDatasetReferencesToTreeView(List<DatasetReference> references) {
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
        if (references != null && !references.isEmpty()) {
            for (DatasetReference reference : references) {
                treeNodes.add(convertReferenceToTreeNode(reference));
            }
        }
        return treeNodes;
    }
    
    private static List<TreeNode> convertGermplasmListToTreeView(List<GermplasmList> germplasmLists) {
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
        if (germplasmLists != null && !germplasmLists.isEmpty()) {
            for (GermplasmList germplasmList : germplasmLists) {
                treeNodes.add(convertGermplasmListToTreeNode(germplasmList));
            }
        }
        return treeNodes;
    }

	private static TreeNode convertReferenceToTreeNode(Reference reference) {
		TreeNode treeNode = new TreeNode();
		
		treeNode.setKey(reference.getId().toString());
		treeNode.setTitle(reference.getName());
		treeNode.setIsFolder(reference instanceof DatasetReference ? false : true);
		treeNode.setIsLazy(true);

		return treeNode;
	}
	
	private static TreeNode convertGermplasmListToTreeNode(GermplasmList germplasmList) {
	    TreeNode treeNode = new TreeNode();
	    
	    treeNode.setKey(germplasmList.getId().toString());
	    treeNode.setTitle(germplasmList.getName());
	    treeNode.setIsFolder(germplasmList.getType() != null && germplasmList.getType().equals("FOLDER") ? true : false);
	    treeNode.setIsLazy(true);
	    
	    return treeNode;
	}
	
	public static String convertTreeViewToJson(List<TreeNode> treeNodes) throws Exception {
	    if (treeNodes != null && !treeNodes.isEmpty()) {
    		ObjectMapper mapper = new ObjectMapper();
    		return mapper.writeValueAsString(treeNodes);
	    }
	    return "[]"; 
	}
	
	
	//for the ontology Browser
	public static String convertOntologyTraitsToJson(List<TraitReference> traitReferences) throws Exception {
	    TreeNode treeNode = new TreeNode();
        
        treeNode.setKey("0");
        treeNode.setTitle("Trait Class");
        treeNode.setIsFolder(true);
        treeNode.setIsLazy(false);
        treeNode.setExpand(true);
        
        List<TreeNode> treeNodes = convertTraitReferencesToTreeView(traitReferences);
        treeNode.setChildren(treeNodes);
        
        List<TreeNode> tempList = new ArrayList();
        tempList.add(treeNode);
        return convertTreeViewToJson(tempList);
    }
	private static List<TreeNode> convertTraitReferencesToTreeView(List<TraitReference> traitReferences) {
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
        if (traitReferences != null && !traitReferences.isEmpty()) {
            for (TraitReference reference : traitReferences) {
                treeNodes.add(convertTraitReferenceToTreeNode(reference));
            }
        }
        return treeNodes;
    }
	
	private static TreeNode convertTraitReferenceToTreeNode(TraitReference reference) {
        TreeNode treeNode = new TreeNode();
        String parentId = reference.getId().toString();
        treeNode.setKey(parentId);
        treeNode.setAddClass(parentId);
        treeNode.setTitle(reference.getName());
        treeNode.setIsFolder(true);
        treeNode.setIsLazy(false);
        //treeNode.setExpand(true);
        //we need to set the children for the property
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
        if(reference.getProperties() != null && !reference.getProperties().isEmpty()){
            for (PropertyReference propRef : reference.getProperties()) {
                treeNodes.add(convertPropertyReferenceToTreeNode(parentId, propRef));
            }
            
        }
        treeNode.setChildren(treeNodes);
        
        return treeNode;
    }
	
	private static TreeNode convertPropertyReferenceToTreeNode(String parentId, PropertyReference reference) {
        TreeNode treeNode = new TreeNode();
        String id = parentId+"_"+reference.getId().toString();
        treeNode.setKey(id);
        treeNode.setAddClass(id);
        treeNode.setTitle(reference.getName());
        treeNode.setIsFolder(true);
        treeNode.setIsLazy(false);
        //treeNode.setExpand(true);
        //we need to set the children for the property
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
        if(reference.getStandardVariables() != null && !reference.getStandardVariables().isEmpty()){
            for (StandardVariableReference variableRef : reference.getStandardVariables()) {
                treeNodes.add(convertStandardVariableReferenceToTreeNode(id, variableRef));
            }
            
        }
        treeNode.setChildren(treeNodes);
        
        return treeNode;
    }

	private static TreeNode convertStandardVariableReferenceToTreeNode(String parentId, StandardVariableReference reference) {
        TreeNode treeNode = new TreeNode();
        String id = parentId+"_"+reference.getId().toString();
        treeNode.setKey(id);
        treeNode.setAddClass(id);
        treeNode.setTitle(reference.getName());
        treeNode.setIsFolder(false);
        treeNode.setIsLazy(false);
        treeNode.setLastChildren(true);
        //treeNode.setExpand(true);
        //we need to set the children for the property
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();           
        treeNode.setChildren(treeNodes);
        
        return treeNode;
    }
}
