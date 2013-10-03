package com.efficio.fieldbook.service.api;

import java.util.List;

import com.efficio.pojos.cropontology.CropTerm;
import com.efficio.pojos.cropontology.Ontology;

public interface CropOntologyService {
	
	/**
	 * Returns an Array of objects matching the search query - each object being a term.
	 * 
	 * @param query
	 * @return
	 */
	List<CropTerm> searchTerms(String query);
	
	/**
	 * Retrieve Ontology ID by its Name
	 * 
	 * @param name
	 * @return
	 */
	String getOntologyIdByName(String name);

	List<Ontology> getOntologiesByCategory(String category);
}
