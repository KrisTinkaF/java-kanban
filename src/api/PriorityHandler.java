package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class PriorityHandler extends BaseHttpHandler implements HttpHandler {
    public PriorityHandler(TaskManager fileBackedTaskManager) {
        PriorityHandler.fileBackedTaskManager = fileBackedTaskManager;
    }

    private static TaskManager fileBackedTaskManager;


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case PRIORITIZED: {
                handlePriority(exchange);
                break;
            }
            default:
                writeResponse(exchange, "Такого эндпоинта не существует", 404);
        }
    }

    private void handlePriority(HttpExchange exchange) throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationTypeAdapter());
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();

        String tasks = gson.toJson(fileBackedTaskManager.getPrioritizedTasks());

        writeResponse(exchange, tasks, 200);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (requestMethod.equals("GET")) {
            if (pathParts[1].equals("prioritized")) {
                if (pathParts.length == 2) {
                    return Endpoint.PRIORITIZED;
                }
            }
        }

        return Endpoint.UNKNOWN;
    }
}
