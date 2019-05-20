
package com.efficio.fieldbook.util;

import com.efficio.fieldbook.web.label.printing.template.LabelPaper;
import com.efficio.fieldbook.web.label.printing.template.Paper3by10A4;
import com.efficio.fieldbook.web.label.printing.template.Paper3by10Letter;
import com.efficio.fieldbook.web.label.printing.template.Paper3by7A4;
import com.efficio.fieldbook.web.label.printing.template.Paper3by7Letter;
import com.efficio.fieldbook.web.label.printing.template.Paper3by8A4;
import com.efficio.fieldbook.web.label.printing.template.Paper3by8Letter;
import org.generationcp.commons.constant.AppConstants;

public class LabelPaperFactory {

	public static LabelPaper generateLabelPaper(int labelsPerRow, int numberOfRowsPerPage, int pageSize) {
		LabelPaper paper = new Paper3by7A4();
		if (AppConstants.SIZE_OF_PAPER_LETTER.getInt() == pageSize) {
			if (labelsPerRow == 3) {
				switch (numberOfRowsPerPage) {
					case 7:
						paper = new Paper3by7Letter();
						break;
					case 8:
						paper = new Paper3by8Letter();
						break;
					case 10:
						paper = new Paper3by10Letter();
						break;
				}
			}
		} else if (AppConstants.SIZE_OF_PAPER_A4.getInt() == pageSize) {
			if (labelsPerRow == 3) {
				switch (numberOfRowsPerPage) {
					case 7:
						paper = new Paper3by7A4();
						break;
					case 8:
						paper = new Paper3by8A4();
						break;
					case 10:
						paper = new Paper3by10A4();
						break;
				}
			}
		}
		return paper;
	}
}
