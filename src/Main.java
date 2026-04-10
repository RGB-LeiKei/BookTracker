//  THERE IS TWO WAYS TO COMPILE AND RUN THIS CODE:
//
//  1. If you are in the project root directory (where the 'src' folder is):
//  javac -cp "lib\\sqlite-jdbc-3.51.3.0.jar" src\\Main.java src\\DataLoader.java
//  java -cp "lib\\sqlite-jdbc-3.51.3.0.jar;src" Main
//
// OR
//
// javac -cp lib/sqlite-jdbc-3.51.3.0.jar src/Main.java src/DataLoader.java
// java -cp lib/sqlite-jdbc-3.51.3.0.jar:src Main
//
//  2. Run the run.bat file if you are on Windows, or run.sh if you are on Linux/Mac. These scripts will compile and run the code for you.

import java.sql.*;
import java.util.Scanner;

public class Main {
    private static Connection conn;
    private static Statement s;
    private static Scanner scanner;

    public static void main(String[] args) throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:data/booktracker.db");
        s = conn.createStatement();
        scanner = new Scanner(System.in);

        s.execute("CREATE TABLE IF NOT EXISTS User (userID INTEGER PRIMARY KEY, age INTEGER, gender TEXT)");
        s.execute("CREATE TABLE IF NOT EXISTS ReadingHabit (habitID INTEGER PRIMARY KEY, book TEXT, pagesRead INTEGER, submissionMoment DATETIME, user INTEGER, FOREIGN KEY (user) REFERENCES User(userID))");

        DataLoader.load(s);
        System.out.println("All data loaded.\n");

        int choice;
        boolean running = true;
        while (running) {
            displayMenu();
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (choice) {
                    case 1:
                        addUser();
                        break;
                    case 2:
                        showReadingHabits();
                        break;
                    case 3:
                        changeBookTitle();
                        break;
                    case 4:
                        deleteRecord();
                        break;
                    case 5:
                        showMeanAge();
                        break;
                    case 6:
                        countUsersForBook();
                        break;
                    case 7:
                        showTotalPages();
                        break;
                    case 8:
                        showUsersMultipleBooks();
                        break;
                    case 9:
                        addNameColumn();
                        break;
                    case 10:
                        running = false;
                        System.out.println("Exiting...\n");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.\n");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.\n");
                scanner.nextLine();
            }
        }

        conn.close();
        scanner.close();
    }

    private static void displayMenu() {
        System.out.println("========== BOOK TRACKER MENU ==========");
        System.out.println("1. Add a user");
        System.out.println("2. Show reading habits for a user");
        System.out.println("3. Change a book title");
        System.out.println("4. Delete a reading habit record");
        System.out.println("5. Show mean age of users");
        System.out.println("6. Count users who read a specific book");
        System.out.println("7. Show total pages read by all users");
        System.out.println("8. Show users who read more than one book");
        System.out.println("9. Add 'Name' column to User table");
        System.out.println("10. Exit");
        System.out.print("Enter your choice (1-10): ");
    }

    private static void addUser() throws SQLException {
        System.out.print("Enter userID: ");
        int userID = scanner.nextInt();
        System.out.print("Enter age: ");
        int age = scanner.nextInt();
        System.out.print("Enter gender (m/f): ");
        String gender = scanner.next();
        scanner.nextLine();

        s.execute("INSERT OR IGNORE INTO User (userID, age, gender) VALUES (" + userID + ", " + age + ", '" + gender + "')");
        System.out.println("✓ User added successfully!\n");
    }

    private static void showReadingHabits() throws SQLException {
        System.out.print("Enter userID: ");
        int userID = scanner.nextInt();
        scanner.nextLine();

        ResultSet results = s.executeQuery("SELECT * FROM ReadingHabit WHERE user = " + userID);
        System.out.println("\nReading habits for user " + userID + ":");
        boolean found = false;
        while (results.next()) {
            found = true;
            System.out.println("   - " + results.getString("book") + " (" + results.getInt("pagesRead") + " pages)");
        }
        if (!found) {
            System.out.println("   No reading habits found for this user.");
        }
        System.out.println();
    }

    private static void changeBookTitle() throws SQLException {
        scanner.nextLine(); // clear any remaining input
        System.out.print("Enter current book title: ");
        String oldTitle = scanner.nextLine();
        System.out.print("Enter new book title: ");
        String newTitle = scanner.nextLine();

        String oldTitleEscaped = oldTitle.replace("'", "''");
        String newTitleEscaped = newTitle.replace("'", "''");
        s.execute("UPDATE ReadingHabit SET book = '" + newTitleEscaped + "' WHERE book = '" + oldTitleEscaped + "'");
        System.out.println("✓ Book title updated successfully!\n");
    }

    private static void deleteRecord() throws SQLException {
        System.out.print("Enter habitID to delete: ");
        int habitID = scanner.nextInt();
        scanner.nextLine();

        s.execute("DELETE FROM ReadingHabit WHERE habitID = " + habitID);
        System.out.println("✓ Record deleted successfully!\n");
    }

    private static void showMeanAge() throws SQLException {
        ResultSet results = s.executeQuery("SELECT AVG(age) AS meanAge FROM User");
        if (results.next()) {
            System.out.println("Mean age of all users: " + String.format("%.2f", results.getDouble("meanAge")) + "\n");
        }
    }

    private static void countUsersForBook() throws SQLException {
        System.out.print("Enter book title: ");
        String bookTitle = scanner.nextLine();

        String bookTitleEscaped = bookTitle.replace("'", "''");
        ResultSet results = s.executeQuery("SELECT COUNT(DISTINCT user) AS total FROM ReadingHabit WHERE book = '" + bookTitleEscaped + "'");
        if (results.next()) {
            System.out.println("Users who read '" + bookTitle + "': " + results.getInt("total") + "\n");
        }
    }

    private static void showTotalPages() throws SQLException {
        ResultSet results = s.executeQuery("SELECT SUM(pagesRead) AS total FROM ReadingHabit");
        if (results.next()) {
            System.out.println("Total pages read by all users: " + results.getInt("total") + "\n");
        }
    }

    private static void showUsersMultipleBooks() throws SQLException {
        ResultSet results = s.executeQuery("SELECT COUNT(*) AS total FROM (SELECT user FROM ReadingHabit GROUP BY user HAVING COUNT(DISTINCT book) > 1)");
        if (results.next()) {
            System.out.println("Users who read more than one book: " + results.getInt("total") + "\n");
        }
    }

    private static void addNameColumn() throws SQLException {
        try {
            s.execute("ALTER TABLE User ADD COLUMN Name TEXT");
            System.out.println("✓ 'Name' column added successfully!\n");
        } catch (SQLException e) {
            System.out.println("✓ 'Name' column already exists.\n");
        }
    }
}
