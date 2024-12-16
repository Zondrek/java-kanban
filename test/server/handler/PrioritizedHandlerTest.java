package server.handler;

import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.Task;
import org.junit.jupiter.api.Test;
import util.HttpUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static util.TestTaskFabric.*;

class PrioritizedHandlerTest extends BaseHandlerTest {

    private final HttpRequest request = HttpUtil.get(URI.create("http://localhost:8080/prioritized"));

    PrioritizedHandlerTest() throws IOException {
    }

    @Test
    public void getPrioritized() throws IOException, InterruptedException {
        taskManager.upsertTask(
                createTestTask(
                        LocalDateTime.of(2024, 11, 20, 12, 0),
                        Duration.ofMinutes(1)
                )
        );
        Epic epic = taskManager.upsertEpic(createTestEpic());
        taskManager.upsertSubTask(
                createTestSubTask(
                        LocalDateTime.of(2024, 11, 20, 11, 0),
                        Duration.ofMinutes(1),
                        epic.getId()
                )
        );
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        List<Task> fromManager = taskManager.getPrioritizedTasks();
        List<Task> fromServer = gson.fromJson(
                response.body(),
                new TypeToken<List<Task>>() {
                }.getType()
        );
        assertIterableEquals(fromManager, fromServer);
    }

    @Test
    public void getPrioritizedEmpty() throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
    }
}