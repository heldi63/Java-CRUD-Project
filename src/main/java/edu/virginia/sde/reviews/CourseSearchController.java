package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.io.IOException;
import java.sql.*;

public class CourseSearchController {

    @FXML
    private Label name_label;
    @FXML
    private TableView<CourseItem> courseTable;
    @FXML
    private TableColumn<CourseItem, String> subjectColumn;
    @FXML
    private TableColumn<CourseItem, Integer> numberColumn;
    @FXML
    private TableColumn<CourseItem, String> titleColumn;
    @FXML
    private TableColumn<CourseItem, Double> ratingColumn;
    @FXML
    private TextField subjectField;
    @FXML
    private TextField numberField;
    @FXML
    private TextField titleField;
    @FXML
    private Button searchButton;
    @FXML
    private Button myReviewsButton;
//    @FXML
//    private Button writeReviewButton;
    @FXML
    private Label errorLabel;
    @FXML
    private Button logoutButton;

    private ObservableList<CourseItem> courses = FXCollections.observableArrayList();

    private String username;

    @FXML
    public void initialize() {

        subjectColumn.setCellValueFactory(cellData -> cellData.getValue().subjectProperty());
        numberColumn.setCellValueFactory(cellData -> cellData.getValue().numberProperty().asObject());
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        ratingColumn.setCellValueFactory(cellData -> cellData.getValue().averageRatingProperty().asObject());

        fetchCoursesFromDatabase("", "", "");

        courseTable.setItems(courses);
        searchButton.setOnAction(event -> handleSearch());
       // writeReviewButton.setOnAction(event -> writeReview());
        courseTable.setRowFactory(tv -> {
            TableRow<CourseItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    CourseItem clickedCourse = row.getItem();
                    openCourseReviewScreen(clickedCourse);
                }
            });
            return row;
        });
    }

    public void setCurrentUser(String username) {
        this.username = username;
        name_label.setText(username);

    }

    private void fetchCoursesFromDatabase(String subject, String number, String title) {
        courses.clear();

        String sql = "SELECT id, subject, number, title, average_rating FROM courses WHERE "
                + "(? = '' OR LOWER(subject) = LOWER(?)) AND "
                + "(? = '' OR number = ?) AND "
                + "(? = '' OR LOWER(title) LIKE LOWER(?))";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, subject);
            stmt.setString(2, subject);
            stmt.setString(3, number);
            stmt.setString(4, number);
            stmt.setString(5, title);
            stmt.setString(6, "%" + title + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                int fetchedId = rs.getInt("id");
                String fetchedSubject = rs.getString("subject");
                int fetchedNumber = rs.getInt("number");
                String fetchedTitle = rs.getString("title");
                double avgRating = rs.getDouble("average_rating");

                courses.add(new CourseItem(fetchedId, fetchedSubject, fetchedNumber, fetchedTitle, avgRating));
            }

        } catch (SQLException e) {
            e.printStackTrace();

        }

    }

    @FXML
    private void handleSearch() {
        String subject = subjectField.getText().trim();
        String number = numberField.getText().trim();
        String title = titleField.getText().trim();
        fetchCoursesFromDatabase(subject, number, title);
    }


    private void openCourseReviewScreen(CourseItem clickedCourse) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("course-review.fxml"));
            Parent courseReviewRoot = loader.load();

            CourseReviewController controller = loader.getController();

            Course course = convertToCourse(clickedCourse);
            controller.setCourse(course);
            controller.setCurrentUser(username);

            Stage stage = (Stage) courseTable.getScene().getWindow();
            stage.setScene(new Scene(courseReviewRoot));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openMyReviewsScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MyReviewsScreen.fxml"));
            Parent myReviewsScene = loader.load();
            MyReviewsController controller = loader.getController();
            String username = name_label.getText();
            controller.setUsername(username);
            Stage stage = (Stage) myReviewsButton.getScene().getWindow();
            stage.setScene(new Scene(myReviewsScene));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void writeReview() {
        CourseItem selectedCourse = courseTable.getSelectionModel().getSelectedItem();

        if (selectedCourse == null) {
            showErrorAlert("Selection Error", "Please select a course to write a review.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ReviewForm.fxml"));
            Parent root = loader.load();

            ReviewFormController controller = loader.getController();
            controller.setCourse(convertToCourse(selectedCourse));
            controller.setUsername(username);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Write a Review");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not load the review form.");
        }
    }

    @FXML
    private void showAddCoursePopup() {
        TextField subjectInput = new TextField();
        TextField numberInput = new TextField();
        TextField titleInput = new TextField();
        Button addButton = new Button("Add Course");

        VBox vbox = new VBox(10, new Label("Subject (2-4 letters):"), subjectInput,
                new Label("Course Number (4 digits):"), numberInput,
                new Label("Course Title (1-50 characters):"), titleInput, addButton);

        Stage addStage = new Stage();
        addStage.setTitle("Add New Course");
        addStage.setScene(new javafx.scene.Scene(vbox, 300, 200));
        addStage.show();

        addButton.setOnAction(event -> {
            String subject = subjectInput.getText().trim();
            String num = numberInput.getText().trim();
            String title = titleInput.getText().trim();

            if (validateCourseInput(subject, num, title)) {
                try (Connection con = DatabaseManager.getConnection()) {
                    String insertQuery = """
                            INSERT INTO courses (subject, number, title)
                            VALUES (?, ?, ?)
                            """;
                    try (PreparedStatement statement = con.prepareStatement(insertQuery)) {
                        statement.setString(1, subject.toUpperCase());
                        statement.setInt(2, Integer.parseInt(num));
                        statement.setString(3, title);

                        int rowsInserted = statement.executeUpdate();
                        if (rowsInserted > 0) {
                            fetchCoursesFromDatabase(subject, num, title);
                            addStage.close();
                        } else {
                            showErrorAlert("Error", "Could not add the course.");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showErrorAlert("Database Error", "Could not add the course into the database.");
                }
                int number = Integer.parseInt(num);
                CourseItem newCourse = new CourseItem(subject.toUpperCase(), number, title);
                courses.add(newCourse);
                addStage.close();
            }
        });
    }

    private boolean validateCourseInput(String subject, String courseNumber, String courseTitle) {
        if (subject.length() < 2 || subject.length() > 4 || !subject.matches("[a-zA-Z]+")) {
            errorLabel.setText("Subject must be 2-4 letters.");
            return false;
        }
        if (courseNumber.length() != 4 || !courseNumber.matches("[0-9]{4}")) {
            errorLabel.setText("Course number must be 4 digits.");
            return false;
        }
        if (courseTitle.length() < 1 || courseTitle.length() > 50) {
            errorLabel.setText("Course title must be between 1 and 50 characters.");
            return false;
        }

        return true;
    }



    private boolean hasReviewedCourse(int courseId, String username) {
        String query = """
        SELECT COUNT(*) AS review_count 
        FROM reviews 
        JOIN users ON reviews.user_id = users.id 
        WHERE reviews.course_id = ? AND users.username = ?
    """;

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, courseId);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("review_count") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    private Course convertToCourse(CourseItem courseItem) {
        return new Course(
                courseItem.getId(),
                courseItem.getSubject(),
                courseItem.getNumber(),
                courseItem.getTitle(),
                courseItem.getAverageRating()
        );
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();

            stage.setScene(new Scene(loginRoot));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
