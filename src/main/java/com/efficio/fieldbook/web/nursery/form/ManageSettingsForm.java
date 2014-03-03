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

import com.efficio.fieldbook.web.nursery.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.bean.SettingVariable;

// TODO: Auto-generated Javadoc
/**
 * The Class AddOrRemoveTraitsForm.
 */
public class ManageSettingsForm {
	
	/** The selected setting id. */
	private int selectedSettingId;
	
	/** The setting name. */
	private String settingName;
	
	/** The nursery level variables. */
	private List<SettingDetail> nurseryLevelVariables;
	
	/** The plot level variables. */
	private List<SettingDetail> plotLevelVariables;
	
	/** The baseline trait variables. */
	private List<SettingDetail> baselineTraitVariables;
	
	/** The is default. */
	private boolean isDefault;
	
	/** The selected variables. */
	private List<SettingVariable> selectedVariables;
	
	/** The project id. */
	private String projectId;
	
	/** The initial load. */
	private String initialLoad;
	
	/**
	 * Gets the selected setting id.
	 *
	 * @return the selected setting id
	 */
	public int getSelectedSettingId() {
	    return selectedSettingId;
	}
	
	/**
	 * Sets the selected setting id.
	 *
	 * @param selectedSettingId the new selected setting id
	 */
	public void setSelectedSettingId(int selectedSettingId) {
	    this.selectedSettingId = selectedSettingId;
	}
	
	/**
	 * Gets the setting name.
	 *
	 * @return the setting name
	 */
	public String getSettingName() {
            return settingName;
        }
        
        /**
         * Sets the setting name.
         *
         * @param settingName the new setting name
         */
        public void setSettingName(String settingName) {
            this.settingName = settingName;
        }
        
        /**
         * Gets the nursery level variables.
         *
         * @return the nursery level variables
         */
        public List<SettingDetail> getNurseryLevelVariables() {
            return nurseryLevelVariables;
        }
        
        /**
         * Sets the nursery level variables.
         *
         * @param nurseryLevelVariables the new nursery level variables
         */
        public void setNurseryLevelVariables(List<SettingDetail> nurseryLevelVariables) {
            this.nurseryLevelVariables = nurseryLevelVariables;
        }
        
        /**
         * Gets the plot level variables.
         *
         * @return the plot level variables
         */
        public List<SettingDetail> getPlotLevelVariables() {
            return plotLevelVariables;
        }
        
        /**
         * Sets the plot level variables.
         *
         * @param plotLevelVariables the new plot level variables
         */
        public void setPlotLevelVariables(List<SettingDetail> plotLevelVariables) {
            this.plotLevelVariables = plotLevelVariables;
        }
        
        /**
         * Gets the baseline trait variables.
         *
         * @return the baseline trait variables
         */
        public List<SettingDetail> getBaselineTraitVariables() {
            return baselineTraitVariables;
        }
        
        /**
         * Sets the baseline trait variables.
         *
         * @param baselineTraitVariables the new baseline trait variables
         */
        public void setBaselineTraitVariables(List<SettingDetail> baselineTraitVariables) {
            this.baselineTraitVariables = baselineTraitVariables;
        }
        
        /**
         * Gets the checks if is default.
         *
         * @return the checks if is default
         */
        public boolean getIsDefault() {
            return isDefault;
        }
        
        /**
         * Sets the checks if is default.
         *
         * @param isDefault the new checks if is default
         */
        public void setIsDefault(boolean isDefault) {
            this.isDefault = isDefault;
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
		 * Gets the selected variables.
		 *
		 * @return the selected variables
		 */
		public List<SettingVariable> getSelectedVariables() {
			return selectedVariables;
		}

		/**
		 * Sets the selected variables.
		 *
		 * @param selectedVariables the new selected variables
		 */
		public void setSelectedVariables(List<SettingVariable> selectedVariables) {
			this.selectedVariables = selectedVariables;
		}
		
		

		
		/**
		 * Gets the initial load.
		 *
		 * @return the initial load
		 */
		public String getInitialLoad() {
			return initialLoad;
		}

		/**
		 * Sets the initial load.
		 *
		 * @param initialLoad the new initial load
		 */
		public void setInitialLoad(String initialLoad) {
			this.initialLoad = initialLoad;
		}

		/**
		 * Clear.
		 */
		public void clear() {
			this.selectedSettingId = 0;
			this.settingName = null;
			this.nurseryLevelVariables = null;
			this.plotLevelVariables = null;
			this.baselineTraitVariables = null;
			this.isDefault = false;
			this.selectedVariables = null;
		}
}
