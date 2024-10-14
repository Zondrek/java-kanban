package manager.task;

import manager.Managers;
import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private InMemoryTaskManager taskManager;
    private final Random random = new Random();

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
        Task newTask = createTestTask(TaskStatus.DONE);
        newTask.setId(task.getId());
        taskManager.upsertTask(newTask);
        assertNotSame(task, newTask);
        assertEquals(TaskStatus.DONE, taskManager.getTask(task.getId()).getStatus());
    }

    @Test
    void updateEpic() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        Epic newEpic = createTestEpic();
        newEpic.setId(epic.getId());
        taskManager.upsertEpic(newEpic);
        assertNotSame(epic, newEpic);
        assertEquals(newEpic, taskManager.getEpic(epic.getId()));
    }

    @Test
    void updateSubTask() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        SubTask subTask = taskManager.upsertSubTask(createTestSubTask(epic.getId()));
        SubTask newSubTask = createTestSubTask(epic.getId(), TaskStatus.DONE);
        newSubTask.setId(subTask.getId());
        taskManager.upsertSubTask(newSubTask);
        assertNotSame(subTask, newSubTask);
        assertEquals(TaskStatus.DONE, taskManager.getSubTask(subTask.getId()).getStatus());
    }

    @Test
    void getSubTasksByEpicId() {
        Epic otherEpic = taskManager.upsertEpic(createTestEpic());
        taskManager.upsertSubTask(createTestSubTask(otherEpic.getId()));

        Epic epic = taskManager.upsertEpic(createTestEpic());
        List<SubTask> list = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            SubTask subTask = taskManager.upsertSubTask(createTestSubTask(epic.getId()));
            list.add(subTask);
        }
        assertEquals(6, taskManager.getSubTasks().size());
        assertEquals(list.size(), taskManager.getSubTasks(epic.getId()).size());
        assertIterableEquals(list, taskManager.getSubTasks(epic.getId()));
    }

    @Test
    void removeAllTasks() {
        for (int i = 0; i < 5; i++) {
            taskManager.upsertTask(createTestTask());
        }
        taskManager.removeTasks();
        assertTrue(taskManager.getTasks().isEmpty());
    }

    @Test
    void removeTaskById() {
        List<Task> list = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            list.add(taskManager.upsertTask(createTestTask()));
        }
        for (int i = 0; i < list.size(); i++) {
            int removeId = list.get(random.nextInt(list.size())).getId();
            taskManager.removeTask(removeId);
            assertNull(taskManager.getTask(removeId));
        }
    }

    @Test
    void removeAllEpics() {
        for (int i = 0; i < 5; i++) {
            taskManager.upsertEpic(createTestEpic());
        }
        taskManager.removeEpics();
        assertTrue(taskManager.getEpics().isEmpty());
    }

    @Test
    void removeEpicById() {
        List<Epic> list = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            list.add(taskManager.upsertEpic(createTestEpic()));
        }
        for (int i = 0; i < list.size(); i++) {
            int removeId = list.get(random.nextInt(list.size())).getId();
            taskManager.removeEpic(removeId);
            assertNull(taskManager.getEpic(removeId));
        }
    }

    @Test
    void subtasksDeletedAlongWithEpic() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        SubTask subTask1 = taskManager.upsertSubTask(createTestSubTask(epic.getId()));
        SubTask subTask2 = taskManager.upsertSubTask(createTestSubTask(epic.getId()));
        taskManager.removeEpic(epic.getId());
        assertNull(taskManager.getSubTask(subTask1.getId()));
        assertNull(taskManager.getSubTask(subTask2.getId()));
    }

    @Test
    void removeAllSubTasks() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        for (int i = 0; i < 5; i++) {
            taskManager.upsertSubTask(createTestSubTask(epic.getId()));
        }
        taskManager.removeSubTasks();
        assertTrue(taskManager.getSubTasks().isEmpty());
    }

    @Test
    void removeSubTaskById() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        List<SubTask> list = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            list.add(taskManager.upsertSubTask(createTestSubTask(epic.getId())));
        }
        for (int i = 0; i < list.size(); i++) {
            int removeId = list.get(random.nextInt(list.size())).getId();
            taskManager.removeSubTask(removeId);
            assertNull(taskManager.getSubTask(removeId));
        }
    }

    @Test
    void epicStatusNewEmptySubTask() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        assertEquals(TaskStatus.NEW, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusNewWithSubTask() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        taskManager.upsertSubTask(createTestSubTask(epic.getId(), TaskStatus.NEW));
        assertEquals(TaskStatus.NEW, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusInProgressWith2SubTask_NewDone() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        taskManager.upsertSubTask(createTestSubTask(epic.getId(), TaskStatus.NEW));
        taskManager.upsertSubTask(createTestSubTask(epic.getId(), TaskStatus.DONE));
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusInProgressWith2SubTask_InProgressDone() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        taskManager.upsertSubTask(createTestSubTask(epic.getId(), TaskStatus.IN_PROGRESS));
        taskManager.upsertSubTask(createTestSubTask(epic.getId(), TaskStatus.DONE));
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusInProgressWith3SubTask_NewInProgressDone() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        taskManager.upsertSubTask(createTestSubTask(epic.getId(), TaskStatus.NEW));
        taskManager.upsertSubTask(createTestSubTask(epic.getId(), TaskStatus.IN_PROGRESS));
        taskManager.upsertSubTask(createTestSubTask(epic.getId(), TaskStatus.DONE));
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusDoneWithSubTask() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        taskManager.upsertSubTask(createTestSubTask(epic.getId(), TaskStatus.DONE));
        assertEquals(TaskStatus.DONE, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicChangeStatusWithoutSubTasks_DoneAfterCreate() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        Epic newEpic = new Epic(epic, TaskStatus.DONE);
        taskManager.upsertEpic(newEpic);
        assertEquals(TaskStatus.NEW, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicChangeStatusWithoutSubTasks_DoneInCreate() {
        Epic epic = new Epic(createTestEpic(), TaskStatus.DONE);
        taskManager.upsertEpic(epic);
        assertEquals(TaskStatus.NEW, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void getHistory() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        for (int i = 0; i < 5; i++) {
            taskManager.getEpic(epic.getId());
        }
        assertEquals(5, taskManager.getHistory().size());
    }

    private Task createTestTask() {
        return new Task(
                "TestTaskName",
                "TestTaskDescription"
        );
    }

    private Task createTestTask(TaskStatus status) {
        Task task = new Task(
                "TestTaskName",
                "TestTaskDescription"
        );
        return new Task(task, status);
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

    private SubTask createTestSubTask(int epicId, TaskStatus status) {
        SubTask subTask = new SubTask(
                "TestSubTaskName",
                "TestSubTaskDescription",
                epicId
        );

        return new SubTask(subTask, status);
    }
}