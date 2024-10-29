package manager.history;

import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private final Random random = new Random();
    private HistoryManager historyManager;
    private int index;

    @BeforeEach
    public void beforeEach() {
        historyManager = new InMemoryHistoryManager();
        index = 0;
    }

    @Test
    void shouldReturnHistoryAfterAddingNullItem() {
        historyManager.add(null);
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void shouldReturnHistoryAfterAdding3Items() {
        for (int i = 0; i < 3; i++) {
            historyManager.add(createRandomTask());
        }
        assertEquals(3, historyManager.getHistory().size());
    }

    @Test
    void shouldReturnHistoryAfterAddingIdenticalItems() {
        Task task = createRandomTask();
        historyManager.add(task);
        historyManager.add(task);
        historyManager.add(createRandomTask());
        assertEquals(2, historyManager.getHistory().size());
    }

    @Test
    void removeOneTask() {
        Task task = createRandomTask();
        historyManager.add(task);
        for (int i = 0; i < 3; i++) {
            historyManager.add(createRandomTask());
        }
        historyManager.remove(task.getId());
        assertEquals(3, historyManager.getHistory().size());
    }

    @Test
    void removeAllTasks() {
        for (int i = 0; i < 3; i++) {
            historyManager.add(createRandomTask());
        }
        List<Task> history = historyManager.getHistory();
        for (Task task : history) {
            historyManager.remove(task.getId());
        }
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void shouldReturnOrderedHistoryAfterAddingItems() {
        List<Task> orderedList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Task task = createRandomTask();
            orderedList.add(task);
            historyManager.add(task);
        }
        assertIterableEquals(orderedList, historyManager.getHistory());
    }

    @Test
    void shouldReturnOrderedHistoryAfterAddingItemsWithDuplicates() {
        LinkedList<Task> orderedList = new LinkedList<>();
        Task duplicateTask = createRandomTask();
        historyManager.add(duplicateTask);
        for (int i = 0; i < 3; i++) {
            Task task = createRandomTask();
            orderedList.add(task);
            historyManager.add(task);
        }
        historyManager.add(duplicateTask);
        orderedList.add(duplicateTask);

        assertIterableEquals(orderedList, historyManager.getHistory());
    }

    private Task createRandomTask() {
        int taskType = random.nextInt(3);
        switch (taskType) {
            case 1 -> {
                return createEpicTask();
            }
            case 2 -> {
                return createTestSubTask(random.nextInt());
            }
            default -> {
                return createTestTask();
            }
        }
    }

    private Task createTestTask() {
        Task task = new Task(
                "TestTaskName",
                "TestTaskDescription"
        );
        task.setId(index++);
        return task;
    }

    private Epic createEpicTask() {
        Epic epic = new Epic(
                "TestEpicName",
                "TestEpicDescription"
        );
        epic.setId(index++);
        return epic;
    }

    private SubTask createTestSubTask(int epicId) {
        SubTask subTask = new SubTask(
                "TestSubTaskName",
                "TestSubTaskDescription",
                epicId
        );
        subTask.setId(index++);
        return subTask;
    }
}