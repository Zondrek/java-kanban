package server.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import manager.Managers;
import manager.task.InMemoryTaskManager;
import manager.task.TaskManager;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import util.AssertUtil;
import util.HttpUtil;
import util.TestTaskFabric;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static util.TestTaskFabric.createTestTask;

class TaskHandlerTest {

    TaskManager taskManager = new InMemoryTaskManager(Managers.getDefaultHistory());
    HttpTaskServer taskServer = new HttpTaskServer(taskManager);
    Gson gson = HttpTaskServer.getGson();
    HttpClient client = HttpClient.newHttpClient();

    TaskHandlerTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        taskManager.removeTasks();
        taskManager.removeSubTasks();
        taskManager.removeEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void postTask() throws IOException, InterruptedException {
        Task task = TestTaskFabric.createTestTask();
        String body = gson.toJson(task);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpUtil.post(url, body);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Collection<Task> tasksFromManager = taskManager.getTasks();

        assertNotNull(tasksFromManager);
        assertEquals(1, tasksFromManager.size());
        AssertUtil.assertEqualsTask(task, tasksFromManager.iterator().next());
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
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpUtil.post(url, body);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());

        Collection<Task> tasksFromManager = taskManager.getTasks();

        assertNotNull(tasksFromManager);
        assertEquals(1, tasksFromManager.size());
        assertEquals(task1.getId(), tasksFromManager.iterator().next().getId());
        AssertUtil.assertEqualsTask(task1, tasksFromManager.iterator().next());
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

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpUtil.get(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = List.copyOf(taskManager.getTasks());
        List<Task> tasksFromServer = gson.fromJson(
                response.body(),
                new TypeToken<List<Task>>() {
                }.getType()
        );

        assertNotNull(tasksFromServer);
        assertIterableEquals(tasksFromManager, tasksFromServer);

        for (int i = 0; i < tasksFromManager.size(); i++) {
            AssertUtil.assertEqualsTask(tasksFromServer.get(i), tasksFromManager.get(i));
        }
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