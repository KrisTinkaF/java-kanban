package manager;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TaskManager {
    private static int counter = 0;

    private HashMap<Integer, Task> tasks = new HashMap<>();

    public Task createTask(Task task) {
        counter++;
        task.setId(counter);
        tasks.put(task.getId(), task);
        return task;
    }

    public ArrayList<Task> getAllTasks(Type type){
        ArrayList<Task> allTasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getType() == type) {
                allTasks.add(task);
            }
        }
        return allTasks;
    }

    public void deleteAllTasks(Type type){
        ArrayList<Integer> allTasksId = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getType() == type) {
                allTasksId.add(task.getId());
                if (type == Type.SUBTASK) {
                    Epic parent = (Epic) tasks.get(((Subtask)task).getParentId());
                    if (parent != null) {
                        updateEpicStatus(parent);
                    }
                }
            }
        }
        for (int taskId : allTasksId) {
            tasks.remove(taskId);
        }
        System.out.println("Все задачи с типом "+ type +" удалены. Сейчас в коллекции: " + tasks.values());
    }

    public Task getById(int id) {
        return tasks.get(id);
    }

    public void deleteById(int id) {
        if ((tasks.get(id)).getType() == Type.SUBTASK) {
            int parentId = ((Subtask)(tasks.get(id))).getParentId();
            Epic parent = (Epic) tasks.get(parentId);
            updateEpicStatus(parent);
        }
        tasks.remove(id);
        System.out.println("Удалена задача с id "+id+ ". Сейчас в коллекции: " + tasks.values());
    }

    public Task updateTask (Task task) {
        tasks.put(task.getId(), task);
        return task;
    }

    public Subtask updateSubtask (Subtask subtask) {
        Subtask oldSubtask = (Subtask) getById(subtask.getId());
        Epic parent = (Epic) tasks.get(subtask.getParentId());
        if (oldSubtask.getStatus() != subtask.getStatus()) {
            tasks.put(subtask.getId(), subtask);
            updateEpicStatus(parent);
        } else {
            tasks.put(subtask.getId(), subtask);
        }
        return subtask;
    }

    public Epic updateEpic(Epic epic) {
        Epic oldEpic = (Epic) getById(epic.getId());
        if (oldEpic.getStatus() != epic.getStatus()) {
            System.out.println("Статус задачи типа Epic не может быть изменен пользователем");
            epic.setStatus(oldEpic.getStatus());
        }
        tasks.put(epic.getId(), epic);
        return epic;
    }

    public void updateEpicStatus (Epic epic) {
        ArrayList<Subtask> allSubtasksByEpic = getSubtaskByEpic(epic);
        if (!allSubtasksByEpic.isEmpty()) {
            HashSet<Status> subtaskStatuses = new HashSet<>();
            for (Subtask sub : allSubtasksByEpic) {
                subtaskStatuses.add(sub.getStatus());
            }
            if (subtaskStatuses.size() > 1) {
                epic.setStatus(Status.IN_PROGRESS);
            } else {
                epic.setStatus((allSubtasksByEpic.get(0)).getStatus());
            }
        } else {
            epic.setStatus(Status.NEW);
        }
        tasks.put(epic.getId(), epic);
    }

    public ArrayList<Subtask> getSubtaskByEpic (Epic epic) {
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getType() == Type.SUBTASK) {
                if (((Subtask)task).getParentId() == epic.getId()) {
                    epicSubtasks.add((Subtask)task);
                }
            }
        }
        return epicSubtasks;
    }

    boolean areAllSubtaskIsDone(ArrayList<Subtask> allSubtasksByEpic) {
        for (Subtask sub : allSubtasksByEpic) {
            if (sub.getStatus() != Status.DONE) {
                return false;
            }
        }
        return true;
    }
    boolean areAllSubtaskIsNew(ArrayList<Subtask> allSubtasksByEpic) {
        for (Subtask sub : allSubtasksByEpic) {
            if (sub.getStatus() != Status.NEW) {
                return false;
            }
        }
        return true;
    }
}
