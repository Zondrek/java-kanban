package manager.task;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static util.TestTaskFabric.*;

abstract class BaseTaskManagerTest {

    private TaskManager taskManager;
    private final Random random = new Random();

    protected abstract TaskManager createInstance();

    @BeforeEach
    protected void beforeEach() {
        taskManager = createInstance();
    }

    @Test
    void theIdentifierIsAssignedAccordingToTheIteration() {
        for (int i = 0, taskId = 0, epicId = 1, subTaskId = 2; i < 5; i++, taskId += 3, epicId += 3, subTaskId += 3) {
            Task task = taskManager.upsertTask(
                    createTestTask(
                            LocalDateTime.of(2024, 11, 20, 10, i),
                            Duration.ofMinutes(1)
                    )
            );
            Epic epic = taskManager.upsertEpic(createTestEpic());
            SubTask subTask = taskManager.upsertSubTask(
                    createTestSubTask(
                            LocalDateTime.of(2024, 11, 20, 12, i),
                            Duration.ofMinutes(1),
                            epic.getId()
                    )
            );
            assertEquals(taskId, task.getId());
            assertEquals(epicId, epic.getId());
            assertEquals(subTaskId, subTask.getId());
        }
    }

    @Test
    void shouldReturnTasksAfterAdding5Item() {
        List<Task> tasks = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            Task task = taskManager.upsertTask(
                    createTestTask(
                            LocalDateTime.of(2024, 11, 20, 10, i),
                            Duration.ofMinutes(1)
                    )
            );
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
            SubTask subTask = taskManager.upsertSubTask(
                    createTestSubTask(
                            LocalDateTime.of(2024, 11, 20, 10, i),
                            Duration.ofMinutes(1),
                            epic.getId()
                    )
            );
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
        SubTask newSubTask = createTestSubTask(TaskStatus.DONE, epic.getId());
        newSubTask.setId(subTask.getId());
        taskManager.upsertSubTask(newSubTask);
        assertNotSame(subTask, newSubTask);
        assertEquals(TaskStatus.DONE, taskManager.getSubTask(subTask.getId()).getStatus());
    }

