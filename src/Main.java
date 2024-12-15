import manager.task.FileBackedTaskManager;
import manager.task.TaskManager;
import model.Epic;
import model.SubTask;
import model.Task;
import server.HttpTaskServer;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = FileBackedTaskManager.loadFromFile(File.createTempFile("test", ".csv"));

        Epic epic1 = taskManager.upsertEpic(new Epic("Эпик 1", "Эпик 1"));
        SubTask subTask1 = taskManager.upsertSubTask(
                new SubTask(
                        "Эпик 1 Подзадача 1",
                        "Эпик 1 Подзадача 1",
                        LocalDateTime.of(2024, 11, 20, 10, 20),
                        Duration.ofMinutes(10),
                        epic1.getId()
                )
        );
        SubTask subTask2 = taskManager.upsertSubTask(
                new SubTask(
                        "Эпик 1 Подзадача 2",
                        "Эпик 1 Подзадача 2",
                        LocalDateTime.of(2024, 11, 20, 10, 40),
                        Duration.ofMinutes(40),
                        epic1.getId()
                )
        );

        Epic epic2 = taskManager.upsertEpic(new Epic("Эпик 2", "Эпик 2"));
        SubTask subTask3 = taskManager.upsertSubTask(
                new SubTask(
                        "Эпик 2 Подзадача 1",
                        "Эпик 2 Подзадача 1",
                        LocalDateTime.of(2024, 11, 20, 11, 40),
                        Duration.ofMinutes(60),
                        epic2.getId()
                )
        );

        Task task1 = taskManager.upsertTask(
                new Task(
                        "Задача 1",
                        "Залача 1",
                        LocalDateTime.of(2024, 11, 20, 13, 20),
                        Duration.ofMinutes(10)
                )
        );
        Task task2 = taskManager.upsertTask(
                new Task(
                        "Задача 2",
                        "Задача 2",
                        LocalDateTime.of(2024, 11, 20, 13, 30),
                        Duration.ofMinutes(100)
                )
        );

        HttpTaskServer server = new HttpTaskServer(taskManager);
        server.main();
    }
}



