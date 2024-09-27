import manager.*;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        Epic epic1 = taskManager.upsertEpic(new Epic("Epic 1", "Epic 1"));
        SubTask subTask1 = taskManager.upsertSubTask(
                epic1.getId(),
                new SubTask("Epic 1 SubTask 1", "Epic 1 SubTask 1", TaskStatus.NEW)
        );
        SubTask subTask2 = taskManager.upsertSubTask(
                epic1.getId(),
                new SubTask("Epic 1 SubTask 2", "Epic 1 SubTask 2", TaskStatus.NEW)
        );

        Epic epic2 = taskManager.upsertEpic(new Epic("Epic 2", "Epic 2"));
        SubTask subTask3 = taskManager.upsertSubTask(
                epic2.getId(),
                new SubTask("Epic 2 SubTask 1", "Epic 2 SubTask 1", TaskStatus.NEW)
        );

        Task task1 = taskManager.upsertTask(new Task("Task 1", "Task 1", TaskStatus.NEW));
        Task task2 = taskManager.upsertTask(new Task("Task 2", "Task 2", TaskStatus.NEW));

        printTasks(taskManager);

        taskManager.upsertTask(new Task(task1, TaskStatus.IN_PROGRESS));
        taskManager.upsertTask(new Task(task2, TaskStatus.DONE));
        taskManager.upsertSubTask(epic1.getId(), new SubTask(subTask1, TaskStatus.DONE));
        taskManager.upsertSubTask(epic2.getId(), new SubTask(subTask3, TaskStatus.DONE));

        printTasks(taskManager);

        taskManager.removeSubTask(epic1.getId(), subTask2.getId());
        taskManager.removeEpic(epic2.getId());
        taskManager.removeTask(task2.getId());

        printTasks(taskManager);

        taskManager.removeEpics();
        taskManager.removeTasks();

        printTasks(taskManager);
    }

    private static void printTasks(TaskManager taskManager) {
        System.out.println("All epics: " + taskManager.getEpics().toString());
        System.out.println("All tasks: " + taskManager.getTasks().toString());
        for (Epic epic : taskManager.getEpics()) {
            System.out.println("Subtasks epic " + epic.getName() + ": " + taskManager.getSubTasks(epic.getId()).toString());
        }
    }
}
