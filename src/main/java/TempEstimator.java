package main.java;


import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;


import javafx.scene.layout.AnchorPane;

import javafx.stage.Stage;



import java.util.Locale;
import java.util.ResourceBundle;

public class TempEstimator extends javafx.application.Application {
    private AnchorPane initPane;
    private static Stage mainStage;

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void setMainStage(Stage mainStage) {
        TempEstimator.mainStage = mainStage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setMainStage(primaryStage);

        //primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("icon.ico")));//

        Locale locale = new Locale("ru");
        ResourceBundle bundle = ResourceBundle.getBundle("lang", locale);

        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("views/WelcomeView.fxml"), bundle);

        initPane = (AnchorPane) loader.load();

        Scene scene = new Scene(initPane, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle(bundle.getString("welcome.title"));
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
