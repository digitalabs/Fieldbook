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
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.pojos.GermplasmList;

import com.efficio.pojos.treeview.TreeNode;
import com.efficio.pojos.treeview.TypeAheadSearchTreeNode;

/**
 * The Class TreeViewUtil.
 */
public class TreeViewUtil {

	/**
	 * Convert references to json.
	 *
	 * @param references the references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertReferencesToJson(List<Reference> references) throws Exception {
		List<TreeNode> treeNodes = convertReferencesToTreeView(references);
		return convertTreeViewToJson(treeNodes);
	}
	
	/**
	 * Convert folder references to json.
	 *
	 * @param references the references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertFolderReferencesToJson(List<FolderReference> references) throws Exception {
		List<TreeNode> treeNodes = convertFolderReferencesToTreeView(references);
		return convertTreeViewToJson(treeNodes);
	}
	
    /**
     * Convert dataset references to json.
     *
     * @param references the references
     * @return the string
     * @throws Exception the exception
     */
    public static String convertDatasetReferencesToJson(List<DatasetReference> references) throws Exception {
        List<TreeNode> treeNodes = convertDatasetReferencesToTreeView(references);
        return convertTreeViewToJson(treeNodes);
    }
    
    /**
     * Convert germplasm list to json.
     *
     * @param germplasmLists the germplasm lists
     * @return the string
     * @throws Exception the exception
     */
    public static String convertGermplasmListToJson(List<GermplasmList> germplasmLists) throws Exception {
        List<TreeNode> treeNodes = convertGermplasmListToTreeView(germplasmLists);
        return convertTreeViewToJson(treeNodes);
    }
    
	/**
	 * Convert references to tree view.
	 *
	 * @param references the references
	 * @return the list
	 */
	private static List<TreeNode> convertReferencesToTreeView(List<Reference> references) {
		List<TreeNode> treeNodes = new ArrayList<TreeNode>();
		if (references != null && !references.isEmpty()) {
			for (Reference reference : references) {
				treeNodes.add(convertReferenceToTreeNode(reference));
			}
		}
		return treeNodes;
	}
	
	/**
	 * Convert folder references to tree view.
	 *
	 * @param references the references
	 * @return the list
	 */
	private static List<TreeNode> convertFolderReferencesToTreeView(List<FolderReference> references) {
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
		if (references != null && !references.isEmpty()) {
			for (FolderReference reference : references) {
				treeNodes.add(convertReferenceToTreeNode(reference));
			}
		}
		return treeNodes;
	}

    /**
     * Convert dataset references to tree view.
     *
     * @param references the references
     * @return the list
     */
    private static List<TreeNode> convertDatasetReferencesToTreeView(List<DatasetReference> references) {
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
        if (references != null && !references.isEmpty()) {
            for (DatasetReference reference : references) {
                treeNodes.add(convertReferenceToTreeNode(reference));
            }
        }
        return treeNodes;
    }
    
    /**
     * Convert germplasm list to tree view.
     *
     * @param germplasmLists the germplasm lists
     * @return the list
     */
    private static List<TreeNode> convertGermplasmListToTreeView(List<GermplasmList> germplasmLists) {
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
        if (germplasmLists != null && !germplasmLists.isEmpty()) {
            for (GermplasmList germplasmList : germplasmLists) {
                treeNodes.add(convertGermplasmListToTreeNode(germplasmList));
            }
        }
        return treeNodes;
    }

	/**
	 * Convert reference to tree node.
	 *
	 * @param reference the reference
	 * @return the tree node
	 */
	private static TreeNode convertReferenceToTreeNode(Reference reference) {
		TreeNode treeNode = new TreeNode();
		
		treeNode.setKey(reference.getId().toString());
		treeNode.setTitle(reference.getName());
		treeNode.setIsFolder(reference instanceof DatasetReference ? false : true);
		treeNode.setIsLazy(true);

		return treeNode;
	}
	
	/**
	 * Convert germplasm list to tree node.
	 *
	 * @param germplasmList the germplasm list
	 * @return the tree node
	 */
	private static TreeNode convertGermplasmListToTreeNode(GermplasmList germplasmList) {
	    TreeNode treeNode = new TreeNode();
	    
	    treeNode.setKey(germplasmList.getId().toString());
	    treeNode.setTitle(germplasmList.getName());
	    treeNode.setIsFolder(germplasmList.getType() != null 
	            && germplasmList.getType().equals("FOLDER") ? true : false);
	    treeNode.setIsLazy(true);
	    
	    return treeNode;
	}
	