    @Test
    void getSubTasksByEpicId() {
        Epic otherEpic = taskManager.upsertEpic(createTestEpic());
        taskManager.upsertSubTask(
                createTestSubTask(
                        LocalDateTime.of(2024, 11, 20, 9, 0),
                        Duration.ofMinutes(10),
                        otherEpic.getId()
                )
        );

        Epic epic = taskManager.upsertEpic(createTestEpic());
        List<SubTask> list = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            SubTask subTask = taskManager.upsertSubTask(
                    createTestSubTask(
                            LocalDateTime.of(2024, 11, 20, 10, i),
                            Duration.ofMinutes(1),
                            epic.getId()
                    )
            );
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
            list.add(taskManager.upsertTask(
                    createTestTask(
                            LocalDateTime.of(2024, 11, 20, 10, i),
                            Duration.ofMinutes(1)
                    )
            ));
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
        SubTask subTask1 = taskManager.upsertSubTask(
                createTestSubTask(
                        LocalDateTime.of(2024, 11, 20, 10, 0),
                        Duration.ofMinutes(1),
                        epic.getId()
                )
        );
        SubTask subTask2 = taskManager.upsertSubTask(
                createTestSubTask(
                        LocalDateTime.of(2024, 11, 20, 10, 10),
                        Duration.ofMinutes(1),
                        epic.getId()
                )
        );
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
            list.add(taskManager.upsertSubTask(
                            createTestSubTask(
                                    LocalDateTime.of(2024, 11, 20, 10, i),
                                    Duration.ofMinutes(1),
                                    epic.getId())
                    )
            );
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
        taskManager.upsertSubTask(createTestSubTask(TaskStatus.NEW, epic.getId()));
        assertEquals(TaskStatus.NEW, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusInProgressWith2SubTask_NewDone() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        taskManager.upsertSubTask(
                createTestSubTask(
                        TaskStatus.NEW,
                        LocalDateTime.of(2024, 11, 20, 10, 0),
                        Duration.ofMinutes(1),
                        epic.getId()
                )
        );
        taskManager.upsertSubTask(
                createTestSubTask(
                        TaskStatus.DONE,
                        LocalDateTime.of(2024, 11, 20, 10, 10),
                        Duration.ofMinutes(1),
                        epic.getId()
                )
        );
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusInProgressWith2SubTask_InProgressDone() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        taskManager.upsertSubTask(
                createTestSubTask(
                        TaskStatus.IN_PROGRESS,
                        LocalDateTime.of(2024, 11, 20, 10, 0),
                        Duration.ofMinutes(1),
                        epic.getId()
                )
        );
        taskManager.upsertSubTask(
                createTestSubTask(
                        TaskStatus.DONE,
                        LocalDateTime.of(2024, 11, 20, 10, 10),
                        Duration.ofMinutes(1),
                        epic.getId()
                )
        );
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusInProgressWith3SubTask_NewInProgressDone() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        taskManager.upsertSubTask(
                createTestSubTask(
                        TaskStatus.NEW,
                        LocalDateTime.of(2024, 11, 20, 10, 0),
                        Duration.ofMinutes(1),
                        epic.getId()
                )
        );
        taskManager.upsertSubTask(
                createTestSubTask(
                        TaskStatus.IN_PROGRESS,
                        LocalDateTime.of(2024, 11, 20, 10, 10),
                        Duration.ofMinutes(1),
                        epic.getId()
                )
        );
        taskManager.upsertSubTask(
                createTestSubTask(
                        TaskStatus.DONE,
                        LocalDateTime.of(2024, 11, 20, 10, 20),
                        Duration.ofMinutes(1),
                        epic.getId()
                )
        );
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusDoneWithSubTask() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        taskManager.upsertSubTask(createTestSubTask(TaskStatus.DONE, epic.getId()));
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
    void epicCalculateDurationWithoutSubTasks() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        assertNull(epic.getStartTime());
        assertNull(epic.getDuration());
        assertNull(epic.getEndTime());
    }

    @Test
    void epicCalculateDurationWithSingleSubTask() {
        int epicId = taskManager.upsertEpic(createTestEpic()).getId();
        SubTask subTask = taskManager.upsertSubTask(createTestSubTask(epicId));
        Epic epic = taskManager.getEpic(epicId);
        assertEquals(subTask.getStartTime(), epic.getStartTime());
        assertEquals(subTask.getDuration(), epic.getDuration());
        assertEquals(subTask.getEndTime(), epic.getEndTime());
    }

    @Test
    void epicCalculateDuration() {
        int epicId = taskManager.upsertEpic(createTestEpic()).getId();
        SubTask firstSubTask = taskManager.upsertSubTask(
                createTestSubTask(
                        LocalDateTime.of(2024, 11, 20, 10, 0),
                        Duration.ofMinutes(1),
                        epicId
                )
        );
        SubTask secondSubTask = taskManager.upsertSubTask(
                createTestSubTask(
                        LocalDateTime.of(2024, 11, 20, 10, 10),
                        Duration.ofMinutes(1),
                        epicId
                )
        );

        Epic epic = taskManager.getEpic(epicId);
        assertEquals(firstSubTask.getStartTime(), epic.getStartTime());
        assertEquals(Duration.between(firstSubTask.getStartTime(), secondSubTask.getEndTime()), epic.getDuration());
        assertEquals(secondSubTask.getEndTime(), epic.getEndTime());
    }

    @Test
    void epicCalculateDurationAfterRemoveSubTask() {
        int epicId = taskManager.upsertEpic(createTestEpic()).getId();
        SubTask firstSubTask = taskManager.upsertSubTask(
                createTestSubTask(
                        LocalDateTime.of(2024, 11, 20, 10, 0),
                        Duration.ofMinutes(1),
                        epicId
                )
        );
        SubTask secondSubTask = taskManager.upsertSubTask(
                createTestSubTask(
                        LocalDateTime.of(2024, 11, 20, 10, 10),
                        Duration.ofMinutes(1),
                        epicId
                )
        );

        taskManager.removeSubTask(firstSubTask.getId());

        Epic epic = taskManager.getEpic(epicId);

        assertEquals(secondSubTask.getStartTime(), epic.getStartTime());
        assertEquals(secondSubTask.getDuration(), epic.getDuration());
        assertEquals(secondSubTask.getEndTime(), epic.getEndTime());
    }

    @Test
    void epicCalculateDurationAfterRemoveAllSubTasks() {
        int epicId = taskManager.upsertEpic(createTestEpic()).getId();
        SubTask firstSubTask = taskManager.upsertSubTask(
                createTestSubTask(
                        LocalDateTime.of(2024, 11, 20, 10, 0),
                        Duration.ofMinutes(1),
                        epicId
                )
        );
        SubTask secondSubTask = taskManager.upsertSubTask(
                createTestSubTask(
                        LocalDateTime.of(2024, 11, 20, 10, 10),
                        Duration.ofMinutes(1),
                        epicId
                )
        );

        taskManager.removeSubTask(firstSubTask.getId());
        taskManager.removeSubTask(secondSubTask.getId());

        Epic epic = taskManager.getEpic(epicId);

        assertNull(epic.getStartTime());
        assertNull(epic.getDuration());
        assertNull(epic.getEndTime());
    }

    @Test
    void getHistory() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        for (int i = 0; i < 5; i++) {
            taskManager.getEpic(epic.getId());
        }
        assertEquals(1, taskManager.getHistory().size());
    }

