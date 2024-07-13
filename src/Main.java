import manager.*;
import model.*;

public class Main {

    public static void main(String[] args) {
        final TaskManager inMemoryTaskManager = Managers.getDefault();
        final HistoryManager inMemoryHistoryManager = inMemoryTaskManager.getInMemoryHistoryManager();
        System.out.println("---CREATE---");
        Task task1 = new Task("Купить продукты", "мясо, макароны, лук, морковь");
        System.out.println(inMemoryTaskManager.createTask(task1));
        Task task2 = new Task("Помыть машину", "берешь и моешь");
        System.out.println(inMemoryTaskManager.createTask(task2));
        Epic epic1 = new Epic("Продать квартиру", "продать квартиру через Агенство");
        System.out.println(inMemoryTaskManager.createTask(epic1));
        Subtask subtask11 = new Subtask("Заключить договор с Агентсвом", null, epic1);
        System.out.println(inMemoryTaskManager.createTask(subtask11));
        //System.out.println("Подзадача добавдена в эпик " + taskManager.getSubtaskByEpic(epic1));
        Epic epic2 = new Epic("Закрыть жесткий дедлайн", "Сдать все долги на ЯП");
        System.out.println(inMemoryTaskManager.createTask(epic2));
        Subtask subtask21 = new Subtask("Сдать проектное задание по спринту 4", "Дописать проект и сдать на ревью", epic2);
        System.out.println(inMemoryTaskManager.createTask(subtask21));
        //System.out.println("Подзадача добавдена в эпик " + taskManager.getSubtaskByEpic(epic2));
        Subtask subtask22 = new Subtask("Сдать проектное задание по спринту 5", "Изучить теорию и после выполнить проектное задание спринта 5", epic2);
        System.out.println(inMemoryTaskManager.createTask(subtask22));
        //System.out.println("Подзадача добавдена в эпик " + taskManager.getSubtaskByEpic(epic2));
        System.out.println("---PRINT---");
        System.out.println("ALL_TASKS");
        System.out.println(inMemoryTaskManager.getAllTasks(Type.TASK));
        System.out.println("ALL_EPICS");
        System.out.println(inMemoryTaskManager.getAllTasks(Type.EPIC));
        System.out.println("ALL_SUBTASKS");
        System.out.println(inMemoryTaskManager.getAllTasks(Type.SUBTASK));
        System.out.println("---UPDATE---");
        System.out.println("TASK");
        System.out.println(inMemoryTaskManager.updateTask(new Task(task1.getId(), "Купить продукты", "осталось купить только мясо", Status.IN_PROGRESS)));
        System.out.println("SUBTASK");
        System.out.println(inMemoryTaskManager.updateTask(new Subtask(subtask21.getId(), "Сдать проектное задание по спринту 4", "Дописать проект и сдать на ревью", Status.IN_PROGRESS, epic2)));
        System.out.println("EPIC");
        System.out.println(inMemoryTaskManager.updateTask(new Epic(epic1.getId(), "Продать квартиру быстро", "продать квартиру через Агенство", Status.DONE)));
        System.out.println("---GET_BY_ID---");
        System.out.println("id 4 :" + inMemoryTaskManager.getById(4));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 2 :" + inMemoryTaskManager.getById(2));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 6 :" + inMemoryTaskManager.getById(6));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 2 :" + inMemoryTaskManager.getById(2));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 3 :" + inMemoryTaskManager.getById(3));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 4 :" + inMemoryTaskManager.getById(4));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 4 :" + inMemoryTaskManager.getById(4));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 1 :" + inMemoryTaskManager.getById(1));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 5 :" + inMemoryTaskManager.getById(5));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 1 :" + inMemoryTaskManager.getById(1));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("id 2 :" + inMemoryTaskManager.getById(2));
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("---GET_SUBTASKS_BY_EPIC---");
        System.out.println("Подзадачи " + epic2 + " :" + inMemoryTaskManager.getSubtaskByEpic(epic2));
        System.out.println("---DELETE_BY_ID---");
        inMemoryTaskManager.deleteById(1);
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        inMemoryTaskManager.deleteById(3);
        System.out.println("История :" + inMemoryHistoryManager.getHistory());
        System.out.println("---DELETE_ALL_BY_TYPE---");
        System.out.println("TASK");
        inMemoryTaskManager.deleteAllTasks(Type.TASK);
        System.out.println("История :" + inMemoryHistoryManager.getHistory());

        System.out.println("SUBTASK");
        inMemoryTaskManager.deleteAllTasks(Type.SUBTASK);
        System.out.println("История :" + inMemoryHistoryManager.getHistory());

        System.out.println("EPIC");
        inMemoryTaskManager.deleteAllTasks(Type.EPIC);
        System.out.println("История :" + inMemoryHistoryManager.getHistory());

    }
}
