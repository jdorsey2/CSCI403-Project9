package tools;

import colorclient.ColorUtils;
import data.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Scanner;

public class PointCloudExport {
    private static final String OUTPUT_PREFIX = "./data/";
    private static final String COLORS_BY_NAME_FREQUENCY_QUERY =
            "SELECT AVG(answers.r) AS r, AVG(answers.g) AS g, AVG(answers.b) AS b, names.numusers, names.colorname FROM jdorsey.answers, jdorsey.names WHERE answers.colorname = names.colorname AND names.numusers > 50 GROUP BY names.colorname, names.numusers HAVING (STDDEV_SAMP(r) < 60 AND STDDEV_SAMP(g) < 60 AND STDDEV_SAMP(b) < 60) OR (STDDEV_SAMP(r) IS NULL OR STDDEV_SAMP(g) IS NULL OR STDDEV_SAMP(b) IS NULL);";

    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Enter an output file name: ");
        String outFilename = keyboard.nextLine();

        OcTree<ColorNamePair> data = new OcTree<>(Point3D.zero(), new Point3D(256.1), ColorUtils::toRGBSpace, 3);
        loadFromDatabase(COLORS_BY_NAME_FREQUENCY_QUERY, data);
        System.out.println("Loaded " + data.size() + " color-name pairs.");
        writeOcTreeToPLY(outFilename, data);
    }

    private static void writeOcTreeToPLY(String filename, OcTree<ColorNamePair> tree) {
        Collection<ColorNamePair> collected = tree.collectValues();
        System.out.println("Collected " + collected.size() + " values from octree.");
        System.out.println("Writing to file " + filename + ".");
        long startTime = System.nanoTime();
        PLYExporter.toFile(collected, ColorUtils::toRGBSpace, OUTPUT_PREFIX + filename);
        long endTime = System.nanoTime();
        double timeToWrite = (endTime - startTime) / 1_000_000.;
        System.out.println("Took " + timeToWrite + " milliseconds to write " + filename + ".");
    }

    private static void loadFromDatabase(String query, OcTree<ColorNamePair> data) {
        ResultSet colors = DatabaseManager.runQuery(query);

        try {
            while (colors.next()) {
                int r = colors.getInt("r");
                int g = colors.getInt("g");
                int b = colors.getInt("b");
                String colorname = colors.getString("colorname");
                ColorNamePair pair = new ColorNamePair(new Color(r, g, b), colorname, 0);
                data.add(pair);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getSQLState());
        }
    }
}
