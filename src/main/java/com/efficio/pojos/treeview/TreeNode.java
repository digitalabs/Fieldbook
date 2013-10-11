package com.efficio.pojos.treeview;


public class TreeNode {
	
	private String title;
	
	private String key;
	
	private boolean isFolder;
	
	private boolean isLazy;
	
	private String addClass;
	
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

}
