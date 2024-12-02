package manager.task;

import manager.history.HistoryManager;
import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private final HistoryManager historyManager;

    protected final Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));

    protected final Map<Integer, Task> tasks = new HashMap<>(); // <taskId, Task>
    protected final Map<Integer, Epic> epics = new HashMap<>(); // <epicId, Epic>
    protected final Map<Integer, SubTask> subTasks = new HashMap<>(); // <subTaskId, SubTask>

    private int index = 0;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
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
        return epics.get(epicId).getSubTasks().stream()
                .map(subTasks::get)
                .toList();
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
        if (isIntersect(task)) return null;
        if (task.getId() == null) {
            task.setId(index++);
        } else if (epics.containsKey(task.getId()) || subTasks.containsKey(task.getId())) {
            return null;
        }
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
        return task;
    }

    @Override
    public Epic upsertEpic(Epic epic) {
        if (epic.getId() == null) {
            // Присваиваем id если еще его нет
            epic.setId(index++);
        } else if (tasks.containsKey(epic.getId()) || subTasks.containsKey(epic.getId())) {
            // Возвращаем null если есть задание или подзадание с идентичным id
            return null;
        }
        Epic result = calculateEpic(epic);
        epics.put(epic.getId(), result);
        return result;
    }

    @Override
    public SubTask upsertSubTask(SubTask subTask) {
        if (isIntersect(subTask)) return null;
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
        prioritizedTasks.add(subTask);
        epic.attachSubTask(subTask.getId());
        upsertEpic(epic);
        return subTask;
    }

    @Override
    public void removeTasks() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
            prioritizedTasks.remove(task);
        }
        tasks.clear();
    }

    @Override
    public void removeTask(int taskId) {
        historyManager.remove(taskId);
        Task task = tasks.remove(taskId);
        if (task != null) {
            prioritizedTasks.remove(task);
        }
    }

    @Override
    public void removeEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }
        for (SubTask subTask : subTasks.values()) {
            historyManager.remove(subTask.getEpicId());
            prioritizedTasks.remove(subTask);
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
                SubTask subTask = subTasks.remove(subTaskId);
                if (subTask != null) {
                    prioritizedTasks.remove(subTask);
                }
            }
        }
    }

    @Override
    public void removeSubTasks() {
        for (Epic epic : epics.values()) {
            epic.detachAllSubTasks();
            upsertEpic(epic);
        }
        for (SubTask subTask : subTasks.values()) {
            historyManager.remove(subTask.getId());
            prioritizedTasks.remove(subTask);
        }
        subTasks.clear();
    }

    @Override
    public void removeSubTask(int subTaskId) {
        historyManager.remove(subTaskId);
        SubTask subTask = subTasks.remove(subTaskId);
        if (subTask != null) {
            prioritizedTasks.remove(subTask);
            Epic epic = getEpic(subTask.getEpicId());
            epic.detachSubTask(subTask.getId());
            upsertEpic(epic);
        }
    }

    protected Epic calculateEpic(Epic epic) {
        // Расчитываем статус и меняем его если он изменился
        Epic result = epic;
        TaskStatus newStatus = calculateStatus(result);
        if (newStatus != result.getStatus()) {
            result = new Epic(result, newStatus);
        }
        // Расчитываем startTime и endTime
        LocalDateTime startTime = calculateStartTime(result).orElse(null);
        LocalDateTime endTime = calculateEndTime(result).orElse(null);

        if (!Objects.equals(startTime, result.getStartTime()) || !Objects.equals(endTime, result.getEndTime())) {
            result = new Epic(result, startTime, endTime);
        }
        return result;
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

    private Optional<LocalDateTime> calculateStartTime(Epic epic) {
        return epic.getSubTasks().stream()
                .map((id) -> subTasks.get(id).getStartTime())
                .min(LocalDateTime::compareTo);
    }

    private Optional<LocalDateTime> calculateEndTime(Epic epic) {
        return epic.getSubTasks().stream()
                .map((id) -> subTasks.get(id).getEndTime())
                .max(LocalDateTime::compareTo);
    }

    private boolean isIntersect(Task t2) {
        return prioritizedTasks.stream()
                .filter(t1 -> !t1.getId().equals(t2.getId()))
                .anyMatch(t1 ->
                        t2.getStartTime().isBefore(t1.getEndTime()) &&
                                t2.getEndTime().isAfter(t1.getStartTime())
                );
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
