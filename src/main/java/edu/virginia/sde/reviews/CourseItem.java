package edu.virginia.sde.reviews;

import javafx.beans.property.*;

public class CourseItem {

    private final int id;
    private final StringProperty subject;
    private final IntegerProperty number;
    private final StringProperty title;
    private final DoubleProperty averageRating;


    public CourseItem(int id, String subject, int number, String title, double averageRating) {
        this.id = id;
        this.subject = new SimpleStringProperty(subject);
        this.number = new SimpleIntegerProperty(number);
        this.title = new SimpleStringProperty(title);
        this.averageRating = new SimpleDoubleProperty(averageRating);
    }

    public CourseItem(String subject, int number, String title) {
        this.id = 0;
        this.subject = new SimpleStringProperty(subject);
        this.number = new SimpleIntegerProperty(number);
        this.title = new SimpleStringProperty(title);
        this.averageRating = new SimpleDoubleProperty(0.0);
    }

    public StringProperty subjectProperty() {
        return subject;
    }

    public IntegerProperty numberProperty() {
        return number;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public DoubleProperty averageRatingProperty() {
        return averageRating;
    }

    public double getAverageRating() {
        return averageRating.get();
    }

    public void setAverageRating(double averageRating) {
        this.averageRating.set(averageRating);
    }

    public int getId() {
        return id;
    }

    public String getSubject() {
        return subject.get();
    }

    public int getNumber() {
        return number.get();
    }

    public String getTitle() {
        return title.get();
    }
}
