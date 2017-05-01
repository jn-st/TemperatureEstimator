package main.java.views;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import main.java.TempEstimator;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class WelcomViewController implements Initializable {

    private ResourceBundle langBundle;

    @FXML
    private Button continueBtn;

    @FXML
    private ChoiceBox<String> langChoice;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.langBundle = resources;
        TempEstimator.getMainStage().setTitle(resources.getString("welcome.title"));
        ObservableList<String> items = FXCollections.observableArrayList("Русский", "English");

        langChoice.setItems(items);

        if (resources.getLocale().getLanguage().equals("ru")) {
            langChoice.getSelectionModel().selectFirst();
        }
        else {
            langChoice.getSelectionModel().selectLast();
        }

        langChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                Locale locale;
                String choice = langChoice.getItems().get(newValue.intValue());
                if (choice.equals("English")) {
                    locale = new Locale("en");
                } else {
                    locale = new Locale("ru");
                }

                ResourceBundle bundle = ResourceBundle.getBundle("lang", locale);

                FXMLLoader loader = new FXMLLoader(this.getClass().getResource("WelcomeView.fxml"), bundle);

                try {
                    AnchorPane scenePane = (AnchorPane) loader.load();
                    Scene scene = new Scene(scenePane, 600, 400);
                    TempEstimator.getMainStage().setScene(scene);

                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });
    }

    @FXML
    void continueBtnClicked(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("EstimatorView.fxml"), langBundle);
        BorderPane pane;
        Scene scene;

        try {
            pane = (BorderPane) loader.load();
            scene = new Scene(pane, 600, 400);
            TempEstimator.getMainStage().setScene(scene);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
