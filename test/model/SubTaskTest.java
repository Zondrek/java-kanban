package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static util.TestTaskFabric.createTestSubTask;

class SubTaskTest {

    @Test
    void instancesWithTheSameIdentifiersMustBeTheSame() {
        SubTask subTask1 = createTestSubTask(1, 0);
        SubTask subTask2 = createTestSubTask(1, 0);
        assertEquals(subTask1, subTask2);
    }

    @Test
    void instancesWithDifferentIdentifiersMustBeDifferent() {
        SubTask subTask1 = createTestSubTask(1, 0);
        SubTask subTask2 = createTestSubTask(2, 0);
        assertNotEquals(subTask1, subTask2);
    }

    @Test
    void subTaskIdNotBeEqualToEpicId() {
        SubTask subTask = createTestSubTask(1);
        assertThrows(IllegalArgumentException.class, () -> subTask.setId(1));
    }
}