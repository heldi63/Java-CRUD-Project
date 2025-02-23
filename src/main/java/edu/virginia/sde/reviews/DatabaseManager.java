package edu.virginia.sde.reviews;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DATABASE_URL = "jdbc:sqlite:course_reviews.db";

    private static final String CREATE_USERS_TABLE = """
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE NOT NULL,
            password TEXT NOT NULL
        );
    """;


    //there is another column called average_rating that i added through terminal using ALTER TABLE
    private static final String CREATE_COURSES_TABLE = """
        CREATE TABLE IF NOT EXISTS courses (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            subject TEXT NOT NULL,
            number INTEGER NOT NULL,
            title TEXT NOT NULL,
            average_rating NOT NULL,
            UNIQUE(subject, number, title)
        );
    """;

    private static final String CREATE_REVIEWS_TABLE = """
        CREATE TABLE IF NOT EXISTS reviews (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            course_id INTEGER NOT NULL,
            rating INTEGER NOT NULL,
            comment TEXT,
            timestamp TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            UNIQUE(user_id, course_id),
            FOREIGN KEY(user_id) REFERENCES users(id),
            FOREIGN KEY(course_id) REFERENCES courses(id)
        );
    """;
    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(CREATE_USERS_TABLE);
                    stmt.execute(CREATE_COURSES_TABLE);
                    stmt.execute(CREATE_REVIEWS_TABLE);
                    System.out.println("Database initialized successfully.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            System.err.println("Application will work without a database");
        }
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }


}
