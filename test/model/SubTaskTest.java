package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubTaskTest {

    @Test
    void instancesWithTheSameIdentifiersMustBeTheSame() {

        SubTask subTask1 = new SubTask("TestName1", "TestDescription1", 0);
        subTask1.setId(1);

        SubTask subTask2 = new SubTask("TestName2", "TestDescription2", 0);
        subTask2.setId(1);

        assertEquals(subTask1, subTask2);
    }

    @Test
    void instancesWithDifferentIdentifiersMustBeDifferent() {

        SubTask subTask1 = new SubTask("TestName1", "TestDescription1", 0);
        subTask1.setId(1);

        SubTask subTask2 = new SubTask("TestName2", "TestDescription2", 0);
        subTask2.setId(2);

        assertNotEquals(subTask1, subTask2);
    }

    @Test
    void subTaskIdNotBeEqualToEpicId() {
        SubTask subTask = new SubTask("TestName1", "TestDescription1", 1);
        assertThrows(IllegalArgumentException.class, () -> subTask.setId(1));
    }
}