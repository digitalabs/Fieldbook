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
	
	/** 
	 * set icon to Boolean(false) to suppress icon.
	 * set icon to null to use default icon.
	 * set icon to an image file name relative the the image path to use a custom icon image.
	 */
	private Object icon;
	
	public TreeNode() {
	}
	
	public TreeNode(String key, String title, boolean isFolder, String addClass, Object icon) {
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

    public Object getIcon() {
        return icon;
    }

    public void setIcon(Object icon) {
        this.icon = icon;
    }

}
