package manager;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class InMemoryTaskManagerTest {

    private static TaskManager inMemoryTaskManager;
    private static HistoryManager inMemoryHistoryManager;

    private static TaskManager fileBackedTaskManager;

    @BeforeEach
    void createTestTasks() {
        inMemoryTaskManager = Managers.getDefault();
        inMemoryHistoryManager = inMemoryTaskManager.getInMemoryHistoryManager();
        fileBackedTaskManager = FileBackedTaskManager.loadFromFile(new File("test/testResources/backupFile.csv"));
        Task task = fileBackedTaskManager.createTask(new Task("Test Task", "Test Task desc"));
        Epic epic = (Epic) fileBackedTaskManager.createTask(new Epic("Test Epic", "Test Epic desc"));
        Subtask subtask = (Subtask) fileBackedTaskManager.createTask(new Subtask("Test Subtask", "Test Subtask desc", epic));
        Subtask subtask1 = (Subtask) fileBackedTaskManager.createTask(new Subtask("Test Subtask 1", "Test Subtask 1 desc", epic));
    }


    @Test
    void createNewTaskTest() {
        Task newTask = new Task("Test task", "Task description");
        final int taskId = fileBackedTaskManager.createTask(newTask).getId();

        final Task savedTask = fileBackedTaskManager.getById(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(newTask, savedTask, "Задачи не совпадают.");
        assertEquals(Type.TASK, savedTask.getType(), "Тип задачи не равен ожидаемому.");

        final List<Task> tasks = fileBackedTaskManager.getAllTasks(Type.TASK);

        assertNotEquals(tasks, new ArrayList<>(), "Задачи не возвращаются.");
        System.out.println("tasks " + tasks);
        assertEquals(2, tasks.size(), "Неверное количество задач.");
        assertEquals(newTask, tasks.get(1), "Задачи не совпадают.");

        Epic newEpic = new Epic("Test epic", "Epic description");
        final int epicId = fileBackedTaskManager.createTask(newEpic).getId();

        final Epic savedEpic = (Epic) fileBackedTaskManager.getById(epicId);

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(newEpic, savedEpic, "Эпики не совпадают.");
        assertEquals(Type.EPIC, savedEpic.getType(), "Тип задачи не равен ожидаемому.");

        final List<Task> epics = fileBackedTaskManager.getAllTasks(Type.EPIC);

        assertNotEquals(epics, new ArrayList<>(), "Эпики не возвращаются.");
        System.out.println("epics " + epics);
        assertEquals(2, epics.size(), "Неверное количество эпиков.");
        assertEquals(newEpic, epics.get(1), "Эпики не совпадают.");

        Subtask newSubtask = new Subtask("Test subtask", "Subtask description", newEpic);
        final int subtaskId = fileBackedTaskManager.createTask(newSubtask).getId();

        final Subtask savedSubtask = (Subtask) fileBackedTaskManager.getById(subtaskId);

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(newSubtask, savedSubtask, "Подзадачи не совпадают.");
        assertEquals(Type.SUBTASK, savedSubtask.getType(), "Тип задачи не равен ожидаемому.");

        final List<Task> subtasks = fileBackedTaskManager.getAllTasks(Type.SUBTASK);

        assertNotEquals(subtasks, new ArrayList<>(), "Подзадачи не возвращаются.");
        System.out.println("subtasks createNewTask" + subtasks);
        assertEquals(3, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(newSubtask, subtasks.get(2), "Подзадачи не совпадают.");
    }

    @Test
    void getSubtasksByEpicTest() {
        Epic epic = (Epic) fileBackedTaskManager.getById(2);
        List<Subtask> subtasks = fileBackedTaskManager.getSubtaskByEpic(epic);
        assertNotEquals(subtasks, new ArrayList<>(), "Подзадачи эпика не возвращаются.");
        assertEquals(2, subtasks.size(), "Неверное количество подзадач эпика.");
    }

    @Test
    void updateTaskTest() {
        fileBackedTaskManager.updateTask(new Task(1, "Change name", "Task description", Status.NEW));
        Task newTask = fileBackedTaskManager.getById(1);
        assertEquals("Change name", newTask.getName(), "Имя задачи не изменилось!");

        fileBackedTaskManager.updateTask(new Epic(2, "Test epic", "new desc", Status.IN_PROGRESS));
        Epic newEpic = (Epic) fileBackedTaskManager.getById(2);
        assertEquals("new desc", newEpic.getDescription(), "Описание задачи не изменилось!");
        assertEquals(Status.NEW, newEpic.getStatus(), "Удалось изменить статус!");

        fileBackedTaskManager.updateTask(new Subtask(3, "Test subtask", "Subtask description", Status.IN_PROGRESS, newEpic));
        Subtask newSubtask = (Subtask) fileBackedTaskManager.getById(3);
        assertEquals(Status.IN_PROGRESS, newSubtask.getStatus(), "Статус подзадачи не изменился!");
        assertEquals(Status.IN_PROGRESS, newEpic.getStatus(), "Статус эпика не изменился!");
    }

    @Test
    void getHistoryTest() {
        fileBackedTaskManager.getById(2);
        fileBackedTaskManager.getById(1);
        fileBackedTaskManager.getById(1);
        fileBackedTaskManager.getById(3);
        fileBackedTaskManager.getById(4);
        fileBackedTaskManager.getById(2);

        final List<Task> history = inMemoryHistoryManager.getHistory();
        System.out.println("history " + history);
        assertEquals(4, history.size(), "Количество задач в истории не соответствует ожидаемому");
    }

    @Test
    void deleteTasksTest() {
        fileBackedTaskManager.deleteById(3); //Удаляем подзадачу
        Epic epic = (Epic) fileBackedTaskManager.getById(2);
        assertEquals(Status.NEW, epic.getStatus(), "Статус эпика не изменился, хотя подзадача удалена!");

        fileBackedTaskManager.deleteById(2); //Удаляем эпик
        List<Subtask> subtasks = fileBackedTaskManager.getSubtaskByEpic(epic);
        assertEquals(subtasks, new ArrayList<>(), "Подзадачи эпика не удалены вместе с ним!");

        fileBackedTaskManager.deleteAllTasks(Type.TASK);
        final List<Task> tasks = fileBackedTaskManager.getAllTasks(Type.TASK);
        assertEquals(tasks, new ArrayList<>(), "Задачи не удалены!");

        fileBackedTaskManager.deleteAllTasks(Type.SUBTASK);
        final List<Task> allSubtasks = fileBackedTaskManager.getAllTasks(Type.SUBTASK);
        assertEquals(allSubtasks, new ArrayList<>(), "Подзадачи не удалены!");

        fileBackedTaskManager.deleteAllTasks(Type.EPIC);
        final List<Task> epics = fileBackedTaskManager.getAllTasks(Type.EPIC);
        assertEquals(epics, new ArrayList<>(), "Эпики не удалены!");
    }

    @Test
    void saveTaskTest() {
        Path tempDir = Paths.get("test/testResources/");
        File file = tempDir.resolve("backupFile.csv").toFile();
        assertNotEquals(0, file.length(), "Файл пустой!");
    }

    @Test
    void restoreTaskTest() {
        fileBackedTaskManager = FileBackedTaskManager.loadFromFile(new File("test/testResources/testRestore.csv"));
        final List<Task> tasks = fileBackedTaskManager.getAll();
        assertEquals(1, tasks.size(), "Колиечство задач не соответствует ожидаемому!");
    }

    @AfterEach
    void deleteBackupFile() {
        Path tempDir = Paths.get("test/testResources/");
        File file = tempDir.resolve("backupFile.csv").toFile();
        file.delete();
    }

}