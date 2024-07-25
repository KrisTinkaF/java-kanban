import manager.*;
import model.*;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {

        final TaskManager inMemoryTaskManager = Managers.getDefault();
        final HistoryManager inMemoryHistoryManager = inMemoryTaskManager.getInMemoryHistoryManager();
        final TaskManager fileBackedTaskManager = Managers.getDefaultBackup();

        System.out.println("---CREATE---");
        Task task1 = new Task("Купить продукты", "мясо, макароны, лук, морковь", LocalDateTime.of(2024, 7,25,18,0), Duration.ofMinutes(75));
        System.out.println(fileBackedTaskManager.createTask(task1));
        Task task2 = new Task("Помыть машину", "берешь и моешь", LocalDateTime.of(2024, 7,27,9,30), Duration.ofHours(2));
        System.out.println(fileBackedTaskManager.createTask(task2));
        Epic epic1 = new Epic("Продать квартиру", "продать квартиру через Агенство");
        System.out.println(fileBackedTaskManager.createTask(epic1));
        Subtask subtask11 = new Subtask("Заключить договор с Агентсвом", null, LocalDateTime.of(2024, 9,9,10,0), Duration.ofHours(3), epic1);
        System.out.println(fileBackedTaskManager.createTask(subtask11));
        //System.out.println("Подзадача добавдена в эпик " + taskManager.getSubtaskByEpic(epic1));
        Epic epic2 = new Epic("Закрыть жесткий дедлайн", "Сдать все долги на ЯП");
        System.out.println(fileBackedTaskManager.createTask(epic2));
        Subtask subtask21 = new Subtask("Сдать проектное задание по спринту 8", "Дописать проект и сдать на ревью", LocalDateTime.of(2024, 7,30,18,7), Duration.ofDays(1), epic2);
        System.out.println(fileBackedTaskManager.createTask(subtask21));
        //System.out.println("Подзадача добавдена в эпик " + taskManager.getSubtaskByEpic(epic2));
        Subtask subtask22 = new Subtask("Сдать проектное задание по спринту 9", "Изучить теорию и после выполнить проектное задание спринта 5", LocalDateTime.of(2024, 7,30,19,30), Duration.ofDays(3),epic2);
        System.out.println(fileBackedTaskManager.createTask(subtask22));
        //System.out.println("Подзадача добавдена в эпик " + taskManager.getSubtaskByEpic(epic2));
        System.out.println("---PRINT---");
        System.out.println("ALL_TASKS");
        System.out.println(fileBackedTaskManager.getAllTasks(Type.TASK));
        System.out.println("ALL_EPICS");
        System.out.println(fileBackedTaskManager.getAllTasks(Type.EPIC));
        System.out.println("ALL_SUBTASKS");
        System.out.println(fileBackedTaskManager.getAllTasks(Type.SUBTASK));
        System.out.println("---UPDATE---");
        System.out.println("TASK");
        System.out.println(fileBackedTaskManager.updateTask(new Task(task1.getId(), "Купить продукты", "осталось купить только мясо", Status.IN_PROGRESS, LocalDateTime.of(2024, 7,26,19,30), Duration.ofDays(3))));
        System.out.println("SUBTASK");
        System.out.println(fileBackedTaskManager.updateTask(new Subtask(subtask21.getId(), "Сдать проектное задание по спринту 4", "Дописать проект и сдать на ревью", Status.IN_PROGRESS, LocalDateTime.of(2024, 8,26,19,30), Duration.ofDays(3), epic2)));
        System.out.println("EPIC");
        System.out.println(fileBackedTaskManager.updateTask(new Epic(epic1.getId(), "Продать квартиру быстро", "продать квартиру через Агенство")));

        //блок сработает только при первом запуске, далее задачи будут удалены, из бэкапа в том числе

        /*System.out.println("---GET_BY_ID---");
        System.out.println("id 4 :" + fileBackedTaskManager.getById(4));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 2 :" + fileBackedTaskManager.getById(2));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 6 :" + fileBackedTaskManager.getById(6));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 2 :" + fileBackedTaskManager.getById(2));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 3 :" + fileBackedTaskManager.getById(3));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 4 :" + fileBackedTaskManager.getById(4));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 4 :" + fileBackedTaskManager.getById(4));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 1 :" + fileBackedTaskManager.getById(1));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 5 :" + fileBackedTaskManager.getById(5));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 1 :" + fileBackedTaskManager.getById(1));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 2 :" + fileBackedTaskManager.getById(2));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("---GET_SUBTASKS_BY_EPIC---");
        System.out.println("Подзадачи " + epic2 + " :" + fileBackedTaskManager.getSubtaskByEpic(epic2));

        System.out.println("---DELETE_BY_ID---");
        fileBackedTaskManager.deleteById(1);
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        fileBackedTaskManager.deleteById(3);
        System.out.println("История :" + inMemoryHistoryManager.getHistory());

         */
        System.out.println("---DELETE_ALL_BY_TYPE---");
        System.out.println("TASK");
        fileBackedTaskManager.deleteAllTasks(Type.TASK);
        System.out.println("История :" + inMemoryHistoryManager.getHistory());

        System.out.println("SUBTASK");
        fileBackedTaskManager.deleteAllTasks(Type.SUBTASK);
        System.out.println("История :" + inMemoryHistoryManager.getHistory());

        System.out.println("EPIC");
        fileBackedTaskManager.deleteAllTasks(Type.EPIC);
        System.out.println("История :" + inMemoryHistoryManager.getHistory());

        Task fileControlTask = new Task("Проверка бэкапа", "Если задача есть в файле backupFile, то все ок");
        System.out.println(fileBackedTaskManager.createTask(fileControlTask));

    }

}
