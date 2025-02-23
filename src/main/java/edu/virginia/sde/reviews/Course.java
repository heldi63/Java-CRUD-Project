package edu.virginia.sde.reviews;

public class Course {
    private int id;
    private String subject;
    private int number;
    private String title;
    private double averageRating;


    public Course(int id, String subject, int number, String title, double averageRating) {
        this.id = id;
        this.subject = subject;
        this.number = number;
        this.title = title;
        this.averageRating = averageRating;
    }


    public int getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public double getAverageRating() {
        return averageRating;
    }
}
