package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> c;

    public MaxArrayDeque(Comparator<T> c) {
        this.c = c;
    }

    public T max() {
        if (size() == 0) {
            return null;
        }
        T result = array[front];
        int index = front;
        for (int i = 0; i < size(); i++) {
            if (c.compare(array[index], result) > 0) {
                result = array[index];
            }
            index = next(index);
        }
        return result;
    }

    public T max(Comparator<T> comp) {
        if (size() == 0) {
            return null;
        }
        T result = array[front];
        int index = front;
        for (int i = 0; i < size(); i++) {
            if (comp.compare(array[index], result) > 0) {
                result = array[index];
            }
            index = next(index);
        }
        return result;
    }
}
