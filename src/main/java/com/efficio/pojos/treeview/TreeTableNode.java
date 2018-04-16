
package com.efficio.pojos.treeview;

/**
 * This class holds the data needed for rendering a tree table using jquery treetable.
 * TODO can be merged with {@link TreeNode}?
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
	private String numOfChildren;

	public TreeTableNode() {

	}

	public TreeTableNode(String id, String name, String owner, String description, String type, String noOfEntries, String isFolder) {
		this.id = id;
		this.name = name;
		this.owner = owner;
		this.description = description;
		this.type = type;
		this.noOfEntries = noOfEntries;
		this.isFolder = isFolder;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return this.parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOwner() {
		return this.owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getNoOfEntries() {
		return this.noOfEntries;
	}

	public void setNoOfEntries(String noOfEntries) {
		this.noOfEntries = noOfEntries;
	}

	public String getIsFolder() {
		return this.isFolder;
	}

	public void setIsFolder(String isFolder) {
		this.isFolder = isFolder;
	}

	public String getNumOfChildren() {
		return this.numOfChildren;
	}

	public void setNumOfChildren(String numOfChildren) {
		this.numOfChildren = numOfChildren;
	}

	public void setIsFolder(boolean isFolder) {
		this.setIsFolder(isFolder ? "1" : "0");
	}
}
