package DatabaseDigest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class DigestAnswers {
    public static void main(String[] args) throws ClassNotFoundException {
        Connection db = connectToDb();
    }

    public static Connection connectToDb() {
        Connection db = null;
        try {
            Class.forName("org.postgresql.Driver");
            String connectString = "jdbc:postgresql://flowers.mines.edu/csci403";
            Scanner keyboard = new Scanner(System.in);
            System.out.print("Username: ");
            String username = keyboard.nextLine();
            System.out.print("Password: ");
            String password = keyboard.nextLine();

            db = DriverManager.getConnection(connectString, username, password);
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Error connecting to database: " + e);
            System.exit(1);
        }
        return db;
    }
}
