package deque;

import edu.princeton.cs.introcs.StdRandom;
import org.junit.Test;

import static org.junit.Assert.*;


/** Performs some basic linked list tests. */
public class ArrayDequeTest {

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {

        ArrayDeque<String> lld1 = new ArrayDeque<>();

        assertTrue(lld1.isEmpty());
        lld1.addFirst("front");

        // The && operator is the same as "and" in Python.
        // It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse(lld1.isEmpty());

        lld1.addLast("middle");
        assertEquals(2, lld1.size());

        lld1.addLast("back");
        assertEquals(3, lld1.size());
    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        // should be empty
        assertTrue(lld1.isEmpty());

        lld1.addFirst(10);
        // should not be empty
        assertFalse(lld1.isEmpty());

        lld1.removeFirst();
        // should be empty
        assertTrue(lld1.isEmpty());
    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        lld1.addFirst(3);

        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();

        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);
    }

    @Test
    /* Check if you can create LinkedListDeques with different parameterized types*/
    public void multipleParamTest() {

        ArrayDeque<String> lld1 = new ArrayDeque<>();
        ArrayDeque<Double> lld2 = new ArrayDeque<>();
        ArrayDeque<Boolean> lld3 = new ArrayDeque<>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();
    }

    @Test
    /* check if null is return when removing from an empty LinkedListDeque. */
    public void emptyNullReturnTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals(null, lld1.removeFirst());
        assertEquals(null, lld1.removeLast());

    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        for (int i = 0; i < 8; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 4; i++) {
            assertEquals(i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = 7; i >= 4; i--) {
            assertEquals(i, (double) lld1.removeLast(), 0.0);
        }

    }

    @Test
    public void getTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        for (int i = 0; i < 8; i++) {
            lld1.addLast(i);
        }
        for (int i = 0; i < 8; i++) {
            assertEquals(i, (int) lld1.get(i));
        }
    }

    @Test
    public void arrayDequeRandomTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        ArrayDequeSolution<Integer> ads = new ArrayDequeSolution<>();
        String message = "";
        Integer a = null, b = null;

        for (int i = 0; i < 10000; i++) {
            if (ad.size() == 0) {
                double numberBetweenZeroAndOne = StdRandom.uniform();
                int num = StdRandom.uniform(1000);
                if (numberBetweenZeroAndOne < 0.5) {
                    ad.addLast(num);
                    ads.addLast(num);
                    message += "addLast(" + num + ")\n";
                } else {
                    ad.addFirst(num);
                    ads.addFirst(num);
                    message += "addFirst(" + num + ")\n";
                }
            } else {
                int x = StdRandom.uniform(4);
                int num = StdRandom.uniform(1000);
                switch (x) {
                    case 0:
                        ad.addLast(num);
                        ads.addLast(num);
                        message += "addLast(" + num + ")\n";
                        break;
                    case 1:
                        ad.addFirst(num);
                        ads.addFirst(num);
                        message += "addFirst(" + num + ")\n";
                        break;
                    case 2:
                        a = ad.removeFirst();
                        b = ads.removeFirst();
                        message += "removeFirst()\n";
                        break;
                    case 3:
                        a = ad.removeLast();
                        b = ads.removeLast();
                        message += "removeLast()\n";
                        break;
                    default:
                }
            }
            assertEquals(message, b, a);
        }
    }
}
