package manager;

import model.Subtask;
import model.Task;
import model.Type;

import java.util.ArrayList;

public interface TaskManager {
    Task createTask(Task task);

    ArrayList<Task> getAllTasks(Type type);

    void deleteAllTasks(Type type);

    Task getById(int id);

    void deleteById(int id);

    Task updateTask (Task task);

    //Subtask updateSubtask(Subtask subtask);

    //Epic updateEpic(Epic epic);

    ArrayList<Subtask> getSubtaskByEpic(Task epic);

}
