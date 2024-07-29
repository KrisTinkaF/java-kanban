package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.NotFoundException;
import model.Task;
import model.Type;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class BaseHttpHandler {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    protected void writeResponse(HttpExchange exchange,
                                 String responseString,
                                 int responseCode) throws IOException {
        try (OutputStream os = exchange.getResponseBody()) {
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            exchange.sendResponseHeaders(responseCode, 0);
            os.write(responseString.getBytes(DEFAULT_CHARSET));
        }
        exchange.close();
    }

    protected void handleGetTask(TaskManager fileBackedTaskManager, HttpExchange exchange, Type type) throws IOException {

        Optional<Integer> taskId = getIdFromRequest(exchange);
        if(taskId.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор", 400);
            return;
        }
        int id = taskId.get();

        try {
            Task task = fileBackedTaskManager.getById(id);

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter());
            gsonBuilder.setPrettyPrinting();
            Gson gson = gsonBuilder.create();
            String response = gson.toJson(task);
            writeResponse(exchange, response, 200);
        } catch (NotFoundException e) {
            writeResponse(exchange, type+" с id: "+ id +" не найден!", 404);
        }

    }

    protected void handleGetTasks(TaskManager fileBackedTaskManager, HttpExchange exchange, Type type) throws IOException {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter());
            gsonBuilder.registerTypeAdapter(Duration.class, new DurationTypeAdapter());
            gsonBuilder.serializeNulls();
            gsonBuilder.setPrettyPrinting();
            Gson gson = gsonBuilder.create();

        System.out.println(fileBackedTaskManager.getAllTasks(type));

            String tasks = gson.toJson(fileBackedTaskManager.getAllTasks(type));

            writeResponse(exchange, tasks, 200);

    }


    protected Optional<Integer> getIdFromRequest(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    protected void handleDeleteTask(TaskManager fileBackedTaskManager, HttpExchange exchange, Type type) throws IOException {
        Optional<Integer> taskId = getIdFromRequest(exchange);
        if(taskId.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор", 400);
            return;
        }
        int id = taskId.get();

        try {
            fileBackedTaskManager.deleteById(id);
            writeResponse(exchange, type+" успешно удален!", 200);
        } catch (NotFoundException e) {
            writeResponse(exchange, type+" с id: "+ id +" не найден!", 404);
        }

    }

    class TaskListTypeToken extends TypeToken<List<Task>> {

    }
}