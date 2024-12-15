package server.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import manager.task.TaskManager;
import model.SubTask;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

public class SubTaskHandler extends BaseHttpHandler {

    public SubTaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");
            switch (getEndpoint(method, pathParts)) {
                case Endpoint.GET_SUBTASKS -> getSubTasks(exchange);
                case Endpoint.GET_SUBTASK_BY_ID -> getSubTaskById(exchange, pathParts[2]);
                case Endpoint.POST_SUBTASK -> createSubTask(exchange);
                case Endpoint.DELETE_SUBTASK -> deleteSubTask(exchange, pathParts[2]);
                default -> sendResponse(exchange, 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, e.getMessage(), 500);
        }
    }

    private Optional<Integer> getSubTaskId(String id) {
        return Optional.of(Integer.parseInt(id));
    }

    private void getSubTasks(HttpExchange exchange) throws IOException {
        sendResponse(exchange, gson.toJson(taskManager.getSubTasks()), 200);
    }

    private void getSubTaskById(HttpExchange exchange, String id) throws IOException {
        Optional<Integer> subTaskIdOpt = getSubTaskId(id);
        if (subTaskIdOpt.isEmpty()) {
            sendResponse(exchange, 404);
            return;
        }
        int subTaskId = subTaskIdOpt.get();

        Optional<SubTask> subTaskOpt = Optional.ofNullable(taskManager.getSubTask(subTaskId));
        if (subTaskOpt.isEmpty()) {
            sendResponse(exchange, 404);
            return;
        }

        SubTask subTask = subTaskOpt.get();
        sendResponse(exchange, gson.toJson(subTask), 200);
    }

    private void createSubTask(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
        try {
            JsonElement jsonElement = JsonParser.parseReader(isr);
            if (jsonElement.getAsJsonObject().get("epicId") == null) {
                sendResponse(exchange, "Отсутствует epicId.", 400);
                return;
            }

            SubTask subTaskFromRequest = gson.fromJson(jsonElement, SubTask.class);
            SubTask subTask = taskManager.upsertSubTask(subTaskFromRequest);
            if (subTask == null) {
                sendResponse(exchange, 406);
                return;
            }
            sendResponse(exchange, 201);
        } catch (JsonIOException | JsonSyntaxException e) {
            sendResponse(exchange, 400);
        }
    }

    private void deleteSubTask(HttpExchange exchange, String id) throws IOException {
        Optional<Integer> subTaskIdOpt = getSubTaskId(id);
        if (subTaskIdOpt.isEmpty()) {
            sendResponse(exchange, 404);
            return;
        }
        int subTaskId = subTaskIdOpt.get();
        taskManager.removeSubTask(subTaskId);
        sendResponse(exchange, 200);
    }

    private Endpoint getEndpoint(String method, String[] pathParts) {
        if (pathParts.length == 2) {
            switch (method) {
                case "GET" -> {
                    return Endpoint.GET_SUBTASKS;
                }
                case "POST" -> {
                    return Endpoint.POST_SUBTASK;
                }
            }
        } else if (pathParts.length == 3) {
            switch (method) {
                case "GET" -> {
                    return Endpoint.GET_SUBTASK_BY_ID;
                }
                case "DELETE" -> {
                    return Endpoint.DELETE_SUBTASK;
                }
            }
        }
        return Endpoint.UNKNOWN;
    }

    private enum Endpoint {GET_SUBTASKS, GET_SUBTASK_BY_ID, POST_SUBTASK, DELETE_SUBTASK, UNKNOWN}
}
