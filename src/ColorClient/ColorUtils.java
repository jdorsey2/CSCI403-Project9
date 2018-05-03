package ColorClient;

import ColorClient.Data.Color;
import ColorClient.Data.ColorNamePair;
import ColorClient.Data.Point3D;

public class ColorUtils {
    private ColorUtils() {

    }

    public static Point3D toRGBSpace(ColorNamePair pair) {
        Color c = pair.getColor();
        return new Point3D(c.r, c.g, c.b);
    }

    // Color-space conversions as per:
    // http://www.niwa.nu/2013/05/math-behind-colorspace-conversions-rgb-hsl/
    // and
    // https://stackoverflow.com/questions/23090019/fastest-formula-to-get-hue-from-rgb
    public static Point3D toHSLSpace(ColorNamePair pair) {
        Color c = pair.getColor();
        double hue;
        double r = c.r / 255.;
        double g = c.g / 255.;
        double b = c.b / 255.;

        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));

        if (max == c.r) {
            hue = ((double) (c.g - c.b)) / (max - min);
        } else if (max == c.g) {
            hue = 2.0 + ((double) (c.b - c.r)) / (max - min);
        } else {
            hue = 4.0 + ((double) (c.r - c.g)) / (max - min);
        }

        hue /= 6.0;

        double luminance = (min + max) / 2.;

        double saturation;
        if (luminance > 0.5) {
            saturation = (max - min) / (2. - max - min);
        } else {
            saturation = (max - min) / (max + min);
        }

        return new Point3D(hue, saturation, luminance);
    }
}
