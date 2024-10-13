package model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void instancesWithTheSameIdentifiersMustBeTheSame() {

        Epic epic1 = new Epic("TestName1", "TestDescription1");
        epic1.setId(1);

        Epic epic2 = new Epic("TestName2", "TestDescription2");
        epic2.setId(1);

        assertEquals(epic1, epic2);
    }

    @Test
    void instancesWithDifferentIdentifiersMustBeDifferent() {

        Epic epic1 = new Epic("TestName1", "TestDescription1");
        epic1.setId(1);

        Epic epic2 = new Epic("TestName2", "TestDescription2");
        epic2.setId(2);

        assertNotEquals(epic1, epic2);
    }

    @Test
    void theNumberAndOrderOfReturnedTasksIsEqualToTheAddedOne() {
        List<Integer> subTaskIds = new ArrayList<>(5);
        Epic epic = new Epic("name", "description");
        epic.setId(100);
        for (int i = 0; i < 5; i++) {
            SubTask subTask = new SubTask("name" + i, "description" + i, 100);
            subTask.setId(i);
            epic.attachSubTask(subTask.getId());
            subTaskIds.add(subTask.getId());
        }
        assertEquals(subTaskIds.size(), epic.getSubTasks().size());
        assertIterableEquals(subTaskIds, epic.getSubTasks());
    }

    @Test
    void unableToAddEpicAsSubtask() {
        Epic epic = new Epic("name", "description");
        epic.setId(1);
        SubTask subTask = new SubTask("name", "description", 0);
        subTask.setId(1);
        assertThrows(IllegalArgumentException.class, () -> epic.attachSubTask(subTask.getId()));
    }

    @Test
    void deletingSubtasksDoesNotChangeTheOrder() {
        List<Integer> subTaskIds = new ArrayList<>(5);
        Epic epic = new Epic("name", "description");
        epic.setId(100);
        for (int i = 0; i < 5; i++) {
            SubTask subTask = new SubTask("name" + i, "description" + i, 100);
            subTask.setId(i);
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
        Epic epic = new Epic("name", "description");
        epic.setId(100);
        for (int i = 0; i < 5; i++) {
            SubTask subTask = new SubTask("name" + i, "description" + i, 100);
            subTask.setId(i);
            epic.attachSubTask(subTask.getId());
        }
        epic.detachAllSubTasks();
        assertTrue(epic.getSubTasks().isEmpty());
    }
}