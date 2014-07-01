package com.efficio.fieldbook.web.trial.controller.angular;

import com.efficio.fieldbook.web.nursery.controller.SettingsController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 *
 * This serves as a controller class that can support the back end requests of the Angular-based front end for Trial Management
 */


@Controller
@RequestMapping(AngularTrialController.URL)
public class AngularTrialController extends SettingsController {

    public static final String URL = "/TrialManager/angular";

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AngularTrialController.class);

    @Override
    public String getContentName() {
        // TODO : replace with angular-based template in lieu of the current html which is just for a single tab
        return "TrialManager/angular/environments";
    }

    @RequestMapping(method = RequestMethod.GET)
    public String show(Model model) {
        return super.show(model);
    }
}
