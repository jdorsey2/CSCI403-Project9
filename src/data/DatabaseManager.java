package data;

import java.io.*;
import java.sql.*;
import java.util.Scanner;

public class DatabaseManager {
    private static Connection db;

    private static void connect() {
        try {
            Class.forName("org.postgresql.Driver");
            String connectString = "jdbc:postgresql://flowers.mines.edu/csci403";
            File authFile = new File("auth.txt");
            String username = "";
            String password = "";
            if (authFile.exists()) {
                System.out.println("Found auth file, using those credentials.");
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(authFile));
                    String userLine = reader.readLine();
                    String passLine = reader.readLine();
                    username = userLine.split("user ")[1];
                    password = passLine.split("pass ")[1];
                } catch (FileNotFoundException e) {
                    // bad practice, but we know this file exists
                } catch (IOException e) {
                    System.out.println("Auth file found with incorrect format.");
                }
            } else {
                System.out.println("Did not find an auth file.  Add a file called 'auth.txt' in the working directory of the program with the following format to automatically connect to the db.");
                System.out.println("user <username>");
                System.out.println("pass <password>");
                if (System.console() == null) {
                    Scanner keyboard = new Scanner(System.in);
                    System.out.print("Username: ");
                    username = keyboard.nextLine();
                    System.out.print("Password: ");
                    password = keyboard.nextLine();
                } else {
                    System.out.print("Username: ");
                    username = System.console().readLine();
                    System.out.print("Password: ");
                    password = new String(System.console().readPassword());
                }
            }

            db = DriverManager.getConnection(connectString, username, password);
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Error connecting to database: " + e);
            System.exit(1);
        }
    }

    public static ResultSet runQuery(String q) {
        if (db == null) {
            connect();
        }
        ResultSet results = null;
        try {
            PreparedStatement statement = db.prepareStatement(q);
            results = statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getSQLState());
        }
        return results;
    }

    public static ResultSet runQuery(PreparedStatement statement) {
        if (db == null) {
            connect();
        }
        ResultSet results = null;
        try {
            results = statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getSQLState());
        }
        return results;
    }
}
