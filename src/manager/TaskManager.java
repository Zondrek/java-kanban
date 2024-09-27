package manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>(); // <taskId, Task>
    private final Map<Integer, Epic> epics = new HashMap<>(); // <epicId, Epic>
    private final Map<Integer, Map<Integer, SubTask>> subTasks = new HashMap<>(); // <epicId, <subTaskId, SubTask>>

    public Collection<Task> getTasks() {
        return tasks.values();
    }

    public Collection<Epic> getEpics() {
        return epics.values();
    }

    public Collection<SubTask> getSubTasks(int epicId) {
        return subTasks.get(epicId).values();
    }

    public Task getTask(int taskId) {
        return tasks.get(taskId);
    }

    public Epic getEpic(int epicId) {
        return epics.get(epicId);
    }

    public SubTask getSubTask(int epicId, int subTaskId) {
        return subTasks.get(epicId).get(subTaskId);
    }

    public Task upsertTask(Task task) {
        if (task.getId() == Task.NONE_ID) {
            task.setId(tasks.values().size());
        }
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic upsertEpic(Epic epic) {
        if (epic.getId() == Task.NONE_ID) {
            epic.setId(epics.values().size());
        }
        epics.put(epic.getId(), epic);
        return epic;
    }

    public SubTask upsertSubTask(int epicId, SubTask subTask) {
        Map<Integer, SubTask> epicTasks = subTasks.getOrDefault(epicId, new HashMap<>());
        if (subTask.getId() == Task.NONE_ID) {
            subTask.setId(epicTasks.values().size());
        }
        epicTasks.put(subTask.getId(), subTask);
        subTasks.put(epicId, epicTasks);
        updateEpicStatus(epicId);
        return subTask;
    }

    public void removeTasks() {
        tasks.clear();
    }

    public void removeTask(int taskId) {
        tasks.remove(taskId);
    }

    public void removeEpics() {
        for (Epic epic : epics.values()) {
            removeSubTasks(epic.getId());
        }
        epics.clear();
    }

    public void removeEpic(int epicId) {
        epics.remove(epicId);
    }

    public void removeSubTasks(int epicId) {
        subTasks.remove(epicId);
    }

    public void removeSubTask(int epicId, int subTaskId) {
        subTasks.get(epicId).remove(subTaskId);
        updateEpicStatus(epicId);
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        TaskStatus newStatus = calculateStatus(epicId);
        if (newStatus != epic.getStatus()) {
            upsertEpic(new Epic(epic, newStatus));
        }
    }

    private TaskStatus calculateStatus(int epicId) {
        Collection<SubTask> epicTasks = subTasks.get(epicId).values();
        if (epicTasks.isEmpty()) {
            return TaskStatus.NEW;
        }
        int newTasks = 0;
        int doneTasks = 0;
        for (SubTask subTask : epicTasks) {
            switch (subTask.getStatus()) {
                case NEW -> newTasks++;
                case DONE -> doneTasks++;
            }
        }
        if (newTasks == epicTasks.size()) {
            return TaskStatus.NEW;
        } else if (doneTasks == epicTasks.size()) {
            return TaskStatus.DONE;
        } else {
            return TaskStatus.IN_PROGRESS;
        }
    }
}
