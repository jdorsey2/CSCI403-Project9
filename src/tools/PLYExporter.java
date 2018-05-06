package tools;

import data.ColorNamePair;
import data.Point3D;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

public class PLYExporter {
    public static void toFile(Collection<ColorNamePair> pairs, Function<ColorNamePair, Point3D> cordMapper, String filename) {
        BufferedWriter writer = getWriter(filename);
        writePLYHeader(writer, pairs);
        try {
            for (ColorNamePair pair : pairs) {
                Point3D point = cordMapper.apply(pair);
                writer.write(point.getX() + " ");
                writer.write(point.getY() + " ");
                writer.write(point.getZ() + " ");
                writer.write(pair.getColor().r + " ");
                writer.write(pair.getColor().g + " ");
                writer.write(pair.getColor().b + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedWriter getWriter(String filename) {
        BufferedWriter writer = null;
        try {
            File outFile = new File(filename);
            writer = new BufferedWriter(new FileWriter(outFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer;
    }

    private static void writePLYHeader(BufferedWriter writer, Collection<?> data) {
        try {
            writer.write("ply\nformat ascii 1.0\n");
            writer.write("element vertex " + data.size() + "\n");
            writer.write("property float x\n");
            writer.write("property float y\n");
            writer.write("property float z\n");
            writer.write("property uchar red\n");
            writer.write("property uchar green\n");
            writer.write("property uchar blue\n");
            writer.write("end_header\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
