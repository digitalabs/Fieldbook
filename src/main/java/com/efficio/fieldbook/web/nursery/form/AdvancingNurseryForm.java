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
package com.efficio.fieldbook.web.nursery.form;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;

/**
 * The Class AdvancingNurseryForm.
 */
public class AdvancingNurseryForm {
	
    /** The naming convention. */
    private String namingConvention;
	
    /** The suffix convention. */
    private String suffixConvention;
	
    /** The method choice. */
    private String methodChoice;
	
    /** The line choice. */
    private String lineChoice;
	
    /** The line selected. */
    private String lineSelected;
	
    /** The harvest date. */
    private String harvestDate;
	
    /** The harvest location. */
    private String harvestLocation;
	
    /** The field location id all. */
    private String harvestLocationIdAll;
    
    /** The field location id favorite. */
    private String harvestLocationIdFavorite;
    
    /** The harvest location id. */
    private String harvestLocationId;
    
    /** The harvest location name. */
    private String harvestLocationName;
    
    /** The harvest location abbreviation. */
    private String harvestLocationAbbreviation;
    
    /** The default method id. */
    private String defaultMethodId;
       
    /** The breeding method id. */
    private String breedingMethodId;
    
    /** The field location id all. */
    private String methodIdAll;
    
    /** The field location id favorite. */
    private String methodIdFavorite;
    
    /** The project id. */
    private String projectId;
    
    /** The nursery advance name. */
    private String nurseryAdvanceName;
    
    /** The nursery advance description. */
    private String nurseryAdvanceDescription;
    
    /** The entries. */
    private int entries;
    
    /** The germplasm list. */
    private List<ImportedGermplasm> germplasmList;
    
    private List<ImportedGermplasm> paginatedGermplasmList;	
	
	/** The current page. */
	private int currentPage;
	
	/** The total pages. */
	private int totalPages;
	
	/** The result per page. */
	private int resultPerPage = 100;
	
	private int plotsWithPlantsSelected;
	
	private String locationUrl;
    private String breedingMethodUrl;
    
	
	/**
	 * Gets the method id all.
	 *
	 * @return the method id all
	 */
	public String getMethodIdAll() {
		return methodIdAll;
	}

	/**
	 * Sets the method id all.
	 *
	 * @param methodIdAll the new method id all
	 */
	public void setMethodIdAll(String methodIdAll) {
		this.methodIdAll = methodIdAll;
	}

	/**
	 * Gets the method id favorite.
	 *
	 * @return the method id favorite
	 */
	public String getMethodIdFavorite() {
		return methodIdFavorite;
	}

	/**
	 * Sets the method id favorite.
	 *
	 * @param methodIdFavorite the new method id favorite
	 */
	public void setMethodIdFavorite(String methodIdFavorite) {
		this.methodIdFavorite = methodIdFavorite;
	}

	/**
	 * Gets the harvest location abbreviation.
	 *
	 * @return the harvest location abbreviation
	 */
	public String getHarvestLocationAbbreviation() {
		return harvestLocationAbbreviation;
	}
	
	/**
	 * Sets the harvest location abbreviation.
	 *
	 * @param harvestLocationAbbreviation the new harvest location abbreviation
	 */
	public void setHarvestLocationAbbreviation(String harvestLocationAbbreviation) {
		this.harvestLocationAbbreviation = harvestLocationAbbreviation;
	}
	
	/**
	 * Gets the default method id.
	 *
	 * @return the default method id
	 */
	public String getDefaultMethodId() {
		return defaultMethodId;
	}
	
	/**
	 * Sets the default method id.
	 *
	 * @param defaultMethodId the new default method id
	 */
	public void setDefaultMethodId(String defaultMethodId) {
		this.defaultMethodId = defaultMethodId;
	}
	
	/**
	 * Gets the harvest location id all.
	 *
	 * @return the harvest location id all
	 */
	public String getHarvestLocationIdAll() {
		return harvestLocationIdAll;
	}
	
	/**
	 * Sets the harvest location id all.
	 *
	 * @param harvestLocationIdAll the new harvest location id all
	 */
	public void setHarvestLocationIdAll(String harvestLocationIdAll) {
		this.harvestLocationIdAll = harvestLocationIdAll;
	}
	
	/**
	 * Gets the harvest location id favorite.
	 *
	 * @return the harvest location id favorite
	 */
	public String getHarvestLocationIdFavorite() {
		return harvestLocationIdFavorite;
	}
	
	/**
	 * Sets the harvest location id favorite.
	 *
	 * @param harvestLocationIdFavorite the new harvest location id favorite
	 */
	public void setHarvestLocationIdFavorite(String harvestLocationIdFavorite) {
		this.harvestLocationIdFavorite = harvestLocationIdFavorite;
	}
	
	/**
	 * Gets the harvest location id.
	 *
	 * @return the harvest location id
	 */
	public String getHarvestLocationId() {
		return harvestLocationId;
	}
	
