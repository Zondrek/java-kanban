package manager.task;

import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class FileBackedTaskManagerTest extends BaseTaskManagerTest {

    private final File emptyFile = new File("test/resources/empty.csv");
    private final File allTypesFile = new File("test/resources/all_types.csv");

    private File tempFile;
    private FileBackedTaskManager tempManager;

    private static Epic epic;
    private static Task task;
    private static SubTask subTask;

    // Для тестирования базового функционала
    @Override
    protected TaskManager createInstance() {
        try {
            return FileBackedTaskManager.loadFromFile(File.createTempFile("main_test", ".csv"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    static void beforeAll() {
        task = new Task("Task name", "Task description");
        task.setId(2);

        subTask = new SubTask("SubTask name", "SubTask description", 1);
        subTask.setId(3);
    }

    @BeforeEach
    @Override
    protected void beforeEach() {
        super.beforeEach();

        // Из-за необходимости пересоздания мапы внутри эпика, создаем перед каждым тестом
        epic = new Epic("Epic name", "Epic description");
        epic.setId(1);

        try {
            tempFile = File.createTempFile("test", ".csv");
            tempManager = FileBackedTaskManager.loadFromFile(tempFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void loadFromFileByEmptyFile() {
        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(emptyFile));
    }

    @Test
    public void writeAndLoadFile() {
        tempManager.upsertTask(task);
        tempManager.upsertEpic(epic);
        tempManager.upsertSubTask(subTask);

        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(tempFile);

        assertIterableEquals(manager.getTasks(), tempManager.getTasks());
        assertIterableEquals(manager.getEpics(), tempManager.getEpics());
        assertIterableEquals(manager.getSubTasks(), tempManager.getSubTasks());
    }
}