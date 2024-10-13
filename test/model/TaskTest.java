package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TaskTest {

    @Test
    void instancesWithTheSameIdentifiersMustBeTheSame() {

        Task task1 = new Task("TestName1", "TestDescription1");
        task1.setId(1);

        Task task2 = new Task("TestName2", "TestDescription2");
        task2.setId(1);

        assertEquals(task1, task2);
    }

    @Test
    void instancesWithDifferentIdentifiersMustBeDifferent() {

        Task task1 = new Task("TestName1", "TestDescription1");
        task1.setId(1);

        Task task2 = new Task("TestName2", "TestDescription2");
        task2.setId(2);

        assertNotEquals(task1, task2);
    }

    @Test
    void instancesWithTheSameIdButDifferentClassesMustBeTheSame() {
        Task task = new Task("name", "description");
        task.setId(1);

        Epic epic = new Epic("name", "description");
        epic.setId(1);

        SubTask subTask = new SubTask("name", "description", 0);
        subTask.setId(1);

        assertEquals(task, epic);
        assertEquals(task, subTask);
        assertEquals(epic, subTask);
    }
}