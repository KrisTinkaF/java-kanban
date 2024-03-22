package manager;

import model.Task;

import java.util.LinkedList;

public interface HistoryManager {
    void add(Task task);

    LinkedList<Task> getHistory();
}
