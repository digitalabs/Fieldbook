
package com.efficio.etl.web.bean;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

@Deprecated
public class PageInfo {

	private final int pageNumber;
	private final int startRow;
	private final int lastRow;
	private String pageClickFunction;

	public PageInfo(int pageNumber, int startRow, int lastRow) {
		this.pageNumber = pageNumber;
		this.startRow = startRow;
		this.lastRow = lastRow;
	}

	public int getPageNumber() {
		return this.pageNumber;
	}

	public int getStartRow() {
		return this.startRow;
	}

	public int getLastRow() {
		return this.lastRow;
	}

	public String getPageClickFunction() {
		return this.pageClickFunction;
	}

	public void setPageClickFunction(String pageClickFunction) {
		this.pageClickFunction = pageClickFunction;
	}
}
