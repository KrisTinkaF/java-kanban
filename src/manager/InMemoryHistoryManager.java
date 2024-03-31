package manager;

import model.Task;

import java.util.LinkedList;

public class InMemoryHistoryManager implements HistoryManager{
    private LinkedList<Task> viewsHistory = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (viewsHistory.size() == 10){
            viewsHistory.removeFirst();
        }
        viewsHistory.add(task);
    }

    @Override
    public LinkedList<Task> getHistory() {
        return viewsHistory;
    }
}
