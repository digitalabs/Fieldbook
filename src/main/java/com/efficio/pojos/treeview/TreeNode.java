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
package com.efficio.pojos.treeview;

/**
 * This class holds the data needed for rendering a tree view using dynatree jquery.
 */
public class TreeNode {
	
	private String title;
	
	private String key;
	
	private boolean isFolder;
	
	private boolean isLazy;
	
	private String addClass;
	
	private String icon;
	
	public TreeNode() {
	}
	
	public TreeNode(String key, String title, boolean isFolder, String addClass, String icon) {
	    this.key = key;
	    this.title = title;
	    this.isFolder = isFolder;
	    this.addClass = addClass;
	    this.icon = icon;
	    this.isLazy = true;
	}
	
	public boolean getIsLazy() {
		return isLazy;
	}

	public void setIsLazy(boolean isLazy) {
		this.isLazy = isLazy;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean getIsFolder() {
		return isFolder;
	}

	public void setIsFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}

    public String getAddClass() {
        return addClass;
    }

    public void setAddClass(String addClass) {
        this.addClass = addClass;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

}