	/**
	 * Sets the harvest location id.
	 *
	 * @param harvestLocationId the new harvest location id
	 */
	public void setHarvestLocationId(String harvestLocationId) {
		this.harvestLocationId = harvestLocationId;
	}
	
	/**
	 * Gets the harvest location name.
	 *
	 * @return the harvest location name
	 */
	public String getHarvestLocationName() {
		return harvestLocationName;
	}
	
	/**
	 * Sets the harvest location name.
	 *
	 * @param harvestLocationName the new harvest location name
	 */
	public void setHarvestLocationName(String harvestLocationName) {
		this.harvestLocationName = harvestLocationName;
	}
	
	/**
	 * Gets the harvest date.
	 *
	 * @return the harvest date
	 */
	public String getHarvestDate() {
		return harvestDate;
	}
	
	/**
	 * Sets the harvest date.
	 *
	 * @param harvestDate the new harvest date
	 */
	public void setHarvestDate(String harvestDate) {
		this.harvestDate = harvestDate;
	}
	
	/**
	 * Gets the harvest location.
	 *
	 * @return the harvest location
	 */
	public String getHarvestLocation() {
		return harvestLocation;
	}
	
	/**
	 * Sets the harvest location.
	 *
	 * @param harvestLocation the new harvest location
	 */
	public void setHarvestLocation(String harvestLocation) {
		this.harvestLocation = harvestLocation;
	}
	
	
	/**
	 * Gets the breeding method id.
	 *
	 * @return the breeding method id
	 */
	public String getBreedingMethodId() {
		return breedingMethodId;
	}

	/**
	 * Sets the breeding method id.
	 *
	 * @param breedingMethodId the new breeding method id
	 */
	public void setBreedingMethodId(String breedingMethodId) {
		this.breedingMethodId = breedingMethodId;
	}

	/**
	 * Gets the naming convention.
	 *
	 * @return the naming convention
	 */
	public String getNamingConvention() {
		return namingConvention;
	}
	
	/**
	 * Sets the naming convention.
	 *
	 * @param namingConvention the new naming convention
	 */
	public void setNamingConvention(String namingConvention) {
		this.namingConvention = namingConvention;
	}
	
	/**
	 * Gets the suffix convention.
	 *
	 * @return the suffix convention
	 */
	public String getSuffixConvention() {
		return suffixConvention;
	}
	
	/**
	 * Sets the suffix convention.
	 *
	 * @param suffixConvention the new suffix convention
	 */
	public void setSuffixConvention(String suffixConvention) {
		this.suffixConvention = suffixConvention;
	}
	
	/**
	 * Gets the method choice.
	 *
	 * @return the method choice
	 */
	public String getMethodChoice() {
		return methodChoice;
	}
	
	/**
	 * Sets the method choice.
	 *
	 * @param methodChoice the new method choice
	 */
	public void setMethodChoice(String methodChoice) {
		this.methodChoice = methodChoice;
	}
	
	
	
	/**
	 * Gets the line choice.
	 *
	 * @return the line choice
	 */
	public String getLineChoice() {
		return lineChoice;
	}
	
	/**
	 * Sets the line choice.
	 *
	 * @param lineChoice the new line choice
	 */
	public void setLineChoice(String lineChoice) {
		this.lineChoice = lineChoice;
	}
	
	/**
	 * Gets the line selected.
	 *
	 * @return the line selected
	 */
	public String getLineSelected() {
		return lineSelected;
	}
	
	/**
	 * Sets the line selected.
	 *
	 * @param lineSelected the new line selected
	 */
	public void setLineSelected(String lineSelected) {
		this.lineSelected = lineSelected;
	}
	
	/**
	 * Gets the project id.
	 *
	 * @return the project id
	 */
	public String getProjectId() {
            return projectId;
        }
	
	/**
	 * Sets the project id.
	 *
	 * @param projectId the new project id
	 */
	public void setProjectId(String projectId) {
            this.projectId = projectId;
        }
	
	/**
	 * Gets the nursery advance name.
	 *
	 * @return the nursery advance name
	 */
	public String getNurseryAdvanceName() {
	    return nurseryAdvanceName;
	}
	
	/**
	 * Sets the nursery advance name.
	 *
	 * @param nurseryAdvanceName the new nursery advance name
	 */
	public void setNurseryAdvanceName(String nurseryAdvanceName) {
	    this.nurseryAdvanceName = nurseryAdvanceName;
	}
	
	/**
	 * Gets the nursery advance description.
	 *
	 * @return the nursery advance description
	 */
	public String getNurseryAdvanceDescription() {
            return nurseryAdvanceDescription;
        }
        
        /**
         * Sets the nursery advance description.
         *
         * @param nurseryAdvanceDescription the new nursery advance description
         */
        public void setNurseryAdvanceDescription(String nurseryAdvanceDescription) {
            this.nurseryAdvanceDescription = nurseryAdvanceDescription;
        }
        
