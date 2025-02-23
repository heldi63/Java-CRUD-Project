import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseTest {
    private static final String DATABASE_URL = "jdbc:sqlite:course_reviews.db";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                System.out.println("Connection to SQLite has been established.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
