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
import static util.TestTaskFabric.createTestEpic;
import static util.TestTaskFabric.createTestSubTask;

class EpicHandlerTest extends BaseHandlerTest {

    private final URI epicsUri = URI.create("http://localhost:8080/epics");

    public EpicHandlerTest() throws IOException {
    }

    @Test
    public void postEpic() throws IOException, InterruptedException {
        Epic epic = createTestEpic();
        String body = gson.toJson(epic);
        HttpRequest request = HttpUtil.post(epicsUri, body);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        Collection<Epic> fromManager = taskManager.getEpics();
        assertEquals(1, fromManager.size());
        AssertUtil.assertEqualsEpic(epic, fromManager.iterator().next());
    }

    @Test
    public void getEpicSubTasks() throws IOException, InterruptedException {
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
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId() + "/subtasks");
        HttpRequest request = HttpUtil.get(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        List<SubTask> fromManager = List.copyOf(taskManager.getSubTasks(epic.getId()));
        List<SubTask> fromServer = gson.fromJson(
                response.body(),
                new TypeToken<List<SubTask>>() {
                }.getType()
        );
        assertNotNull(fromServer);
        assertIterableEquals(fromManager, fromServer);
        for (int i = 0; i < fromManager.size(); i++) {
            AssertUtil.assertEqualsSubTask(fromServer.get(i), fromManager.get(i));
        }
    }

    @Test
    public void getEpics() throws IOException, InterruptedException {
        taskManager.upsertEpic(TestTaskFabric.createTestEpic(1));
        taskManager.upsertEpic(TestTaskFabric.createTestEpic(2));
        HttpRequest request = HttpUtil.get(epicsUri);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        List<Epic> fromManager = List.copyOf(taskManager.getEpics());
        List<Epic> fromServer = gson.fromJson(
                response.body(),
                new TypeToken<List<Epic>>() {
                }.getType()
        );
        assertIterableEquals(fromServer, fromManager);
        for (int i = 0; i < fromManager.size(); i++) {
            AssertUtil.assertEqualsEpic(fromServer.get(i), fromManager.get(i));
        }
    }

    @Test
    public void getEpicsEmpty() throws IOException, InterruptedException {
        HttpRequest request = HttpUtil.get(epicsUri);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getEpics().isEmpty());
    }

    @Test
    public void getEpicById() throws IOException, InterruptedException {
        Epic epic = taskManager.upsertEpic(TestTaskFabric.createTestEpic());
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpUtil.get(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Epic fromServer = gson.fromJson(response.body(), Epic.class);
        AssertUtil.assertEqualsEpic(epic, fromServer);
    }

    @Test
    public void deleteEpicById() throws IOException, InterruptedException {
        Epic epic = taskManager.upsertEpic(TestTaskFabric.createTestEpic());
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpUtil.delete(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getEpics().isEmpty());
    }
}