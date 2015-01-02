package com.efficio.fieldbook.service.api;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import org.generationcp.middleware.domain.etl.Workbook;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */
public interface SettingsService {

	public List<SettingDetail> retrieveTrialSettings(Workbook workbook);
}
