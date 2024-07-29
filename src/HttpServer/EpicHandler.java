package HttpServer;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.*;

import java.io.IOException;

import com.google.gson.Gson;
import model.Type;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    public EpicHandler(TaskManager fileBackedTaskManager) {
        EpicHandler.fileBackedTaskManager = fileBackedTaskManager;
    }

    private static TaskManager fileBackedTaskManager;


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_EPIC: {
                handleGetTask(fileBackedTaskManager, exchange, Type.EPIC);
                break;
            }
            case GET_EPICS: {
                handleGetTasks(fileBackedTaskManager, exchange, Type.EPIC);
                break;
            }
            case POST_EPIC: {
                handlePostEpic(exchange);
                break;
            }
            case DELETE_EPIC: {
                handleDeleteTask(fileBackedTaskManager, exchange, Type.EPIC);
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

            Epic epic = gson.fromJson(body, Epic.class);

            if (hasId) {
                fileBackedTaskManager.updateTask(epic);
            } else {
                fileBackedTaskManager.createTask(epic);
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

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter());
            gsonBuilder.registerTypeAdapter(Duration.class, new DurationTypeAdapter());
            gsonBuilder.setPrettyPrinting();
            Gson gson = gsonBuilder.create();

            String response = gson.toJson(fileBackedTaskManager.getSubtaskByEpic(id));

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

