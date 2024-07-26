package manager;

import model.CrossTimeException;
import model.Subtask;
import model.Task;
import model.Type;

import java.util.List;
import java.util.Set;

public interface TaskManager {
    Task createTask(Task task) throws CrossTimeException;

    List<Task> getAllTasks(Type type);

    List<Task> getAll();

    void deleteAllTasks(Type type);

    void deleteAll();

    Task getById(int id);

    void deleteById(int id);

    Task updateTask(Task task) throws CrossTimeException;

    List<Subtask> getSubtaskByEpic(Task epic);

    HistoryManager getInMemoryHistoryManager();

    Set<Task> getPrioritizedTasks();

    boolean crossTime(Task newTask);

}
