package com.example.osmandroidmap.vers6;

/** Утилита для преобразования слоёв карты из Yandex в OSM(Google) */
public class YandexUtil
{
    public static double[] geoToMercator(double[] g) {
        double d = g[0] * Math.PI / 180, m = g[1] * Math.PI / 180, l = 6378137, k = 0.0818191908426, f = k
                * Math.sin(m);
        double h = Math.tan(Math.PI / 4 + m / 2), j = Math.pow(
                Math.tan(Math.PI / 4 + Math.asin(f) / 2), k), i = h / j;
        // return new DoublePoint(Math.round(l * d), Math.round(l *
        // Math.log(i)));
        return new double[] { l * d, l * Math.log(i) };
    }

    public static double[] mercatorToGeo(double[] e) {
        double j = Math.PI, f = j / 2, i = 6378137, n = 0.003356551468879694, k = 0.00000657187271079536, h = 1.764564338702e-8, m = 5.328478445e-11;
        double g = f - 2 * Math.atan(1 / Math.exp(e[1] / i));
        double l = g + n * Math.sin(2 * g) + k * Math.sin(4 * g) + h
                * Math.sin(6 * g) + m * Math.sin(8 * g);
        double d = e[0] / i;
        return new double[] { d * 180 / Math.PI, l * 180 / Math.PI };
    }

    public static double[] mercatorToTiles(double[] e) {
        double d = Math.round((20037508.342789 + e[0]) * 53.5865938), f = Math
                .round((20037508.342789 - e[1]) * 53.5865938);
        d = boundaryRestrict(d, 0, 2147483647);
        f = boundaryRestrict(f, 0, 2147483647);
        return new double[] { d, f };
    }

    public static double[] tileToMercator(long[] d) {
        return new double[] { Math.round(d[0] / 53.5865938 - 20037508.342789),
                Math.round(20037508.342789 - d[1] / 53.5865938) };
    }

    public static double[] tileCoordinatesToPixels(double[] i, int h) {
        double g = Math.pow(2, toScale(h));
        return new double[] { (int) i[0] / g, (int) i[1] / g };
    }

    public static double boundaryRestrict(double f, double e, double d) {
        return Math.max(Math.min(f, d), e);
    }

    public static int toScale(int i) {
        return 23 - i;
    }

    public static long[] getTile(double[] h, int i) {
        long e = 8;
        long j = toScale(i), g = (long) h[0] >> j, f = (long) h[1] >> j;
        return new long[] { g >> e, f >> e };
    }

    public static long[] getPxCoordFromTileCoord(double[] h, int i) {
        long j = toScale(i), g = (long) h[0] >> j, f = (long) h[1] >> j;
        return new long[] { g, f };
    }

    public static long[] getTileCoordFromPixCoord(long[] h, int i) {
        long j = toScale(i), g = h[0] << j, f = h[1] << j;
        return new long[] { g, f };

    }

    public static double[] ReGetTile(double[] h, int i) {
        long e = 8;
        long j = toScale(i);
        long g = (long) h[0] << (int) j;
        long f = (long) h[1] << (int) j;
        double ge = g << (int) e;
        double fe = f << (int) e;
        long g2 = (long) (h[0] + 1) << (int) j;
        long f2 = (long) (h[1] + 1) << (int) j;
        double ge2 = g2 << (int) e;
        double fe2 = f2 << (int) e;

        double ad_g = (ge2 - ge) * (h[0] - Math.floor(h[0]));
        double ad_f = (fe2 - fe) * (h[1] - Math.floor(h[1]));

        return new double[] { ge + ad_g, fe + ad_f };
    }

    public static double[] ReGetTile(long[] h, int i) {
        long e = 8;
        long j = toScale(i);
        long g = (long) h[0] << (int) j;
        long f = (long) h[1] << (int) j;
        return new double[] { g << (int) e, f << (int) e };
    }

