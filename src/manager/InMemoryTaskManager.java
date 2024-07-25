package manager;

import model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    public InMemoryTaskManager() {
    }

    public InMemoryTaskManager(HistoryManager inMemoryHistoryManager) {
        InMemoryTaskManager.inMemoryHistoryManager = inMemoryHistoryManager;
        InMemoryTaskManager.counter = 0;
    }

    public HistoryManager getInMemoryHistoryManager() {
        return inMemoryHistoryManager;
    }

    private static HistoryManager inMemoryHistoryManager;

    private static int counter;

    Comparator<Task> comparator = Comparator.comparing(Task::getStartTime);
    private final Map<Integer, Task> tasks = new HashMap<>();

    private final TreeSet<Task> sortedTasks = new TreeSet<>(comparator);


    @Override
    public Task createTask(Task task) {
        if (task.getId() != 0) {
            counter = task.getId();
        } else {
            counter++;
            task.setId(counter);
        }
        addTask(task);
        if (task.getType() == Type.SUBTASK) {
            updateEpicStartTime(((Subtask) task).getParent());
            updateEpicDuration(((Subtask) task).getParent());
        }
        return task;
    }

    @Override
    public List<Task> getAllTasks(Type type) {
        List<Task> allTasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getType() == type) {
                allTasks.add(task);
            }
        }
        return allTasks;
    }

    @Override
    public List<Task> getAll() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks(Type type) {
        List<Integer> allTasksId = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getType() == type) {
                allTasksId.add(task.getId());
                if (type == Type.SUBTASK) {
                    Epic parent = ((Subtask) task).getParent();
                    if (parent != null) {
                        updateEpicStatus(parent);
                        updateEpicStartTime(parent);
                        updateEpicDuration(parent);
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
            inMemoryHistoryManager.remove(taskId);
            removeTask(tasks.get(taskId));
        }
        System.out.println("Все задачи с типом " + type + " удалены. Сейчас в коллекции: " + tasks.values());
    }

    @Override
    public void deleteAll() {
        tasks.clear();
        sortedTasks.clear();
        inMemoryHistoryManager.clearHistory();
        counter = 0;
    }

    @Override
    public Task getById(int id) {
        inMemoryHistoryManager.add(tasks.get(id));
        System.out.println("В историю добавлена таска " + id);
        return tasks.get(id);
    }

    @Override
    public void deleteById(int id) {
        Task task = tasks.get(id);
        if (task.getType() == Type.SUBTASK) {
            Epic parent = ((Subtask) task).getParent();
            removeTask(task);
            //tasks.remove(id);
            updateEpicStatus(parent);
            updateEpicStartTime(parent);
            updateEpicDuration(parent);
        } else if (task.getType() == Type.EPIC) {
            Epic epic = (Epic) task;
            List<Subtask> subtasks = getSubtaskByEpic(epic);
            for (Subtask subtask : subtasks) {
                //tasks.remove(subtask.getId());
                removeTask(subtask);
                inMemoryHistoryManager.remove(subtask.getId());
            }
            removeTask(epic);
        } else {
            removeTask(task);
        }
        inMemoryHistoryManager.remove(id);
        System.out.println("Удалена задача с id " + id + ". Сейчас в коллекции: " + tasks.values());
    }

    @Override
    public Task updateTask(Task task) {
        if (task.getType() == Type.EPIC) {
            Epic oldEpic = (Epic) tasks.get(task.getId());
            if (oldEpic.getStatus() != task.getStatus()) {
                System.out.println("Статус задачи типа Epic не может быть изменен пользователем");
                task.setStatus(oldEpic.getStatus());
            }
            addTask(task);
        } else if (task.getType() == Type.SUBTASK) {
            Subtask newSubtask = (Subtask) task;
            Subtask oldSubtask = (Subtask) tasks.get(task.getId());
            Epic parent = newSubtask.getParent();
            addTask(task);
            if (oldSubtask.getStatus() != task.getStatus()) {
                updateEpicStatus(parent);
            }
            if (oldSubtask.getStartTime() != task.getStartTime()) {
                updateEpicStartTime(parent);
            }
            if (oldSubtask.getDuration() != task.getDuration()) {
                updateEpicDuration(parent);
            }

        } else {
            addTask(task);
        }
        return tasks.get(task.getId());
    }

    public void updateEpicStatus(Epic epic) {
        List<Subtask> allSubtasksByEpic = getSubtaskByEpic(epic);
        if (!allSubtasksByEpic.isEmpty()) {
            Set<Status> subtaskStatuses = new HashSet<>();
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
        addTask(epic);
    }

    public void updateEpicStartTime(Epic epic) {
        List<Subtask> allSubtasksByEpic = getSubtaskByEpic(epic);

        if (!allSubtasksByEpic.isEmpty()) {
            Optional<Subtask> first = allSubtasksByEpic.stream().sorted(comparator).findFirst();
            System.out.println("время первой сабтаски " + first.get().getStartTime());
            epic.setStartTime(first.get().getStartTime());
            addTask(epic);
        }
    }

    public void updateEpicDuration(Epic epic) {
        List<Subtask> allSubtasksByEpic = getSubtaskByEpic(epic);
        if (!allSubtasksByEpic.isEmpty()) {
            Duration sum = Duration.ZERO;
            for (Subtask sub : allSubtasksByEpic) {
                sum = sum.plus(sub.getDuration());
            }
            epic.setDuration(sum);
            addTask(epic);
        }
    }

    @Override
    public List<Subtask> getSubtaskByEpic(Task epic) {
        List<Subtask> epicSubtasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getType() == Type.SUBTASK) {
                if (((Subtask) task).getParent().equals(epic)) {
                    epicSubtasks.add((Subtask) task);
                }
            }
        }
        return epicSubtasks;
    }

    @Override
    public TreeSet<Task> getPrioritizedTasks() {
        return sortedTasks;
    }

    private void addTask(Task task) {
        if (task.getType() == Type.EPIC || task.getStartTime() == null) {
            tasks.put(task.getId(), task);
        } else {
            try {
                if (crossTime(task)) {
                    throw new CrossTimeException("Задача пересекается по времени с дургими задачами. Она не будет создана или обновлена!" + task);
                } else {
                    if (task.getStartTime() != null) {
                        sortedTasks.add(task);
                    }
                    tasks.put(task.getId(), task);
                }
            } catch (Exception exception) {
                System.out.println(exception.getMessage());
            }
        }

    }

    private void removeTask(Task task) {
        if (task.getStartTime() != null && task.getType() != Type.EPIC) {
            sortedTasks.remove(task);
        }
        tasks.remove(task.getId());
    }

    private boolean crossTime(Task newTask) {
        LocalDateTime start = newTask.getStartTime();
        LocalDateTime end = newTask.getEndTime();
        System.out.println("Отсрортированные таски " + getPrioritizedTasks());
        return getPrioritizedTasks().stream().anyMatch(task -> (start.isAfter(task.getStartTime()) && start.isBefore(task.getEndTime())) || (end.isAfter(task.getStartTime()) && end.isBefore(task.getEndTime())));
    }

}
