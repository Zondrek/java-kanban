package server.handler;

import com.google.gson.reflect.TypeToken;
import model.Task;
import org.junit.jupiter.api.Test;
import util.AssertUtil;
import util.HttpUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static util.TestTaskFabric.createTestTask;

class HistoryHandlerTest extends BaseHandlerTest {

    private final HttpRequest request = HttpUtil.get(URI.create("http://localhost:8080/history"));

    public HistoryHandlerTest() throws IOException {
    }

    @Test
    public void getHistory() throws IOException, InterruptedException {
        Task task1 = taskManager.upsertTask(
                createTestTask(
                        LocalDateTime.of(2024, 11, 20, 10, 0),
                        Duration.ofMinutes(1)
                )
        );
        Task task2 = taskManager.upsertTask(
                createTestTask(
                        LocalDateTime.of(2024, 11, 20, 10, 5),
                        Duration.ofMinutes(1)
                )
        );
        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        List<Task> fromManager = List.copyOf(taskManager.getHistory());
        List<Task> fromServer = gson.fromJson(
                response.body(),
                new TypeToken<List<Task>>() {
                }.getType()
        );
        assertIterableEquals(fromManager, fromServer);
        for (int i = 0; i < fromManager.size(); i++) {
            AssertUtil.assertEqualsTask(fromManager.get(i), fromServer.get(i));
        }
    }

    @Test
    public void getHistoryEmpty() throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getTasks().isEmpty());
    }
}