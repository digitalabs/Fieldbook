
package com.efficio.etl.web.bean;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 * 
 * This bean serves as a lightweight data transfer object for providing sheet index and name data for the application
 */
public class SheetDTO {

	private final int sheetIndex;
	private final String sheetName;

	public SheetDTO(int sheetIndex, String sheetName) {
		this.sheetIndex = sheetIndex;
		this.sheetName = sheetName;
	}

	public int getSheetIndex() {
		return this.sheetIndex;
	}

	public String getSheetName() {
		return this.sheetName;
	}
}
