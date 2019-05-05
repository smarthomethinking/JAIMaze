package org.smarthomethinking.jaimaze;

import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Static class for creating models based on past preferences, and remembering a
 * models settings for next time.
 *
 * @author gde
 */
public class ModelPreferences {

    private static final Logger LOG = Logger.getLogger(ModelPreferences.class.getName());

    /**
     * Create a Model using previous preferences (if available).
     *
     * @return Model
     */
    public static Model getModel() {
        Model m = new Model();
        // load preferences
        Preferences prefs = Preferences.userRoot().node(ModelPreferences.class.getName());
        m.getUrl().set(prefs.get("url", "http://localhost:8080/jaimaze"));
        // save changes
        m.getUrl().addListener((observable) -> {
            LOG.info("URL Changed to "+m.getUrl().get());
            prefs.put("url", m.getUrl().get());
        });
        return m;
    }

}
