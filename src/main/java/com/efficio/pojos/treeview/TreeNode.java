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

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the data needed for rendering a tree view using dynatree jquery.
 */
public class TreeNode {
	
	/** The title. */
	private String title;
	
	/** The key. */
	private String key;
	
	/** The is folder. */
	private boolean isFolder;
	
	/** The is lazy. */
	private boolean isLazy;
	
	/** The add class. */
	private String addClass;
	
	/** The children. */
	private List<TreeNode> children;
	
	/** The expand. */
	private boolean expand;
	
	/** The is last children. */
	private boolean isLastChildren;
	
	/** The parent title. */
	private String parentTitle;
	
	/** The include in search. */
	private boolean includeInSearch;
	
	/** 
	 * set icon to Boolean(false) to suppress icon.
	 * set icon to null to use default icon.
	 * set icon to an image file name relative the the image path to use a custom icon image.
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
	
	/**
	 * Gets the checks if is lazy.
	 *
	 * @return the checks if is lazy
	 */
	public boolean getIsLazy() {
		return isLazy;
	}

	/**
	 * Sets the checks if is lazy.
	 *
	 * @param isLazy the new checks if is lazy
	 */
	public void setIsLazy(boolean isLazy) {
		this.isLazy = isLazy;
	}

	/**
	 * Gets the title.
	 *
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title.
	 *
	 * @param title the new title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Sets the key.
	 *
	 * @param key the new key
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Gets the checks if is folder.
	 *
	 * @return the checks if is folder
	 */
	public boolean getIsFolder() {
		return isFolder;
	}

	/**
	 * Sets the checks if is folder.
	 *
	 * @param isFolder the new checks if is folder
	 */
	public void setIsFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}

    /**
     * Gets the adds the class.
     *
     * @return the adds the class
     */
    public String getAddClass() {
        return addClass;
    }

    /**
     * Sets the adds the class.
     *
     * @param addClass the new adds the class
     */
    public void setAddClass(String addClass) {
        this.addClass = addClass;
    }

    /**
     * Gets the icon.
     *
     * @return the icon
     */
    public Object getIcon() {
        return icon;
    }

    /**
     * Sets the icon.
     *
     * @param icon the new icon
     */
    public void setIcon(Object icon) {
        this.icon = icon;
    }
    
    /**
     * Gets the children.
     *
     * @return the children
     */
    public List<TreeNode> getChildren() {
        return children;
    }

    /**
     * Sets the children.
     *
     * @param children the new children
     */
    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }
    
    /**
     * Checks if is expand.
     *
     * @return true, if is expand
     */
    public boolean isExpand() {
        return expand;
    }
    
    /**
     * Sets the expand.
     *
     * @param expand the new expand
     */
    public void setExpand(boolean expand) {
        this.expand = expand;
    }
    
    /**
     * Checks if is last children.
     *
     * @return true, if is last children
     */
    public boolean isLastChildren() {
        return isLastChildren;
    }
    
    /**
     * Sets the last children.
     *
     * @param isLastChildren the new last children
     */
    public void setLastChildren(boolean isLastChildren) {
        this.isLastChildren = isLastChildren;
    }
    
    /**
     * Gets the parent title.
     *
     * @return the parent title
     */
    public String getParentTitle() {
        return parentTitle;
    }
    
    /**
     * Sets the parent title.
     *
     * @param parentTitle the new parent title
     */
    public void setParentTitle(String parentTitle) {
        this.parentTitle = parentTitle;
    }
    
    /**
     * Checks if is include in search.
     *
     * @return true, if is include in search
     */
    public boolean isIncludeInSearch() {
        return includeInSearch;
    }
    
    /**
     * Sets the include in search.
     *
     * @param includeInSearch the new include in search
     */
    public void setIncludeInSearch(boolean includeInSearch) {
        this.includeInSearch = includeInSearch;
    }

	public String getProgramUUID() {
		return programUUID;
	}

	public void setProgramUUID(String programUUID) {
		this.programUUID = programUUID;
	}
}
