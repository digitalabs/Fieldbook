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
package com.efficio.pojos.svg;


public class Text extends Element{
    
    private int x;
    private int y;
    private String text;
    private String font;

    
    @Override
    protected String getElementTypeName() {
        return text;
    }

    
    public int getX() {
        return x;
    }

    
    public void setX(int x) {
        this.x = x;
    }

    
    public int getY() {
        return y;
    }

    
    public void setY(int y) {
        this.y = y;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getFont() {
        return font;
    }
    
    public void setFont(String font) {
        this.font = font;
    }

}
