package edu.virginia.sde.reviews;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CourseReviewsApplication extends Application {
    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml")); //change this to login
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Course Reviews");
        stage.setScene(scene);
        stage.show();
    }
}
