package com.efficio.pojos.treeview;

/**
 * This class holds the data needed for rendering a tree table using jquery treetable.
 */
public class TreeTableNode {
	
	private String id;
	private String name;
	private String owner;
	private String description;
	private String type;
	private String noOfEntries;
	private String isFolder;
	private String parentId;
	private String hasChildren;
	
	public TreeTableNode() {
		
	}
	
	public TreeTableNode(String id, String name, String owner,
			String description, String type, String noOfEntries, 
			String isFolder) {
		this.id = id;
		this.name = name;
		this.owner = owner;
		this.description = description;
		this.type = type;
		this.noOfEntries = noOfEntries;
		this.isFolder = isFolder;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getNoOfEntries() {
		return noOfEntries;
	}
	public void setNoOfEntries(String noOfEntries) {
		this.noOfEntries = noOfEntries;
	}
	public String getIsFolder() {
		return isFolder;
	}
	public void setIsFolder(String isFolder) {
		this.isFolder = isFolder;
	}
	public String getHasChildren() {
		return hasChildren;
	}
	public void setHasChildren(String hasChildren) {
		this.hasChildren = hasChildren;
	}
}
