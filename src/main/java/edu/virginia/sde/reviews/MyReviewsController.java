package edu.virginia.sde.reviews;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class MyReviewsController {

    @FXML
    private Label name_label;

    @FXML
    private TableView<ReviewTableItem> reviewsTableView;

    @FXML
    private TableColumn<ReviewTableItem, String> courseColumn;

    @FXML
    private TableColumn<ReviewTableItem, Integer> ratingColumn;

    @FXML
    private TableColumn<ReviewTableItem, String> commentColumn;



    @FXML
    private Button backButton;

    private String username;

    public void initialize() {
        courseColumn.setCellValueFactory(new PropertyValueFactory<>("courseMnemonicNumber"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
    }


    public void setUsername(String username) {
        this.username = username;
        name_label.setText(username);
        //THIS LOADS THE REVIEWS FOR THE USER WHO IS CURRENTLY LOGGED IN
        loadUserReviews();
    }


    private void loadUserReviews() {
        reviewsTableView.getItems().clear();
        String getUserIdQuery = "SELECT id FROM users WHERE username = ?";
        String getReviewsQuery = """
            SELECT c.id AS course_id, c.subject, c.number, c.title, r.rating, r.comment 
            FROM reviews r 
            JOIN courses c ON r.course_id = c.id 
            WHERE r.user_id = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdQuery)) {
            getUserIdStmt.setString(1, username);
            try (ResultSet userIdRs = getUserIdStmt.executeQuery()) {
                if (userIdRs.next()) {
                    int userId = userIdRs.getInt("id");
                    try (PreparedStatement getReviewsStmt = conn.prepareStatement(getReviewsQuery)) {
                        getReviewsStmt.setInt(1, userId);
                        try (ResultSet reviewsRs = getReviewsStmt.executeQuery()) {
                            while (reviewsRs.next()) {
                                int courseId = reviewsRs.getInt("course_id"); // Retrieve course ID
                                String courseMnemonic = reviewsRs.getString("subject") + " " + reviewsRs.getInt("number");
                                int rating = reviewsRs.getInt("rating");
                                String comment = reviewsRs.getString("comment");
                                ReviewTableItem tableItem = new ReviewTableItem(
                                        courseId,
                                        courseMnemonic,
                                        rating,
                                        comment != null ? comment : ""
                                );

                                reviewsTableView.getItems().add(tableItem);
                            }
                        }
                     }
                    }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void openCourseReviewScene(int courseId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CourseReviewScreen.fxml"));
            Parent root = loader.load();

            CourseReviewController controller = loader.getController();
            controller.setUsername(username);
            controller.setCourse(getCourseDetails(courseId));

            Stage stage = (Stage) reviewsTableView.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Course getCourseDetails(int courseId) {
        String query = "SELECT id, subject, number, title, average_rating FROM courses WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, courseId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Course(
                            rs.getInt("id"),
                            rs.getString("subject"),
                            rs.getInt("number"),
                            rs.getString("title"),
                            rs.getDouble("average_rating")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }



    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CourseSearchScreen.fxml"));
            Parent courseSearchScene = loader.load();
            CourseSearchController controller = loader.getController();
            controller.setCurrentUser(username);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(courseSearchScene));

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
