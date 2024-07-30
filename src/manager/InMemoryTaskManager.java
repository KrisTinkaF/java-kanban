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

    private final Set<Task> sortedTasks = new TreeSet<>(comparator);


    @Override
    public Task createTask(Task task) throws CrossTimeException {
        if (task.getId() != 0) {
            counter = task.getId();
        } else {
            counter++;
            task.setId(counter);
        }
        addTask(task);
        if (task.getType() == Type.SUBTASK) {
            updateEpicStartTime(((Subtask) task).getParentId());
            updateEpicDuration(((Subtask) task).getParentId());
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
                    int parentId = ((Subtask) task).getParentId();
                    if (parentId != 0) {
                        updateEpicStatus(parentId);
                        updateEpicStartTime(parentId);
                        updateEpicDuration(parentId);
                    }
                } else if (type == Type.EPIC) {
                    Epic epic = (Epic) task;
                    List<Subtask> subtasks = getSubtaskByEpic(epic.getId());
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
    public Task getById(int id) throws NotFoundException {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Таска с таким id не найдена!");
        }
        inMemoryHistoryManager.add(tasks.get(id));
        System.out.println("В историю добавлена таска " + id);
        return task;
    }

    @Override
    public void deleteById(int id) throws NotFoundException {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Таска с таким id не найдена!");
        }
        if (task.getType() == Type.SUBTASK) {
            //Epic parent = (Epic) tasks.get(((Subtask) task).getParentId());
            int epicId = ((Subtask) task).getParentId();
            removeTask(task);
            //tasks.remove(id);
            updateEpicStatus(epicId);
            updateEpicStartTime(epicId);
            updateEpicDuration(epicId);
        } else if (task.getType() == Type.EPIC) {
            Epic epic = (Epic) task;
            List<Subtask> subtasks = getSubtaskByEpic(epic.getId());
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
    public Task updateTask(Task task) throws CrossTimeException, NotFoundException {
        if (task.getType() == Type.EPIC) {
            Epic oldEpic = (Epic) tasks.get(task.getId());
            if (oldEpic == null) {
                throw new NotFoundException("Эпик с таким id не найдена!");
            }
            if (oldEpic.getStatus() != task.getStatus()) {
                System.out.println("Статус задачи типа Epic не может быть изменен пользователем");
                task.setStatus(oldEpic.getStatus());
            }
            addTask(task);
        } else if (task.getType() == Type.SUBTASK) {
            Subtask newSubtask = (Subtask) task;
            Subtask oldSubtask = (Subtask) tasks.get(task.getId());
            if (oldSubtask == null) {
                throw new NotFoundException("Сабтаск с таким id не найдена!");
            }
            //Epic parent = (Epic) tasks.get((newSubtask).getParentId());
            addTask(task);
            if (oldSubtask.getStatus() != task.getStatus()) {
                updateEpicStatus(newSubtask.getParentId());
            }
            if (oldSubtask.getStartTime() != task.getStartTime()) {
                updateEpicStartTime(newSubtask.getParentId());
            }
            if (oldSubtask.getDuration() != task.getDuration()) {
                updateEpicDuration(newSubtask.getParentId());
            }

        } else {
            addTask(task);
        }
        if (tasks.get(task.getId()) == null) {
            throw new NotFoundException("Сабтаск с таким id не найдена!");
        }
        return tasks.get(task.getId());
    }

    public void updateEpicStatus(int epicId) {
        Epic epic = (Epic) tasks.get(epicId);
        List<Subtask> allSubtasksByEpic = getSubtaskByEpic(epicId);
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
        tasks.put(epic.getId(), epic);
    }

    public void updateEpicStartTime(int epicId) {
        Epic epic = (Epic) tasks.get(epicId);
        List<Subtask> allSubtasksByEpic = getSubtaskByEpic(epicId);

        if (!allSubtasksByEpic.isEmpty()) {
            allSubtasksByEpic.stream().sorted(comparator).findFirst().ifPresent(first -> {
                        System.out.println("время первой сабтаски " + first.getStartTime());
                        epic.setStartTime(first.getStartTime());
                        System.out.println("время эпика " + epic.getStartTime());
                    }
            );
            tasks.put(epic.getId(), epic);
        }
    }

    public void updateEpicDuration(int epicId) {
        Epic epic = (Epic) tasks.get(epicId);
        List<Subtask> allSubtasksByEpic = getSubtaskByEpic(epicId);
        if (!allSubtasksByEpic.isEmpty()) {
            Duration sum = Duration.ZERO;
            for (Subtask sub : allSubtasksByEpic) {
                sum = sum.plus(sub.getDuration());
            }
            epic.setDuration(sum);
            tasks.put(epic.getId(), epic);
        }
    }

    @Override
    public List<Subtask> getSubtaskByEpic(int epicId) {
        List<Subtask> epicSubtasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getType() == Type.SUBTASK) {
                if (((Subtask) task).getParentId() == epicId) {
                    epicSubtasks.add((Subtask) task);
                }
            }
        }
        return epicSubtasks;
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return sortedTasks;
    }

    private void addTask(Task task) throws CrossTimeException {
        if (task.getType() == Type.EPIC || task.getStartTime() == null) {
            tasks.put(task.getId(), task);
        } else {
            if (crossTime(task)) {
                throw new CrossTimeException("Задача пересекается по времени с дургими задачами. Она не будет создана или обновлена!" + task);
            } else {
                if (task.getStartTime() != null) {
                    sortedTasks.add(task);
                }
                tasks.put(task.getId(), task);
            }
        }

    }

    private void removeTask(Task task) {
        if (task.getStartTime() != null && task.getType() != Type.EPIC) {
            sortedTasks.remove(task);
        }
        tasks.remove(task.getId());
    }

    public boolean crossTime(Task newTask) {
        LocalDateTime start = newTask.getStartTime();
        LocalDateTime end = newTask.getEndTime();
        System.out.println("Отсрортированные таски " + getPrioritizedTasks());
        return getPrioritizedTasks().stream().anyMatch(task -> isCross(start, end, task));
    }

    private boolean isCross(LocalDateTime start, LocalDateTime end, Task task) {
        return start.equals(task.getStartTime()) || end.equals(task.getEndTime()) || (start.isAfter(task.getStartTime()) && start.isBefore(task.getEndTime()))
                || (end.isAfter(task.getStartTime()) && end.isBefore(task.getEndTime()));
    }

}
