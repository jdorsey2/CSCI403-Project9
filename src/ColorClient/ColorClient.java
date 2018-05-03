package ColorClient;

import ColorClient.Data.Color;
import ColorClient.Data.ColorNamePair;
import ColorClient.Data.OcTree;
import ColorClient.Data.Point3D;
import DatabaseDigest.DatabaseManager;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Scanner;

public class ColorClient extends JFrame {
    public static final String DATA_PREFIX = "./data/";
    public static final String INPUT_FILE = "satfaces.txt";
    public static OcTree<ColorNamePair> data;

    public static final String COLORS_BY_NAME_FREQUENCY_QUERY =
            "SELECT answers.r, answers.g, answers.b, answers.colorname, names.numusers " +
                    "FROM jdorsey.answers, jdorsey.names " +
                    "WHERE answers.colorname = names.colorname AND numusers > 1";

    public static void main(String[] args) {
        data = new OcTree<>(Point3D.zero(), new Point3D(255), ColorUtils::toRGBSpace, 3);
        //loadFromFile();
        loadFromDatabase();

        System.out.println("Done loading " + data.size() + " color-name pairs.");
        for (int i = 0; i < 100; i++) {
            long startTime = System.nanoTime();
            double r = Math.random() * 256;
            double g = Math.random() * 256;
            double b = Math.random() * 256;

            Point3D color = new Point3D(r, g, b);
            OcTree<ColorNamePair> tree = data.getOctantBy(color);
            if (tree != null) {
                System.out.println("Colors near: " + color);
                tree.getContents().stream()
                        .sorted(Comparator.comparing(pair -> Point3D.distance(color, tree.getCordMapper().apply(pair))))
                        .limit(10)
                        .forEach(System.out::println);
            }
            long endTime = System.nanoTime();
            System.out.println("Lookup took " + (endTime-startTime)/1_000_000. + " milliseconds.");
        }

        int[] stats = {0, 0, 0};
        data.getStatistics(stats);
        System.out.println("Minimum number in leaf node: " + stats[0]);
        System.out.println("Maximum number in leaf node: " + stats[1]);
        System.out.println("Number of leaf nodes: " + stats[2]);
        System.out.println("Average colors per leaf: " + ((double)data.size())/((double)stats[2]));
        System.out.println("Total colors: " + data.size());
    }

    private static void loadFromDatabase() {
        Connection db = DatabaseManager.connect();
        ResultSet colors = DatabaseManager.runQuery(db, COLORS_BY_NAME_FREQUENCY_QUERY);

        try {
            while (colors.next()) {
                int r = colors.getInt("r");
                int g = colors.getInt("g");
                int b = colors.getInt("b");
                int frequency = colors.getInt("numusers");
                String colorname = colors.getString("colorname");
                ColorNamePair pair = new ColorNamePair(new Color(r, g, b), colorname, frequency);
                data.add(pair);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getSQLState());
        }
    }

    private static void loadFromFile() {
        File dataFile = new File(DATA_PREFIX + INPUT_FILE);
        Scanner scanner;
        try {
            scanner = new Scanner(dataFile);
        } catch (FileNotFoundException e) {
            System.out.println("File " + DATA_PREFIX + INPUT_FILE + " not found");
            System.exit(1);
            return;
        }

        while (scanner.hasNextLine()) {
            String[] split = scanner.nextLine().split("] ");
            if (split.length == 2) {
                Color c = Color.fromString(split[0]);
                ColorNamePair pair = new ColorNamePair(c, split[1], 0);
                data.add(pair);
            }
        }
    }
}