        /**
         * Gets the entries.
         *
         * @return the entries
         */
        public int getEntries() {
            return entries;
        }
        
        /**
         * Sets the entries.
         *
         * @param entries the new entries
         */
        public void setEntries(int entries) {
            this.entries = entries;
        }
        
        /**
         * Gets the germplasm list.
         *
         * @return the germplasm list
         */
        public List<ImportedGermplasm> getGermplasmList() {
            return germplasmList;
        }
        
        /**
         * Sets the germplasm list.
         *
         * @param germplasmList the new germplasm list
         */
        public void setGermplasmList(List<ImportedGermplasm> germplasmList) {
            this.germplasmList = germplasmList;
        }
        
        

        public List<ImportedGermplasm> getPaginatedGermplasmList() {
			return paginatedGermplasmList;
		}

		public void setPaginatedGermplasmList(
				List<ImportedGermplasm> paginatedGermplasmList) {
			this.paginatedGermplasmList = paginatedGermplasmList;
		}

		public int getCurrentPage() {
			return currentPage;
		}

		public void setCurrentPage(int currentPage) {
			this.currentPage = currentPage;
		}

		public int getTotalPages() {
			 if(germplasmList != null && !germplasmList.isEmpty()){           
	            totalPages = (int) Math.ceil((germplasmList.size() * 1f) / getResultPerPage()); 
	        }else{
	            totalPages = 0;
	        }
		    return totalPages;
		}

		public void setTotalPages(int totalPages) {
			this.totalPages = totalPages;
		}

		public int getResultPerPage() {
			return resultPerPage;
		}

		public void setResultPerPage(int resultPerPage) {
			this.resultPerPage = resultPerPage;
		}
		
		/**
		 * Change page.
		 *
		 * @param currentPage the current page
		 */
		public void changePage(int currentPage){
	    	 //assumption is there is an imported germplasm already
	        if(germplasmList != null && !germplasmList.isEmpty()){
	            int totalItemsPerPage = getResultPerPage();
	            int start = (currentPage - 1) * totalItemsPerPage;
	            int end = start + totalItemsPerPage;
	            if(germplasmList.size() < end){
	                end = germplasmList.size();
	            }
	            paginatedGermplasmList = germplasmList.subList(start, end);
	            this.currentPage = currentPage;
	        }else{
	            this.currentPage = 0;
	        }
		}
	
	public int getPlotsWithPlantsSelected() {
	    return plotsWithPlantsSelected;
	}
	
	public void setPlotsWithPlantsSelected(int plotsWithPlantsSelected) {
	    this.plotsWithPlantsSelected = plotsWithPlantsSelected;
	}
	
	public String getLocationUrl() {
		return locationUrl;
	}

	public void setLocationUrl(String locationUrl) {
		this.locationUrl = locationUrl;
	}

	public String getBreedingMethodUrl() {
		return breedingMethodUrl;
	}

	public void setBreedingMethodUrl(String breedingMethodUrl) {
		this.breedingMethodUrl = breedingMethodUrl;
	}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("AdvancingNurseryForm [namingConvention=");
            builder.append(namingConvention);
            builder.append(", suffixConvention=");
            builder.append(suffixConvention);
            builder.append(", methodChoice=");
            builder.append(methodChoice);
            builder.append(", lineChoice=");
            builder.append(lineChoice);
            builder.append(", lineSelected=");
            builder.append(lineSelected);
            builder.append(", harvestDate=");
            builder.append(harvestDate);
            builder.append(", harvestLocation=");
            builder.append(harvestLocation);
            builder.append(", harvestLocationIdAll=");
            builder.append(harvestLocationIdAll);
            builder.append(", harvestLocationIdFavorite=");
            builder.append(harvestLocationIdFavorite);
            builder.append(", harvestLocationId=");
            builder.append(harvestLocationId);
            builder.append(", harvestLocationName=");
            builder.append(harvestLocationName);
            builder.append(", harvestLocationAbbreviation=");
            builder.append(harvestLocationAbbreviation);
            builder.append(", defaultMethodId=");
            builder.append(defaultMethodId);
            builder.append(", breedingMethodId=");
            builder.append(breedingMethodId);
            builder.append(", methodIdAll=");
            builder.append(methodIdAll);
            builder.append(", methodIdFavorite=");
            builder.append(methodIdFavorite);
            builder.append(", projectId=");
            builder.append(projectId);
            builder.append(", nurseryAdvanceName=");
            builder.append(nurseryAdvanceName);
            builder.append(", nurseryAdvanceDescription=");
            builder.append(nurseryAdvanceDescription);
            builder.append(", entries=");
            builder.append(entries);
            builder.append(", germplasmList=");
            builder.append(germplasmList);
            builder.append("]");
            return builder.toString();
        }
}
