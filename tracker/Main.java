package tracker;
import java.awt.geom.Point2D;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.regex.MatchResult;
import util.QuadTree;

/** Main class of the tracker program.
 * @author Daniel Paden Tomasello cs61b-bz
 */
public class Main {

    /** The main tracker program.  ARGS are as described in the
     *  project 2 handout:
     *      [--debug=N] [ INPUTFILE [ OUTPUTFILE ] ]
     */
    public static void main(String... args) {
        String inputFileName, outputFileName;
        inputFileName = outputFileName = null;
        String input = "";
        int debug = 0;
        for (String x: args) {
            input += x + " ";
        }
        Scanner commandScanner = new Scanner(input);
        commandScanner.findInLine("\\s*(?:--debug=(\\d+))?"
                + "\\s*(\\S+)(?:\\s+(\\S+)?)");
        MatchResult parts = commandScanner.match();
        if (parts.group(1) != null) {
            debug = Integer.parseInt(parts.group(1));
        }
        if (parts.group(2) != null) {
            inputFileName = parts.group(2);
        }
        if (parts.group(3) != null) {
            outputFileName = parts.group(3);
        }

        if (inputFileName != null) {
            try {
                System.setIn(new FileInputStream(inputFileName));
            } catch (FileNotFoundException e) {
                System.err.printf("Error: could not open %s%n", inputFileName);
                System.exit(1);
            }
        }
        if (outputFileName != null) {
            try {
                System.setOut(new PrintStream(outputFileName));
            } catch (FileNotFoundException e) {
                System.err.printf("Error: could not open %s%n",
                        outputFileName);
                System.exit(1);
            }
        }
        parseInput();
        System.out.close();
    }
    /** Parses input and instantiate posts. */
    private static void parseInput() {
        LinkedList<Double> numbers = new LinkedList<Double>();
        Scanner inputScanner = new Scanner(System.in);
        double tMax = 0;
        double S = 0;
        double D = 0;
        double y = 0;
        double vX = 0;
        boolean configured = false;
        inputScanner.skip("(#.*)*");
        while (inputScanner.hasNext()) {
            inputScanner.skip("(#.*)*");
            inputScanner.findWithinHorizon("\\s*([+|-]?[\\d]"
                    + "*\\.?[\\d]+)\\s*", 0);
            Double temp = Double.parseDouble(inputScanner.match().group(1));
            numbers.add(temp);
            if (numbers.size() == 5) {
                tMax = numbers.poll();
                S = numbers.poll();
                D = numbers.poll();
                y = numbers.poll();
                vX = numbers.poll();
                configured = true;
                break;
            }
        }
        if (!configured) {
            System.err.println("Error: Incorrect input data");
            System.exit(1);
        }

        int postnum = 1;
        inputScanner.skip("(#.*)*");
        while (inputScanner.hasNext()) {
            inputScanner.skip("(#.*)*");
            inputScanner.findWithinHorizon("\\s*([+|-]?"
                    + "[\\d]*\\.?[\\d]+)\\s*", 0);
            double temp = Double.parseDouble(inputScanner.match().group(1));
            numbers.add(temp);
            if (numbers.size() == 3) {
                double xp = numbers.poll();
                double yp = numbers.poll();
                double t = numbers.poll();
                if (t < 0) {
                    System.err.println("Error: incorrect input data");
                    System.exit(1);
                }
                addPost(xp, yp, t, postnum);
                postnum += 1;
            }
        }
        if (numbers.size() != 0) {
            System.err.println("Error: incorrect input data");
            System.exit(1);
        }
        configure(tMax, S, D, y, vX);
        simulate();
    }
    /** Sorted list of all times. Will be used to generated times in
     * simulations.
     */
    private static PriorityQueue<Double> timessorted;

