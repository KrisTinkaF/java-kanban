package api;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.*;

import java.io.IOException;

import model.Type;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    public EpicHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    private final TaskManager taskManager;


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_EPIC: {
                handleGetTask(taskManager, exchange, Type.EPIC);
                break;
            }
            case GET_EPICS: {
                handleGetTasks(taskManager, exchange, Type.EPIC);
                break;
            }
            case POST_EPIC: {
                handlePostEpic(exchange);
                break;
            }
            case DELETE_EPIC: {
                handleDeleteTask(taskManager, exchange, Type.EPIC);
                break;
            }
            case GET_EPICS_SUBTASKS: {
                handleGetEpicSubtasks(exchange);
                break;
            }
            default:
                writeResponse(exchange, "Такого эндпоинта не существует", 404);
        }
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {

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
            Epic epic = gson.fromJson(body, Epic.class);
            if (!hasId) {
                taskManager.createTask(epic);
            } else {
                int id = jsonObject.get("id").getAsInt();
                if (id == 0) {
                    taskManager.createTask(epic);
                } else {
                    taskManager.updateTask(epic);
                }
            }
            String response = gson.toJson(epic);
            writeResponse(exchange, response, 201);
        } catch (CrossTimeException e) {
            writeResponse(exchange, e.getMessage(), 404);
        } catch (Exception e) {
            writeResponse(exchange, e.getMessage(), 400);
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange) throws IOException {
        Optional<Integer> epicId = getIdFromRequest(exchange);
        if (epicId.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор", 400);
            return;
        }
        int id = epicId.get();
        try {
            String response = gson.toJson(taskManager.getSubtaskByEpic(id));
            writeResponse(exchange, response, 200);
        } catch (NotFoundException e) {
            writeResponse(exchange, "Эпик с id: " + id + " не найден!", 404);
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (requestMethod.equals("GET")) {

            if (pathParts[1].equals("epics")) {
                if (pathParts.length == 2) {
                    return Endpoint.GET_EPICS;
                }
                if (pathParts.length == 3) {
                    return Endpoint.GET_EPIC;
                }
                if (pathParts.length == 4 && pathParts[3].equals("subtasks")) {
                    return Endpoint.GET_EPICS_SUBTASKS;
                }
            }
        }
        if (requestMethod.equals("POST") && pathParts[1].equals("epics") && pathParts.length == 2) {
            return Endpoint.POST_EPIC;
        }
        if (requestMethod.equals("DELETE") && pathParts[1].equals("epics") && pathParts.length == 3) {
            return Endpoint.DELETE_EPIC;
        }

        return Endpoint.UNKNOWN;
    }


}

