package api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import manager.FileBackedTaskManager;
import manager.HistoryManager;
import manager.Managers;
import manager.TaskManager;
import model.CrossTimeException;
import model.Epic;
import model.Type;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskServerTest {

    private static HttpTaskServer httpTaskServer;

    private HistoryManager inMemoryHistoryManager;

    private TaskManager fileBackedTaskManager;

    @BeforeEach
    void createHttpServer() throws CrossTimeException {

        final TaskManager inMemoryTaskManager = Managers.getDefault();
        inMemoryHistoryManager = inMemoryTaskManager.getInMemoryHistoryManager();
        fileBackedTaskManager = FileBackedTaskManager.loadFromFile(new File("test/testResources/testRestore.csv"));

        httpTaskServer = new HttpTaskServer(fileBackedTaskManager, inMemoryHistoryManager);
        httpTaskServer.start();
    }

    @Test
    void getTasksTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
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

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Статус код не соответствует ожидаемому!");
        assertEquals(2, fileBackedTaskManager.getAllTasks(Type.SUBTASK).size(), "Колиечство задач не соответствует ожидаемому!");
        fileBackedTaskManager.deleteById(15);
    }

    @Test
    void testDeleteEpic() throws IOException, InterruptedException, CrossTimeException {

        fileBackedTaskManager.createTask(new Epic("Закрыть жесткий дедлайн", "Сдать все долги на ЯП"));
        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/epics/15");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Статус код не соответствует ожидаемому!");
        assertEquals(1, fileBackedTaskManager.getAllTasks(Type.EPIC).size(), "Колиечство задач не соответствует ожидаемому!");

    }

    @Test
    void testGetHistory() throws IOException, InterruptedException {

        fileBackedTaskManager.getById(11);
        fileBackedTaskManager.getById(12);
        fileBackedTaskManager.getById(11);

        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
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
        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
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

}
