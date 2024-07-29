package api;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    private final TaskManager taskManager;


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_TASK: {
                handleGetTask(taskManager, exchange, Type.TASK);
                break;
            }
            case GET_TASKS: {
                handleGetTasks(taskManager, exchange, Type.TASK);
                break;
            }
            case POST_TASK: {
                handlePostTask(exchange);
                break;
            }
            case DELETE_TASK: {
                handleDeleteTask(taskManager, exchange, Type.TASK);
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
        boolean hasId = jsonObject.has("id");
        try {
            Task task = gson.fromJson(body, Task.class);

            if (hasId) {
                taskManager.updateTask(task);
            } else {
                taskManager.createTask(task);
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
