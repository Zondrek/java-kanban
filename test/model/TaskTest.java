package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static util.TestTaskFabric.*;

class TaskTest {

    @Test
    void instancesWithTheSameIdentifiersMustBeTheSame() {
        Task task1 = createTestTask(1);
        Task task2 = createTestTask(1);
        assertEquals(task1, task2);
    }

    @Test
    void instancesWithDifferentIdentifiersMustBeDifferent() {
        Task task1 = createTestTask(1);
        Task task2 = createTestTask(2);
        assertNotEquals(task1, task2);
    }

    @Test
    void instancesWithTheSameIdButDifferentClassesMustBeTheSame() {
        Task task = createTestTask(1);
        Epic epic = createTestEpic(1);
        SubTask subTask = createTestSubTask(1, 0);

        assertEquals(task, epic);
        assertEquals(task, subTask);
        assertEquals(epic, subTask);
    }

    @Test
    void setName() {
        Task task = createTestTask();
        task.setName("NewName");
        assertEquals("NewName", task.getName());
    }

    @Test
    void setDescription() {
        Task task = createTestTask();
        task.setDescription("NewDescription");
        assertEquals("NewDescription", task.getDescription());
    }

    @Test
    void setId() {
        Task task = createTestTask();
        task.setId(1);
        assertEquals(1, task.getId());
    }

    @Test
    void createStatus() {
        Task task = createTestTask();
        assertEquals(TaskStatus.NEW, task.getStatus());
    }

    @Test
    void changeStatus() {
        Task task = createTestTask();
        Task taskWithNewStatus = new Task(task, TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, taskWithNewStatus.getStatus());
    }
}