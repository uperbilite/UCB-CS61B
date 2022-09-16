package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>{
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

    @Override
    public void addFirst(T item) {
        if (size == 0) {
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
        if (size == 0) {
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
    public boolean isEmpty() {
        return size() == 0;
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
        if (size() == 0) {
            return null;
        }
        if (size() == 1) {
            size -= 1;
            return array[front];
        }
        int newFront = next(front);
        T result = array[front];
        front = newFront;
        size -= 1;
        return result;
    }
    @Override
    public T removeLast() {
        if (size() == 0) {
            return null;
        }
        if (size() == 1) {
            size -= 1;
            return array[front];
        }
        int newEnd = prev(end);
        T result = array[end];
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

    public Iterator<T> iterator() {
        return null;
    }

    public boolean equals(Object o) {
        return false;
    }
}
