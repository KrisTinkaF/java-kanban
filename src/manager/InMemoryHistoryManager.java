package manager;

import model.Node;
import model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager{
    private Node<Task> head;
    private Node<Task> tail;
    private final Map<Integer, Node<Task>> viewsHistory = new HashMap<>();


    @Override
    public void add(Task task) {
        int taskId = task.getId();
        if (viewsHistory.containsKey(taskId)) {
            removeNode(viewsHistory.get(taskId));
            System.out.println("Удалена из истории таска "+taskId);
        }
            linkLast(task);
            viewsHistory.put(taskId, tail);

    }

    private void removeNode(Node<Task> node) {

        if (node == null) {
            return;
        }
        if (head == null) {
            return;
        }
        if (head.equals(node)) {
            head = node.next;
            if (head == null) {
                return;
            }
            if (head.prev != null) {
                head.prev = null;
            }
            return;
        }
        if (tail.equals(node)) {
            tail = node.prev;
            if (tail.next != null) {
                tail.next = null;
            }
            return;
        }

        if (node.prev != null) {
            node.prev.next = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        }

    }

    @Override
    public void remove(int id) {
        removeNode(viewsHistory.get(id));
        viewsHistory.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private ArrayList<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        if (head != null) {
            Node<Task> currentNode = head;
            tasks.add(currentNode.data);
            while (currentNode.next != null) {
                currentNode = currentNode.next;
                tasks.add(currentNode.data);
            }

        }
        return tasks;
    }

    private void linkLast(Task task) {
        final Node<Task> oldTail = tail;
        final Node<Task> newNode = new Node<>(null, task, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
            tail.prev = oldTail;
        }


    }


}

