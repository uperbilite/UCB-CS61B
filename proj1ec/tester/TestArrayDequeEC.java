package tester;

import static org.junit.Assert.*;
import edu.princeton.cs.introcs.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {
    @Test
    public void arrayDequeRandomTest() {
        StudentArrayDeque<Integer> sad = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> ads = new ArrayDequeSolution<>();
        String message = "";
        Integer a = null, b = null;

        for (int i = 0; i < 1000; i++) {
            if (sad.size() == 0) {
                double numberBetweenZeroAndOne = StdRandom.uniform();
                int num = StdRandom.uniform(1000);
                if (numberBetweenZeroAndOne < 0.5) {
                    sad.addLast(num);
                    ads.addLast(num);
                    message += "addLast(" + num + ")\n";
                } else {
                    sad.addFirst(num);
                    ads.addFirst(num);
                    message += "addFirst(" + num + ")\n";
                }
            } else {
                int x = StdRandom.uniform(4);
                int num = StdRandom.uniform(1000);
                switch (x) {
                    case 0:
                        sad.addLast(num);
                        ads.addLast(num);
                        message += "addLast(" + num + ")\n";
                        break;
                    case 1:
                        sad.addFirst(num);
                        ads.addFirst(num);
                        message += "addFirst(" + num + ")\n";
                        break;
                    case 2:
                        a = sad.removeFirst();
                        b = ads.removeFirst();
                        message += "removeFirst()\n";
                        break;
                    case 3:
                        a = sad.removeLast();
                        b = ads.removeLast();
                        message += "removeLast()\n";
                        break;
                    default:
                }
            }
            assertEquals(message, a, b);
        }

        for (int i = 0; i < 8; i++) {
            double numberBetweenZeroAndOne = StdRandom.uniform();

            if (numberBetweenZeroAndOne < 0.5) {
                assertEquals(message, sad.removeLast(), ads.removeLast());
            } else {
                assertEquals(message, sad.removeFirst(), ads.removeFirst());
            }
        }
    }
}
