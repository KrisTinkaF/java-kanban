package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;

import java.io.IOException;

public class PriorityHandler extends BaseHttpHandler implements HttpHandler {
    public PriorityHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    private final TaskManager taskManager;


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
        String tasks = gson.toJson(taskManager.getPrioritizedTasks());
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
