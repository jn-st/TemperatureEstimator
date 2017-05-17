package main.java.views;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import main.java.TempEstimator;
import main.java.model.Estimation;
import main.java.util.ModalAlerts;


import javax.imageio.ImageIO;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InterfaceAddress;
import java.net.URL;
import java.util.ResourceBundle;

public class EstimatorViewController implements Initializable {

    private ResourceBundle langBundle;

    private Estimation tempEstimation;

    @FXML
    private ChoiceBox<String> planetPicker;

    @FXML
    private Label solutionLabel;

    @FXML
    private TextField solutionField;

    @FXML
    private Button backBtn;

    @FXML
    private Button saveFileBtn;

    @FXML
    private Button computeBtn;

    @FXML
    private Button savePlotBtn;

    @FXML
    private TextField massField;

    @FXML
    private TextField distanceField;

    @FXML
    private TextField albedoField;

    @FXML
    private TextField greenhouseField;

    @FXML
    void loadWelcomeView(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("WelcomeView.fxml"), langBundle);
        AnchorPane pane;
        Scene scene;

        try {
            pane = (AnchorPane) loader.load();
            scene = new Scene(pane, 600, 400);
            TempEstimator.getMainStage().setScene(scene);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @FXML
    boolean computeNewTemp(ActionEvent event) {

        double mass;
        double distance;
        double albedo;
        double greenhouse;

        try {
            mass = Double.valueOf(massField.getText().split("\\s+")[0]);
            distance = Double.valueOf(distanceField.getText().split("\\s+")[0]);
            albedo = Double.valueOf(albedoField.getText());
            greenhouse = Double.valueOf(greenhouseField.getText());
        } catch (NumberFormatException e) {
            ModalAlerts.displayError(
                    langBundle.getString("numexeption.title"),
                    langBundle.getString("numexeption.header"),
                    langBundle.getString("numexeption.msg")
            );
            return false;
        }

        String intervalErrorString = constructIntervalError(mass, distance, albedo, greenhouse);

        if (intervalErrorString.length() != 0) {
            ModalAlerts.displayError(
                    langBundle.getString("intervalerror.title"),
                    langBundle.getString("intervalerror.header"),
                    intervalErrorString
            );
            return false;
        }

        tempEstimation.setVariables(mass, distance, albedo, greenhouse);

        tempEstimation.evaluate();

        solutionField.setText(tempEstimation.toString());

        return true;
    }

    @FXML
    @SuppressWarnings("all")
    void savePlotToFile(ActionEvent event) {
        final NumberAxis yAxis = new NumberAxis(-60.0, 500.0, 20.0);
        final NumberAxis xAxis = new NumberAxis(0.0, 2.0, 0.1);

        final ScatterChart<Number, Number> chart = new ScatterChart<>(xAxis, yAxis);

        xAxis.setLabel(langBundle.getString("estimator.chart.distance"));
        yAxis.setLabel(langBundle.getString("estimator.chart.temperature"));

        chart.setTitle(langBundle.getString("estimator.chart.title"));

        XYChart.Series<Number, Number> earth = new XYChart.Series<>();
        earth.setName(langBundle.getString("estimator.earth"));

        earth.getData().add(new XYChart.Data<Number, Number>(1.0, 15.0));

        XYChart.Series<Number, Number> mercury = new XYChart.Series<>();
        mercury.setName(langBundle.getString("estimator.mercury"));

        mercury.getData().add(new XYChart.Data<Number, Number>(0.4, 165.0));

        XYChart.Series<Number, Number> venus = new XYChart.Series<>();
        venus.setName(langBundle.getString("estimator.venus"));

        venus.getData().add(new XYChart.Data<Number, Number>(0.7, 469.0));

        XYChart.Series<Number, Number> mars = new XYChart.Series<>();
        mars.setName(langBundle.getString("estimator.mars"));

        mars.getData().add(new XYChart.Data<Number, Number>(1.5, -49.0));

        chart.getData().addAll(earth, venus, mars, mercury);

        for (XYChart.Series<Number, Number> series : chart.getData()) {
            for (XYChart.Data<Number, Number> data : series.getData()) {
                StackPane stackPane =  (StackPane) data.getNode();
                stackPane.setPrefWidth(25);
                stackPane.setPrefHeight(25);
            }
        }

        chart.setPrefSize(1000, 1000);
        chart.setPadding(new Insets(40));
        chart.setAnimated(false);
        chart.applyCss();
        chart.layout();

        WritableImage image = new WritableImage(1000, 1000);

        Scene scene = new Scene(chart);

        scene.snapshot(image);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("planet-plot.png");
        fileChooser.setTitle(langBundle.getString("estimator.chooser.title"));

        File file = fileChooser.showSaveDialog(TempEstimator.getMainStage());

        if (file == null) {
            return;
        }

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException ioe) {
            // TODO: ModalAlerts.display blablabla error happened during saving file
        }
    }

