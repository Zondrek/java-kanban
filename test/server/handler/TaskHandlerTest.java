package server.handler;

import com.google.gson.reflect.TypeToken;
import model.Task;
import org.junit.jupiter.api.Test;
import util.AssertUtil;
import util.HttpUtil;
import util.TestTaskFabric;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static util.TestTaskFabric.createTestTask;

class TaskHandlerTest extends BaseHandlerTest {

    private final URI tasksUri = URI.create("http://localhost:8080/tasks");

    TaskHandlerTest() throws IOException {
    }

    @Test
    public void postTask() throws IOException, InterruptedException {
        Task task = TestTaskFabric.createTestTask();
        String body = gson.toJson(task);
        HttpRequest request = HttpUtil.post(tasksUri, body);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        Collection<Task> fromManager = taskManager.getTasks();
        assertEquals(1, fromManager.size());
        AssertUtil.assertEqualsTask(task, fromManager.iterator().next());
    }

    @Test
    public void postTaskWithIntersection() throws IOException, InterruptedException {
        Task task1 = TestTaskFabric.createTestTask(
                1,
                LocalDateTime.of(2024, 11, 20, 10, 0),
                Duration.ofMinutes(1)
        );
        Task task2 = TestTaskFabric.createTestTask(
                2,
                LocalDateTime.of(2024, 11, 20, 10, 0),
                Duration.ofMinutes(1)
        );
        taskManager.upsertTask(task1);
        String body = gson.toJson(task2);
        HttpRequest request = HttpUtil.post(tasksUri, body);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        Collection<Task> fromManager = taskManager.getTasks();
        assertEquals(1, fromManager.size());
        assertEquals(task1.getId(), fromManager.iterator().next().getId());
        AssertUtil.assertEqualsTask(task1, fromManager.iterator().next());
    }

    @Test
    public void getTasks() throws IOException, InterruptedException {
        taskManager.upsertTask(
                createTestTask(
                        LocalDateTime.of(2024, 11, 20, 10, 0),
                        Duration.ofMinutes(1)
                )
        );
        taskManager.upsertTask(
                createTestTask(
                        LocalDateTime.of(2024, 11, 20, 10, 5),
                        Duration.ofMinutes(1)
                )
        );
        HttpRequest request = HttpUtil.get(tasksUri);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        List<Task> fromManager = List.copyOf(taskManager.getTasks());
        List<Task> fromServer = gson.fromJson(
                response.body(),
                new TypeToken<List<Task>>() {
                }.getType()
        );
        assertIterableEquals(fromManager, fromServer);
        for (int i = 0; i < fromManager.size(); i++) {
            AssertUtil.assertEqualsTask(fromServer.get(i), fromManager.get(i));
        }
    }

    @Test
    public void getTasksEmpty() throws IOException, InterruptedException {
        HttpRequest request = HttpUtil.get(tasksUri);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getTasks().isEmpty());
    }

    @Test
    public void getTaskById() throws IOException, InterruptedException {
        Task task = taskManager.upsertTask(TestTaskFabric.createTestTask());
        URI url = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest request = HttpUtil.get(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Task taskFromServer = gson.fromJson(response.body(), Task.class);
        AssertUtil.assertEqualsTask(task, taskFromServer);
    }

    @Test
    public void deleteTaskById() throws IOException, InterruptedException {
        Task task = taskManager.upsertTask(TestTaskFabric.createTestTask());
        URI url = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest request = HttpUtil.delete(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getTasks().isEmpty());
    }
}