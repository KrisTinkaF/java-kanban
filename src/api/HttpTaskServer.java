package api;

import com.sun.net.httpserver.HttpServer;
import manager.HistoryManager;
import manager.Managers;
import manager.TaskManager;
import model.CrossTimeException;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {

    public HttpTaskServer(TaskManager fileBackedTaskManager, HistoryManager inMemoryHistoryManager) {
        this.taskManager = fileBackedTaskManager;
        this.inMemoryHistoryManager = inMemoryHistoryManager;
    }

    private static final int PORT = 8080;

    private final TaskManager taskManager;

    private final HistoryManager inMemoryHistoryManager;

    private static HttpServer httpServer;

    public static void main(String[] args) throws CrossTimeException {
        final TaskManager inMemoryTaskManager = Managers.getDefault();
        final HistoryManager inMemoryHistoryManager = inMemoryTaskManager.getInMemoryHistoryManager();
        final TaskManager fileBackedTaskManager = Managers.getDefaultBackup();
        HttpTaskServer httpTaskServer = new HttpTaskServer(fileBackedTaskManager, inMemoryHistoryManager);
        httpTaskServer.start();
    }

    public void start() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
            httpServer.createContext("/tasks", new TaskHandler(taskManager));
            httpServer.createContext("/subtasks", new SubtaskHandler(taskManager));
            httpServer.createContext("/epics", new EpicHandler(taskManager));
            httpServer.createContext("/history", new HistoryHandler(inMemoryHistoryManager));
            httpServer.createContext("/prioritized", new PriorityHandler(taskManager));
            httpServer.start();
            System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("HTTP-сервер остановлен!");
    }

}