    @FXML
    void saveToFile(ActionEvent event) {
        if (!computeNewTemp(null)) {
            return;
        }

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle(langBundle.getString("estimator.chooser.title"));
        fileChooser.setInitialFileName("planet-data.txt");

        File file = fileChooser.showSaveDialog(TempEstimator.getMainStage());

        if (file == null) {
            return;
        }

        StringBuilder sb = new StringBuilder("");

        sb.append(langBundle.getString("estimator.field.mass"))
                .append(" ").append(massField.getText()).append("\r\n");

        sb.append(langBundle.getString("estimator.field.distance"))
                .append(" ").append(distanceField.getText()).append("\r\n");

        sb.append(langBundle.getString("estimator.field.albedo"))
                .append(" ").append(albedoField.getText()).append("\r\n");

        sb.append(langBundle.getString("estimator.field.greenhouse"))
                .append(" ").append(greenhouseField.getText()).append("\r\n");

        sb.append(langBundle.getString("estimator.field.temperature"))
                .append(" ").append(solutionField.getText());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(sb.toString());
            writer.close(); // force flush and save the file
        } catch (IOException ioe) {
            // TODO: ModalAlerts.display blablabla error happened during saving file
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.langBundle = resources;
        this.tempEstimation = new Estimation();

        ObservableList<String> items = FXCollections.observableArrayList();

        String earth = langBundle.getString("estimator.earth");
        String mars = langBundle.getString("estimator.mars");
        String venus = langBundle.getString("estimator.venus");
        String mercury = langBundle.getString("estimator.mercury");
        String anyValue = langBundle.getString("estimator.any");

        items.addAll(earth, venus, mars, mercury, anyValue);

        planetPicker.setItems(items);

        planetPicker.getSelectionModel().selectFirst();

        disableEditing();

        planetPicker.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                String chosenPlanet = planetPicker.getItems().get(newValue.intValue());
                if (chosenPlanet.equals(earth)) {
                    disableEditing();
                    setEarthSolution();
                }
                else if (chosenPlanet.equals(mars)) {
                    disableEditing();
                    setMarsSolution();
                }
                else if (chosenPlanet.equals(mercury)) {
                    disableEditing();
                    setMercurySolution();
                }
                else if (chosenPlanet.equals(venus)) {
                    disableEditing();
                    setVenusSolution();
                }
                else if (chosenPlanet.equals(anyValue)) {
                    solutionLabel.setText(langBundle.getString("estimator.solution.general"));
                    enableEditing();
                    clearVariableInputs();
                }
            }
        });
    }

    private void enableEditing() {
        massField.setEditable(true);
        distanceField.setEditable(true);
        albedoField.setEditable(true);
        greenhouseField.setEditable(true);
    }

    private void disableEditing() {
        solutionLabel.setText(langBundle.getString("estimator.solution.original"));
        massField.setEditable(false);
        distanceField.setEditable(false);
        albedoField.setEditable(false);
        greenhouseField.setEditable(false);
    }

    private void clearVariableInputs() {
        solutionField.setText("");
        massField.setText("");
        distanceField.setText("");
        albedoField.setText("");
        greenhouseField.setText("");
    }

    private void setEarthSolution() {
        massField.setText(langBundle.getString("estimator.earth.mass"));
        distanceField.setText(langBundle.getString("estimator.earth.distance"));
        albedoField.setText(langBundle.getString("estimator.earth.albedo"));
        greenhouseField.setText(langBundle.getString("estimator.earth.greenhouse"));
        solutionField.setText(langBundle.getString("estimator.earth.solution") + " \u00b0C");
    }

    private void setMarsSolution() {
        massField.setText(langBundle.getString("estimator.mars.mass"));
        distanceField.setText(langBundle.getString("estimator.mars.distance"));
        albedoField.setText(langBundle.getString("estimator.mars.albedo"));
        greenhouseField.setText(langBundle.getString("estimator.mars.greenhouse"));
        solutionField.setText(langBundle.getString("estimator.mars.solution") + " \u00b0C");

    }

    private void setMercurySolution() {
        massField.setText(langBundle.getString("estimator.mercury.mass"));
        distanceField.setText(langBundle.getString("estimator.mercury.distance"));
        albedoField.setText(langBundle.getString("estimator.mercury.albedo"));
        greenhouseField.setText(langBundle.getString("estimator.mercury.greenhouse"));
        solutionField.setText(langBundle.getString("estimator.mercury.solution") + " \u00b0C");

    }

    private void setVenusSolution() {
        massField.setText(langBundle.getString("estimator.venus.mass"));
        distanceField.setText(langBundle.getString("estimator.venus.distance"));
        albedoField.setText(langBundle.getString("estimator.venus.albedo"));
        greenhouseField.setText(langBundle.getString("estimator.venus.greenhouse"));
        solutionField.setText(langBundle.getString("estimator.venus.solution") + " \u00b0C");

    }

    private boolean checkRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    private String constructIntervalError(double mass, double distance, double albedo, double greenhouse) {
        StringBuilder intervalErrorMessage = new StringBuilder("");

        if (!checkRange(mass, 0.08, 100)) {
            intervalErrorMessage.append(langBundle.getString("intervalerror.mass")).append("\n");
        }
        if (!checkRange(distance, 0.1, 100)) {
            intervalErrorMessage.append(langBundle.getString("intervalerror.distance")).append("\n");
        }
        if (!checkRange(albedo, 0.0, 1.0)) {
            intervalErrorMessage.append(langBundle.getString("intervalerror.albedo")).append("\n");
        }
        if (!checkRange(greenhouse, 0.0, 500)) {
            intervalErrorMessage.append(langBundle.getString("intervalerror.greenhouse")).append("\n");
        }

        return intervalErrorMessage.toString();
    }
}
