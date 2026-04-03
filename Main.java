import java.sql.*;

public class Main {

    public static void main(String[] args) throws SQLException {

        Connection conn = DriverManager.getConnection("jdbc:sqlite:booktracker.db");
        Statement s = conn.createStatement();

        s.execute("CREATE TABLE IF NOT EXISTS User (userID INTEGER PRIMARY KEY, age INTEGER, gender TEXT)");
        s.execute("CREATE TABLE IF NOT EXISTS ReadingHabit (habitID INTEGER PRIMARY KEY, book TEXT, pagesRead INTEGER, submissionMoment DATETIME, user INTEGER, FOREIGN KEY (user) REFERENCES User(userID))");

        DataLoader.load(s);
        System.out.println("All data loaded.");

        // 1. Add a user
        s.execute("INSERT OR IGNORE INTO User VALUES (66, 28, 'm')");
        System.out.println("1. User added");

        // 2. Show reading habits for user 1
        ResultSet results = s.executeQuery("SELECT * FROM ReadingHabit WHERE user = 1");
        System.out.println("2. Reading habits for user 1:");
        while (results.next()) {
            System.out.println("   " + results.getString("book") + " - " + results.getInt("pagesRead") + " pages");
        }

        // 3. Change a book title
        s.execute("UPDATE ReadingHabit SET book = 'Boa Vs. Python (Updated)' WHERE book = 'Boa Vs. Python'");
        System.out.println("3. Book title updated");

        // 4. Delete a reading habit record
        s.execute("DELETE FROM ReadingHabit WHERE habitID = 100");
        System.out.println("4. Record deleted");

        // 5. Mean age of all users
        results = s.executeQuery("SELECT AVG(age) AS meanAge FROM User");
        System.out.println("5. Mean age: " + results.getDouble("meanAge"));

        // 6. Number of users that read a specific book
        results = s.executeQuery("SELECT COUNT(DISTINCT user) AS total FROM ReadingHabit WHERE book = 'Topological Data Analysis with Applications'");
        System.out.println("6. Users who read Topological Data Analysis: " + results.getInt("total"));

        // 7. Total pages read by all users
        results = s.executeQuery("SELECT SUM(pagesRead) AS total FROM ReadingHabit");
        System.out.println("7. Total pages read: " + results.getInt("total"));

        // 8. Users who read more than one book
        results = s.executeQuery("SELECT COUNT(*) AS total FROM (SELECT user FROM ReadingHabit GROUP BY user HAVING COUNT(DISTINCT book) > 1)");
        System.out.println("8. Users with more than one book: " + results.getInt("total"));

        // 9. Add Name column to the User table
        try {
            s.execute("ALTER TABLE User ADD COLUMN Name TEXT");
            System.out.println("9. Name column added");
        } catch (SQLException e) {
            System.out.println("9. Name column already exists");
        }

        conn.close();
    }
}
