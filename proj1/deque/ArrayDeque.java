package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] items;
    private int nextFirst;
    private int nextLast;
    private int size;
    public ArrayDeque() {
        this.items = (T[]) new Object[8];
        this.nextFirst = 3;
        this.nextLast = 4;
        this.size = 0;
    }
    @Override
    public void addFirst(T item) {
        return;
    }
    @Override
    public void addLast(T item) {
        return;
    }
    @Override
    public int size() {
        return this.size;
    }
    @Override
    public void printDeque() {
        return;
    }
    @Override
    public T removeFirst() {
        return null;
    }
    @Override
    public T removeLast() {
        return null;
    }
    @Override
    public T get(int index) {
        return null;
    }
    private class ArrayDequeIterator implements Iterator<T> {
        @Override
        public boolean hasNext() {
            return false;
        }
        @Override
        public T next() {
            return null;
        }
    }
    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }
    @Override
    public boolean equals(Object o) {
        return false;
    }
}
