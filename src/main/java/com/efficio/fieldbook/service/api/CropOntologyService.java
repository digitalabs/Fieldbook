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
package com.efficio.fieldbook.service.api;

import java.util.List;

import com.efficio.pojos.cropontology.CropTerm;
import com.efficio.pojos.cropontology.Ontology;

/**
 * The Interface CropOntologyService.
 */
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

	/**
	 * Gets the ontologies by category.
	 *
	 * @param category 
	 * @return the ontologies by category
	 */
	List<Ontology> getOntologiesByCategory(String category);
}
