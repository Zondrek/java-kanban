package manager.task;

import manager.Managers;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private InMemoryTaskManager taskManager;

    @BeforeEach
    void beforeEach() {
        taskManager = new InMemoryTaskManager(Managers.getDefaultHistory());
    }

    @Test
    void theIdentifierIsAssignedAccordingToTheIteration() {
        for (int i = 0, taskId = 0, epicId = 1, subTaskId = 2; i < 5; i++, taskId += 3, epicId += 3, subTaskId += 3) {
            Task task = taskManager.upsertTask(createTestTask());
            Epic epic = taskManager.upsertEpic(createTestEpic());
            SubTask subTask = taskManager.upsertSubTask(createTestSubTask(epic.getId()));
            assertEquals(taskId, task.getId());
            assertEquals(epicId, epic.getId());
            assertEquals(subTaskId, subTask.getId());
        }
    }

    @Test
    void shouldReturnTasksAfterAdding5Item() {
        List<Task> tasks = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            Task task = taskManager.upsertTask(createTestTask());
            tasks.add(task);
        }
        assertEquals(tasks.size(), taskManager.getTasks().size());
        assertIterableEquals(tasks, taskManager.getTasks()); // проверка на порядок
    }

    @Test
    void shouldReturnEpicsAfterAdding5Item() {
        List<Epic> epics = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            Epic epic = taskManager.upsertEpic(createTestEpic());
            epics.add(epic);
        }
        assertEquals(epics.size(), taskManager.getEpics().size());
        assertIterableEquals(epics, taskManager.getEpics()); // проверка на порядок
    }

    @Test
    void shouldReturnSubTasksAfterAdding5Item() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        List<SubTask> subTasks = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            SubTask subTask = taskManager.upsertSubTask(createTestSubTask(epic.getId()));
            subTasks.add(subTask);
        }
        assertEquals(subTasks.size(), taskManager.getSubTasks().size());
        assertIterableEquals(subTasks, taskManager.getSubTasks()); // проверка на порядок
    }

    @Test
    void shouldReturnTaskAfterAddingItem() {
        Task task = taskManager.upsertTask(createTestTask());
        Task searchTask = taskManager.getTask(task.getId());
        assertEquals(task, searchTask);
    }

    @Test
    void shouldReturnEpicAfterAddingItem() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        Epic searchEpic = taskManager.getEpic(epic.getId());
        assertEquals(epic, searchEpic);
    }

    @Test
    void shouldReturnSubTaskAfterAddingItem() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        SubTask subTask = taskManager.upsertSubTask(createTestSubTask(epic.getId()));
        SubTask searchSubTask = taskManager.getSubTask(subTask.getId());
        assertEquals(subTask, searchSubTask);
    }

    @Test
    void impossibleToAttachSubTaskToSomeoneElseEpic() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        SubTask subTask = taskManager.upsertSubTask(createTestSubTask(epic.getId() + 1)); // указываем наверняка иной id эпика
        assertNull(subTask);
    }

    @Test
    void unableAddTaskIfIdAlreadyExists() {
        Task task = taskManager.upsertTask(createTestTask());
        Epic epic = createTestEpic();
        epic.setId(task.getId());
        assertNull(taskManager.upsertEpic(epic));
    }

    @Test
    void updateTask() {
        Task task = taskManager.upsertTask(createTestTask());
        Task newTask = createTestTask();
        newTask.setId(task.getId());
        taskManager.upsertTask(newTask);
        assertEquals(newTask, taskManager.getTask(task.getId()));
    }

    @Test
    void updateEpic() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        Epic newEpic = createTestEpic();
        newEpic.setId(epic.getId());
        taskManager.upsertEpic(newEpic);
        assertEquals(newEpic, taskManager.getEpic(epic.getId()));
    }

    @Test
    void updateSubTask() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        SubTask subTask = taskManager.upsertSubTask(createTestSubTask(epic.getId()));
        SubTask newSubTask = createTestSubTask(epic.getId());
        newSubTask.setId(subTask.getId());
        taskManager.upsertSubTask(newSubTask);
        assertEquals(newSubTask, taskManager.getSubTask(subTask.getId()));
    }

    @Test
    void getSubTasksByEpicId() {

    }

    @Test
    void removeTasks() {
    }

    @Test
    void removeTask() {
    }

    @Test
    void removeEpics() {
    }

    @Test
    void removeEpic() {
    }

    @Test
    void removeSubTasks() {
    }

    @Test
    void removeSubTask() {
    }

    @Test
    void updateEpicStatus() {
    }

    @Test
    void calculateStatus() {
    }

    private Task createTestTask() {
        return new Task(
                "TestTaskName",
                "TestTaskDescription"
        );
    }

    private Epic createTestEpic() {
        return new Epic(
                "TestEpicName",
                "TestEpicDescription"
        );
    }

    private SubTask createTestSubTask(int epicId) {
        return new SubTask(
                "TestSubTaskName",
                "TestSubTaskDescription",
                epicId
        );
    }
}