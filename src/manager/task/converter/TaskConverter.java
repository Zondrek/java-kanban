package manager.task.converter;

import model.*;
import model.dto.TaskDto;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class TaskConverter {

    private static final ZoneOffset DEFAULT_ZONE = ZoneOffset.UTC;

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
                Long.parseLong(strArr.get(5)),
                Long.parseLong(strArr.get(6)),
                strArr.get(7).isBlank() ? null : Integer.valueOf(strArr.get(5))
        );
    }

    public static String dtoToString(TaskDto dto) {
        //id, type, name, status, description, startDate, duration, epic
        return String.format(
                "%s, %s, %s, %s, %s, %s, %s, %s",
                dto.id(),
                dto.type(),
                dto.name(),
                dto.status(),
                dto.description(),
                dto.startDate(),
                dto.duration(),
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
                getIfNotNull(task.getStartTime(), TaskConverter::dateToMilli),
                getIfNotNull(task.getDuration(), Duration::toMinutes),
                getEpicId(task.getType(), task)
        );
    }

    public static Task dtoToTask(TaskDto dto) {
        Task task = new Task(
                dto.name(),
                dto.description(),
                getIfNotNull(dto.startDate(), TaskConverter::milliToDate),
                getIfNotNull(dto.duration(), Duration::ofMinutes)
        );
        task.setId(dto.id());
        return new Task(task, stringToStatus(dto.status()));
    }

    public static SubTask dtoToSubTask(TaskDto dto) {
        Long startDate = dto.startDate();
        SubTask subTask = new SubTask(
                dto.name(),
                dto.description(),
                startDate != null ? milliToDate(dto.startDate()) : null,
                Duration.ofMinutes(dto.duration()),
                dto.epicId()
        );
        subTask.setId(dto.id());
        return new SubTask(subTask, stringToStatus(dto.status()));
    }

    public static Epic dtoToEpic(TaskDto dto) {
        Epic epic = new Epic(dto.name(), dto.description());
        epic.setId(dto.id());
        return epic;
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

    private static long dateToMilli(LocalDateTime date) {
        return date.toInstant(DEFAULT_ZONE).toEpochMilli();
    }

    private static LocalDateTime milliToDate(long milli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(milli), DEFAULT_ZONE);
    }

    private static <P, R> R getIfNotNull(P param, Function<P, R> getFun) {
        if (param != null) {
            return getFun.apply(param);
        }
        return null;
    }
}
