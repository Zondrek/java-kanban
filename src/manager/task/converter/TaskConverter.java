package manager.task.converter;

import model.*;
import model.dto.TaskDto;

import java.util.Arrays;
import java.util.List;

public class TaskConverter {

    public static TaskDto stringToDto(String str) {
        List<String> strArr = Arrays.stream(str.split(","))
                .map(String::trim)
                .toList();
        return new TaskDto(
                Integer.parseInt(strArr.get(0)),
                strArr.get(1),
                strArr.get(2),
                strArr.get(3),
                strArr.get(4),
                strArr.get(5).isBlank() ? null : Integer.valueOf(strArr.get(5))
        );
    }

    public static String dtoToString(TaskDto dto) {
        //id,type,name,status,description,epic
        return String.format(
                "%s, %s, %s, %s, %s, %s",
                dto.id(),
                dto.type(),
                dto.name(),
                dto.status(),
                dto.description(),
                dto.epicId() == null ? "" : dto.epicId()
        );
    }

    public static <T extends Task> TaskDto taskToDto(T task) {
        return new TaskDto(
                task.getId(),
                typeToString(task.getType()),
                task.getName(),
                statusToString(task.getStatus()),
                task.getDescription(),
                getEpicId(task.getType(), task)
        );
    }

    public static Task dtoToTask(TaskDto dto) {
        Task task = new Task(dto.name(), dto.description());
        task.setId(dto.id());
        return new Task(task, stringToStatus(dto.status()));
    }

    public static SubTask dtoToSubTask(TaskDto dto) {
        SubTask subTask = new SubTask(dto.name(), dto.description(), dto.epicId());
        subTask.setId(dto.id());
        return new SubTask(subTask, stringToStatus(dto.status()));
    }

    public static Epic dtoToEpic(TaskDto dto) {
        Epic epic = new Epic(dto.name(), dto.description());
        epic.setId(dto.id());
        return new Epic(epic, stringToStatus(dto.status()));
    }

    public static String statusToString(TaskStatus status) {
        return status.name();
    }

    public static TaskStatus stringToStatus(String status) {
        return TaskStatus.valueOf(status);
    }

    public static String typeToString(TaskType type) {
        return type.name();
    }

    public static TaskType stringToType(String type) {
        return TaskType.valueOf(type);
    }

    private static <T extends Task> Integer getEpicId(TaskType type, T task) {
        if (type == TaskType.SUBTASK) {
            return ((SubTask) task).getEpicId();
        }
        return null;
    }
}
