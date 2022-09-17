package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> c;
    public MaxArrayDeque(Comparator<T> c) {
        this.c = c;
    }
    public T max() {
        return null;
    }
    public T max(Comparator<T> c) {
        return null;
    }
}
