package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {

    private final int epicId;

    public SubTask(String name, String description, LocalDateTime startTime, Duration duration, int epicId) {
        super(name, description, startTime, duration);
        this.epicId = epicId;
    }

    public SubTask(SubTask task, TaskStatus status) {
        super(task.getId(), task.getName(), task.getDescription(), status, task.getStartTime(), task.getDuration());
        this.epicId = task.epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public void setId(int id) {
        if (epicId == id) {
            throw new IllegalArgumentException("SubTaskId can't be equal to epicId.");
        }
        super.setId(id);
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "id=" + getId() +
                ", epicId=" + epicId +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                '}';
    }
}
