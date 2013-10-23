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
public class TypeAheadSearchTreeNode {
	
	private String value;
	private List<String> tokens;
	
	private String key;
	
	private String parentTitle;
	
	public TypeAheadSearchTreeNode() {
	}
	
	public TypeAheadSearchTreeNode(String key, List<String>  tokens, String value, String parentTitle) {
	    this.key = key;
	    this.tokens = tokens;	
	    this.value = value;
	    this.parentTitle = parentTitle;
	    
	}

    
    public String getValue() {
        return value;
    }

    
    public void setValue(String value) {
        this.value = value;
    }

    
    public List<String>  getTokens() {
        return tokens;
    }

    
    public void setTokens(List<String>  tokens) {
        this.tokens = tokens;
    }

    
    public String getKey() {
        return key;
    }

    
    public void setKey(String key) {
        this.key = key;
    }

    
    public String getParentTitle() {
        return parentTitle;
    }

    
    public void setParentTitle(String parentTitle) {
        this.parentTitle = parentTitle;
    }

    
    
}
