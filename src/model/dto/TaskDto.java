package model.dto;

public record TaskDto(
        int id,
        String type,
        String name,
        String status,
        String description,
        Long startDate,
        Long duration,
        Integer epicId
) {
}
