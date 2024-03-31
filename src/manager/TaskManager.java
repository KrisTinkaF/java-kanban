package manager;

import model.Subtask;
import model.Task;
import model.Type;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    Task createTask(Task task);

    List<Task> getAllTasks(Type type);

    void deleteAllTasks(Type type);

    Task getById(int id);

    void deleteById(int id);

    Task updateTask (Task task);

    //Subtask updateSubtask(Subtask subtask);

    //Epic updateEpic(Epic epic);

    List<Subtask> getSubtaskByEpic(Task epic);

    HistoryManager getInMemoryHistoryManager();
}