    public static double[] getGeoFromTile(int x, int y, int zoom) {
        double a, c1, c2, c3, c4, g, z, mercX, mercY;
        a = 6378137;
        c1 = 0.00335655146887969;
        c2 = 0.00000657187271079536;
        c3 = 0.00000001764564338702;
        c4 = 0.00000000005328478445;
        mercX = (x * 256 * 2 ^ (23 - zoom)) / 53.5865938 - 20037508.342789;
        mercY = 20037508.342789 - (y * 256 * 2 ^ (23 - zoom)) / 53.5865938;

        g = Math.PI / 2 - 2 * Math.atan(1 / Math.exp(mercY / a));
        z = g + c1 * Math.sin(2 * g) + c2 * Math.sin(4 * g) + c3
                * Math.sin(6 * g) + c4 * Math.sin(8 * g);

        return new double[] { mercX / a * 180 / Math.PI, z * 180 / Math.PI };
    }

    public static long[] getTileFromGeo(double lat, double lon, int zoom) {
        double rLon, rLat, a, k, z;
        rLon = lon * Math.PI / 180;
        rLat = lat * Math.PI / 180;
        a = 6378137;
        k = 0.0818191908426;
        z = Math.pow(
                Math.tan(Math.PI / 4 + rLat / 2)
                        / (Math.tan(Math.PI / 4 + Math.asin(k * Math.sin(rLat))
                        / 2)), k);
        return new long[] {
                (int) (((20037508.342789 + a * rLon) * 53.5865938 / Math.pow(2,
                        (23 - zoom))) / 256),
                (int) (((20037508.342789 - a * Math.log(z)) * 53.5865938 / Math
                        .pow(2, (23 - zoom)))) / 256 };
    }

    public static double tile2lon(int x, int aZoom) {
        return (x / Math.pow(2.0, aZoom) * 360.0) - 180;
    }

    public static double tile2lat(int y, int aZoom) {

        final double MerkElipsK = 0.0000001;
        final long sradiusa = 6378137;
        final long sradiusb = 6356752;
        final double FExct = (double) Math.sqrt(sradiusa * sradiusa - sradiusb
                * sradiusb)
                / sradiusa;
        final int TilesAtZoom = 1 << aZoom;
        double result = (y - TilesAtZoom / 2) / -(TilesAtZoom / (2 * Math.PI));
        result = (2 * Math.atan(Math.exp(result)) - Math.PI / 2) * 180
                / Math.PI;
        double Zu = result / (180 / Math.PI);
        double yy = ((y) - TilesAtZoom / 2);

        double Zum1 = Zu;
        Zu = Math.asin(1
                - ((1 + Math.sin(Zum1)) * Math.pow(1 - FExct * Math.sin(Zum1),
                FExct))
                / (Math.exp((2 * yy) / -(TilesAtZoom / (2 * Math.PI))) * Math
                .pow(1 + FExct * Math.sin(Zum1), FExct)));
        while (Math.abs(Zum1 - Zu) >= MerkElipsK) {
            Zum1 = Zu;
            Zu = Math
                    .asin(1
                            - ((1 + Math.sin(Zum1)) * Math.pow(
                            1 - FExct * Math.sin(Zum1), FExct))
                            / (Math.exp((2 * yy)
                            / -(TilesAtZoom / (2 * Math.PI))) * Math
                            .pow(1 + FExct * Math.sin(Zum1), FExct)));
        }

        result = Zu * 180 / Math.PI;

        return result;

    }

    public static int[] getMapTileFromCoordinates(final double aLat,
                                                  final double aLon, final int zoom) {
        final int[] out = new int[2];

        final double E2 = (double) aLat * Math.PI / 180;
        final long sradiusa = 6378137;
        final long sradiusb = 6356752;
        final double J2 = (double) Math.sqrt(sradiusa * sradiusa - sradiusb
                * sradiusb)
                / sradiusa;
        final double M2 = (double) Math.log((1 + Math.sin(E2))
                / (1 - Math.sin(E2)))
                / 2
                - J2
                * Math.log((1 + J2 * Math.sin(E2)) / (1 - J2 * Math.sin(E2)))
                / 2;
        final double B2 = (double) (1 << zoom);
        out[0] = (int) Math.floor(B2 / 2 - M2 * B2 / 2 / Math.PI);

        out[1] = (int) Math.floor((aLon + 180) / 360 * (1 << zoom));

        return out;
    }
}