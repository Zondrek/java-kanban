package manager.model;

import java.util.Collection;
import java.util.HashSet;

public class Epic extends Task {

    private final Collection<Integer> subTaskIds;

    public Epic(String name, String description) {
        super(null, name, description, null);
        this.subTaskIds = new HashSet<>();
    }

    public Epic(Epic epic, TaskStatus status) {
        super(epic.getId(), epic.getName(), epic.getDescription(), status);
        this.subTaskIds = epic.getSubTasks();
    }

    public Collection<Integer> getSubTasks() {
        return subTaskIds;
    }

    public void attachSubTask(int subTaskId) {
        subTaskIds.add(subTaskId);
    }

    public void detachSubTask(int subTaskId) {
        subTaskIds.remove(subTaskId);
    }

    public void detachAllSubTasks() {
        subTaskIds.clear();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subTaskIds=" + subTaskIds +
                '}';
    }
}
