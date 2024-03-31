package manager;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

class InMemoryTaskManagerTest {

    final static TaskManager inMemoryTaskManager = Managers.getDefault();
    //final HistoryManager inMemoryHistoryManager = InMemoryTaskManager.inMemoryHistoryManager;
    final HistoryManager inMemoryHistoryManager = inMemoryTaskManager.getInMemoryHistoryManager();
    @BeforeAll
    static void beforeAll() {
        Task task = inMemoryTaskManager.createTask(new Task("Test Task", "Test Task desc"));
        Epic epic = (Epic) inMemoryTaskManager.createTask(new Epic("Test Epic", "Test Epic desc"));
        Subtask subtask = (Subtask) inMemoryTaskManager.createTask(new Subtask("Test Subtask", "Test Subtask desc", epic));
        Subtask subtask1 = (Subtask) inMemoryTaskManager.createTask(new Subtask("Test Subtask 1", "Test Subtask 1 desc", epic));
    }

    @Test
    void createNewTask() {
        Task newTask = new Task("Test task", "Task description");
        final int taskId = inMemoryTaskManager.createTask(newTask).getId();

        final Task savedTask = inMemoryTaskManager.getById(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(newTask, savedTask, "Задачи не совпадают.");
        assertEquals(Type.TASK, savedTask.getType(), "Тип задачи не равен ожидаемому.");

        final List<Task> tasks = inMemoryTaskManager.getAllTasks(Type.TASK);

        assertNotEquals(tasks, new ArrayList<>(),"Задачи не возвращаются.");
        System.out.println("tasks "+tasks);
        assertEquals(2, tasks.size(), "Неверное количество задач.");
        assertEquals(newTask, tasks.get(1), "Задачи не совпадают.");

        Epic newEpic = new Epic("Test epic", "Epic description");
        final int epicId = inMemoryTaskManager.createTask(newEpic).getId();

        final Epic savedEpic = (Epic) inMemoryTaskManager.getById(epicId);

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(newEpic, savedEpic, "Эпики не совпадают.");
        assertEquals(Type.EPIC, savedEpic.getType(), "Тип задачи не равен ожидаемому.");

        final List<Task> epics = inMemoryTaskManager.getAllTasks(Type.EPIC);

        assertNotEquals(epics, new ArrayList<>(),"Эпики не возвращаются.");
        System.out.println("epics "+epics);
        assertEquals(2, epics.size(), "Неверное количество эпиков.");
        assertEquals(newEpic, epics.get(1), "Эпики не совпадают.");

        Subtask newSubtask = new Subtask("Test subtask", "Subtask description", newEpic);
        final int subtaskId = inMemoryTaskManager.createTask(newSubtask).getId();

        final Subtask savedSubtask = (Subtask)inMemoryTaskManager.getById(subtaskId);

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(newSubtask, savedSubtask, "Подзадачи не совпадают.");
        assertEquals(Type.SUBTASK, savedSubtask.getType(), "Тип задачи не равен ожидаемому.");

        final List<Task> subtasks = inMemoryTaskManager.getAllTasks(Type.SUBTASK);

        assertNotEquals(subtasks, new ArrayList<>(), "Подзадачи не возвращаются.");
        System.out.println("subtasks createNewTask"+subtasks);
        assertEquals(3, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(newSubtask, subtasks.get(2), "Подзадачи не совпадают.");
    }

    @Test
    void getSubtasksByEpic() {
        Epic epic = (Epic) inMemoryTaskManager.getById(2);
        List<Subtask> subtasks = inMemoryTaskManager.getSubtaskByEpic(epic);
        assertNotEquals(subtasks, new ArrayList<>(),"Подзадачи эпика не возвращаются.");
        assertEquals(2, subtasks.size(), "Неверное количество подзадач эпика.");
    }

    @Test
    void updateTask() {
        inMemoryTaskManager.updateTask(new Task(1, "Change name", "Task description", Status.NEW));
        Task newTask = inMemoryTaskManager.getById(1);
        assertEquals("Change name", newTask.getName(), "Имя задачи не изменилось!");

        inMemoryTaskManager.updateTask(new Epic(2, "Test epic", "new desc", Status.IN_PROGRESS));
        Epic newEpic = (Epic) inMemoryTaskManager.getById(2);
        assertEquals("new desc", newEpic.getDescription(), "Описание задачи не изменилось!");
        assertEquals(Status.NEW, newEpic.getStatus(), "Удалось изменить статус!");

        inMemoryTaskManager.updateTask(new Subtask(3, "Test subtask", "Subtask description", Status.IN_PROGRESS, newEpic));
        Subtask newSubtask = (Subtask) inMemoryTaskManager.getById(3);
        assertEquals(Status.IN_PROGRESS, newSubtask.getStatus(), "Статус подзадачи не изменился!");
        assertEquals(Status.IN_PROGRESS, newEpic.getStatus(), "Статус эпика не изменился!");
    }

    @Test
    void deleteTask() {

        inMemoryTaskManager.deleteById(3); //Удаляем подзадачу
        Epic epic = (Epic) inMemoryTaskManager.getById(2);
        assertEquals(Status.NEW, epic.getStatus(), "Статус эпика не изменился, хотя подзадача удалена!");

        inMemoryTaskManager.deleteById(2); //Удаляем эпик
        List<Subtask> subtasks = inMemoryTaskManager.getSubtaskByEpic(epic);
        assertEquals(subtasks, new ArrayList<>(),"Подзадачи эпика не удалены вместе с ним!");

        inMemoryTaskManager.deleteAllTasks(Type.TASK);
        final List<Task> tasks = inMemoryTaskManager.getAllTasks(Type.TASK);
        assertEquals(tasks, new ArrayList<>(),"Задачи не удалены!");

        inMemoryTaskManager.deleteAllTasks(Type.SUBTASK);
        final List<Task> allSubtasks = inMemoryTaskManager.getAllTasks(Type.SUBTASK);
        assertEquals(allSubtasks, new ArrayList<>(),"Подзадачи не удалены!");

        inMemoryTaskManager.deleteAllTasks(Type.EPIC);
        final List<Task> epicss = inMemoryTaskManager.getAllTasks(Type.EPIC);
        assertEquals(epicss, new ArrayList<>(),"Эпики не удалены!");
    }

    @Test
    void getHistory() {
        final LinkedList<Task> history = inMemoryHistoryManager.getHistory();
        assertNotEquals(history, new LinkedList<>(),"История пустая.");
        System.out.println("history "+ history);
        assertEquals(8, history.size(), "История не пустая.");
    }

}