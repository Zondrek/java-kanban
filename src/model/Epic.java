package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;

public class Epic extends Task {

    private final Collection<Integer> subTaskIds;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description, null, null);
        this.subTaskIds = new HashSet<>();
    }

    public Epic(Epic epic, LocalDateTime startTime, LocalDateTime endTime) {
        super(
                epic.getId(),
                epic.getName(),
                epic.getDescription(),
                epic.getStatus(),
                startTime,
                Duration.between(startTime, endTime)
        );
        this.subTaskIds = epic.getSubTasks();
        this.endTime = endTime;
    }

    public Epic(Epic epic, TaskStatus status) {
        super(epic.getId(), epic.getName(), epic.getDescription(), status, epic.getStartTime(), epic.getDuration());
        this.subTaskIds = epic.getSubTasks();
    }

    public Collection<Integer> getSubTasks() {
        return subTaskIds;
    }

    public void attachSubTask(int subTaskId) {
        if (subTaskId == getId()) {
            throw new IllegalArgumentException("SubTaskId can't be equal to epicId.");
        }
        subTaskIds.add(subTaskId);
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public void detachSubTask(int subTaskId) {
        subTaskIds.remove(subTaskId);
    }

    public void detachAllSubTasks() {
        subTaskIds.clear();
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
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
