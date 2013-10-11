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
import org.generationcp.middleware.domain.dms.StudyReference;

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
	
	private static List<TreeNode> convertReferencesToTreeView(List<Reference> references) {
		List<TreeNode> treeNodes = null;
		if (references != null && references.size() > 0) {
			treeNodes = new ArrayList<TreeNode>();
			for (Reference reference : references) {
				treeNodes.add(convertReferenceToTreeNode(reference));
			}
		}
		return treeNodes;
	}
	
	private static List<TreeNode> convertFolderReferencesToTreeView(List<FolderReference> references) {
		List<TreeNode> treeNodes = null;
		if (references != null && references.size() > 0) {
			treeNodes = new ArrayList<TreeNode>();
			for (FolderReference reference : references) {
				treeNodes.add(convertReferenceToTreeNode(reference));
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
	
	private static String convertTreeViewToJson(List<TreeNode> treeNodes) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(treeNodes);
	}
}
