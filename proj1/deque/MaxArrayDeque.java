package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> c;
    public MaxArrayDeque(Comparator<T> comp) {
        this.c = comp;
    }
    public T max() {
        return max(this.c);
    }
    public T max(Comparator<T> comp) {
        if (isEmpty()) {
            return null;
        }
        T result = get(0);
        for (int i = 0; i < size(); i++) {
            if (comp.compare(get(i), result) > 0) {
                result = get(i);
            }
        }
        return result;
    }
}
