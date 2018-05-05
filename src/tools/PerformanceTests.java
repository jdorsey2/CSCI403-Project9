package tools;

import colorclient.ColorUtils;
import data.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PerformanceTests {
    public static final String ALL_COLORS_QUERY = "SELECT r, g, b, colorname FROM jdorsey.answers";

    public static void main(String[] args) {
        testLookupTimes();
    }

    private static void testLookupTimes() {
        OcTree<ColorNamePair> data = new OcTree<>(Point3D.zero(), new Point3D(255), ColorUtils::toRGBSpace, 3);
        loadFromDatabase(ALL_COLORS_QUERY, data);

        List<Double> lookupTimes = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            long startTime = System.nanoTime();
            double r = Math.random() * 256;
            double g = Math.random() * 256;
            double b = Math.random() * 256;

            Point3D color = new Point3D(r, g, b);
            OcTree<ColorNamePair> tree = data.getOctantBy(color);
            List<ColorNamePair> sorted = tree.getContents().stream()
                    .sorted(Comparator.comparing(pair -> Point3D.distance(color, tree.getCordMapper().apply(pair))))
                    .limit(100).collect(Collectors.toList());
            long endTime = System.nanoTime();
            double lookupTime = (endTime - startTime) / 1_000_000.;
            System.out.println("Lookup/sort took " + lookupTime + " milliseconds.");
            lookupTimes.add(lookupTime);
        }

        System.out.println("Average time to lookup nearby colors: " + lookupTimes.stream().mapToDouble(a -> a).average().orElse(0.0));
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
