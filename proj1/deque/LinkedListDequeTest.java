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
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
        ArrayDeque<Integer> lld2 = new ArrayDeque<>();
        ArrayDequeSolution<Integer> ads = new ArrayDequeSolution<>();
        String message = "";
        Integer a = null, b = null;
        int index = 0;

        for (int i = 0; i < 10000; i++) {
            if (lld1.size() == 0) {
                double numberBetweenZeroAndOne = StdRandom.uniform();
                int item = StdRandom.uniform(1000);
                if (numberBetweenZeroAndOne < 0.5) {
                    lld1.addLast(item);
                    lld2.addLast(item);
                    ads.addLast(item);
                    message += "addLast(" + item + ")\n";
                } else {
                    lld1.addFirst(item);
                    lld2.addFirst(item);
                    ads.addFirst(item);
                    message += "addFirst(" + item + ")\n";
                }
            } else {
                int x = StdRandom.uniform(8);
                int item = StdRandom.uniform(1000);
                switch (x) {
                    case 0:
                        lld1.addLast(item);
                        lld2.addLast(item);
                        ads.addLast(item);
                        message += "addLast(" + item + ")\n";
                        break;
                    case 1:
                        lld1.addFirst(item);
                        lld2.addFirst(item);
                        ads.addFirst(item);
                        message += "addFirst(" + item + ")\n";
                        break;
                    case 2:
                        a = lld1.removeFirst();
                        lld2.removeFirst();
                        b = ads.removeFirst();
                        message += "removeFirst()\n";
                        break;
                    case 3:
                        a = lld1.removeLast();
                        lld2.removeLast();
                        b = ads.removeLast();
                        message += "removeLast()\n";
                        break;
                    case 4:
                        assertEquals(ads.size(), lld1.size());
                        index = StdRandom.uniform(ads.size());
                        a = lld1.get(index);
                        b = ads.get(index);
                        break;
                    case 5:
                        assertEquals(ads.size(), lld1.size());
                        index = StdRandom.uniform(ads.size());
                        a = lld1.getRecursive(index);
                        b = ads.get(index);
                        break;
                    case 6:
                        assertTrue(message, lld1.equals(lld2));
                        break;
                    case 7:
                        index = 0;
                        for (var e : lld1) {
                            assertEquals(e, ads.get(index));
                            index += 1;
                        }
                        break;
                    default:
                }
            }
            assertEquals(message, b, a);
        }
    }
}
