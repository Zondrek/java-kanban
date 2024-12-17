package server.handler;

import com.google.gson.Gson;
import manager.Managers;
import manager.task.InMemoryTaskManager;
import manager.task.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import server.HttpTaskServer;

import java.io.IOException;
import java.net.http.HttpClient;

public class BaseHandlerTest {

    protected TaskManager taskManager = new InMemoryTaskManager(Managers.getDefaultHistory());
    protected HttpTaskServer taskServer = new HttpTaskServer(taskManager);
    protected Gson gson = HttpTaskServer.getGson();
    protected HttpClient client = HttpClient.newHttpClient();

    public BaseHandlerTest() throws IOException {
    }

    @BeforeEach
    protected void setUp() {
        taskManager.removeTasks();
        taskManager.removeSubTasks();
        taskManager.removeEpics();
        taskServer.start();
    }

    @AfterEach
    protected void shutDown() {
        taskServer.stop();
    }
}
