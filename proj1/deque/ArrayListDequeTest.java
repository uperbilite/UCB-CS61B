package deque;

import org.junit.Test;

import static org.junit.Assert.*;


/** Performs some basic linked list tests. */
public class ArrayListDequeTest {

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {

        ArrayListDeque<String> lld1 = new ArrayListDeque<>();

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

		System.out.println("Printing out deque: ");
		lld1.printDeque();
    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {

        ArrayListDeque<Integer> lld1 = new ArrayListDeque<>();
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

        ArrayListDeque<Integer> lld1 = new ArrayListDeque<>();
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

        ArrayListDeque<String> lld1 = new ArrayListDeque<>();
        ArrayListDeque<Double> lld2 = new ArrayListDeque<>();
        ArrayListDeque<Boolean> lld3 = new ArrayListDeque<>();

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

        ArrayListDeque<Integer> lld1 = new ArrayListDeque<>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals(null, lld1.removeFirst());
        assertEquals(null, lld1.removeLast());

    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {

        ArrayListDeque<Integer> lld1 = new ArrayListDeque<>();
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
        ArrayListDeque<Integer> lld1 = new ArrayListDeque<>();
        for (int i = 0; i < 8; i++) {
            lld1.addLast(i);
        }
        for (int i = 0; i < 8; i++) {
            assertEquals(i, (int) lld1.get(i));
        }
    }
}
