/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.service.api.CropOntologyService;
import com.efficio.pojos.cropontology.CropTerm;
import com.efficio.pojos.cropontology.Ontology;

/**
 * The Class CropOntologyServiceImpl.
 */
@Service
public class CropOntologyServiceImpl extends AbstractRestfulService implements
		CropOntologyService {

	/** The Constant SEARCH_TERMS_URL. */
	private static final String SEARCH_TERMS_URL = "http://www.cropontology.org/search?q=";
	
	/** The Constant GET_ONTOLOGY_ID_BY_NAME_URL. */
	private static final String GET_ONTOLOGY_ID_BY_NAME_URL = "http://www.cropontology.org/get-ontology-id?ontology_name=";
	
	/** The Constant GET_ONTOLOGIES_BY_CATEGORY. */
	private static final String GET_ONTOLOGIES_BY_CATEGORY = "http://www.cropontology.org/ontologies?category=";
	
	/* (non-Javadoc)
	 * @see com.efficio.fieldbook.service.api.CropOntologyService#searchTerms(java.lang.String)
	 */
	@Override
	public List<CropTerm> searchTerms(String query) {
		if (StringUtils.isNotBlank(query)) {
			String url = SEARCH_TERMS_URL + query;
			return (List<CropTerm>) getList(url, CropTerm.class);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.efficio.fieldbook.service.api.CropOntologyService#getOntologyIdByName(java.lang.String)
	 */
	@Override
	public String getOntologyIdByName(String name) {
		if (StringUtils.isNotBlank(name)) {
			String url = GET_ONTOLOGY_ID_BY_NAME_URL + name;
			List<CropTerm> cropTerms = getList(url, CropTerm.class);
			return cropTerms != null && cropTerms.size() > 0 ? cropTerms.get(0).getId() : null;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.efficio.fieldbook.service.api.CropOntologyService#getOntologiesByCategory(java.lang.String)
	 */
	@Override
	public List<Ontology> getOntologiesByCategory(String category) {
		if (StringUtils.isNotBlank(category)) {
			String url = GET_ONTOLOGIES_BY_CATEGORY + category;
			return getList(url, Ontology.class);
		}
		return null;
	}

	
}
