package api;

import com.sun.net.httpserver.HttpServer;
import manager.HistoryManager;
import manager.Managers;
import manager.TaskManager;
import model.CrossTimeException;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws CrossTimeException {
        final TaskManager inMemoryTaskManager = Managers.getDefault();
        final HistoryManager inMemoryHistoryManager = inMemoryTaskManager.getInMemoryHistoryManager();
        final TaskManager fileBackedTaskManager = Managers.getDefaultBackup();
        start(fileBackedTaskManager, inMemoryHistoryManager);

    }

    public static void start(TaskManager fileBackedTaskManager, HistoryManager inMemoryHistoryManager) {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
            httpServer.createContext("/tasks", new TaskHandler(fileBackedTaskManager));
            httpServer.createContext("/subtasks", new SubtaskHandler(fileBackedTaskManager));
            httpServer.createContext("/epics", new EpicHandler(fileBackedTaskManager));
            httpServer.createContext("/history", new HistoryHandler(inMemoryHistoryManager));
            httpServer.createContext("/prioritized", new PriorityHandler(fileBackedTaskManager));
            httpServer.start();
            System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
