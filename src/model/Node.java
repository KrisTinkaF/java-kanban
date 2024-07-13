package model;

import java.util.Objects;

public class Node <T> {
    public T data;
    public Node<T> next;
    public Node<T> prev;

    public Node(Node<T> prev, T data, Node<T> next) {
        this.data = data;
        this.next = next;
        this.prev = prev;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Node node = (Node) object;
        return Objects.equals(data, node.data) &&
                Objects.equals(next, node.next) &&
                Objects.equals(prev, node.prev);

    }

    public String toString() {
        return "Node{" +
                "prevNode=" + (prev != null ? prev.data : null) +
                ", node='" + data + '\'' +
                ", nextNode='" + (next != null ? next.data : null) + '\'' +
                "} ";
    }

}
