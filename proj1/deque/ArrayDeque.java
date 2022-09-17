package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] array;
    private int front;
    private int end;
    private int size;

    public ArrayDeque() {
        this.array = (T[]) new Object[8];
        this.front = 0;
        this.end = 0;
        this.size = 0;
    }

    private int next(int index) {
        return (index + 1) % array.length;
    }

    private int prev(int index) {
        if (index == 0) {
            return array.length - 1;
        }
        return index - 1;
    }

    private void resizeArray(int newSize) {
        T[] newArray = (T[]) new Object[newSize];
        int index = front;
        int newFront = 0;
        for (int i = 0; i < size(); i++) {
            newArray[newFront] = array[index];
            newFront += 1;
            index = next(index);
        }
        array = newArray;
        front = 0;
        end = newFront - 1;
    }
    @Override
    public void addFirst(T item) {
        if (size() > array.length * 0.75) {
            int newSize = array.length * 2;
            resizeArray(newSize);
        }
        if (isEmpty()) {
            array[front] = item;
            size += 1;
            return;
        }
        int index = prev(front);
        array[index] = item;
        front = index;
        size += 1;
    }

    @Override
    public void addLast(T item) {
        if (size() > array.length * 0.75) {
            int newSize = array.length * 2;
            resizeArray(newSize);
        }
        if (isEmpty()) {
            array[end] = item;
            size += 1;
            return;
        }
        int index = next(end);
        array[index] = item;
        end = index;
        size += 1;
    }

    @Override
    public int size() {
        return this.size;
    }
    @Override
    public void printDeque() {
        if (isEmpty()) {
            return;
        }
        int index = front;
        for (int i = 0; i < size(); i++) {
            System.out.print(array[index] + " ");
            index = next(index);
        }
        System.out.println();
    }
    @Override
    public T removeFirst() {
        if (size() < array.length * 0.25) {
            int newSize = (int) (array.length * 0.5);
            if (newSize < 8) {
                newSize = 8;
            }
            resizeArray(newSize);
        }
        if (isEmpty()) {
            return null;
        }
        if (this.size() == 1) {
            this.size -= 1;
            return this.array[front];
        }
        int newFront = next(front);
        T result = array[front];
        front = newFront;
        size -= 1;
        return result;
    }
    @Override
    public T removeLast() {
        if (size() < array.length * 0.25) {
            int newSize = (int) (array.length * 0.5);
            if (newSize < 8) {
                newSize = 8;
            }
            resizeArray(newSize);
        }
        if (isEmpty()) {
            return null;
        }
        if (size() == 1) {
            size -= 1;
            return array[front];
        }
        int newEnd = prev(end);
        T result = this.array[end];
        end = newEnd;
        size -= 1;
        return result;
    }
    @Override
    public T get(int index) {
        if (index >= size()) {
            return null;
        }
        int current = front;
        for (int i = 0; i < index; i++) {
            current = next(current);
        }
        return array[current];
    }
    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        ArrayDeque<T> other = (ArrayDeque<T>) o;
        if (this.size() != other.size()) {
            return false;
        }
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i) != other.get(i)) {
                return false;
            }
        }
        return true;
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private T[] array;
        private int front;
        private int end;
        private int current;


        ArrayDequeIterator() {
            this.array = ArrayDeque.this.array;
            this.front = ArrayDeque.this.front;
            this.end = ArrayDeque.this.end;
            this.current = this.front;
        }
        @Override
        public boolean hasNext() {
            return ArrayDeque.this.next(current) == ArrayDeque.this.next(end);
        }

        @Override
        public T next() {
            T item = array[current];
            current = ArrayDeque.this.next(current);
            return item;
        }
    }
}
