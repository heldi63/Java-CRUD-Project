package edu.virginia.sde.reviews;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


public class ReviewTableItem {

    private final int courseId;
    private final String courseMnemonicNumber;
    private final int rating;
    private final String comment;
    private final Timestamp timestamp;


    public ReviewTableItem(Timestamp timestamp, int rating, String comment) {
        this.courseId = -1;
        this.courseMnemonicNumber = "";
        this.timestamp = timestamp;


        this.rating = rating;
        this.comment = comment;
    }

    //constructor for "My Reviews Scene" with course info
    public ReviewTableItem(int courseId, String courseMnemonicNumber, int rating, String comment) {
        this.courseId = courseId;
        this.courseMnemonicNumber = courseMnemonicNumber;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = null;
    }

    public int getCourseId() {
        return courseId;
    }

    public String getCourseMnemonicNumber() {
        return courseMnemonicNumber;
    }


    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getFormattedTimestamp() {
        if (timestamp != null) {
            SimpleDateFormat formatter =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formatter.setTimeZone(TimeZone.getDefault());
            return formatter.format(timestamp);
        }
        return "";
    }
}
