import java.io.File;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.Scanner;
import java.util.function.Function;

public class ColorSearch {
    public static final String DATA_PREFIX = "../";
    public static OcTree<ColorNamePair> data;

    public static void main(String[] args) {
        data = new OcTree<>(Point3D.zero(), new Point3D(255), pair -> {
            Color c = pair.getColor();
            return new Point3D(c.r, c.g, c.b);
        }, 3);

        File dataFile = new File(DATA_PREFIX + "satfaces.txt");
        Scanner scanner;
        try {
            scanner = new Scanner(dataFile);
        } catch (FileNotFoundException e) {
            System.out.println("File " + DATA_PREFIX + "satfaces.txt not found");
            return;
        }

        while (scanner.hasNextLine()) {
            String[] split = scanner.nextLine().split("] ");
            Color c = Color.fromString(split[0]);
            ColorNamePair pair = new ColorNamePair(c, split[1]);
            data.add(pair);
        }

        System.out.println("Done loading " + data.size() + " color-name pairs.");
        for (int i = 0; i < 100; i++) {
            double r = Math.random() * 256;
            double g = Math.random() * 256;
            double b = Math.random() * 256;

            Point3D color = new Point3D(r, g, b);
            OcTree<ColorNamePair> tree = data.getOctantBy(color);
            if (tree != null) {
                System.out.println("Colors near: " + color);
                tree.getContents().stream()
                        .sorted(Comparator.comparing(pair -> Point3D.manhattanDistance(color, tree.getCordMapper().apply(pair))))
                        .limit(10)
                        .forEach(System.out::println);
            }
        }
    }
}