    /** Simulation. */
    private static void simulate() {
        Vehicle car = new Vehicle(_y, _vX);
        Chooser chooser = new Chooser(_S);
        Double time = 0.0;
        Post post;
        PostComparator comp = new PostComparator();
        if (posts.size() > 0) {
            PriorityQueue<Post> reportingPosts
                = new PriorityQueue<Post>(posts.size(), comp);
            time = timessorted.poll();
            while (time != null) {
                car.advance(time);
                double x = car.x();
                double y = car.y();
                Iterator<Post> postsIter = posts.iterator(x - _D,
                        y - _D, x + _D, y + _D);
                while (postsIter.hasNext()) {
                    post = postsIter.next();
                    if (Point2D.distance(x, y, view.getX(post),
                            view.getY(post)) <= _D) {
                        post.detect(x, y, time);
                        if (!reportingPosts.contains(post)) {
                            reportingPosts.add(post);
                        }
                    }
                }
                car.detected(chooser);
                time = timessorted.poll();
            }
            while (!reportingPosts.isEmpty()) {
                reportingPosts.poll().report();
            }
        } else {
            car.advance(_tMax);
        }
        time = _tMax;
        System.out.println("");
        car.reportFinal(time);
        System.exit(0);
    }
    /** Comparator for Posts. */
    public static class PostComparator implements Comparator<Post> {
        @Override
        public int compare(Post post1, Post post2) {
            if (post1.getPostNum() == post2.getPostNum()) {
                return 0;
            } else if (post1.getPostNum() > post2.getPostNum()) {
                return 1;
            } else {
                return -1;
            }

        }
        /** Returns true if POST1 and POST2 are the same. */
        public boolean equals(Post post1, Post post2) {
            if (post1.getPostNum() == post2.getPostNum()) {
                return true;
            }
            return false;
        }
    }

    /** Sets duration to TMAX, random seed to S, initial y location
     * to Y and the initial horizontal velocity to VX and
     * Maximun distance of pulse to D. */
    private static void configure(double tMax, double S,
            double D, double y, double vX) {
        if (D < 0 | tMax < 0 | vX < 0) {
            System.err.printf("Error: Incorrect input data");
            System.exit(1);
        }
        printParameters(tMax, S, D, y, vX);
        _tMax = tMax;
        _D = D;
        _S = S;
        _y = y;
        _vX = vX;
        if (posts.size() != 0) {
            timessorted = new PriorityQueue<Double>(posts.size());
            Iterator<Post> postIterator = posts.iterator();
            while (postIterator.hasNext()) {
                Post post = postIterator.next();
                double time = post.gettx();
                if (time != 0) {
                    for (double t = time; t < _tMax; t += time) {
                        timessorted.add(t);
                    }
                }
            }
            timessorted.add(_tMax);
        }
    }


    /** Simulation Duration. */
    private static double _tMax;

    /**Seed for random generator. */
    private static double _S;

    /** Maximun detection radius. */
    private static double _D;

    /** Initial Vertical Location. */
    private static double _y;

    /** INitial horizontal velocity of vehicle. */
    private static double _vX;




    /** Prints parameters in correct format.
     * @param tMax duration.
     * @param S random seed.
     * @param D maximun detection distance.
     * @param y starting y location.
     * @param vX initial x horizontal velocity.
     */
    private static void printParameters(double tMax,
            double S, double D, double y, double vX) {
        System.out.println("Simulation parameters:");
        System.out.println(String.format("  "
                + "Total simulated time: %.1f", tMax));
        System.out.println(String.format("  "
                + "Random seed: %.0f", S));
        System.out.println(String.format("  "
                + "Maximum detection radius: %.1f", D));
        System.out.println(String.format("  "
                + "Initial vertical location: %.1f", y));
        System.out.println(String.format("  "
                + "Initial horizontal velocity of vehicle: %.1f", vX));
        System.out.println("");
        System.out.println("Reports:");
    }
    /** Adds post to a quadtree.
     * @param xp x position
     * @param yp y position
     * @param t t interval of pulses
     * @param postnum Post number.
     */
    private static void addPost(double xp, double yp, double t, int postnum) {
        posts.add(new Post(xp, yp, t, postnum));
    }

    /** Represent small interval so that within bounds will
     * check bound points. */
    private static final double DELTA = 0.00001;


    /** PostViewer. */
    private static PostView view = new PostView();

    /** QuadTree to hold Posts. */
    private static QuadTree<Post> posts = new QuadTree<Post>(view, 1);
    /** Print brief description of the command-line format. */
    static void usage() {
        System.exit(1);
    }
}
