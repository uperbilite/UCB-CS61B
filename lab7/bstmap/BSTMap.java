package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private int size = 0;

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        assert key != null;
        return getHelper(key, root) != null;
    }

    /**
     * return the value of key in BSTMap, if there is no key, return null.
     */
    @Override
    public V get(K key) {
        assert key != null;
        if (getHelper(key, root) == null) {
            return null;
        }
        return getHelper(key, root).value;
    }

    /**
     * return the BSTNode which has the key.
     * if the key doesn't exist, return null.
     */
    private BSTNode getHelper(K key, BSTNode root) {
        if (root == null) {
            return null;
        }
        else if (root.key.equals(key)) {
            return root;
        }
        else if (root.key.compareTo(key) > 0) {
            return getHelper(key, root.left);
        }
        else {
            return getHelper(key, root.right);
        }
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void put(K key, V value) {
        if (root == null) {
            root = new BSTNode(key, value, null, null);
            size += 1;
            return;
        }
        BSTNode putPosition = putHelper(key, root);
        if (putPosition == null) {
            return;
        }
        if (putPosition.key.compareTo(key) > 0) {
            assert putPosition.left == null;
            putPosition.left = new BSTNode(key, value, null, null);
        } else {
            assert putPosition.right == null;
            putPosition.right = new BSTNode(key, value, null, null);
        }
        size += 1;
    }

    /**
     * return null if key is existed in BSTMap.
     * Otherwise, return parent BSTNode of the put position.
     */
    private BSTNode putHelper(K key, BSTNode root) {
        if (root.key.compareTo(key) > 0) {
            if (root.left != null) {
                return putHelper(key, root.left);
            } else {
                return root;
            }
        }
        if (root.key.compareTo(key) < 0) {
            if (root.right != null) {
                return putHelper(key, root.right);
            } else {
                return root;
            }
        }
        assert root.key.equals(key);
        return null;
    }

    public void printInOrder() {
        throw new UnsupportedOperationException();
    }

    private BSTNode root;

    private class BSTNode {
        K key;
        V value;
        BSTNode left;
        BSTNode right;

        BSTNode(K key, V value, BSTNode left, BSTNode right) {
            this.key = key;
            this.value = value;
            this.left = left;
            this.right = right;
        }
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }
}
