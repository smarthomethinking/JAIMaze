package org.smarthomethinking.jaimaze;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author gde
 */
public class Model {

    private final IntegerProperty width = new SimpleIntegerProperty(10);
    private final IntegerProperty height = new SimpleIntegerProperty(10);
    private final BooleanProperty enabled = new SimpleBooleanProperty(true);
    private final StringProperty url = new SimpleStringProperty();

    public IntegerProperty getWidth() {
        return width;
    }

    public IntegerProperty getHeight() {
        return height;
    }

    public BooleanProperty getUIEnabled() {
        return enabled;
    }

    public StringProperty getUrl() {
        return url;
    }
    
    
}
