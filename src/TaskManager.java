import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
public class TaskManager {
    private static int counter = 0;

    public HashMap<Integer, Object> tasks = new HashMap<>();

    public Task createTask(Task task) {
        counter++;
        task.setId(counter);
        tasks.put(task.getId(), task);
        return task;
    }
    public Epic createEpic(Epic epic) {
        counter++;
        epic.setId(counter);
        tasks.put(epic.getId(), epic);
        return epic;
    }
    public Subtask createSubtask(Subtask subtask) {
        counter++;
        subtask.setId(counter);
        tasks.put(subtask.getId(), subtask);
        return subtask;
    }

    public ArrayList<Object> getAllTasks(Type type){
        ArrayList<Object> allTasks = new ArrayList<>();
        boolean matchesType = false;
        for (Object task : tasks.values()) {
            switch (type) {
                case TASK:
                    matchesType = task.getClass() == Task.class;
                    break;
                case EPIC:
                    matchesType = task.getClass() == Epic.class;
                    break;
                case SUBTASK:
                    matchesType = task.getClass() == Subtask.class;
                    break;
            }
            if (matchesType) {
                allTasks.add(task);
            }
        }
        return allTasks;
    }

    public void deleteAllTasks(Type type){
        ArrayList<Integer> allTasksId = new ArrayList<>();
        for (Object task : tasks.values()) {
            switch (type) {
                case TASK:
                    if (task.getClass() == Task.class) {
                        allTasksId.add(((Task) task).getId());
                    }
                    break;
                case EPIC:
                    if (task.getClass() == Epic.class) {
                        allTasksId.add(((Epic) task).getId());
                    }
                    break;
                case SUBTASK:
                    if (task.getClass() == Subtask.class) {
                        allTasksId.add(((Subtask) task).getId());
                    }
                    break;
            }
        }
        for (int taskId : allTasksId) {
            tasks.remove(taskId);
        }
        System.out.println("Все задачи с типом "+ type +" удалены. Сейчас в коллекции: " + tasks.values());
    }

    public Object getById(int id) {
        return tasks.get(id);
    }

    public void deleteById(int id) {
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
            ArrayList<Subtask> allSubtasksByEpic = getSubtaskByEpic(parent);
            boolean epicNew;
            boolean epicDone;
            if (!allSubtasksByEpic.isEmpty()) {
                epicDone = areAllSubtaskIsDone(allSubtasksByEpic);
                epicNew = areAllSubtaskIsNew(allSubtasksByEpic);
                if (epicDone) {
                    parent.setStatus(Status.DONE);
                    System.out.println(parent);
                } else if (epicNew) {
                    parent.setStatus(Status.NEW);
                    System.out.println(parent);
                } else {
                    parent.setStatus(Status.IN_PROGRESS);
                    System.out.println(parent);
                }
                tasks.put(parent.getId(), parent);
            }
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

    public ArrayList<Subtask> getSubtaskByEpic (Epic epic) {
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        for (Object task : tasks.values()) {
            if (task.getClass() == Subtask.class) {
                if (((Subtask) task).getParentId() == epic.getId()) {
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
