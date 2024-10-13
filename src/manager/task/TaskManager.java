package manager.task;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;

import java.util.Collection;
import java.util.List;

public interface TaskManager {

    Collection<Task> getTasks();

    Collection<Epic> getEpics();

    Collection<SubTask> getSubTasks();

    Collection<SubTask> getSubTasks(int epicId);

    Task getTask(int taskId);

    Epic getEpic(int epicId);

    SubTask getSubTask(int subTaskId);

    Task upsertTask(Task task);

    Epic upsertEpic(Epic epic);

    SubTask upsertSubTask(SubTask subTask);

    void removeTasks();

    void removeTask(int taskId);

    void removeEpics();

    void removeEpic(int epicId);

    void removeSubTasks();

    void removeSubTask(int subTaskId);

    List<Task> getHistory();
}
