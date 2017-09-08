
package com.efficio.fieldbook.web.study;

public enum ImportStudyType {
	IMPORT_NURSERY_EXCEL(3), IMPORT_KSU_EXCEL(5),
    IMPORT_KSU_CSV(6), IMPORT_NURSERY_CSV(7);

	private int type;

	ImportStudyType(final int type) {
		this.type = type;
	}

    public static ImportStudyType getImportType(final int importType) {
        for (final ImportStudyType importStudyType : ImportStudyType.values()) {
            if (importStudyType.getType() == importType) {
                return importStudyType;
            }
        }

        return null;
    }

    public int getType() {
        return type;
    }
}
