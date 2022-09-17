package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private class Node {
        public T item;
        public Node next;
        public Node prev;

        public Node(T item) {
            this.item = item;
            this.next = null;
            this.prev = null;
        }
    }

    private Node sentinal;
    private int size;
    public LinkedListDeque() {
        this.sentinal = new Node(null);
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
    public T getRecursive(int index) {
        return null;
    }
    private class LinkedListDequeIterator implements Iterator<T> {
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
        return new LinkedListDequeIterator();
    }
    @Override
    public boolean equals(Object o) {
        return false;
    }
}