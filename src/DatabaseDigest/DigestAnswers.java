package DatabaseDigest;

import Data.Color;
import Data.ColorNamePair;
import Data.DatabaseManager;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Takes all the rgb values and names from the answers and writes them to a file.
 */
public class DigestAnswers {
    public static final String DATA_PREFIX = "./data/";
    private static final String OUTPUT_FILE = "DigestOutput.txt";

    public static void main(String[] args) throws ClassNotFoundException {
        Connection db = DatabaseManager.connect();
        ResultSet results = DatabaseManager.runQuery(db, "SELECT r, g, b, colorname FROM jdorsey.answers;");
        List<ColorNamePair> data = new ArrayList<>();
        try {
            while (results.next()) {
                int r = results.getInt("r");
                int g = results.getInt("g");
                int b = results.getInt("b");
                String name = results.getString("colorname");

                data.add(new ColorNamePair(new Color(r, g, b), name, 0));
            }

            System.out.println("Retrieved " + data.size() + " items.");

            System.out.println("Opening output file " + DATA_PREFIX + OUTPUT_FILE);
            File outFile = new File(DATA_PREFIX + OUTPUT_FILE);
            if (!outFile.exists()) {
                if (!outFile.createNewFile()) {
                    throw new IOException("Unable to create output file " + outFile.getAbsolutePath());
                }
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

            System.out.println("Writing data to file.");
            for (ColorNamePair datum : data) {
                String toString = datum.toString();
                writer.write(toString + "\n");
            }

            System.out.println("Finished writing data to file.");
            writer.close();
            System.out.println("Closed output file.");

            BufferedReader reader = new BufferedReader(new FileReader(outFile));
            for(int i = 0; i < 100; i++){
                System.out.println(reader.readLine());
            }
            reader.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getSQLState());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}