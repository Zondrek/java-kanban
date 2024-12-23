package manager.history;

import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static util.TestTaskFabric.*;

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
    void removeNullTask() {
        historyManager.add(createRandomTask());
        historyManager.remove(random.nextInt());
        assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    void removeNonExistentTask() {
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
    void shouldReturnOrderedListAfterRemoveFirstItem() {
        List<Task> orderedList = new ArrayList<>();
        Task firstItem = createRandomTask();
        historyManager.add(firstItem);
        for (int i = 0; i < 3; i++) {
            Task task = createRandomTask();
            orderedList.add(task);
            historyManager.add(task);
        }
        historyManager.remove(firstItem.getId());
        assertIterableEquals(orderedList, historyManager.getHistory());
    }

    @Test
    void shouldReturnOrderedListAfterRemoveMiddleItem() {
        List<Task> orderedList = new ArrayList<>();
        Task firstItem = createRandomTask();
        historyManager.add(firstItem);
        orderedList.add(firstItem);
        Task middleItem = createRandomTask();
        historyManager.add(middleItem);
        Task lastItem = createRandomTask();
        historyManager.add(lastItem);
        orderedList.add(lastItem);

        historyManager.remove(middleItem.getId());

        assertIterableEquals(orderedList, historyManager.getHistory());
    }

    @Test
    void shouldReturnOrderedListAfterRemoveLastItem() {
        List<Task> orderedList = new ArrayList<>();
        Task firstItem = createRandomTask();
        historyManager.add(firstItem);
        orderedList.add(firstItem);
        Task middleItem = createRandomTask();
        historyManager.add(middleItem);
        orderedList.add(middleItem);
        Task lastItem = createRandomTask();
        historyManager.add(lastItem);

        historyManager.remove(lastItem.getId());

        assertIterableEquals(orderedList, historyManager.getHistory());
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
                return createTestEpic(index++);
            }
            case 2 -> {
                return createTestSubTask(index++, random.nextInt());
            }
            default -> {
                return createTestTask(index++);
            }
        }
    }
}