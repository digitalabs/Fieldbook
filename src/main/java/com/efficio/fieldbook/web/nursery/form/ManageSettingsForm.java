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

/**
 * The Class AddOrRemoveTraitsForm.
 */
public class ManageSettingsForm {
	
	private int selectedSettingId;
	
	private String settingName;
	
	private List<SettingDetail> nurseryLevelVariables;
	
	private List<SettingDetail> plotLevelVariables;
	
	private List<SettingDetail> baselineTraitVariables;
	
	private boolean isDefault;
	
	private List<Integer> selectedIds;
	
	public int getSelectedSettingId() {
	    return selectedSettingId;
	}
	
	public void setSelectedSettingId(int selectedSettingId) {
	    this.selectedSettingId = selectedSettingId;
	}
	
	public String getSettingName() {
            return settingName;
        }
        
        public void setSettingName(String settingName) {
            this.settingName = settingName;
        }
        
        public List<SettingDetail> getNurseryLevelVariables() {
            return nurseryLevelVariables;
        }
        
        public void setNurseryLevelVariables(List<SettingDetail> nurseryLevelVariables) {
            this.nurseryLevelVariables = nurseryLevelVariables;
        }
        
        public List<SettingDetail> getPlotLevelVariables() {
            return plotLevelVariables;
        }
        
        public void setPlotLevelVariables(List<SettingDetail> plotLevelVariables) {
            this.plotLevelVariables = plotLevelVariables;
        }
        
        public List<SettingDetail> getBaselineTraitVariables() {
            return baselineTraitVariables;
        }
        
        public void setBaselineTraitVariables(List<SettingDetail> baselineTraitVariables) {
            this.baselineTraitVariables = baselineTraitVariables;
        }
        
        public boolean getIsDefault() {
            return isDefault;
        }
        
        public void setIsDefault(boolean isDefault) {
            this.isDefault = isDefault;
        }

		public List<Integer> getSelectedIds() {
			return selectedIds;
		}

		public void setSelectedIds(List<Integer> selectedIds) {
			this.selectedIds = selectedIds;
		}
        
        
}
