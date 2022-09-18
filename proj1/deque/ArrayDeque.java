package deque;

import net.sf.saxon.om.Item;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] items;
    private int nextFirst;
    private int nextLast;
    private int size;
    private int capacity;
    public ArrayDeque() {
        this.items = (T[]) new Object[8];
        this.nextFirst = 3;
        this.nextLast = 4;
        this.size = 0;
        this.capacity = 8;
    }
    private int getNext(int index) {
        return (index + 1) % capacity;
    }
    private int getPrev(int index) {
        return index == 0 ? capacity - 1 : index - 1;
    }
    private void resize(int newCapacity) {
        T[] a = (T[]) new Object[newCapacity];
        int index = getNext(nextFirst);
        for (int i = 0; i < size(); i++) {
            a[i] = items[index];
            index = getNext(index);
        }
        items = a;
        capacity = newCapacity;
        nextFirst = capacity - 1;
        nextLast = size;
    }
    @Override
    public void addFirst(T item) {
        if (size > capacity * 0.75) {
            resize(capacity * 2);
        }
        items[nextFirst] = item;
        nextFirst = getPrev(nextFirst);
        size += 1;
    }
    @Override
    public void addLast(T item) {
        if (size > capacity * 0.75) {
            resize(capacity * 2);
        }
        items[nextLast] = item;
        nextLast = getNext(nextLast);
        size += 1;
    }
    @Override
    public int size() {
        return this.size;
    }
    @Override
    public void printDeque() {
        int index = getNext(nextFirst);
        for (int i = 0; i < size(); i++) {
            System.out.print(items[index]);
            index = getNext(index);
        }
        System.out.println();
    }
    private void removeCheck() {
        if (size < capacity * 0.25) {
            int newCapacity = capacity / 2;
            newCapacity = Math.max(newCapacity, 8);
            resize(newCapacity);
        }
    }
    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        removeCheck();
        T result = items[getNext(nextFirst)];
        nextFirst = getNext(nextFirst);
        size -= 1;
        return result;
    }
    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        removeCheck();
        T result = items[getPrev(nextLast)];
        nextLast = getPrev(nextLast);
        size -= 1;
        return result;
    }
    @Override
    public T get(int index) {
        if (isEmpty() || index >= size()) {
            return null;
        }
        return items[(getNext(nextFirst) + index) % capacity];
    }
    private class ArrayDequeIterator implements Iterator<T> {
        private int index;
        ArrayDequeIterator() {
            this.index = 0;
        }
        @Override
        public boolean hasNext() {
            return index + 1 == size();
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
        if (!(o instanceof ArrayDeque)) {
            return false;
        }
        ArrayDeque other = (ArrayDeque) o;
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
