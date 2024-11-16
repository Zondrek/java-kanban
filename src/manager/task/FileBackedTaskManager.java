package manager.task;

import manager.Managers;
import manager.exception.ManagerLoadException;
import manager.exception.ManagerSaveException;
import manager.history.HistoryManager;
import manager.task.converter.TaskConverter;
import model.Epic;
import model.SubTask;
import model.Task;
import model.dto.TaskType;

import java.io.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.stream.Stream;

import static manager.task.converter.TaskConverter.*;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File backedFile;

    private FileBackedTaskManager(HistoryManager historyManager, File backedFile) {
        super(historyManager);
        this.backedFile = backedFile;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(Managers.getDefaultHistory(), file);
        loadFromFile(manager, file);
        return manager;
    }

    @Override
    public Task upsertTask(Task task) {
        Task result = super.upsertTask(task);
        save();
        return result;
    }

    @Override
    public Epic upsertEpic(Epic epic) {
        Epic result = super.upsertEpic(epic);
        save();
        return result;
    }

    @Override
    public SubTask upsertSubTask(SubTask subTask) {
        SubTask result = super.upsertSubTask(subTask);
        save();
        return result;
    }

    @Override
    public void removeTasks() {
        super.removeTasks();
        save();
    }

    @Override
    public void removeTask(int taskId) {
        super.removeTask(taskId);
        save();
    }

    @Override
    public void removeEpics() {
        super.removeEpics();
        save();
    }

    @Override
    public void removeEpic(int epicId) {
        super.removeEpic(epicId);
        save();
    }

    @Override
    public void removeSubTasks() {
        super.removeSubTasks();
        save();
    }

    @Override
    public void removeSubTask(int subTaskId) {
        super.removeSubTask(subTaskId);
        save();
    }

    private void save() {
        try (Writer writer = new FileWriter(backedFile)) {
            Stream.of(getTasks(), getEpics(), getSubTasks())
                    .flatMap(Collection::stream)
                    .map(TaskConverter::taskToDto)
                    .map(TaskConverter::dtoToString)
                    .forEach(str -> {
                        try {
                            writer.append(str).append(System.lineSeparator());
                            writer.flush();
                        } catch (IOException ioe) {
                            throw new UncheckedIOException(ioe);
                        }
                    });
        } catch (Exception e) {
            throw new ManagerSaveException(e);
        }
    }

    private static void loadFromFile(TaskManager manager, File file) {
        try (Stream<String> stream = Files.lines(file.toPath())) {
            stream.map(TaskConverter::stringToDto)
                    .forEach(dto -> {
                        TaskType type = TaskConverter.stringToType(dto.type());
                        switch (type) {
                            case EPIC -> manager.upsertEpic(dtoToEpic(dto));
                            case SUB_TASK -> manager.upsertSubTask(dtoToSubTask(dto));
                            case TASK -> manager.upsertTask(dtoToTask(dto));
                        }
                    });
        } catch (Exception e) {
            throw new ManagerLoadException(e);
        }
    }
}
