package api;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    public TaskHandler(TaskManager fileBackedTaskManager) {
        TaskHandler.fileBackedTaskManager = fileBackedTaskManager;
    }

    private static TaskManager fileBackedTaskManager;


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_TASK: {
                handleGetTask(fileBackedTaskManager, exchange, Type.TASK);
                break;
            }
            case GET_TASKS: {
                handleGetTasks(fileBackedTaskManager, exchange, Type.TASK);
                break;
            }
            case POST_TASK: {
                handlePostTask(exchange);
                break;
            }
            case DELETE_TASK: {
                handleDeleteTask(fileBackedTaskManager, exchange, Type.TASK);
                break;
            }
            default:
                writeResponse(exchange, "Такого эндпоинта не существует", 404);
        }
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.isEmpty()) {
            writeResponse(exchange, "Тело запроса на создание заявки не может быть пустым!", 400);
            return;
        }
        JsonElement jsonElement = JsonParser.parseString(body);
        if (!jsonElement.isJsonObject()) {
            System.out.println("Ответ от сервера не соответствует ожидаемому.");
            return;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        boolean hasType = jsonObject.has("type");

        if (!hasType) {
            writeResponse(exchange, "Поле \"type\" обязательно!", 400);
            return;
        }

        boolean hasId = jsonObject.has("id");
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter());
            gsonBuilder.registerTypeAdapter(Duration.class, new DurationTypeAdapter());
            Gson gson = gsonBuilder.create();

            Task task = gson.fromJson(body, Task.class);

            if (hasId) {
                fileBackedTaskManager.updateTask(task);
            } else {
                fileBackedTaskManager.createTask(task);
            }

            String response = gson.toJson(task);
            writeResponse(exchange, response, 201);

        } catch (CrossTimeException e) {
            writeResponse(exchange, e.getMessage(), 404);
        } catch (Exception e) {
            writeResponse(exchange, e.getMessage(), 400);
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (requestMethod.equals("GET")) {
            if (pathParts[1].equals("tasks")) {
                if (pathParts.length == 2) {
                    return Endpoint.GET_TASKS;
                }
                if (pathParts.length == 3) {
                    return Endpoint.GET_TASK;
                }
            }
        }
        if (requestMethod.equals("POST") && pathParts[1].equals("tasks") && pathParts.length == 2) {
            return Endpoint.POST_TASK;
        }
        if (requestMethod.equals("DELETE") && pathParts[1].equals("tasks") && pathParts.length == 3) {
            return Endpoint.DELETE_TASK;
        }

        return Endpoint.UNKNOWN;
    }

    class TaskListTypeToken extends TypeToken<List<Task>> {

    }

}
