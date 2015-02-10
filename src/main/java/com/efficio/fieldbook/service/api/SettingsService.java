package com.efficio.fieldbook.service.api;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import org.generationcp.middleware.domain.etl.Workbook;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */
public interface SettingsService {

	public List<SettingDetail> retrieveTrialSettings(Workbook workbook);

	public List<LabelFields> retrieveTrialSettingsAsLabels(Workbook workbook);

	List<LabelFields> retrieveTraitsAsLabels(Workbook workbook);

	public List<LabelFields> retrieveGermplasmDescriptorsAsLabels(Workbook workbook);

	public List<LabelFields> retrieveTrialEnvironmentAndExperimentalDesignSettingsAsLabels(
			Workbook workbook);

	public List<LabelFields> retrieveNurseryManagementDetailsAsLabels(Workbook workbook);
}
