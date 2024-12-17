package server.handler;

import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.SubTask;
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
import static util.TestTaskFabric.createTestSubTask;

class SubTaskHandlerTest extends BaseHandlerTest {

    private final URI subtasksUri = URI.create("http://localhost:8080/subtasks");

    SubTaskHandlerTest() throws IOException {
    }

    @Test
    public void postSubTask() throws IOException, InterruptedException {
        Epic epic = taskManager.upsertEpic(TestTaskFabric.createTestEpic());
        SubTask subTask = createTestSubTask(epic.getId());
        String subTaskBody = gson.toJson(subTask);
        HttpRequest request = HttpUtil.post(subtasksUri, subTaskBody);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        Collection<SubTask> fromManager = taskManager.getSubTasks();
        assertEquals(1, fromManager.size());
        AssertUtil.assertEqualsSubTask(subTask, fromManager.iterator().next());
    }

    @Test
    public void postSubTaskWithoutEpic() throws IOException, InterruptedException {
        SubTask subTask = createTestSubTask(0);
        String subTaskBody = gson.toJson(subTask);
        HttpRequest request = HttpUtil.post(subtasksUri, subTaskBody);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        assertTrue(taskManager.getSubTasks().isEmpty());
    }

    @Test
    public void postSubTaskWithIntersection() throws IOException, InterruptedException {
        Epic epic = taskManager.upsertEpic(TestTaskFabric.createTestEpic());
        SubTask subTask1 = createTestSubTask(
                1,
                LocalDateTime.of(2024, 11, 20, 10, 0),
                Duration.ofMinutes(1),
                epic.getId()
        );
        SubTask subTask2 = createTestSubTask(
                2,
                LocalDateTime.of(2024, 11, 20, 10, 0),
                Duration.ofMinutes(1),
                epic.getId()
        );
        taskManager.upsertSubTask(subTask1);
        String body = gson.toJson(subTask2);
        HttpRequest request = HttpUtil.post(subtasksUri, body);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        Collection<SubTask> fromManager = taskManager.getSubTasks();
        assertEquals(1, fromManager.size());
        assertEquals(subTask1.getId(), fromManager.iterator().next().getId());
        AssertUtil.assertEqualsSubTask(subTask1, fromManager.iterator().next());
    }

    @Test
    public void getSubTasks() throws IOException, InterruptedException {
        Epic epic = taskManager.upsertEpic(TestTaskFabric.createTestEpic());
        taskManager.upsertSubTask(
                createTestSubTask(
                        LocalDateTime.of(2024, 11, 20, 10, 0),
                        Duration.ofMinutes(1),
                        epic.getId()
                )
        );
        taskManager.upsertSubTask(
                createTestSubTask(
                        LocalDateTime.of(2024, 11, 20, 10, 5),
                        Duration.ofMinutes(1),
                        epic.getId()
                )
        );
        HttpRequest request = HttpUtil.get(subtasksUri);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        List<SubTask> fromManager = List.copyOf(taskManager.getSubTasks());
        List<SubTask> fromServer = gson.fromJson(
                response.body(),
                new TypeToken<List<SubTask>>() {
                }.getType()
        );
        assertIterableEquals(fromManager, fromServer);
        for (int i = 0; i < fromManager.size(); i++) {
            AssertUtil.assertEqualsSubTask(fromServer.get(i), fromManager.get(i));
        }
    }

    @Test
    public void getSubTasksEmpty() throws IOException, InterruptedException {
        HttpRequest request = HttpUtil.get(subtasksUri);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getSubTasks().isEmpty());
    }

    @Test
    public void getSubTaskById() throws IOException, InterruptedException {
        Epic epic = taskManager.upsertEpic(TestTaskFabric.createTestEpic());
        SubTask subTask = taskManager.upsertSubTask(TestTaskFabric.createTestSubTask(epic.getId()));
        URI url = URI.create("http://localhost:8080/subtasks/" + subTask.getId());
        HttpRequest request = HttpUtil.get(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        SubTask fromServer = gson.fromJson(response.body(), SubTask.class);
        AssertUtil.assertEqualsSubTask(subTask, fromServer);
    }

    @Test
    public void deleteSubTaskById() throws IOException, InterruptedException {
        Epic epic = taskManager.upsertEpic(TestTaskFabric.createTestEpic());
        SubTask subTask = taskManager.upsertSubTask(TestTaskFabric.createTestSubTask(epic.getId()));
        URI url = URI.create("http://localhost:8080/subtasks/" + subTask.getId());
        HttpRequest request = HttpUtil.delete(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getSubTasks().isEmpty());
    }
}