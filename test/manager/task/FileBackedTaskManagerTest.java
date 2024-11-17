package manager.task;

import manager.task.converter.TaskConverter;
import model.Epic;
import model.SubTask;
import model.Task;
import model.dto.TaskDto;
import model.dto.TaskType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public void writeEpic() {
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            tempManager.upsertEpic(epic);
            TaskDto dto = TaskConverter.stringToDto(reader.readLine());
            assertEquals(TaskType.EPIC, TaskConverter.stringToType(dto.type()));
            assertEquals(epic, TaskConverter.dtoToEpic(dto));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void writeSubTask() {
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {

            tempManager.upsertEpic(epic);
            tempManager.upsertSubTask(subTask);

            TaskDto epicDto = TaskConverter.stringToDto(reader.readLine());
            TaskDto subTaskDto = TaskConverter.stringToDto(reader.readLine());

            assertEquals(TaskType.EPIC, TaskConverter.stringToType(epicDto.type()));
            assertEquals(epic, TaskConverter.dtoToEpic(epicDto));

            assertEquals(TaskType.SUBTASK, TaskConverter.stringToType(subTaskDto.type()));
            assertEquals(subTask, TaskConverter.dtoToSubTask(subTaskDto));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void writeTask() {
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            tempManager.upsertTask(task);
            TaskDto dto = TaskConverter.stringToDto(reader.readLine());
            assertEquals(TaskType.TASK, TaskConverter.stringToType(dto.type()));
            assertEquals(task, TaskConverter.dtoToTask(dto));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void writeAllTaskType() {
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {

            tempManager.upsertTask(task);
            tempManager.upsertEpic(epic);
            tempManager.upsertSubTask(subTask);

            TaskDto taskDto = TaskConverter.stringToDto(reader.readLine());
            TaskDto epicDto = TaskConverter.stringToDto(reader.readLine());
            TaskDto subTaskDto = TaskConverter.stringToDto(reader.readLine());

            assertEquals(TaskType.TASK, TaskConverter.stringToType(taskDto.type()));
            assertEquals(task, TaskConverter.dtoToTask(taskDto));

            assertEquals(TaskType.EPIC, TaskConverter.stringToType(epicDto.type()));
            assertEquals(epic, TaskConverter.dtoToEpic(epicDto));

            assertEquals(TaskType.SUBTASK, TaskConverter.stringToType(subTaskDto.type()));
            assertEquals(subTask, TaskConverter.dtoToSubTask(subTaskDto));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void readAllTaskType() {
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(allTypesFile);

        Collection<Task> tasks = manager.getTasks();
        Collection<Epic> epics = manager.getEpics();
        Collection<SubTask> subTasks = manager.getSubTasks();

        assertEquals(1, tasks.size());
        assertEquals(task, tasks.iterator().next());

        assertEquals(1, epics.size());
        assertEquals(epic, epics.iterator().next());

        assertEquals(1, subTasks.size());
        assertEquals(subTask, subTasks.iterator().next());
    }
}