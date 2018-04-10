/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.pojos.treeview;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the data needed for rendering a tree view using dynatree jquery.
 */
public class TreeNode {

	private String title;
	private String key;
	private boolean isFolder;
	private boolean isLazy;
	private String addClass;
	private int numOfChildren;
	private List<TreeNode> children;
	private boolean expand;
	private boolean isLastChildren;
	private String parentTitle;
	private String parentId;
	private boolean includeInSearch;

	/**
	 * set icon to Boolean(false) to suppress icon. set icon to null to use default icon. set icon to an image file name relative the the
	 * image path to use a custom icon image.
	 */
	private Object icon;

	private String programUUID;

	/**
	 * Instantiates a new tree node.
	 */
	public TreeNode() {
	}

	/**
	 * Instantiates a new tree node.
	 *
	 * @param key the key
	 * @param title the title
	 * @param isFolder the is folder
	 * @param addClass the add class
	 * @param icon the icon
	 */
	public TreeNode(String key, String title, boolean isFolder, String addClass, Object icon, String programUUID) {
		this.key = key;
		this.title = title;
		this.isFolder = isFolder;
		this.addClass = addClass;
		this.icon = icon;
		this.isLazy = true;
		this.children = new ArrayList<TreeNode>();
		this.programUUID = programUUID;
	}

	public boolean getIsLazy() {
		return this.isLazy;
	}

	public void setIsLazy(boolean isLazy) {
		this.isLazy = isLazy;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean getIsFolder() {
		return this.isFolder;
	}

	public void setIsFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}

	public String getAddClass() {
		return this.addClass;
	}

	public void setAddClass(String addClass) {
		this.addClass = addClass;
	}

	public Object getIcon() {
		return this.icon;
	}

	public void setIcon(Object icon) {
		this.icon = icon;
	}

	public List<TreeNode> getChildren() {
		return this.children;
	}

	public void setChildren(List<TreeNode> children) {
		this.children = children;
	}

	public boolean isExpand() {
		return this.expand;
	}

	public void setExpand(boolean expand) {
		this.expand = expand;
	}

	public boolean isLastChildren() {
		return this.isLastChildren;
	}

	public void setLastChildren(boolean isLastChildren) {
		this.isLastChildren = isLastChildren;
	}

	public String getParentTitle() {
		return this.parentTitle;
	}

	public void setParentTitle(String parentTitle) {
		this.parentTitle = parentTitle;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(final String parentId) {
		this.parentId = parentId;
	}

	public boolean isIncludeInSearch() {
		return this.includeInSearch;
	}

	public void setIncludeInSearch(boolean includeInSearch) {
		this.includeInSearch = includeInSearch;
	}

	public String getProgramUUID() {
		return this.programUUID;
	}

	public void setProgramUUID(String programUUID) {
		this.programUUID = programUUID;
	}

	public int getNumOfChildren() {
		return numOfChildren;
	}

	public void setNumOfChildren(final int numOfChildren) {
		this.numOfChildren = numOfChildren;
	}
}
