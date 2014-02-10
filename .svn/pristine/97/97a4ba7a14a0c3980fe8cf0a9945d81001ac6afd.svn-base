package util;

import ucb.junit.textui;

/** The suite of all JUnit tests for the util package
 *  @author
 */
public class UnitTest {

    /** A utility class that allows us to treat 2-element arrays of double
     *  as points. */
    static class ArrayPointView implements PointView<double[]> {
        @Override
        public double getX(double[] p) {
            return p[0];
        }

        @Override
        public double getY(double[] p) {
            return p[1];
        }

        @Override
        public boolean equals(double[] p1, double[] p2) {
            return p1[0] == p2[0] && p1[1] == p2[1];
        }

    }

    /** A standard ArrayPointView. */
    static final ArrayPointView VIEW = new ArrayPointView();

    /** Returns a pair {x, y}. */
    static double[] pnt(double x, double y) {
        return new double[] {x, y};
    }

    /** A few test points. */
    static final double[][] PTS = {
        { 0.0, 0.0 },
        { 1.0, 0.0 },
        { 0.0, 1.0 },
        { 1.0, 1.0 },
        { -1.0, 0.0 },
        { 0.0, -1.0 },
        { -1.0, 0.0 },
        { -1.0, 1.0 },
        { 1.0, -1.0 },
    };

    /** Run the JUnit tests in the puzzle package. */
    public static void main(String[] ignored) {
        textui.runClasses(QuadTreeTest.class);
    }

}


