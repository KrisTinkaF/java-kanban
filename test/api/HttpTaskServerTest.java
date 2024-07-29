package api;

import com.google.gson.*;
import manager.FileBackedTaskManager;
import manager.HistoryManager;
import manager.Managers;
import manager.TaskManager;
import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskServerTest {

    private HttpTaskServer httpTaskServer;

    private HistoryManager inMemoryHistoryManager;

    private TaskManager taskManager;

    protected Gson gson = creteGson();

    @BeforeEach
    void createHttpServer() throws CrossTimeException {

        final TaskManager inMemoryTaskManager = Managers.getDefault();
        inMemoryHistoryManager = inMemoryTaskManager.getInMemoryHistoryManager();
        taskManager = FileBackedTaskManager.loadFromFile(new File("test/testResources/testRestore.csv"));

        httpTaskServer = new HttpTaskServer(taskManager, inMemoryHistoryManager);
        httpTaskServer.start();
    }

    @Test
    void getTasksTest() throws IOException, InterruptedException {
        HttpResponse<String> response = getGetTasksResponse();
        assertEquals(200, response.statusCode(), "Статус код не соответствует ожидаемому!");
        JsonElement jsonElement = JsonParser.parseString(response.body());
        if (!jsonElement.isJsonArray()) {
            System.out.println("Ответ от сервера не соответствует ожидаемому.");
            return;
        }
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(3, jsonArray.size(), "Колиечство задач не соответствует ожидаемому!");
    }

    @Test
    void testCreateSubtask() throws IOException, InterruptedException {
        HttpResponse<String> response = getCreateSubtaskResponse();
        assertEquals(201, response.statusCode(), "Статус код не соответствует ожидаемому!");
        assertEquals(2, taskManager.getAllTasks(Type.SUBTASK).size(), "Колиечство задач не соответствует ожидаемому!");
        taskManager.deleteById(15);
    }

    @Test
    void testDeleteEpic() throws IOException, InterruptedException, CrossTimeException {
        taskManager.createTask(new Epic("Закрыть жесткий дедлайн", "Сдать все долги на ЯП"));
        HttpResponse<String> response = getDeleteEpicResponse();
        assertEquals(200, response.statusCode(), "Статус код не соответствует ожидаемому!");
        assertEquals(1, taskManager.getAllTasks(Type.EPIC).size(), "Колиечство задач не соответствует ожидаемому!");

    }

    @Test
    void testGetHistory() throws IOException, InterruptedException {

        taskManager.getById(11);
        taskManager.getById(12);
        taskManager.getById(11);

        HttpResponse<String> response = getHistoryResponse();
        assertEquals(200, response.statusCode(), "Статус код не соответствует ожидаемому!");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        if (!jsonElement.isJsonArray()) {
            System.out.println("Ответ от сервера не соответствует ожидаемому.");
            return;
        }
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(2, jsonArray.size(), "Колиечство задач не соответствует ожидаемому!");
    }

    @Test
    void testGetPrioritized() throws IOException, InterruptedException {
        HttpResponse<String> response = getPrioritizedResponse();
        assertEquals(200, response.statusCode(), "Статус код не соответствует ожидаемому!");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        if (!jsonElement.isJsonArray()) {
            System.out.println("Ответ от сервера не соответствует ожидаемому.");
            return;
        }
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(3, jsonArray.size(), "Колиечство задач не соответствует ожидаемому!");
    }

    @AfterEach
    void stopHttpServer() {
        httpTaskServer.stop();
    }

    private Gson creteGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationTypeAdapter());
        gsonBuilder.serializeNulls();
        gsonBuilder.setPrettyPrinting();
        return gsonBuilder.create();
    }

    private HttpResponse<String> getPrioritizedResponse() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getHistoryResponse() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getDeleteEpicResponse() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/15");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getCreateSubtaskResponse() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString("{\n" +
                        "    \"type\":\"SUBTASK\",\n" +
                        "    \"name\":\"Имя\",\n" +
                        "    \"description\":\"описание\",\n" +
                        "    \"status\":\"NEW\",\n" +
                        "    \"startTime\":\"21-12-2024, 09:30\",\n" +
                        "    \"duration\":\"PT30M\",\n" +
                        "    \"parentId\":13\n" +
                        "}"))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getCreateSubtaskResponse2() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Имя", "описание", LocalDateTime.of(2024, 7, 21, 9, 30), Duration.ofMinutes(30), taskManager.getById(13).getId());
        subtask.setStatus(Status.NEW);
        HttpClient client = HttpClient.newHttpClient();
        System.out.println("sub " + subtask);
        System.out.println("sub json " + gson.toJson(subtask));

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask)))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getGetTasksResponse() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

}
