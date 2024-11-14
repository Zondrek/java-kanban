package manager.task;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import manager.history.HistoryManager;
import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import model.dto.TaskDto;
import model.dto.TaskType;

import java.io.File;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Collection;
import java.util.stream.Stream;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File backedFile;

    public FileBackedTaskManager(HistoryManager historyManager, File backedFile) {
        super(historyManager);
        this.backedFile = backedFile;
        updateInMemory();
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
            Stream<TaskDto> taskStream = Stream.of(getTasks(), getEpics(), getSubTasks())
                    .flatMap(Collection::stream)
                    .map(this::mapToDto);
            StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
            beanToCsv.write(taskStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateInMemory() {
        try (Reader reader = Files.newBufferedReader(backedFile.toPath())) {
            CsvToBean<TaskDto> csvToBean = new CsvToBeanBuilder(reader)
                    .withType(TaskDto.class)
                    .build();
            for (TaskDto taskDto : csvToBean.parse()) {
                TaskType type = mapToType(taskDto.getType());
                switch (type) {
                    case EPIC -> upsertEpic(dtoToEpic(taskDto));
                    case SUB_TASK -> upsertSubTask(dtoToSubTask(taskDto));
                    case TASK -> upsertTask(dtoToTask(taskDto));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends Task> TaskDto mapToDto(T task) {
        TaskType type = classToType(task);
        return new TaskDto(
                task.getId(),
                mapToString(type),
                task.getName(),
                mapToString(task.getStatus()),
                task.getDescription(),
                getEpicId(type, task)
        );
    }

    private Task dtoToTask(TaskDto dto) {
        Task task = new Task(dto.getName(), dto.getDescription());
        task.setId(dto.getId());
        return new Task(task, mapToStatus(dto.getStatus()));
    }

    private SubTask dtoToSubTask(TaskDto dto) {
        SubTask subTask = new SubTask(dto.getName(), dto.getDescription(), dto.getEpicId());
        subTask.setId(dto.getId());
        return new SubTask(subTask, mapToStatus(dto.getStatus()));
    }

    private Epic dtoToEpic(TaskDto dto) {
        Epic epic = new Epic(dto.getName(), dto.getDescription());
        epic.setId(dto.getId());
        return new Epic(epic, mapToStatus(dto.getStatus()));
    }

    private String mapToString(TaskStatus status) {
        return status.name();
    }

    private TaskStatus mapToStatus(String status) {
        return TaskStatus.valueOf(status);
    }

    private TaskType classToType(Task task) {
        if (task instanceof Epic) {
            return TaskType.EPIC;
        } else if (task instanceof SubTask) {
            return TaskType.SUB_TASK;
        } else {
            return TaskType.TASK;
        }
    }

    private String mapToString(TaskType type) {
        return type.name();
    }

    private TaskType mapToType(String type) {
        return TaskType.valueOf(type);
    }

    private <T extends Task> Integer getEpicId(TaskType type, T task) {
        if (type == TaskType.SUB_TASK) {
            return ((SubTask) task).getEpicId();
        }
        return null;
    }
}
