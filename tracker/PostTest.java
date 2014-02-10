package tracker;

import static org.junit.Assert.*;

import org.junit.Test;

public class PostTest {

    @Test
    public void test() {
        Post post1 = new Post(1, 2, 3, 1);
        Post post2 = new Post(1, 2, 3, 2);
        assertEquals("Post number faulty", 1, post1.getPostNum(), 0.0001);
        assertEquals("Posts not functioning "
                + "correctly", post1.getX(), post2.getX(), 0.0001);
        assertEquals("Posts not functioning "
                + "correctly", post1.getY(), post2.getY(), 0.0001);
        assertEquals("Posts not functioning "
                + "correctly", false, post1.equals(post2));
        post1.detect(1, 1, 10);
        assertEquals("Post detection faulty", "  Post #1 at (1.0, 2.0):"
                + "\n" + "     (1.0, 1.0)@10.0", post1.getReport());
        assertEquals("Post number faulty", true, post1.isPulsing(3));
        assertEquals("Post number faulty", false, post1.isPulsing(4));
        assertEquals("Post number faulty", true, post1.isPulsing(9));
    }
}
