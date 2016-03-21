package com.efficio.fieldbook.web.label.printing.constant;

public enum LabelPrintingFileTypes {
	PDF ("1",".pdf"), EXCEL ("2", ".xls"), CSV ("3", ".csv"), INVALID;

	private String formIndex;
	private String extension;

	LabelPrintingFileTypes() {
		this.formIndex = "0";
		this.extension = "";
	}

	LabelPrintingFileTypes(String formIndex, String extension) {
		this.formIndex = formIndex;
		this.extension = extension;
	}

	public String getFormIndex() {
		return formIndex;
	}

	public String getExtension() {
		return extension;
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
