package com.efficio.fieldbook.web.label.printing.constant;

public enum LabelPrintingFileTypes {
	PDF ("1", "PDF", ".pdf"), EXCEL ("2", "EXCEL", ".xls"), CSV ("3", "CSV", ".csv"), INVALID;

	private String formIndex;
	private String extension;
	private String type;

	LabelPrintingFileTypes() {
		this.formIndex = "0";
		this.type = "INVALID";
		this.extension = "";
	}

	LabelPrintingFileTypes(String formIndex, String type, String extension) {
		this.formIndex = formIndex;
		this.type = type;
		this.extension = extension;
	}

	public String getFormIndex() {
		return formIndex;
	}

	public String getExtension() {
		return extension;
	}

	public String getType() {
		return type;
	}

	public static LabelPrintingFileTypes getFileTypeByIndex(String formIndex) {
		for (LabelPrintingFileTypes fileType : LabelPrintingFileTypes.values()) {
			if (fileType.getFormIndex().equals(formIndex)) {
				return fileType;
			}
		}
		return INVALID;
	}

	public boolean isValid() {
		return !INVALID.equals(this);
	}
}
