package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ReviewFormController {

    @FXML
    private Label courseTitleLabel;

    @FXML
    private TextField ratingField;

    @FXML
    private TextArea commentField;

    @FXML
    private Button submitButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button backButton;

    private String username;
    private Course course;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCourse(Course course) {
        this.course = course;
        courseTitleLabel.setText(course.getTitle());
        loadExistingReview();
    }

    private void loadExistingReview() {
        String query = """
            SELECT rating, comment 
            FROM reviews 
            JOIN users ON reviews.user_id = users.id 
            WHERE reviews.course_id = ? AND users.username = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, course.getId());
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ratingField.setText(String.valueOf(rs.getInt("rating")));
                commentField.setText(rs.getString("comment"));
                deleteButton.setDisable(false);
            } else {
                deleteButton.setDisable(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void submitReview() {
        String ratingText = ratingField.getText();
        String comment = commentField.getText();

        int rating;
        try {
            rating = Integer.parseInt(ratingText);
            if (rating < 1 || rating > 5) {
                showErrorAlert("Invalid Rating", "Please enter a rating between 1 and 5.");
                return;
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Invalid Rating", "Please enter a valid integer rating.");
            return;
        }

        if (course == null || username == null) {
            showErrorAlert("Error", "Course or User data is missing.");
            return;
        }

        try (Connection con = DatabaseManager.getConnection()) {
            String checkReviewQuery = "SELECT * FROM reviews WHERE course_id = ? AND user_id = ?";
            PreparedStatement checkStatement = con.prepareStatement(checkReviewQuery);
            checkStatement.setInt(1, course.getId());
            checkStatement.setInt(2, getUserId(username));
            ResultSet rs = checkStatement.executeQuery();

            if (rs.next()) {
                String updateReviewQuery = "UPDATE reviews SET rating = ?, comment = ?, review_date = ? WHERE course_id = ? AND user_id = ?";
                PreparedStatement updateStatement = con.prepareStatement(updateReviewQuery);
                updateStatement.setInt(1, rating);
                updateStatement.setString(2, comment.isEmpty() ? null : comment);
                updateStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                updateStatement.setInt(4, course.getId());
                updateStatement.setInt(5, getUserId(username));
                updateStatement.executeUpdate();
                showInfoAlert("Review Updated", "Your review has been updated.");
            } else {
                String insertReviewQuery = """
                INSERT INTO reviews (course_id, user_id, rating, comment, review_date) 
                VALUES (?, ?, ?, ?, ?)
            """;
                PreparedStatement insertStatement = con.prepareStatement(insertReviewQuery);
                insertStatement.setInt(1, course.getId());
                insertStatement.setInt(2, getUserId(username));
                insertStatement.setInt(3, rating);
                insertStatement.setString(4, comment.isEmpty() ? null : comment);
                insertStatement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                insertStatement.executeUpdate();
                showInfoAlert("Review Submitted", "Your review has been submitted.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Database Error", "Could not submit the review.");
        }

        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
    }

    private int getUserId(String username) throws SQLException {
        try (Connection con = DatabaseManager.getConnection()) {
            String query = "SELECT id FROM users WHERE username = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    @FXML
    private void deleteReview() {
        String query = """
            DELETE FROM reviews 
            WHERE course_id = ? AND user_id = (SELECT id FROM users WHERE username = ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, course.getId());
            stmt.setString(2, username);
            stmt.executeUpdate();
            closeWindow();
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Database Error", "Failed to delete the review.");
        }
    }

    @FXML
    private void goBack() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
