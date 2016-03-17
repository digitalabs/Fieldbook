
package com.efficio.fieldbook.service.api;

import java.util.List;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareException;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public interface SettingsService {

	List<SettingDetail> retrieveTrialSettings(Workbook workbook);

	List<LabelFields> retrieveTrialSettingsAsLabels(Workbook workbook);

	List<LabelFields> retrieveTraitsAsLabels(Workbook workbook);

	List<LabelFields> retrieveGermplasmDescriptorsAsLabels(Workbook workbook);

	List<LabelFields> retrieveTrialEnvironmentAndExperimentalDesignSettingsAsLabels(Workbook workbook);

	List<LabelFields> retrieveNurseryManagementDetailsAsLabels(Workbook workbook);

	SettingDetail createSettingDetail(int id, String name, UserSelection userSelection,
			int currentIbDbUserId, String programUUID) throws MiddlewareException;
}
