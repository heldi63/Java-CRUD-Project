package edu.virginia.sde.reviews;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.w3c.dom.Node;
import org.w3c.dom.Text;


import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private Label loginErrorMessage;
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField passwordTextField;

    private DatabaseConnection databaseConnection = new DatabaseConnection();



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }


    public void loginButtonOnAction() {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            loginErrorMessage.setText("Username and password are required.");
        } else {
            boolean isValid = databaseConnection.validateLogin(username, password);
            if (isValid) {
                loginErrorMessage.setText("Login successful!");
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("CourseSearchScreen.fxml"));
                    Parent root = loader.load();
                    CourseSearchController controller = loader.getController();
                    controller.setCurrentUser(username);
                    Stage stage = (Stage) loginErrorMessage.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                    loginErrorMessage.setText("Error loading the next screen.");
                }
            } else {
                loginErrorMessage.setText("Invalid username or password.");
            }
        }
    }
    public void registerButtonOnAction() {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            loginErrorMessage.setText("Username and password are required.");
        } else if (password.length() < 8) {
            loginErrorMessage.setText("Password must be at least 8 characters long.");
        } else {
            boolean isRegistered = databaseConnection.registerUser(username, password);
            if (isRegistered) {
                loginErrorMessage.setText("Registration successful! Please log in.");
                //clear the fields and prepare for login
                usernameTextField.clear();
                passwordTextField.clear();
            } else {
                loginErrorMessage.setText("Username already exists. Please choose another.");
            }
        }
    }


    public void closeButtonOnAction(ActionEvent event) {
        Platform.exit();
    }
}
