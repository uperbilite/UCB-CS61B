package deque;

import edu.princeton.cs.introcs.StdRandom;
import org.junit.Test;

import static org.junit.Assert.*;


/** Performs some basic linked list tests. */
public class LinkedListDequeTest {

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {

        LinkedListDeque<String> lld1 = new LinkedListDeque<String>();

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

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
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

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
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

        LinkedListDeque<String> lld1 = new LinkedListDeque<String>();
        LinkedListDeque<Double> lld2 = new LinkedListDeque<Double>();
        LinkedListDeque<Boolean> lld3 = new LinkedListDeque<Boolean>();

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

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals(null, lld1.removeFirst());
        assertEquals(null, lld1.removeLast());

    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals(i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals(i, (double) lld1.removeLast(), 0.0);
        }

    }

    @Test
    public void getTest() {
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
        for (int i = 0; i < 5000; i++) {
            lld1.addLast(i);
        }
        for (int i = 0; i < 5000; i++) {
            assertEquals(i, (int) lld1.get(i));
        }
    }

    @Test
    public void getRecursiveTest() {
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
        for (int i = 0; i < 5000; i++) {
            lld1.addLast(i);
        }
        for (int i = 0; i < 5000; i++) {
            assertEquals(i, (int) lld1.getRecursive(i));
        }
    }

    @Test
    public void linkedListDequeRandomTest() {
        LinkedListDeque<Integer> lld = new LinkedListDeque<>();
        ArrayDequeSolution<Integer> ads = new ArrayDequeSolution<>();
        String message = "";
        Integer a = null, b = null;

        for (int i = 0; i < 10000; i++) {
            if (lld.size() == 0) {
                double numberBetweenZeroAndOne = StdRandom.uniform();
                int num = StdRandom.uniform(1000);
                if (numberBetweenZeroAndOne < 0.5) {
                    lld.addLast(num);
                    ads.addLast(num);
                    message += "addLast(" + num + ")\n";
                } else {
                    lld.addFirst(num);
                    ads.addFirst(num);
                    message += "addFirst(" + num + ")\n";
                }
            } else {
                int x = StdRandom.uniform(4);
                int num = StdRandom.uniform(1000);
                switch (x) {
                    case 0:
                        lld.addLast(num);
                        ads.addLast(num);
                        message += "addLast(" + num + ")\n";
                        break;
                    case 1:
                        lld.addFirst(num);
                        ads.addFirst(num);
                        message += "addFirst(" + num + ")\n";
                        break;
                    case 2:
                        a = lld.removeFirst();
                        b = ads.removeFirst();
                        message += "removeFirst()\n";
                        break;
                    case 3:
                        a = lld.removeLast();
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
