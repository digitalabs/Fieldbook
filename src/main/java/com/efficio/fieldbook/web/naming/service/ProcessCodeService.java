
package com.efficio.fieldbook.web.naming.service;

import java.util.List;

import com.efficio.fieldbook.web.trial.bean.AdvancingSource;

public interface ProcessCodeService {

	List<String> applyProcessCode(String currentInput, String processCode, AdvancingSource source);
}
