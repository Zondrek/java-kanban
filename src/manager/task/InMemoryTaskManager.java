package manager.task;

import manager.history.HistoryManager;
import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private final HistoryManager historyManager;

    private final Map<Integer, Task> tasks = new HashMap<>(); // <taskId, Task>
    private final Map<Integer, Epic> epics = new HashMap<>(); // <epicId, Epic>
    private final Map<Integer, SubTask> subTasks = new HashMap<>(); // <subTaskId, SubTask>

    private int index = 0;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public Collection<Task> getTasks() {
        return List.copyOf(tasks.values());
    }

    @Override
    public Collection<Epic> getEpics() {
        return List.copyOf(epics.values());
    }

    @Override
    public Collection<SubTask> getSubTasks() {
        return List.copyOf(subTasks.values());
    }

    @Override
    public Collection<SubTask> getSubTasks(int epicId) {
        Collection<SubTask> result = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (int subTaskId : epic.getSubTasks()) {
                result.add(subTasks.get(subTaskId));
            }
        }
        return result;
    }

    @Override
    public Task getTask(int taskId) {
        Task task = tasks.get(taskId);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int epicId) {
        Epic epic = epics.get(epicId);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public SubTask getSubTask(int subTaskId) {
        SubTask subTask = subTasks.get(subTaskId);
        historyManager.add(subTask);
        return subTask;
    }

    @Override
    public Task upsertTask(Task task) {
        if (task.getId() == null) {
            task.setId(index++);
        } else if (epics.containsKey(task.getId()) || subTasks.containsKey(task.getId())) {
            return null;
        }
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic upsertEpic(Epic epic) {
        if (epic.getId() == null) {
            epic.setId(index++);
            epics.put(epic.getId(), epic);
        } else if (tasks.containsKey(epic.getId()) || subTasks.containsKey(epic.getId())) {
            return null;
        }
        return updateEpicStatus(epic);
    }

    @Override
    public SubTask upsertSubTask(SubTask subTask) {
        Epic epic = epics.get(subTask.getEpicId());
        if (epic == null) {
            return null;
        }
        if (subTask.getId() == null) {
            subTask.setId(index++);
        } else if (epics.containsKey(subTask.getId()) || tasks.containsKey(subTask.getId())) {
            return null;
        }
        subTasks.put(subTask.getId(), subTask);
        epic.attachSubTask(subTask.getId());
        updateEpicStatus(epic);
        return subTask;
    }

    @Override
    public void removeTasks() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    @Override
    public void removeTask(int taskId) {
        historyManager.remove(taskId);
        tasks.remove(taskId);
    }

    @Override
    public void removeEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }
        for (SubTask subTask : subTasks.values()) {
            historyManager.remove(subTask.getEpicId());
        }
        epics.clear();
        subTasks.clear();
    }

    @Override
    public void removeEpic(int epicId) {
        historyManager.remove(epicId);
        Epic epic = epics.remove(epicId);
        if (epic != null) {
            for (int subTaskId : epic.getSubTasks()) {
                historyManager.remove(subTaskId);
                subTasks.remove(subTaskId);
            }
        }
    }

    @Override
    public void removeSubTasks() {
        for (Epic epic : epics.values()) {
            epic.detachAllSubTasks();
        }
        for (SubTask subTask : subTasks.values()) {
            historyManager.remove(subTask.getId());
        }
        subTasks.clear();
    }

    @Override
    public void removeSubTask(int subTaskId) {
        historyManager.remove(subTaskId);
        SubTask subTask = subTasks.remove(subTaskId);
        if (subTask != null) {
            Epic epic = getEpic(subTask.getEpicId());
            epic.detachSubTask(subTask.getId());
            updateEpicStatus(epic);
        }
    }

    private Epic updateEpicStatus(Epic epic) {
        TaskStatus newStatus = calculateStatus(epic);
        if (newStatus != epic.getStatus()) {
            Epic newEpic = new Epic(epic, newStatus);
            epics.put(newEpic.getId(), newEpic);
            return newEpic;
        }
        return epic;
    }

    private TaskStatus calculateStatus(Epic epic) {
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

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
