
package com.efficio.fieldbook.util;

public class FileExportInfo {

	private String filePath;
	private String downloadFileName;

	public FileExportInfo() {
		// fields to be filled up later
	}
	
	public FileExportInfo(final String filePath, final String downloadFileName) {
		super();
		this.filePath = filePath;
		this.downloadFileName = downloadFileName;
	}

	public String getFilePath() {
		return this.filePath;
	}

	public void setFilePath(final String filePath) {
		this.filePath = filePath;
	}

	public String getDownloadFileName() {
		return this.downloadFileName;
	}

	public void setDownloadFileName(final String downloadFileName) {
		this.downloadFileName = downloadFileName;
	}

}