    @Test
    void removeAllTasksHistory() {
        for (int i = 0; i < 5; i++) {
            Task task = taskManager.upsertTask(
                    createTestTask(
                            LocalDateTime.of(2024, 11, 20, 10, i),
                            Duration.ofMinutes(1)
                    )
            );
            taskManager.getTask(task.getId());
        }
        taskManager.removeTasks();
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void removeAllEpicsHistory() {
        for (int i = 0; i < 5; i++) {
            Epic epic = taskManager.upsertEpic(createTestEpic());
            taskManager.getTask(epic.getId());
        }
        taskManager.removeEpics();
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void subtasksDeletedAlongWithEpicHistory() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        taskManager.getEpic(epic.getId());

        SubTask subTask1 = taskManager.upsertSubTask(
                createTestSubTask(
                        LocalDateTime.of(2024, 11, 20, 10, 0),
                        Duration.ofMinutes(1),
                        epic.getId()
                )
        );
        taskManager.getSubTask(subTask1.getId());

        SubTask subTask2 = taskManager.upsertSubTask(
                createTestSubTask(
                        LocalDateTime.of(2024, 11, 20, 10, 10),
                        Duration.ofMinutes(1),
                        epic.getId()
                )
        );
        taskManager.getSubTask(subTask2.getId());

        taskManager.removeEpic(epic.getId());
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void removeAllSubTasksHistory() {
        Epic epic = taskManager.upsertEpic(createTestEpic());
        taskManager.getEpic(epic.getId());
        for (int i = 0; i < 5; i++) {
            SubTask subTask = taskManager.upsertSubTask(
                    createTestSubTask(
                            LocalDateTime.of(2024, 11, 20, 10, i),
                            Duration.ofMinutes(1),
                            epic.getId()
                    )
            );
            taskManager.getSubTask(subTask.getId());
        }
        taskManager.removeSubTasks();
        assertEquals(1, taskManager.getHistory().size());
    }

    @Test
    void calculateIntervals() {

        Task task1 = taskManager.upsertTask(
                createTestTask(
                        LocalDateTime.of(2024, 11, 20, 12, 0),
                        Duration.ofMinutes(1)
                )
        );

        Task task2 = taskManager.upsertTask(
                createTestTask(
                        LocalDateTime.of(2024, 11, 20, 12, 0),
                        Duration.ofMinutes(1)
                )
        );

        int epicId = taskManager.upsertEpic(createTestEpic()).getId();

        SubTask subTask1 = taskManager.upsertSubTask(
                createTestSubTask(
                        LocalDateTime.of(2024, 11, 20, 10, 0),
                        Duration.ofMinutes(70),
                        epicId
                )
        );

        SubTask subTask2 = taskManager.upsertSubTask(
                createTestSubTask(
                        LocalDateTime.of(2024, 11, 20, 11, 0),
                        Duration.ofMinutes(1),
                        epicId
                )
        );

        assertIterableEquals(List.of(subTask1, task1), taskManager.getPrioritizedTasks());
    }
}