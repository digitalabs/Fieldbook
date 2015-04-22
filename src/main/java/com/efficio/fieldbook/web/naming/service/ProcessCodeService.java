package com.efficio.fieldbook.web.naming.service;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

import java.util.List;

public interface ProcessCodeService {

	List<String> applyProcessCode(String currentInput, String processCode, AdvancingSource source);
}
