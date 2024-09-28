package manager;

import manager.model.Epic;
import manager.model.SubTask;
import manager.model.Task;
import manager.model.TaskStatus;

import java.util.*;

public class TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>(); // <taskId, Task>
    private final Map<Integer, Epic> epics = new HashMap<>(); // <epicId, Epic>
    private final Map<Integer, SubTask> subTasks = new HashMap<>(); // <subTaskId, SubTask>

    private int index = 0;

    public Collection<Task> getTasks() {
        return List.copyOf(tasks.values());
    }

    public Collection<Epic> getEpics() {
        return List.copyOf(epics.values());
    }

    public Collection<SubTask> getSubTasks() {
        return List.copyOf(subTasks.values());
    }

    public Collection<SubTask> getSubTasks(int epicId) {
        Epic epic = epics.get(epicId);
        Collection<SubTask> result = new ArrayList<>();
        for (int subTaskId : epic.getSubTasks()) {
            result.add(subTasks.get(subTaskId));
        }
        return result;
    }


    public Task getTask(int taskId) {
        return tasks.get(taskId);
    }

    public Epic getEpic(int epicId) {
        return epics.get(epicId);
    }

    public SubTask getSubTask(int subTaskId) {
        return subTasks.get(subTaskId);
    }

    public Task upsertTask(Task task) {
        if (task.getId() == null) {
            task.setId(index++);
        }
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic upsertEpic(Epic epic) {
        if (epic.getId() == null) {
            epic.setId(index++);
        }
        epics.put(epic.getId(), epic);
        return epic;
    }

    public SubTask upsertSubTask(SubTask subTask) {
        if (subTask.getId() == null) {
            subTask.setId(index++);
        }
        subTasks.put(subTask.getId(), subTask);
        Epic epic = epics.get(subTask.getEpicId());
        epic.attachSubTask(subTask.getId());
        updateEpicStatus(epic.getId());
        return subTask;
    }

    public void removeTasks() {
        tasks.clear();
    }

    public void removeTask(int taskId) {
        tasks.remove(taskId);
    }

    public void removeEpics() {
        epics.clear();
        subTasks.clear();
    }

    public void removeEpic(int epicId) {
        Epic epic = epics.get(epicId);
        epics.remove(epicId);
        for (int subTaskId : epic.getSubTasks()) {
            subTasks.remove(subTaskId);
        }
    }

    public void removeSubTasks() {
        for (SubTask subTask : subTasks.values()) {
            Epic epic = getEpic(subTask.getEpicId());
            epic.detachSubTask(subTask.getId());
        }
        subTasks.clear();
    }

    public void removeSubTask(int subTaskId) {
        SubTask subTask = subTasks.get(subTaskId);
        Epic epic = getEpic(subTask.getEpicId());
        epic.detachSubTask(subTask.getId());
        subTasks.remove(subTaskId);
        updateEpicStatus(epic.getId());
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        TaskStatus newStatus = calculateStatus(epicId);
        if (newStatus != epic.getStatus()) {
            upsertEpic(new Epic(epic, newStatus));
        }
    }

    private TaskStatus calculateStatus(int epicId) {
        Epic epic = epics.get(epicId);
        int allCount = epic.getSubTasks().size();
        if (allCount == 0) {
            return TaskStatus.NEW;
        }
        int newCount = 0;
        int doneCount = 0;
        for (int subTaskId : epic.getSubTasks()) {
            SubTask subTask = subTasks.get(subTaskId);
            switch (subTask.getStatus()) {
                case NEW -> newCount++;
                case DONE -> doneCount++;
            }
        }
        if (newCount == allCount) {
            return TaskStatus.NEW;
        } else if (doneCount == allCount) {
            return TaskStatus.DONE;
        } else {
            return TaskStatus.IN_PROGRESS;
        }
    }
}
