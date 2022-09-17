package deque;

import edu.princeton.cs.introcs.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

public class MaxArrayDequeTest {
    /*
    @Test
    public void maxTest() {
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<>((x, y) -> x - y);
        Integer maxNum = Integer.MIN_VALUE;
        for (int i = 0; i < 1000; i++) {
            int num = StdRandom.uniform(100000);
            mad.addFirst(num);
            if (num > maxNum) {
                maxNum = num;
            }
        }
        assertEquals(maxNum, mad.max());
    }

    @Test
    public void comparatorTest() {
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<>((x, y) -> x - y);
        Integer minNum = Integer.MAX_VALUE;
        for (int i = 0; i < 1000; i++) {
            int num = StdRandom.uniform(100000);
            mad.addFirst(num);
            if (num < minNum) {
                minNum = num;
            }
        }
        assertEquals(minNum, mad.max((x, y) -> y - x));
    }
     */
}
