package com.efficio.fieldbook.web.label.printing.template;

public class LabelPaper {
	private float cellHeight;
	private float marginLeft;
	private float marginRight;
	private float marginTop;
	private float marginBottom;
	private float fontSize;
	private float spacingAfter;
	
	public LabelPaper(float cellHeight, float marginLeft, float marginRight, float marginTop, float marginBottom, float fontSize, float spacingAfter){
		this.cellHeight = cellHeight;
		this.marginLeft = marginLeft;
		this.marginRight = marginRight;
		this.marginTop = marginTop;
		this.marginBottom = marginBottom;
		this.fontSize = fontSize;
		this.spacingAfter = spacingAfter;
	}

	public float getCellHeight() {
		return cellHeight;
	}

	public void setCellHeight(float cellHeight) {
		this.cellHeight = cellHeight;
	}

	public float getMarginLeft() {
		return marginLeft;
	}

	public void setMarginLeft(float marginLeft) {
		this.marginLeft = marginLeft;
	}

	public float getMarginRight() {
		return marginRight;
	}

	public void setMarginRight(float marginRight) {
		this.marginRight = marginRight;
	}

	public float getMarginTop() {
		return marginTop;
	}

	public void setMarginTop(float marginTop) {
		this.marginTop = marginTop;
	}

	public float getMarginBottom() {
		return marginBottom;
	}

	public void setMarginBottom(float marginBottom) {
		this.marginBottom = marginBottom;
	}

	public float getFontSize() {
		return fontSize;
	}

	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}

	public float getSpacingAfter() {
		return spacingAfter;
	}

	public void setSpacingAfter(float spacingAfter) {
		this.spacingAfter = spacingAfter;
	}
	
	
	
	
}
