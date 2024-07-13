package manager;

import model.Subtask;
import model.Task;
import model.Type;

import java.util.List;

public interface TaskManager {
    Task createTask(Task task);

    List<Task> getAllTasks(Type type);

    List<Task> getAll();

    void deleteAllTasks(Type type);

    void deleteAll();

    Task getById(int id);

    void deleteById(int id);

    Task updateTask(Task task);

    List<Subtask> getSubtaskByEpic(Task epic);

    HistoryManager getInMemoryHistoryManager();
}
