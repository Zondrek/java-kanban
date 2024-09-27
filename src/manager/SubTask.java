package manager;

public class SubTask extends Task {

    public SubTask(String name, String description, TaskStatus status) {
        super(name, description, status);
    }

    public SubTask(SubTask task, TaskStatus status) {
        super(task.getId(), task.getName(), task.getDescription(), status);
    }
}
