package com.efficio.fieldbook.util.labelprinting.comparators;

class ComparatorUtil {

	static int compareNumbers(final long a, final long b) {
		return a < b ? -1 : a > b ? 1 : 0;
	}

}
