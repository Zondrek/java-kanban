package model;

public class SubTask extends Task {

    private final int epicId;

    public SubTask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public SubTask(SubTask task, TaskStatus status) {
        super(task.getId(), task.getName(), task.getDescription(), status);
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
