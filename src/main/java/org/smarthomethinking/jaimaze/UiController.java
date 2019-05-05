package org.smarthomethinking.jaimaze;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.util.converter.NumberStringConverter;
import org.springframework.stereotype.Component;

/**
 * FXML Controller class
 *
 * @author gde
 */
@Component
public class UiController implements Initializable {
    
    Model model = ModelPreferences.getModel();
    MazeRunnerTask runner = null;
    
    @FXML
    private TextField destination;
    
    @FXML
    private ToggleButton toggle;
    
    @FXML
    private TextField width;
    
    @FXML
    private TextField height;
    
    @FXML
    private ImageView boardImage;
    
    @FXML
    void toggleRunState(ActionEvent event) {
        if (runner == null) {
            // nothing currently running - start it up
            model.getUIEnabled().set(false);
            runner = new MazeRunnerTask(model, this);
            runner.start();
            toggle.setText("Stop");
        } else {
            // thread currently running - stop it
            runner.stopThread();
            // forget about the thread
            runner = null;
            // reset the UI
            toggle.setText("Start");
            model.getUIEnabled().set(true);
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert destination != null : "fx:id=\"destination\" was not injected: check your FXML file 'ui.fxml'.";
        assert toggle != null : "fx:id=\"toggle\" was not injected: check your FXML file 'ui.fxml'.";
        assert width != null : "fx:id=\"width\" was not injected: check your FXML file 'ui.fxml'.";
        assert height != null : "fx:id=\"height\" was not injected: check your FXML file 'ui.fxml'.";
        // bind to the model
        destination.textProperty().bindBidirectional(model.getUrl());
        width.setTextFormatter(new TextFormatter<>(integerFilter));
        width.textProperty().bindBidirectional(model.getWidth(), new NumberStringConverter());
        height.setTextFormatter(new TextFormatter<>(integerFilter));
        height.textProperty().bindBidirectional(model.getHeight(), new NumberStringConverter());
    }
    
    private UnaryOperator<Change> integerFilter = change -> {
        String text = change.getText();
        
        if (text.matches("[0-9]*")) {
            return change;
        }
        
        return null;
    };

    /**
     * This must only be called in the JavaFX thread (use Platform.runLater)
     *
     * @param image
     */
    protected void updateBoardImage(BufferedImage image) {
        boardImage.setImage(SwingFXUtils.toFXImage(image, null));
        boardImage.setFitHeight(image.getHeight());
        boardImage.setFitWidth(image.getWidth());
    }
    
}
