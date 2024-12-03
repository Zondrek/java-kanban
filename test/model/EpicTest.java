package model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static util.TestTaskFabric.createTestEpic;
import static util.TestTaskFabric.createTestSubTask;

class EpicTest {

    @Test
    void instancesWithTheSameIdentifiersMustBeTheSame() {
        Epic epic1 = createTestEpic(1);
        Epic epic2 = createTestEpic(1);
        assertEquals(epic1, epic2);
    }

    @Test
    void instancesWithDifferentIdentifiersMustBeDifferent() {
        Epic epic1 = createTestEpic(1);
        Epic epic2 = createTestEpic(2);
        assertNotEquals(epic1, epic2);
    }

    @Test
    void theNumberAndOrderOfReturnedTasksIsEqualToTheAddedOne() {
        List<Integer> subTaskIds = new ArrayList<>(5);
        Epic epic = createTestEpic(100);
        for (int i = 0; i < 5; i++) {
            SubTask subTask = createTestSubTask(i, 100);
            epic.attachSubTask(subTask.getId());
            subTaskIds.add(subTask.getId());
        }
        assertEquals(subTaskIds.size(), epic.getSubTasks().size());
        assertIterableEquals(subTaskIds, epic.getSubTasks());
    }

    @Test
    void unableToAddEpicAsSubtask() {
        Epic epic = createTestEpic(1);
        SubTask subTask = createTestSubTask(1, 0);
        assertThrows(IllegalArgumentException.class, () -> epic.attachSubTask(subTask.getId()));
    }

    @Test
    void deletingSubtasksDoesNotChangeTheOrder() {
        List<Integer> subTaskIds = new ArrayList<>(5);
        Epic epic = createTestEpic(100);
        for (int i = 0; i < 5; i++) {
            SubTask subTask = createTestSubTask(i, 100);
            epic.attachSubTask(subTask.getId());
            subTaskIds.add(subTask.getId());
        }

        // удаляем элементы с конца, что бы не менять порядок до
        subTaskIds.remove(3);
        epic.detachSubTask(3);

        subTaskIds.remove(1);
        epic.detachSubTask(1);

        assertEquals(subTaskIds.size(), epic.getSubTasks().size());
        assertIterableEquals(subTaskIds, epic.getSubTasks());
    }

    @Test
    void detachAllSubTasks() {
        Epic epic = createTestEpic(100);
        for (int i = 0; i < 5; i++) {
            SubTask subTask = createTestSubTask(i,100);
            epic.attachSubTask(subTask.getId());
        }
        epic.detachAllSubTasks();
        assertTrue(epic.getSubTasks().isEmpty());
    }
}