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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.oms.PropertyReference;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.pojos.treeview.TreeNode;
import com.efficio.pojos.treeview.TreeTableNode;
import com.efficio.pojos.treeview.TypeAheadSearchTreeNode;

/**
 * The Class TreeViewUtil.
 */
public class TreeViewUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(TreeViewUtil.class);
    
	private TreeViewUtil() {
		
	}
	/**
	 * Convert references to json.
	 *
	 * @param references the references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertReferencesToJson(List<Reference> references) throws IOException {
		List<TreeNode> treeNodes = convertReferencesToTreeView(references);
		return convertTreeViewToJson(treeNodes);
	}
	
	public static List<FolderReference> convertReferenceToFolderReference(List<Reference> refList){
		 List<FolderReference> folRefs = new ArrayList<FolderReference>();
         for(Reference ref : refList){
         	FolderReference folderReference = new FolderReference(ref.getId(), ref.getName());                	
         	folRefs.add(folderReference);
         }
         return folRefs;
	}
	
	/**
	 * Convert folder references to json.
	 *
	 * @param references the references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertStudyFolderReferencesToJson(List<FolderReference> references, boolean isNursery,boolean isAll, boolean isLazy, FieldbookService fieldbookService, boolean isFolderOnly) throws IOException {		
		List<TreeNode> treeNodes = convertStudyFolderReferencesToTreeView(references, isNursery, isAll, isLazy , fieldbookService, isFolderOnly);
		return convertTreeViewToJson(treeNodes);
	}
	
	/**
	 * Convert folder references to json.
	 *
	 * @param references the references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertFolderReferencesToJson(List<FolderReference> references, boolean isLazy) throws IOException {
		List<TreeNode> treeNodes = convertFolderReferencesToTreeView(references, isLazy);
		return convertTreeViewToJson(treeNodes);
	}
	
    /**
     * Convert dataset references to json.
     *
     * @param references the references
     * @return the string
     * @throws Exception the exception
     */
    public static String convertDatasetReferencesToJson(List<DatasetReference> references) throws IOException {
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
    public static String convertGermplasmListToJson(List<GermplasmList> germplasmLists, boolean isFolderOnly) throws IOException {
        List<TreeNode> treeNodes = convertGermplasmListToTreeView(germplasmLists, isFolderOnly);
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
    private static List<TreeNode> convertFolderReferencesToTreeView(List<FolderReference> references, boolean isLazy) {
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
        TreeNode treeNode;
        if (references != null && !references.isEmpty()) {
            for (FolderReference reference : references) {
                treeNode = convertReferenceToTreeNode(reference);
                treeNode.setIsLazy(isLazy);
                treeNodes.add(treeNode);
                if (reference.getSubFolders() != null && !reference.getSubFolders().isEmpty()) {
                    treeNode.setChildren(convertFolderReferencesToTreeView(reference.getSubFolders(), isLazy));
                } else {
                    treeNode.setIsFolder(false);
                }
            }
        }
        return treeNodes;
    }

    public static List<TreeNode> convertStudyFolderReferencesToTreeView(List<FolderReference> references, boolean isNursery, boolean isAll, boolean isLazy,FieldbookService fieldbookService, boolean isFolderOnly) {
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
        TreeNode treeNode;
        if (references != null && !references.isEmpty()) {
            for (FolderReference reference : references) {
                treeNode = convertStudyReferenceToTreeNode(reference, isNursery,isAll, fieldbookService, isFolderOnly);
                if(treeNode == null) {
                	continue;
                }
                treeNode.setIsLazy(isLazy);
                treeNodes.add(treeNode);
                if (reference.getSubFolders() != null && !reference.getSubFolders().isEmpty()) {
                    treeNode.setChildren(convertFolderReferencesToTreeView(reference.getSubFolders(), isLazy));
                }
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
    public static List<TreeNode> convertGermplasmListToTreeView(List<GermplasmList> germplasmLists, boolean isFolderOnly) {
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
        if (germplasmLists != null && !germplasmLists.isEmpty()) {
            for (GermplasmList germplasmList : germplasmLists) {
            	TreeNode node = convertGermplasmListToTreeNode(germplasmList, isFolderOnly);
            	if(node != null) {
            		treeNodes.add(node);
            	}
            }
        }
        return treeNodes;
    }
    
    /**
     * Convert list of germplasmList to tree table nodes.
     *
     * @param germplasmLists the germplasm lists
     * @return the list
     */
    public static List<TreeTableNode> convertGermplasmListToTreeTableNodes(
    		List<GermplasmList> germplasmLists, 
    		UserDataManager userDataManager,
    		GermplasmListManager germplasmListManager) {
        List<TreeTableNode> treeTableNodes = new ArrayList<TreeTableNode>();
        if (germplasmLists != null && !germplasmLists.isEmpty()) {
            for (GermplasmList germplasmList : germplasmLists) {
            	TreeTableNode node = convertGermplasmListToTreeTableNode(germplasmList, 
            			userDataManager, germplasmListManager);
            	if(node != null) {
            		treeTableNodes.add(node);
            	}
            }
        }
        return treeTableNodes;
    }
    
    private static String getDescriptionForDisplay(GermplasmList germplasmList){
        String description = "-";
        if(germplasmList != null && germplasmList.getDescription() != null && germplasmList.getDescription().length() != 0){
            description = germplasmList.getDescription().replaceAll("<", "&lt;");
            description = description.replaceAll(">", "&gt;");
            if(description.length() > 27){
                description = description.substring(0, 27) + "...";
            }
        }
        return description;
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
	
	public static boolean isFolder(Integer value, FieldbookService fieldbookService) {
        try {
            boolean isStudy = fieldbookService.isStudy(value);
           
            return !isStudy;
        } catch (MiddlewareQueryException e) {
           LOG.error(e.getMessage(),e);
        }

        return false;
    }
	/**
	 * Convert reference to tree node.
	 *
	 * @param reference the reference
	 * @return the tree node
	 */
	private static TreeNode convertStudyReferenceToTreeNode(Reference reference, 
			boolean isNursery,boolean isAll, FieldbookService fieldbookService, boolean isFolderOnly) {
		TreeNode treeNode = new TreeNode();
		
		treeNode.setKey(reference.getId().toString());
		treeNode.setTitle(reference.getName());
		boolean isFolder = isFolder(reference.getId(), fieldbookService);
		treeNode.setIsFolder(isFolder);
		treeNode.setIsLazy(true);
		if(isFolder){
			treeNode.setIcon(AppConstants.FOLDER_ICON_PNG.getString());
		} else {
			if(isFolderOnly) {
				return null;
			}
			treeNode.setIcon(AppConstants.STUDY_ICON_PNG.getString());
				if(!isNurseryStudy(reference.getId(), isNursery, fieldbookService)) {
					return null;
				}
		}
		
		
		
		return treeNode;
	}
	
	private static boolean isNurseryStudy(Integer studyId, boolean isNursery, FieldbookService fieldbookService){
		try {
			TermId termId = fieldbookService.getStudyType(studyId);
		
			if(isNursery) {
				if(TermId.NURSERY == termId) {
					return true;
				}
			} else {
				if(TermId.TRIAL == termId) {
					return true;
				}
			}
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(),e);
		}
		return false;
	}
	
	/**
	 * Convert germplasm list to tree node.
	 *
	 * @param germplasmList the germplasm list
	 * @return the tree node
	 */
	private static TreeNode convertGermplasmListToTreeNode(GermplasmList germplasmList, boolean isFolderOnly) {
	    TreeNode treeNode = new TreeNode();
	    
	    treeNode.setKey(germplasmList.getId().toString());
	    treeNode.setTitle(germplasmList.getName());
	    treeNode.setIsFolder(germplasmList.getType() != null 
	            && "FOLDER".equals(germplasmList.getType()) ? true : false);
	    treeNode.setIsLazy(false);
	    if(treeNode.getIsFolder()) {
	    	treeNode.setIcon(AppConstants.FOLDER_ICON_PNG.getString());
	    } else {
	    	treeNode.setIcon(AppConstants.BASIC_DETAILS_PNG.getString());
	    }
	    if(isFolderOnly && !treeNode.getIsFolder()) {
	    	return null;
	    }
	    	
	    
	    return treeNode;
	}
	
	/**
	 * Convert germplasm list to tree node.
	 *
	 * @param germplasmList the germplasm list
	 * @return the tree node
	 */
	private static TreeTableNode convertGermplasmListToTreeTableNode(
			GermplasmList germplasmList, 
			UserDataManager userDataManager,
			GermplasmListManager germplasmListManager) {
	    TreeTableNode treeTableNode = new TreeTableNode();
	    
	    treeTableNode.setId(germplasmList.getId().toString());
	    treeTableNode.setName(germplasmList.getName());
	    treeTableNode.setDescription(getDescriptionForDisplay(germplasmList));
	    treeTableNode.setType(getTypeString(germplasmList.getType(), germplasmListManager));
	    treeTableNode.setOwner(getOwnerListName(germplasmList.getUserId(),userDataManager));
	    
	    treeTableNode.setIsFolder(germplasmList.getType() != null 
	            && "FOLDER".equals(germplasmList.getType()) ? "1" : "0");
	    int noOfEntries = germplasmList.getListData().size();
	    treeTableNode.setNoOfEntries(noOfEntries==0?"":String.valueOf(noOfEntries));
	    treeTableNode.setParentId(getParentId(germplasmList));
	    return treeTableNode;
	}
	
	private static String getParentId(GermplasmList germplasmList) {
		Integer parentId = germplasmList.getParentId();
		if(parentId==null) {
			return "LISTS";
		}
		return String.valueOf(parentId);
	}

	private static String getTypeString(String typeCode, GermplasmListManager germplasmListManager) {
        String type = "Germplasm List";
        if(typeCode==null) {
        	return type;
        }
		try{    	
    		List<UserDefinedField> listTypes = germplasmListManager.getGermplasmListTypes();
            for (UserDefinedField listType : listTypes) {
                if(typeCode.equals(listType.getFcode())){
                    return listType.getFname();
                }
            }
        }catch(MiddlewareQueryException ex){
            LOG.error("Error in getting list types.", ex);
            return "";
        }
		return type;
    }
	
	private static String getOwnerListName(Integer userId, UserDataManager userDataManager) {
        try{
            User user=userDataManager.getUserById(userId);
            if(user != null){
                int personId=user.getPersonid();
                Person p =userDataManager.getPersonById(personId);

                if(p!=null){
                    return p.getFirstName()+" "+p.getMiddleName() + " "+p.getLastName();
                }else{
                    return user.getName();
                }
            } else {
                return "";
            }
        } catch(MiddlewareQueryException ex){
            LOG.error("Error with getting list owner name of user with id: " + userId, ex);
            return "";
        }
    }
	
	/**
	 * Convert tree view to json.
	 *
	 * @param treeNodes the tree nodes
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertTreeViewToJson(List<TreeNode> treeNodes) throws IOException {
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
	        throws IOException {
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
	        List<TraitClassReference> traitClassReferences, Map<String, StandardVariableReference> mapVariableRef) throws IOException {
		return convertSearchTreeViewToJson(getTypeAheadTreeNodes("", traitClassReferences, mapVariableRef));
    }
	
	private static List<TypeAheadSearchTreeNode> getTypeAheadTreeNodes(
	        String parentId, List<TraitClassReference> traitClassReferences, Map<String, StandardVariableReference> mapVariableRef){
	    List<TypeAheadSearchTreeNode> treeNodes = new ArrayList<TypeAheadSearchTreeNode>();

	    if (traitClassReferences != null && !traitClassReferences.isEmpty()) {
            for (TraitClassReference reference : traitClassReferences) {
              //this is for the inner trait classes
                if(reference.getTraitClassChildren() != null 
                        && !reference.getTraitClassChildren().isEmpty()){
                    String newParentId = "";
                    if(parentId != null && !"".equals(parentId)){
                        newParentId = parentId + "_";
                    }
                    newParentId = newParentId + reference.getId().toString();
                    treeNodes.addAll(getTypeAheadTreeNodes(newParentId, reference.getTraitClassChildren(), mapVariableRef));
                }
                
                List<PropertyReference> propRefList = reference.getProperties();
                for(PropertyReference propRef : propRefList){                                       
                    List<StandardVariableReference> variableRefList = propRef.getStandardVariables();
                    String parentTitle = reference.getName();
                    String key = reference.getId().toString() + "_" + propRef.getId().toString(); 
                    
                    if(parentId != null && !"".equals(parentId)){
                        key = parentId + "_" + key;
                    }
                    
                    List<String> token = new ArrayList<String>();
                    token.add(propRef.getName());
                    TypeAheadSearchTreeNode searchTreeNode = new TypeAheadSearchTreeNode(
                            key, token , propRef.getName(), parentTitle, "Property");
                    treeNodes.add(searchTreeNode);
                    
                    for(StandardVariableReference variableRef : variableRefList){
                    	boolean addVariableToSearch = true;
                    	if(mapVariableRef != null && !mapVariableRef.isEmpty()){
                			//we only show variables that are in the map
                			if(mapVariableRef.containsKey(variableRef.getId().toString())){
                				addVariableToSearch = true;
                			}else{
                				addVariableToSearch = false;
                			}
                    	}
                			
                			
                		
                    	if(addVariableToSearch){
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
	public static String convertOntologyTraitsToJson(List<TraitClassReference> traitClassReferences, Map<String, StandardVariableReference> mapVariableRef) 
	        throws IOException {		
		
        List<TreeNode> treeNodes = convertTraitClassReferencesToTreeView(traitClassReferences, mapVariableRef);
        
        return convertTreeViewToJson(treeNodes);
    }
	
	/**
	 * Convert trait references to tree view.
	 *
	 * @param traitClassReferences the trait references
	 * @return the list
	 */
	private static List<TreeNode> convertTraitClassReferencesToTreeView(
	        List<TraitClassReference> traitClassReferences, Map<String, StandardVariableReference> mapVariableRef) {
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
        if (traitClassReferences != null && !traitClassReferences.isEmpty()) {
            for (TraitClassReference reference : traitClassReferences) {
                treeNodes.add(convertTraitClassReferenceToTreeNode("", reference, mapVariableRef));
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
	        String parentParentId, TraitClassReference reference, Map<String, StandardVariableReference> mapVariableRef) {
        TreeNode treeNode = new TreeNode();
        String parentId = reference.getId().toString();
        if(parentParentId != null && !"".equals(parentParentId)){
            parentId = parentParentId + "_" + parentId;
        }
        treeNode.setKey(parentId);
        treeNode.setAddClass(parentId);
        treeNode.setTitle(reference.getName());
        treeNode.setIsFolder(true);
        treeNode.setIsLazy(false);
        treeNode.setIcon(false);
        treeNode.setIncludeInSearch(false);
        
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
        
        //this is for the inner trait classes
        if(reference.getTraitClassChildren() != null && !reference.getTraitClassChildren().isEmpty()){
            for (TraitClassReference childTrait : reference.getTraitClassChildren()) {
                treeNodes.add(convertTraitClassReferenceToTreeNode(parentId, childTrait, mapVariableRef));
            }
        }
        //we need to set the children for the property
        
        if(reference.getProperties() != null && !reference.getProperties().isEmpty()){
            for (PropertyReference propRef : reference.getProperties()) {
                treeNodes.add(convertPropertyReferenceToTreeNode(parentId, propRef, reference.getName(), mapVariableRef));
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
	        String parentId, PropertyReference reference, String parentTitle, Map<String, StandardVariableReference> mapVariableRef) {
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
        //we need to set the children for the property
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();
        if(reference.getStandardVariables() != null && !reference.getStandardVariables().isEmpty()){
            for (StandardVariableReference variableRef : reference.getStandardVariables()) {
            	TreeNode variableTreeNode = convertStandardVariableReferenceToTreeNode(id, variableRef, newParentTitle, mapVariableRef);
    			if(variableTreeNode != null) {
    				treeNodes.add(variableTreeNode);
    			}
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
	        String parentId, StandardVariableReference reference, String parentTitle, Map<String, StandardVariableReference> mapVariableRef) {
		
		if(mapVariableRef != null && !mapVariableRef.isEmpty() && 
			!mapVariableRef.containsKey(reference.getId().toString())){
			return null;
		}
		
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
        //we need to set the children for the property
        List<TreeNode> treeNodes = new ArrayList<TreeNode>();           
        treeNode.setChildren(treeNodes);
        
        return treeNode;
    }
}
