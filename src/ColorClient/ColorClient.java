package ColorClient;

import ColorClient.Data.Color;
import ColorClient.Data.ColorNamePair;
import ColorClient.Data.OcTree;
import ColorClient.Data.Point3D;
import DatabaseDigest.DatabaseManager;
import DatabaseDigest.PLYExporter;
import javafx.application.Application;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class ColorClient extends Application {
    public static final String DATA_PREFIX = "./data/";
    public static final String INPUT_FILE = "satfaces.txt";
    public static OcTree<ColorNamePair> data;

    public static final String COLORS_BY_NAME_FREQUENCY_QUERY =
            "SELECT AVG(answers.r) AS r, AVG(answers.g) AS g, AVG(answers.b) AS b, names.numusers, names.colorname FROM jdorsey.answers, jdorsey.names WHERE answers.colorname = names.colorname AND names.numusers = 2 GROUP BY names.colorname, names.numusers";

    public static final String COLORS_BY_NAME =
            "SELECT answers.r, answers.g, answers.b, names.colorname, names.numusers FROM jdorsey.answers, jdorsey.names WHERE names.colorname = answers.colorname";

    public static void main(String[] args) {
        data = new OcTree<>(Point3D.zero(), new Point3D(255), ColorUtils::toHSLSpace, 3);
        //loadFromFile();
        loadFromDatabase(COLORS_BY_NAME);
        System.out.println("Done loading " + data.size() + " color-name pairs.");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

    }

    private static void writeOcTreeToPLY(String filename, OcTree<ColorNamePair> tree) {
        Collection<ColorNamePair> collected = tree.collectValues();
        System.out.println("Collected " + collected.size() + " values from octree.");
        System.out.println("Writing to file " + filename + ".");
        long startTime = System.nanoTime();
        PLYExporter.toFile(collected, ColorUtils::toRGBSpace, filename);
        long endTime = System.nanoTime();
        double timeToWrite = (endTime - startTime) / 1_000_000.;
        System.out.println("Took " + timeToWrite + " milliseconds to write " + filename + ".");
    }

    private static void printOcTreeStatistics() {
        int[] stats = {0, 0, 0};
        data.getStatistics(stats);
        System.out.println("Minimum number in leaf node: " + stats[0]);
        System.out.println("Maximum number in leaf node: " + stats[1]);
        System.out.println("Number of leaf nodes: " + stats[2]);
        System.out.println("Average colors per leaf: " + ((double) data.size()) / ((double) stats[2]));
        System.out.println("Total colors: " + data.size());
    }

    private static void testLookupTimes() {
        List<Double> lookupTimes = new ArrayList<>();
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
            double lookupTime = (endTime - startTime) / 1_000_000.;
            System.out.println("Lookup took " + lookupTime + " milliseconds.");
            lookupTimes.add(lookupTime);
        }

        System.out.println("Average time to lookup nearby colors: " + lookupTimes.stream().mapToDouble(a -> a).average().orElse(0.0));
    }

    private static void loadFromDatabase(String query) {
        Connection db = DatabaseManager.connect();
        ResultSet colors = DatabaseManager.runQuery(db, query);

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