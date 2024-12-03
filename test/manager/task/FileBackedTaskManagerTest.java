package manager.task;

import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static util.TestTaskFabric.*;

class FileBackedTaskManagerTest extends BaseTaskManagerTest<FileBackedTaskManager> {

    private final File emptyFile = new File("test/resources/empty.csv");

    private File tempFile;
    private FileBackedTaskManager tempManager;

    private static Epic epic;
    private static Task task;
    private static SubTask subTask;

    // Для тестирования базового функционала
    @Override
    protected FileBackedTaskManager createInstance() {
        try {
            return FileBackedTaskManager.loadFromFile(File.createTempFile("main_test", ".csv"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    static void beforeAll() {
        task = createTestTask(
                2,
                LocalDateTime.of(2024, 11, 20, 12, 0),
                Duration.ofMinutes(1)
        );
        subTask = createTestSubTask(
                3,
                LocalDateTime.of(2024, 11, 20, 18, 0),
                Duration.ofMinutes(100),
                1
        );
    }

    @BeforeEach
    @Override
    protected void beforeEach() {
        super.beforeEach();

        // Из-за необходимости пересоздания мапы внутри эпика, создаем перед каждым тестом
        epic = createTestEpic(1);

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

        assertEquals(1, manager.getTasks().size());
        assertEquals(1, manager.getEpics().size());
        assertEquals(1, manager.getSubTasks().size());

        assertIterableEquals(manager.getTasks(), tempManager.getTasks());
        assertIterableEquals(manager.getEpics(), tempManager.getEpics());
        assertIterableEquals(manager.getSubTasks(), tempManager.getSubTasks());
    }

    @Test
    public void writeAndLoadFileWithOrdered() {
        tempManager.upsertTask(
                createTestTask(
                        LocalDateTime.of(2024, 11, 20, 10, 0),
                        Duration.ofMinutes(10)
                )
        );

        int epicId = tempManager.upsertEpic(createTestEpic()).getId();

        tempManager.upsertSubTask(
                createTestSubTask(
                        LocalDateTime.of(2024, 11, 20, 10, 5),
                        Duration.ofMinutes(10),
                        epicId
                )
        );

        tempManager.upsertSubTask(
                createTestSubTask(
                        LocalDateTime.of(2024, 11, 20, 9, 50),
                        Duration.ofMinutes(10),
                        epicId
                )
        );

        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(tempFile);

        assertIterableEquals(
                tempManager.getPrioritizedTasks(),
                manager.getPrioritizedTasks()
        );
    }

    @Test
    public void writeTaskAndLoadFile() {
        tempManager.upsertTask(task);

        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(tempFile);

        Task newTask = manager.getTask(task.getId());

        assertEqualsTask(task, newTask);
        assertIterableEquals(manager.getTasks(), tempManager.getTasks());
    }

    @Test
    public void writeEpicAndLoadFile() {
        tempManager.upsertEpic(epic);

        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(tempFile);

        Task newEpic = manager.getEpic(epic.getId());

        assertNull(newEpic.getStartTime());
        assertNull(newEpic.getDuration());
        assertIterableEquals(manager.getEpics(), tempManager.getEpics());
    }

    @Test
    public void writeEpicWithSubTaskAndLoadFile() {
        tempManager.upsertEpic(epic);
        tempManager.upsertSubTask(subTask);

        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(tempFile);

        Epic newEpic = manager.getEpic(epic.getId());
        SubTask newSubTask = manager.getSubTask(subTask.getId());

        assertEqualsTask(tempManager.getEpic(epic.getId()), newEpic);
        assertIterableEquals(epic.getSubTasks(), newEpic.getSubTasks());
        assertIterableEquals(manager.getEpics(), tempManager.getEpics());

        assertEqualsTask(subTask, newSubTask);
        assertEquals(subTask.getEpicId(), newSubTask.getEpicId());
        assertIterableEquals(manager.getSubTasks(), tempManager.getSubTasks());
    }

    private void assertEqualsTask(Task t1, Task t2) {
        assertEquals(t1.getType(), t2.getType());
        assertEquals(t1.getStatus(), t2.getStatus());
        assertEquals(t1.getName(), t2.getName());
        assertEquals(t1.getDescription(), t2.getDescription());
        assertEquals(t1.getStartTime(), t2.getStartTime());
        assertEquals(t1.getDuration(), t2.getDuration());
    }
}