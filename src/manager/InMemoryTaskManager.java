package manager;

import model.*;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    public InMemoryTaskManager (HistoryManager inMemoryHistoryManager) {
        InMemoryTaskManager.inMemoryHistoryManager = inMemoryHistoryManager;
    }

    public HistoryManager getInMemoryHistoryManager() {
        return inMemoryHistoryManager;
    }

    private static HistoryManager inMemoryHistoryManager;

    private static int counter = 0;

    private final Map<Integer, Task> tasks = new HashMap<>();

    @Override
    public Task createTask(Task task) {
        counter++;
        task.setId(counter);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public List<Task> getAllTasks(Type type){
        List<Task> allTasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getType() == type) {
                allTasks.add(task);
            }
        }
        return allTasks;
    }

    @Override
    public void deleteAllTasks(Type type){
        List<Integer> allTasksId = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getType() == type) {
                allTasksId.add(task.getId());
                if (type == Type.SUBTASK) {
                    Epic parent = (Epic) tasks.get(((Subtask)task).getParent());
                    if (parent != null) {
                        updateEpicStatus(parent);
                    }
                } else if (type == Type.EPIC) {
                    Epic epic = (Epic) task;
                    List<Subtask> subtasks = getSubtaskByEpic(epic);
                    for (Subtask subtask : subtasks) {
                        allTasksId.add(subtask.getId());
                    }
                }
            }
        }
        for (int taskId : allTasksId) {
            tasks.remove(taskId);
        }
        System.out.println("Все задачи с типом "+ type +" удалены. Сейчас в коллекции: " + tasks.values());
    }

    @Override
    public Task getById(int id) {
        inMemoryHistoryManager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public void deleteById(int id) {
        Task task = tasks.get(id);
        if (task.getType() == Type.SUBTASK) {
            Epic parent = ((Subtask)(tasks.get(id))).getParent();
            tasks.remove(id);
            updateEpicStatus(parent);
        } else if (task.getType() == Type.EPIC) {
            Epic epic = (Epic) task;
            List<Subtask> subtasks = getSubtaskByEpic(epic);
            for (Subtask subtask : subtasks) {
                tasks.remove(subtask.getId());
            }
            tasks.remove(id);
        } else {
            tasks.remove(id);
        }

        System.out.println("Удалена задача с id "+id+ ". Сейчас в коллекции: " + tasks.values());
    }

    @Override
    public Task updateTask (Task task) {
        if (task.getType() == Type.EPIC) {
            Epic oldEpic = (Epic) tasks.get(task.getId());
            if (oldEpic.getStatus() != task.getStatus()) {
                System.out.println("Статус задачи типа Epic не может быть изменен пользователем");
                task.setStatus(oldEpic.getStatus());
            }
            tasks.put(task.getId(), task);
        } else if (task.getType() == Type.SUBTASK) {
            Subtask newSubtask = (Subtask) task;
            Subtask oldSubtask = (Subtask) tasks.get(task.getId());
            Epic parent = newSubtask.getParent();
            tasks.put(task.getId(), task);
            if (oldSubtask.getStatus() != task.getStatus()) {
                updateEpicStatus(parent);
            }
        } else {
            tasks.put(task.getId(), task);
        }
        return tasks.get(task.getId());
    }

    public void updateEpicStatus (Epic epic) {
        List<Subtask> allSubtasksByEpic = getSubtaskByEpic(epic);
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

    @Override
    public List<Subtask> getSubtaskByEpic (Task epic) {
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getType() == Type.SUBTASK) {
                if (((Subtask)task).getParent().equals(epic)) {
                    epicSubtasks.add((Subtask)task);
                }
            }
        }
        return epicSubtasks;
    }

}
