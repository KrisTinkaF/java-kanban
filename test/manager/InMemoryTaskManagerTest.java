package manager;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class InMemoryTaskManagerTest {

    private static TaskManager inMemoryTaskManager;
    private static HistoryManager inMemoryHistoryManager;

    private static TaskManager fileBackedTaskManager;

    @BeforeEach
    void createTestTasks() throws CrossTimeException {
        inMemoryTaskManager = Managers.getDefault();
        inMemoryHistoryManager = inMemoryTaskManager.getInMemoryHistoryManager();
        fileBackedTaskManager = FileBackedTaskManager.loadFromFile(new File("test/testResources/backupFile.csv"));
        Task task = fileBackedTaskManager.createTask(new Task("Test Task", "Test Task desc", LocalDateTime.of(2024, 7, 25, 21, 54), Duration.ofMinutes(20)));
        Epic epic = (Epic) fileBackedTaskManager.createTask(new Epic("Test Epic", "Test Epic desc"));
        Subtask subtask = (Subtask) fileBackedTaskManager.createTask(new Subtask("Test Subtask", "Test Subtask desc", LocalDateTime.of(2024, 7, 28, 15, 30), Duration.ofHours(15), epic.getId()));
        Subtask subtask1 = (Subtask) fileBackedTaskManager.createTask(new Subtask("Test Subtask 1", "Test Subtask 1 desc", LocalDateTime.of(2024, 7, 30, 9, 15), Duration.ofMinutes(120), epic.getId()));
    }


    @Test
    void createNewTaskTest() throws CrossTimeException {
        Task newTask = new Task("Test Task", "Test Task desc", LocalDateTime.of(2024, 7, 25, 21, 5), Duration.ofMinutes(2));
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

        Subtask newSubtask = new Subtask("Test Subtask", "Test Subtask desc", LocalDateTime.of(2024, 8, 28, 15, 30), Duration.ofHours(15), newEpic.getId());
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
        List<Subtask> subtasks = fileBackedTaskManager.getSubtaskByEpic(epic.getId());
        assertNotEquals(subtasks, new ArrayList<>(), "Подзадачи эпика не возвращаются.");
        assertEquals(2, subtasks.size(), "Неверное количество подзадач эпика.");
    }

    @Test
    void updateTaskTest() throws CrossTimeException {
        fileBackedTaskManager.updateTask(new Task(1, "Change name", "Task description", Status.NEW, LocalDateTime.of(2024, 7, 31, 21, 54), Duration.ofMinutes(20)));
        Task newTask = fileBackedTaskManager.getById(1);
        assertEquals("Change name", newTask.getName(), "Имя задачи не изменилось!");

        fileBackedTaskManager.updateTask(new Epic(2, "Test epic", "new desc", Status.IN_PROGRESS, LocalDateTime.of(2024, 7, 30, 9, 15), Duration.ofMinutes(120)));
        Epic newEpic = (Epic) fileBackedTaskManager.getById(2);
        assertEquals("new desc", newEpic.getDescription(), "Описание задачи не изменилось!");
        assertEquals(Status.NEW, newEpic.getStatus(), "Удалось изменить статус!");

        fileBackedTaskManager.updateTask(new Subtask(3, "Test subtask", "Subtask description", Status.IN_PROGRESS, LocalDateTime.of(2024, 10, 28, 15, 30), Duration.ofHours(15), newEpic.getId()));
        Subtask newSubtask = (Subtask) fileBackedTaskManager.getById(3);
        assertEquals(Status.IN_PROGRESS, newSubtask.getStatus(), "Статус подзадачи не изменился!");
        assertEquals(Status.IN_PROGRESS, newEpic.getStatus(), "Статус эпика не изменился!");

        fileBackedTaskManager.updateTask(new Subtask(4, "Test Subtask 1", "Test Subtask 1 desc", Status.IN_PROGRESS, LocalDateTime.of(2024, 7, 1, 9, 15), Duration.ofMinutes(120), newEpic.getId()));
        Subtask newSubtask2 = (Subtask) fileBackedTaskManager.getById(4);
        assertEquals(Status.IN_PROGRESS, newSubtask2.getStatus(), "Статус подзадачи не изменился!");
        assertEquals(Status.IN_PROGRESS, newEpic.getStatus(), "Неправильный статус эпика!");

        fileBackedTaskManager.updateTask(new Subtask(3, "Test subtask", "Subtask description", Status.DONE, LocalDateTime.of(2024, 7, 31, 15, 30), Duration.ofHours(15), newEpic.getId()));
        Subtask new2Subtask = (Subtask) fileBackedTaskManager.getById(3);
        assertEquals(Status.DONE, new2Subtask.getStatus(), "Статус подзадачи не изменился!");
        assertEquals(Status.IN_PROGRESS, newEpic.getStatus(), "Статус эпика не изменился!");

        fileBackedTaskManager.updateTask(new Subtask(4, "Test Subtask 1", "Test Subtask 1 desc", Status.DONE, LocalDateTime.of(2024, 7, 30, 18, 15), Duration.ofMinutes(120), newEpic.getId()));
        Subtask new2Subtask2 = (Subtask) fileBackedTaskManager.getById(4);
        assertEquals(Status.DONE, new2Subtask2.getStatus(), "Статус подзадачи не изменился!");
        assertEquals(Status.DONE, newEpic.getStatus(), "Неправильный статус эпика!");

        fileBackedTaskManager.updateTask(new Subtask(3, "Test subtask", "Subtask description", Status.NEW, LocalDateTime.of(2024, 10, 10, 15, 30), Duration.ofHours(15), newEpic.getId()));
        Subtask new3Subtask = (Subtask) fileBackedTaskManager.getById(3);
        assertEquals(Status.NEW, new3Subtask.getStatus(), "Статус подзадачи не изменился!");
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
        List<Subtask> subtasks = fileBackedTaskManager.getSubtaskByEpic(epic.getId());
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
    void restoreTaskTest() throws CrossTimeException {
        fileBackedTaskManager = FileBackedTaskManager.loadFromFile(new File("test/testResources/testRestore.csv"));
        final List<Task> tasks = fileBackedTaskManager.getAll();
        assertEquals(1, tasks.size(), "Колиечство задач не соответствует ожидаемому!");
    }

    @Test
    public void testManagerSaveException() {
        assertThrows(ManagerSaveException.class, () -> {
            fileBackedTaskManager = FileBackedTaskManager.loadFromFile(new File("test/testResources/backupFileTestException.csv"));
        }, "Отсутствие файла должно приводить к ошибке!");
    }

    @Test
    public void testCrossTime() {
        Task newTaskAfterStart = new Task("New Task", "New Task desc", LocalDateTime.of(2024, 7, 25, 22, 0), Duration.ofMinutes(20)); // начало новой задачи пересекается с отрезком старой
        assertTrue(fileBackedTaskManager.crossTime(newTaskAfterStart), "Пропустили перекрест времени!");
        Task newTaskBeforeEnd = new Task("New Task", "New Task desc", LocalDateTime.of(2024, 7, 25, 16, 0), Duration.ofHours(6)); // конец новой задачи пересекается с отрезком старой
        assertTrue(fileBackedTaskManager.crossTime(newTaskBeforeEnd), "Пропустили перекрест времени!");
        Task newTaskAfterStartAndBeforeEnd = new Task("New Task", "New Task desc", LocalDateTime.of(2024, 7, 25, 22, 0), Duration.ofMinutes(6)); // и начало и конец новой задачи пересекаются с отрезком старой
        assertTrue(fileBackedTaskManager.crossTime(newTaskAfterStartAndBeforeEnd), "Пропустили перекрест времени!");
        Task newTaskEqualsStart = new Task("New Task", "New Task desc", LocalDateTime.of(2024, 7, 25, 21, 54), Duration.ofMinutes(6)); // начало новой задачи равно началу старой
        assertTrue(fileBackedTaskManager.crossTime(newTaskEqualsStart), "Пропустили перекрест времени!");
        Task newTaskEqualsEnd = new Task("New Task", "New Task desc", LocalDateTime.of(2024, 7, 25, 21, 44), Duration.ofMinutes(16)); // конец новой задачи равен концу старой
        assertTrue(fileBackedTaskManager.crossTime(newTaskEqualsEnd), "Пропустили перекрест времени!");
        Task newTaskDoNotCross = new Task("New Task", "New Task desc", LocalDateTime.of(2024, 7, 25, 23, 30), Duration.ofMinutes(30)); // новой задача не пересекается по времени со старыми
        assertFalse(fileBackedTaskManager.crossTime(newTaskDoNotCross), "Не должно быть пересечения");
    }

    @AfterEach
    void deleteBackupFile() throws IOException {
        Path tempDir = Paths.get("test/testResources/");
        File file = tempDir.resolve("backupFile.csv").toFile();
        try (FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8)) {
            fileWriter.write("");
        }
    }

}