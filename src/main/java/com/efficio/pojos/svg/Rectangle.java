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


public class Rectangle extends Element {

    private int x;
    private int y;
    private int width;
    private int height;
    private String fill;
    private String stroke;
    

    @Override
    protected String getElementTypeName() {
        return "rect";
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
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public String getFill() {
        return fill;
    }
    
    public void setFill(String fill) {
        this.fill = fill;
    }
    
    public String getStroke() {
        return stroke;
    }
    
    public void setStroke(String stroke) {
        this.stroke = stroke;
    }
    
}
