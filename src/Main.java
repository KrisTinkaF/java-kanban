import manager.TaskManager;
import model.*;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        System.out.println("---CREATE---");
        Task task1 = new Task("Купить продукты", "мясо, макароны, лук, морковь", Status.NEW);
        System.out.println(taskManager.createTask(task1));
        Task task2 = new Task("Помыть машину", "берешь и моешь", Status.NEW);
        System.out.println(taskManager.createTask(task2));
        Epic epic1 = new Epic("Продать квартиру", "продать квартиру через Агенство", Status.NEW);
        System.out.println(taskManager.createTask(epic1));
        Subtask subtask11 = new Subtask("Заключить договор с Агентсвом", null, Status.NEW, epic1.getId());
        System.out.println(taskManager.createTask(subtask11));
        //System.out.println("Подзадача добавдена в эпик " + taskManager.getSubtaskByEpic(epic1));
        Epic epic2 = new Epic("Закрыть жесткий дедлайн", "Сдать все долги на ЯП", Status.NEW);
        System.out.println(taskManager.createTask(epic2));
        Subtask subtask21 = new Subtask("Сдать проектное задание по спринту 4", "Дописать проект и сдать на ревью", Status.NEW, epic2.getId());
        System.out.println(taskManager.createTask(subtask21));
        //System.out.println("Подзадача добавдена в эпик " + taskManager.getSubtaskByEpic(epic2));
        Subtask subtask22 = new Subtask("Сдать проектное задание по спринту 5", "Изучить теорию и после выполнить проектное задание спринта 5", Status.NEW, epic2.getId());
        System.out.println(taskManager.createTask(subtask22));
        //System.out.println("Подзадача добавдена в эпик " + taskManager.getSubtaskByEpic(epic2));
        System.out.println("---PRINT---");
        System.out.println("ALL_TASKS");
        System.out.println(taskManager.getAllTasks(Type.TASK));
        System.out.println("ALL_EPICS");
        System.out.println(taskManager.getAllTasks(Type.EPIC));
        System.out.println("ALL_SUBTASKS");
        System.out.println(taskManager.getAllTasks(Type.SUBTASK));
        System.out.println("---UPDATE---");
        System.out.println("TASK");
        System.out.println(taskManager.updateTask(new Task(task1.getId(),"Купить продукты", "осталось купить только мясо", Status.IN_PROGRESS)));
        System.out.println("SUBTASK");
        System.out.println(taskManager.updateSubtask(new Subtask(subtask21.getId(),"Сдать проектное задание по спринту 4", "Дописать проект и сдать на ревью", Status.IN_PROGRESS, epic2.getId())));
        System.out.println("EPIC");
        System.out.println(taskManager.updateEpic(new Epic(epic1.getId(),"Продать квартиру быстро", "продать квартиру через Агенство", Status.DONE, Type.EPIC)));
        System.out.println("---GET_BY_ID---");
        System.out.println ("id 4 :"+ taskManager.getById(4));
        System.out.println("---GET_SUBTASKS_BY_EPIC---");
        System.out.println ("Подзадачи "+epic2+" :"+ taskManager.getSubtaskByEpic(epic2));
        System.out.println("---DELETE_BY_ID---");
        taskManager.deleteById(1);
        System.out.println("---DELETE_ALL_BY_TYPE---");
        System.out.println("TASK");
        taskManager.deleteAllTasks(Type.TASK);
        System.out.println("EPIC");
        taskManager.deleteAllTasks(Type.EPIC);
        System.out.println("SUBTASK");
        taskManager.deleteAllTasks(Type.SUBTASK);

    }
}
