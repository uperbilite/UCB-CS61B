package deque;

import java.util.Iterator;

public class ArrayListDeque<T> {
    public class Node<T> {
        T item;
        public ArrayListDeque<T>.Node<T> next;
        public ArrayListDeque<T>.Node<T> prev;

        public Node(T item, ArrayListDeque<T>.Node<T> prev, ArrayListDeque<T>.Node<T> next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }

    private Node<T> sentinal;
    private int size;

    public ArrayListDeque() {
        this.sentinal = new Node<>(null, null, null);
        this.size = 0;
    }

    public void addFirst(T item) {
        Node<T> firstNode = this.sentinal.next;
        Node<T> newNode = new Node<>(item, null, null);
        this.sentinal.next = newNode;
        if (firstNode != null) {
            Node<T> lastNode = firstNode.prev;
            newNode.prev = lastNode;
            newNode.next = firstNode;
            lastNode.next = firstNode.prev = newNode;
        } else {
            newNode.prev = newNode.next = newNode;
        }
        this.size += 1;
    }

    public void addLast(T item) {
        Node<T> firstNode = this.sentinal.next;
        Node<T> newNode = new Node<>(item, null, null);
        if (firstNode != null) {
            Node<T> lastNode = firstNode.prev;
            newNode.prev = lastNode;
            newNode.next = firstNode;
            lastNode.next = firstNode.prev = newNode;
        } else {
            this.sentinal.next = newNode;
            newNode.prev = newNode.next = newNode;
        }
        this.size += 1;
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    public int size() {
        return this.size;
    }

    public void printDeque() {
        if (this.isEmpty())
            return;
        Node<T> current = this.sentinal.next;
        for (int i = 0; i < this.size(); i++) {
            System.out.print(current.item + " ");
            current = current.next;
        }
        System.out.println();
    }

    public T removeFirst() {
        Node<T> firstNode = this.sentinal.next;
        if (firstNode == null)
            return null;
        if (firstNode.prev == firstNode && firstNode.next == firstNode) {
            this.sentinal.next = null;
            this.size -= 1;
            return firstNode.item;
        }
        Node<T> lastNode = firstNode.prev;
        Node<T> newFirstNode = firstNode.next;
        this.sentinal.next = newFirstNode;
        newFirstNode.prev = lastNode;
        lastNode.next = newFirstNode;
        this.size -= 1;
        return firstNode.item;
    }

    public T removeLast() {
        Node<T> firstNode = this.sentinal.next;
        if (firstNode == null)
            return null;
        if (firstNode.prev == firstNode && firstNode.next == firstNode) {
            this.sentinal.next = null;
            this.size -= 1;
            return firstNode.item;
        }
        Node<T> lastNode = firstNode.prev;
        Node<T> newLastNode = lastNode.prev;
        newLastNode.next = firstNode;
        firstNode.prev = newLastNode;
        this.size -= 1;
        return lastNode.item;
    }

    public T get(int index) {
        Node<T> current = this.sentinal.next;
        if (current == null)
            return null;
        if (current.next == current && current.prev == current && index != 0)
            return null;
        while (index != 0) {
            current = current.next;
            index -= 1;
            if (current.next == this.sentinal.next && index != 0)
                return null;
        }
        return current.item;
    }

    public T getRecursive(int index) {
        Node<T> current = this.sentinal.next;
        if (current == null)
            return null;
        if (current.next == current && current.prev == current && index != 0)
            return null;
        return getRecusiveHelper(index, current);
    }

    public T getRecusiveHelper(int index, Node<T> current) {
        if (current.next == this.sentinal.next && index != 0)
            return null;
        if (index == 0)
            return current.item;
        return getRecusiveHelper(index - 1, current.next);
    }

    public Iterator<T> iterator() {
        return null;
    }

    public boolean equals(Object o) {
        return false;
    }
}
