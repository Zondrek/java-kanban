import manager.task.FileBackedTaskManager;
import manager.task.TaskManager;
import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Инициализация");
        TaskManager taskManager = FileBackedTaskManager.loadFromFile(File.createTempFile("test", ".csv"));
        Epic epic1 = taskManager.upsertEpic(new Epic("Эпик 1", "Эпик 1"));
        SubTask subTask1 = taskManager.upsertSubTask(
                new SubTask("Эпик 1 Подзадача 1", "Эпик 1 Подзадача 1", epic1.getId())
        );
        SubTask subTask2 = taskManager.upsertSubTask(
                new SubTask("Эпик 1 Подзадача 2", "Эпик 1 Подзадача 2", epic1.getId())
        );

        Epic epic2 = taskManager.upsertEpic(new Epic("Эпик 2", "Эпик 2"));
        SubTask subTask3 = taskManager.upsertSubTask(
                new SubTask("Эпик 2 Подзадача 1", "Эпик 2 Подзадача 1", epic2.getId())
        );

        Task task1 = taskManager.upsertTask(new Task("Задача 1", "Залача 1"));
        Task task2 = taskManager.upsertTask(new Task("Задача 2", "Задача 2"));

        printTasks(taskManager);

        System.out.println("Изменение статусов");
        taskManager.upsertTask(new Task(task1, TaskStatus.IN_PROGRESS));
        taskManager.upsertTask(new Task(task2, TaskStatus.DONE));
        taskManager.upsertSubTask(new SubTask(subTask1, TaskStatus.DONE));
        taskManager.upsertSubTask(new SubTask(subTask3, TaskStatus.DONE));

        printTasks(taskManager);

        taskManager.getTask(task1.getId());
        taskManager.getEpic(epic2.getId());
        taskManager.getSubTask(subTask3.getId());
        taskManager.getTask(task1.getId());

        System.out.println("История: " + taskManager.getHistory());
    }

    private static void printTasks(TaskManager taskManager) {
        System.out.println("Все эпики: " + taskManager.getEpics());
        System.out.println("Все задачи: " + taskManager.getTasks());
        System.out.println("Все подзадачи: " + taskManager.getSubTasks());
    }
}



