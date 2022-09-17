package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>{
    public class Node {
        private T item;
        private Node next;
        private Node prev;

        public Node(T item, Node prev, Node next) {
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
    @Override
    public void addFirst(T item) {
        Node firstNode = this.sentinal.next;
        Node newNode = new Node(item, null, null);
        this.sentinal.next = newNode;
        if (firstNode != null) {
            Node lastNode = firstNode.prev;
            newNode.prev = lastNode;
            newNode.next = firstNode;
            lastNode.next = firstNode.prev = newNode;
        } else {
            newNode.prev = newNode.next = newNode;
        }
        this.size += 1;
    }
    @Override
    public void addLast(T item) {
        Node firstNode = this.sentinal.next;
        Node newNode = new Node(item, null, null);
        if (firstNode != null) {
            Node lastNode = firstNode.prev;
            newNode.prev = lastNode;
            newNode.next = firstNode;
            lastNode.next = firstNode.prev = newNode;
        } else {
            this.sentinal.next = newNode;
            newNode.prev = newNode.next = newNode;
        }
        this.size += 1;
    }

    @Override
    public int size() {
        return this.size;
    }
    @Override
    public void printDeque() {
        if (this.isEmpty()) {
            return;
        }
        Node current = this.sentinal.next;
        for (int i = 0; i < this.size(); i++) {
            System.out.print(current.item + " ");
            current = current.next;
        }
        System.out.println();
    }
    @Override
    public T removeFirst() {
        Node firstNode = this.sentinal.next;
        if (firstNode == null) {
            return null;
        }
        if (firstNode.prev == firstNode && firstNode.next == firstNode) {
            this.sentinal.next = null;
            this.size -= 1;
            return firstNode.item;
        }
        Node lastNode = firstNode.prev;
        Node newFirstNode = firstNode.next;
        this.sentinal.next = newFirstNode;
        newFirstNode.prev = lastNode;
        lastNode.next = newFirstNode;
        this.size -= 1;
        return firstNode.item;
    }
    @Override
    public T removeLast() {
        Node firstNode = this.sentinal.next;
        if (firstNode == null) {
            return null;
        }
        if (firstNode.prev == firstNode && firstNode.next == firstNode) {
            this.sentinal.next = null;
            this.size -= 1;
            return firstNode.item;
        }
        Node lastNode = firstNode.prev;
        Node newLastNode = lastNode.prev;
        newLastNode.next = firstNode;
        firstNode.prev = newLastNode;
        this.size -= 1;
        return lastNode.item;
    }
    @Override
    public T get(int index) {
        Node current = this.sentinal.next;
        if (current == null) {
            return null;
        }
        if (current.next == current && current.prev == current && index != 0) {
            return null;
        }
        while (index != 0) {
            current = current.next;
            index -= 1;
            if (current.next == this.sentinal.next && index != 0) {
                return null;
            }
        }
        return current.item;
    }

    public T getRecursive(int index) {
        Node current = this.sentinal.next;
        if (current == null) {
            return null;
        }
        if (current.next == current && current.prev == current && index != 0) {
            return null;
        }
        return getRecursiveHelper(index, current);
    }

    public T getRecursiveHelper(int index, Node current) {
        if (current.next == this.sentinal.next && index != 0) {
            return null;
        }
        if (index == 0) {
            return current.item;
        }
        return getRecursiveHelper(index - 1, current.next);
    }

    public Iterator<T> iterator() {
        return null;
    }

    public boolean equals(Object o) {
        return false;
    }
}
