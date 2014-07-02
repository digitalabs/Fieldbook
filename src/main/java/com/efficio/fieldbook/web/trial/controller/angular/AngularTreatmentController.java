package com.efficio.fieldbook.web.trial.controller.angular;

import com.efficio.fieldbook.web.nursery.controller.SettingsController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by cyrus on 7/1/14.
 */

@Controller
@RequestMapping(AngularTreatmentController.URL)
public class AngularTreatmentController extends SettingsController {

    public static final String URL = "/TrialManager/treatment";

    @Override
    public String getContentName() {
        return "TrialManager/templates/treatment";
    }

    @RequestMapping(method = RequestMethod.GET)
    public String show(Model model) {
        return getContentName();
    }
}
