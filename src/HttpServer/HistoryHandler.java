package HttpServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.HistoryManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    public HistoryHandler(HistoryManager inMemoryHistoryManager) {
        HistoryHandler.inMemoryHistoryManager = inMemoryHistoryManager;
    }

    private static HistoryManager inMemoryHistoryManager;


    @Override
    public void handle(HttpExchange exchange) throws IOException {

        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case HISTORY: {
                handleHistory(exchange);
                break;
            }
            default:
                writeResponse(exchange, "Такого эндпоинта не существует", 404);
        }

    }

    private void handleHistory(HttpExchange exchange) throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationTypeAdapter());
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();

        String tasks = gson.toJson(inMemoryHistoryManager.getHistory());

        writeResponse(exchange, tasks, 200);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (requestMethod.equals("GET")) {
            if (pathParts[1].equals("history")) {
                if (pathParts.length == 2) {
                    return Endpoint.HISTORY;
                }
            }
        }

        return Endpoint.UNKNOWN;
    }
}
