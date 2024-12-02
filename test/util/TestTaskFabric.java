package util;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

public class TestTaskFabric {

    public static Task createTestTask() {
        return new Task(
                "TestTaskName",
                "TestTaskDescription",
                LocalDateTime.now(),
                Duration.ofMinutes(10)
        );
    }

    public static Task createTestTask(int id, LocalDateTime start, Duration duration) {
        Task task = new Task(
                "TestTaskName",
                "TestTaskDescription",
                start,
                duration
        );
        task.setId(id);
        return task;
    }

    public static Task createTestTask(LocalDateTime start, Duration duration) {
        return new Task(
                "TestTaskName",
                "TestTaskDescription",
                start,
                duration
        );
    }

    public static Task createTestTask(int id) {
        Task task = new Task(
                "TestTaskName",
                "TestTaskDescription",
                LocalDateTime.now(),
                Duration.ofMinutes(10)
        );
        task.setId(id);
        return task;
    }

    public static Task createTestTask(TaskStatus status) {
        Task task = new Task(
                "TestTaskName",
                "TestTaskDescription",
                LocalDateTime.now(),
                Duration.ofMinutes(10)
        );
        return new Task(task, status);
    }

    public static Epic createTestEpic() {
        return new Epic(
                "TestEpicName",
                "TestEpicDescription"
        );
    }

    public static Epic createTestEpic(int id) {
        Epic epic = new Epic(
                "TestEpicName",
                "TestEpicDescription"
        );
        epic.setId(id);
        return epic;
    }

    public static SubTask createTestSubTask(int id, int epicId) {
        SubTask subTask = new SubTask(
                "TestSubTaskName",
                "TestSubTaskDescription",
                LocalDateTime.now(),
                Duration.ofMinutes(10),
                epicId
        );
        subTask.setId(id);
        return subTask;
    }

    public static SubTask createTestSubTask(int epicId) {
        return new SubTask(
                "TestSubTaskName",
                "TestSubTaskDescription",
                LocalDateTime.now(),
                Duration.ofMinutes(10),
                epicId
        );
    }


    public static SubTask createTestSubTask(
            LocalDateTime start,
            Duration duration,
            int epicId
    ) {
        return new SubTask(
                "TestSubTaskName",
                "TestSubTaskDescription",
                start,
                duration,
                epicId
        );
    }

    public static SubTask createTestSubTask(
            int id,
            LocalDateTime start,
            Duration duration,
            int epicId
    ) {
        SubTask subTask = new SubTask(
                "TestSubTaskName",
                "TestSubTaskDescription",
                start,
                duration,
                epicId
        );
        subTask.setId(id);
        return subTask;
    }

    public static SubTask createTestSubTask(
            TaskStatus status,
            LocalDateTime start,
            Duration duration,
            int epicId
    ) {
        SubTask subTask = new SubTask(
                "TestSubTaskName",
                "TestSubTaskDescription",
                start,
                duration,
                epicId
        );

        return new SubTask(subTask, status);
    }

    public static SubTask createTestSubTask(TaskStatus status, int epicId) {
        SubTask subTask = new SubTask(
                "TestSubTaskName",
                "TestSubTaskDescription",
                LocalDateTime.now(),
                Duration.ofMinutes(10),
                epicId
        );

        return new SubTask(subTask, status);
    }
}