	/**
	 * Convert tree view to json.
	 *
	 * @param treeNodes the tree nodes
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertTreeViewToJson(List<TreeNode> treeNodes) throws Exception {
	    if (treeNodes != null && !treeNodes.isEmpty()) {
    		ObjectMapper mapper = new ObjectMapper();
    		return mapper.writeValueAsString(treeNodes);
	    }
	    return "[]"; 
	}
	
	/**
	 * Convert search tree view to json.
	 *
	 * @param treeNodes the tree nodes
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertSearchTreeViewToJson(List<TypeAheadSearchTreeNode> treeNodes) 
	        throws Exception {
        if (treeNodes != null && !treeNodes.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(treeNodes);
        }
        return "[]"; 
    }
	
	//for the ontology Browser
	/**
	 * Convert ontology traits to search single level json.
	 *
	 * @param traitClassReferences the trait references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertOntologyTraitsToSearchSingleLevelJson(
	        List<TraitClassReference> traitClassReferences) throws Exception {
                
        //return treeNodes;
        
        return convertSearchTreeViewToJson(getTypeAheadTreeNodes("", traitClassReferences));
    }
	
	private static List<TypeAheadSearchTreeNode> getTypeAheadTreeNodes(
	        String parentId, List<TraitClassReference> traitClassReferences){
	    List<TypeAheadSearchTreeNode> treeNodes = new ArrayList<TypeAheadSearchTreeNode>();

	    if (traitClassReferences != null && !traitClassReferences.isEmpty()) {
            for (TraitClassReference reference : traitClassReferences) {
              //this is for the inner trait classes
                if(reference.getTraitClassChildren() != null 
                        && !reference.getTraitClassChildren().isEmpty()){
                    String newParentId = "";
                    if(parentId != null && !parentId.equalsIgnoreCase("")){
                        newParentId = parentId + "_";
                    }
                    newParentId = newParentId + reference.getId().toString();
                    treeNodes.addAll(getTypeAheadTreeNodes(newParentId, reference.getTraitClassChildren()));
                }
                
                List<PropertyReference> propRefList = reference.getProperties();
                for(PropertyReference propRef : propRefList){                                       
                    List<StandardVariableReference> variableRefList = propRef.getStandardVariables();
                    String parentTitle = reference.getName();
                    String key = reference.getId().toString() + "_" + propRef.getId().toString(); 
                    
                    if(parentId != null && !parentId.equalsIgnoreCase("")){
                        key = parentId + "_" + key;
                    }
                    
                    List<String> token = new ArrayList<String>();
                    token.add(propRef.getName());
                    TypeAheadSearchTreeNode searchTreeNode = new TypeAheadSearchTreeNode(
                            key, token , propRef.getName(), parentTitle, "Property");
                    treeNodes.add(searchTreeNode);
                    
                    for(StandardVariableReference variableRef : variableRefList){
                        String varParentTitle = reference.getName() + " > " + propRef.getName();
                        String varKey = key + "_" + variableRef.getId().toString();
                        List<String> varToken = new ArrayList<String>();
                        varToken.add(variableRef.getName());
                        TypeAheadSearchTreeNode varSearchTreeNode = new TypeAheadSearchTreeNode(
                                varKey, varToken, variableRef.getName(), varParentTitle, "Standard Variable");
                        treeNodes.add(varSearchTreeNode);
                    }
                }
                
            }
        }
	    
	    return treeNodes;
	}
     
	
	/**
	 * Convert ontology traits to json.
	 *
	 * @param traitClassReferences the trait references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertOntologyTraitsToJson(List<TraitClassReference> traitClassReferences) 
	        throws Exception {
	    /*
	    TreeNode treeNode = new TreeNode();
        
        treeNode.setKey("0");
        treeNode.setTitle("Trait Class");
        treeNode.setIsFolder(true);
        treeNode.setIsLazy(false);
        treeNode.setExpand(true);
        treeNode.setIcon(false);
        */
        List<TreeNode> treeNodes = convertTraitClassReferencesToTreeView(traitClassReferences);
        
//        treeNode.setChildren(treeNodes);
        
//        List<TreeNode> tempList = new ArrayList();
//        tempList.add(treeNode);
//        return convertTreeViewToJson(tempList);
        return convertTreeViewToJson(treeNodes);
    }
	
	/**
	 * Convert trait references to tree view.
	 *
	 * @param traitClassReferences the trait references
	 * @return the list
	 */
	private static List<TreeNode> convertTraitClassReferencesToTreeView(
	        List<TraitClassReference> traitClassReferences) {
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
        if (traitClassReferences != null && !traitClassReferences.isEmpty()) {
            for (TraitClassReference reference : traitClassReferences) {
                treeNodes.add(convertTraitClassReferenceToTreeNode("", reference));
            }
        }
        return treeNodes;
    }
	
	/**
	 * Convert trait reference to tree node.
	 *
	 * @param reference the reference
	 * @return the tree node
	 */
	private static TreeNode convertTraitClassReferenceToTreeNode(
	        String parentParentId, TraitClassReference reference) {
        TreeNode treeNode = new TreeNode();
        String parentId = reference.getId().toString();
        if(parentParentId != null && !parentParentId.equalsIgnoreCase("")){
            parentId = parentParentId + "_" + parentId;
        }
        treeNode.setKey(parentId);
        treeNode.setAddClass(parentId);
        treeNode.setTitle(reference.getName());
        treeNode.setIsFolder(true);
        treeNode.setIsLazy(false);
        treeNode.setIcon(false);
        treeNode.setIncludeInSearch(false);
        //treeNode.setExpand(true);
        
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
        
        //this is for the inner trait classes
        if(reference.getTraitClassChildren() != null && !reference.getTraitClassChildren().isEmpty()){
            for (TraitClassReference childTrait : reference.getTraitClassChildren()) {
                treeNodes.add(convertTraitClassReferenceToTreeNode(parentId, childTrait));
            }
        }
        //we need to set the children for the property
        
        if(reference.getProperties() != null && !reference.getProperties().isEmpty()){
            for (PropertyReference propRef : reference.getProperties()) {
                treeNodes.add(convertPropertyReferenceToTreeNode(parentId, propRef, reference.getName()));
            }
            
        }
        treeNode.setChildren(treeNodes);
        
        return treeNode;
    }
	
	/**
	 * Convert property reference to tree node.
	 *
	 * @param parentId the parent id
	 * @param reference the reference
	 * @param parentTitle the parent title
	 * @return the tree node
	 */
	private static TreeNode convertPropertyReferenceToTreeNode(
	        String parentId, PropertyReference reference, String parentTitle) {
        TreeNode treeNode = new TreeNode();
        String id = parentId+"_"+reference.getId().toString();
        treeNode.setKey(id);
        treeNode.setAddClass(id);
        treeNode.setTitle(reference.getName());
        treeNode.setIsFolder(true);
        treeNode.setIsLazy(false);
        treeNode.setIcon(false);
        treeNode.setIncludeInSearch(true);
        String newParentTitle = parentTitle + " > " + reference.getName();
        treeNode.setParentTitle(newParentTitle);
        //treeNode.setExpand(true);
        //we need to set the children for the property
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
        if(reference.getStandardVariables() != null && !reference.getStandardVariables().isEmpty()){
            for (StandardVariableReference variableRef : reference.getStandardVariables()) {
                treeNodes.add(convertStandardVariableReferenceToTreeNode(id, variableRef, newParentTitle));
            }
            
        }
        treeNode.setChildren(treeNodes);
        
        return treeNode;
    }

	/**
	 * Convert standard variable reference to tree node.
	 *
	 * @param parentId the parent id
	 * @param reference the reference
	 * @param parentTitle the parent title
	 * @return the tree node
	 */
	private static TreeNode convertStandardVariableReferenceToTreeNode(
	        String parentId, StandardVariableReference reference, String parentTitle) {
        TreeNode treeNode = new TreeNode();
        String id = parentId+"_"+reference.getId().toString();
        treeNode.setKey(id);
        treeNode.setAddClass(id);
        treeNode.setTitle(reference.getName());
        treeNode.setIsFolder(false);
        treeNode.setIsLazy(false);
        treeNode.setLastChildren(true);
        treeNode.setIcon(false);
        treeNode.setIncludeInSearch(true);
        String newParentTitle = parentTitle + " > " + reference.getName();
        treeNode.setParentTitle(newParentTitle);
        //treeNode.setExpand(true);
        //we need to set the children for the property
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();           
        treeNode.setChildren(treeNodes);
        
        return treeNode;
    }
}
