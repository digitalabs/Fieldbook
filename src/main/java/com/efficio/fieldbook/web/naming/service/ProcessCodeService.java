package com.efficio.fieldbook.web.naming.service;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public interface ProcessCodeService {

	List<String> applyToName(String paddedExpression, AdvancingSource source);
}
