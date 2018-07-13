
package com.efficio.fieldbook.service.api;

import java.util.List;

import com.efficio.fieldbook.web.common.bean.SettingVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public interface SettingsService {

	public List<LabelFields> retrieveTrialSettingsAsLabels(Workbook workbook);

	List<LabelFields> retrieveTraitsAsLabels(Workbook workbook);

	public List<LabelFields> retrieveGermplasmDescriptorsAsLabels(Workbook workbook);

	public List<LabelFields> retrieveTrialEnvironmentConditionsAsLabels(Workbook workbook);

	public List<LabelFields> retrieveNurseryManagementDetailsAsLabels(Workbook workbook);

	public SettingDetail createSettingDetail(int id, String name, UserSelection userSelection,
			int currentIbDbUserId, String programUUID) throws MiddlewareException;

	public List<LabelFields> retrieveExperimentalDesignFactorsAsLabels(Workbook workbook);

	void populateSettingVariable(SettingVariable var);

	void addNewSettingDetails(int mode, List<SettingDetail> newDetails) throws Exception;
}
