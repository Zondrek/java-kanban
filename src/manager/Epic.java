package manager;

public class Epic extends Task {

    public Epic(String name, String description) {
        super(name, description, null);
    }

    public Epic(Epic epic, TaskStatus status) {
        super(epic.getId(), epic.getName(), epic.getDescription(), status);
    }
}
