package edu.virginia.sde.reviews;

import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.sql.*;
import java.sql.Timestamp;
import java.io.IOException;
import java.text.SimpleDateFormat;


public class CourseReviewController {


    @FXML
    private Label courseTitleLabel;

    @FXML
    private TableView<ReviewTableItem> reviewsTableView;

    @FXML
    private TableColumn<ReviewTableItem, String> timestampColumn;

    @FXML
    private TableColumn<ReviewTableItem, Integer> ratingColumn;

    @FXML
    private TableColumn<ReviewTableItem, String> commentColumn;

    @FXML
    private Button backButton;

    @FXML
    private Button addReviewButton;

    @FXML
    private Button deleteReviewButton;

    @FXML
    public Button editReviewButton;


    private String username;
    private Course course;
    private Timestamp timestamp;
    private String currentUser;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCourse(Course course) {
        this.course = course;
        String courseDetails = course.getSubject() + " " + course.getNumber() + ": " + course.getTitle();
        courseTitleLabel.setText(courseDetails);

        loadCourseReviews();

    }

    @FXML
    private void initialize() {
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        timestampColumn.setCellValueFactory(cellData -> {
            ReviewTableItem item = cellData.getValue();
            return new SimpleStringProperty(item.getFormattedTimestamp());
                });

        addReviewButton.setOnAction(event -> openAddReviewDialog());
//        deleteReviewButton.setOnAction(event -> deleteReview());
        backButton.setOnAction(event -> goBack());
    }

    private void loadCourseReviews() {
        reviewsTableView.getItems().clear();
        String query = """
            SELECT r.rating, r.timestamp, r.comment
            FROM reviews r
            WHERE r.course_id = ?
        """;

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement statement = con.prepareStatement(query)) {
            statement.setInt(1, course.getId());

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
//                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    Timestamp timestamp = rs.getTimestamp("timestamp");
                    int rating = rs.getInt("rating");
                    String comment = rs.getString("comment");

                    reviewsTableView.getItems().add(new ReviewTableItem(timestamp, rating, comment));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Database Error", "Could not load course reviews.");
        }
    }

    @FXML
    private void openAddReviewDialog() {
        try (Connection conn = DatabaseManager.getConnection()) {
            String checkQuery = """
                SELECT COUNT(*)
                FROM reviews r
                JOIN users u ON r.user_id = u.id
                WHERE r.course_id = ? AND u.username = ?
            """;

            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, course.getId());
//            checkStmt.setString(2, currentUser);
            checkStmt.setString(2, username);

            ResultSet checkRs = checkStmt.executeQuery();
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                showAlert(Alert.AlertType.WARNING, "Duplicate Review", "You have already reviewed this course.");
                return;
            }

            Dialog<ReviewTableItem> dialog = new Dialog<>();
            dialog.setTitle("Add Review");
            dialog.setHeaderText("Add your review for " + course.getTitle());

            // Create fields for review input
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            TextField ratingField = new TextField();
            ratingField.setPromptText("Rating (1-5)");

            TextField commentField = new TextField();
            commentField.setPromptText("Comment (Optional)");

            dialogPane.setContent(new VBox(10, new Label("Rating:"), ratingField, new Label("Comment:"), commentField));

            dialog.setResultConverter(button -> {
                if (button == ButtonType.OK) {
                    try {
                        int rating = Integer.parseInt(ratingField.getText());
                        String comment = commentField.getText();
//                        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
                        return new ReviewTableItem(new Timestamp(System.currentTimeMillis()), rating, comment);
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid rating (1-5).");
                    }

                }
                return null;
            });

            dialog.showAndWait().ifPresent(review -> {
                String insertQuery = """
                    INSERT INTO reviews (user_id, course_id, rating, comment, timestamp)
                    VALUES ((SELECT id FROM users WHERE username = ?), ?, ?, ?, ?)
                """;

                try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
//                    stmt.setString(1, currentUser);
                    stmt.setString(1, username);
                    stmt.setInt(2, course.getId());
                    stmt.setDouble(3, review.getRating());
                    stmt.setString(4, review.getComment());
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedTimestamp = formatter.format(now);
                    stmt.setString(5, formattedTimestamp);

                    stmt.executeUpdate();
                    loadCourseReviews();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteReview() {
        ReviewTableItem selectedReview = reviewsTableView.getSelectionModel().getSelectedItem();
        if (selectedReview == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a review to delete.");
            return;
        }

        String deleteQuery = """
            DELETE FROM reviews
            WHERE course_id = ? AND user_id = (SELECT id FROM users WHERE username = ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setInt(1, course.getId());
            stmt.setString(2, username);

            stmt.executeUpdate();
            loadCourseReviews();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openEditReviewDialog() {
        ReviewTableItem selectedReview = reviewsTableView.getSelectionModel().getSelectedItem();
        if (selectedReview == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a review to edit.");
            return;
        }

        Dialog<ReviewTableItem> dialog = new Dialog<>();
        dialog.setTitle("Edit Review");
        dialog.setHeaderText("Edit your review for " + course.getTitle());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField ratingField = new TextField(String.valueOf(selectedReview.getRating()));
        TextField commentField = new TextField(selectedReview.getComment());

        dialogPane.setContent(new VBox(10, new Label("Rating:"), ratingField, new Label("Comment:"), commentField));

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    int rating = Integer.parseInt(ratingField.getText());
                    String comment = commentField.getText();
//                    return new ReviewTableItem(selectedReview.getCourseId(), course.getSubject() + " " + course.getNumber(), rating, comment);
                    return new ReviewTableItem(selectedReview.getCourseId(), "", rating, comment);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid rating (1-5).");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedReview -> {
            String updateQuery = """
            UPDATE reviews
            SET rating = ?, comment = ?, timestamp = ?
            WHERE course_id = ? AND user_id = (SELECT id FROM users WHERE username = ?)
        """;

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setInt(1, updatedReview.getRating());
                stmt.setString(2, updatedReview.getComment());
                Timestamp now = new Timestamp(System.currentTimeMillis());
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedTimestamp = formatter.format(now);
                stmt.setString(3, formattedTimestamp);
                stmt.setInt(4, course.getId());
                stmt.setString(5, username);

                stmt.executeUpdate();
                loadCourseReviews();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not update the review.");
            }
        });
    }

    private void showErrorAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setContentText(content);
        a.showAndWait();
    }

    public void setCurrentUser(String username) {
        this.username = username;
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CourseSearchScreen.fxml"));
            Parent courseSearchRoot = loader.load();

            CourseSearchController controller = loader.getController();
            controller.setCurrentUser(username);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(courseSearchRoot));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.showAndWait();
    }
}
