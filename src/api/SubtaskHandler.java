package api;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.CrossTimeException;
import model.Subtask;
import model.Type;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {

    public SubtaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    private final TaskManager taskManager;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_SUBTASK: {
                handleGetTask(taskManager, exchange, Type.SUBTASK);
                break;
            }
            case GET_SUBTASKS: {
                handleGetTasks(taskManager, exchange, Type.SUBTASK);
                break;
            }
            case POST_SUBTASK: {
                handlePostSubtask(exchange);
                break;
            }
            case DELETE_SUBTASK: {
                handleDeleteTask(taskManager, exchange, Type.SUBTASK);
                break;
            }
            default:
                writeResponse(exchange, "Такого эндпоинта не существует", 404);
        }
    }

    private void handlePostSubtask(HttpExchange exchange) throws IOException {

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

        boolean hasEpicId = jsonObject.has("parentId");

        if (!hasEpicId) {
            writeResponse(exchange, "Поле \"parentId\" обязательно!", 400);
            return;
        }

        boolean hasId = jsonObject.has("id");
        try {
            Subtask subtask = gson.fromJson(body, Subtask.class);
            if (hasId) {
                taskManager.updateTask(subtask);
            } else {
                taskManager.createTask(subtask);
            }
            String response = gson.toJson(subtask);
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

            if (pathParts[1].equals("subtasks")) {
                if (pathParts.length == 2) {
                    return Endpoint.GET_SUBTASKS;
                }
                if (pathParts.length == 3) {
                    return Endpoint.GET_SUBTASK;
                }
            }
        }
        if (requestMethod.equals("POST") && pathParts[1].equals("subtasks") && pathParts.length == 2) {
            return Endpoint.POST_SUBTASK;
        }
        if (requestMethod.equals("DELETE") && pathParts[1].equals("subtasks") && pathParts.length == 3) {
            return Endpoint.DELETE_SUBTASK;
        }

        return Endpoint.UNKNOWN;
    }


}
