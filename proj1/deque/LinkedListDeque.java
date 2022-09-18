package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private class Node {
        T item;
        Node next;
        Node prev;
        Node(T item, Node prev, Node next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }

    private Node sentinal;
    private int size;
    public LinkedListDeque() {
        this.sentinal = new Node(null, null, null);
        this.size = 0;
    }
    private void addEmpty(T item) {
        sentinal.next = new Node(item, null, null);
        sentinal.next.prev = sentinal.next.next = sentinal.next;
        size += 1;
    }
    @Override
    public void addFirst(T item) {
        Node first = sentinal.next;
        if (first == null) {
            addEmpty(item);
            return;
        }
        sentinal.next = new Node(item, first.prev, first);
        first.prev = first.prev.next = sentinal.next;
        size += 1;
    }
    @Override
    public void addLast(T item) {
        Node first = sentinal.next;
        if (first == null) {
            addEmpty(item);
            return;
        }
        first.prev = first.prev.next = new Node(item, first.prev, first);
        size += 1;
    }
    @Override
    public int size() {
        return this.size;
    }
    @Override
    public void printDeque() {
        Node first = sentinal.next;
        if (first != null) {
            do {
                System.out.println(first.item + " ");
                first = first.next;
            } while (first != sentinal.next);
            System.out.println();
        }
    }
    private T removeOne() {
        T result = sentinal.next.item;
        sentinal.next = null;
        size -= 1;
        return result;
    }
    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        if (size() == 1) {
            return removeOne();
        }
        Node first = sentinal.next;
        sentinal.next = first.next;
        first.next.prev = first.prev;
        first.prev.next = first.next;
        size -= 1;
        return first.item;
    }
    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        if (size() == 1) {
            return removeOne();
        }
        Node last = sentinal.next.prev;
        last.next.prev = last.prev;
        last.prev.next = last.next;
        size -= 1;
        return last.item;
    }
    @Override
    public T get(int index) {
        Node first = sentinal.next;
        if (size() == 0) {
            return null;
        }
        do {
            if (index == 0) {
                return first.item;
            }
            first = first.next;
            index -= 1;
        } while (first != sentinal.next);
        return null;
    }
    private T recursiveHelper(int index, Node current) {
        if (index == 0) {
            return current.item;
        }
        if (current.next != sentinal.next) {
            return recursiveHelper(index - 1, current.next);
        } else {
            return null;
        }
    }
    public T getRecursive(int index) {
        Node first = sentinal.next;
        if (size() == 0) {
            return null;
        }
        return recursiveHelper(index, first);
    }
    private class LinkedListDequeIterator implements Iterator<T> {
        private int index;
        LinkedListDequeIterator() {
            this.index = 0;
        }
        @Override
        public boolean hasNext() {
            return index < size();
        }
        @Override
        public T next() {
            T result = get(index);
            index += 1;
            return result;
        }
    }
    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof Deque)) {
            return false;
        }
        Deque other = (Deque) o;
        if (this.size() != other.size()) {
            return false;
        }
        for (int i = 0; i < this.size(); i++) {
            if (!(this.get(i).equals(other.get(i)))) {
                return false;
            }
        }
        return true;
    }
}
