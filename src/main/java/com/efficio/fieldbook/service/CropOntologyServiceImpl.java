package com.efficio.fieldbook.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.service.api.CropOntologyService;
import com.efficio.pojos.cropontology.CropTerm;
import com.efficio.pojos.cropontology.Ontology;

@Service
public class CropOntologyServiceImpl extends AbstractRestfulService implements
		CropOntologyService {

	private static final String SEARCH_TERMS_URL = "http://www.cropontology.org/search?q=";
	private static final String GET_ONTOLOGY_ID_BY_NAME_URL = "http://www.cropontology.org/get-ontology-id?ontology_name=";
	private static final String GET_ONTOLOGIES_BY_CATEGORY = "http://www.cropontology.org/ontologies?category=";
	
	@Override
	public List<CropTerm> searchTerms(String query) {
		if (StringUtils.isNotBlank(query)) {
			String url = SEARCH_TERMS_URL + query;
			return (List<CropTerm>) getList(url, CropTerm.class);
		}
		return null;
	}

	@Override
	public String getOntologyIdByName(String name) {
		if (StringUtils.isNotBlank(name)) {
			String url = GET_ONTOLOGY_ID_BY_NAME_URL + name;
			List<CropTerm> cropTerms = getList(url, CropTerm.class);
			return cropTerms != null && cropTerms.size() > 0 ? cropTerms.get(0).getId() : null;
		}
		return null;
	}

	@Override
	public List<Ontology> getOntologiesByCategory(String category) {
		if (StringUtils.isNotBlank(category)) {
			String url = GET_ONTOLOGIES_BY_CATEGORY + category;
			return getList(url, Ontology.class);
		}
		return null;
	}

	
}
