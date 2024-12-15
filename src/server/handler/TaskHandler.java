package server.handler;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import manager.task.TaskManager;
import model.Task;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

public class TaskHandler extends BaseHttpHandler {

    public TaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");
            switch (getEndpoint(method, pathParts)) {
                case Endpoint.GET_TASKS -> getTasks(exchange);
                case Endpoint.GET_TASK_BY_ID -> getTaskById(exchange, pathParts[2]);
                case Endpoint.POST_TASK -> createTask(exchange);
                case Endpoint.DELETE_TASK -> deleteTask(exchange, pathParts[2]);
                default -> sendResponse(exchange, 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, e.getMessage(), 500);
        }
    }

    private Optional<Integer> getTaskId(String id) {
        return Optional.of(Integer.parseInt(id));
    }

    private void getTasks(HttpExchange exchange) throws IOException {
        sendResponse(exchange, gson.toJson(taskManager.getTasks()), 200);
    }

    private void getTaskById(HttpExchange exchange, String id) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(id);
        if (taskIdOpt.isEmpty()) {
            sendResponse(exchange, 404);
            return;
        }
        int taskId = taskIdOpt.get();

        Optional<Task> taskOpt = Optional.ofNullable(taskManager.getTask(taskId));
        if (taskOpt.isEmpty()) {
            sendResponse(exchange, 404);
            return;
        }

        Task task = taskOpt.get();
        sendResponse(exchange, gson.toJson(task), 200);
    }

    private void createTask(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
        try {
            Task taskFromRequest = gson.fromJson(JsonParser.parseReader(isr), Task.class);
            Task task = taskManager.upsertTask(taskFromRequest);
            if (task == null) {
                sendResponse(exchange, 406);
                return;
            }
            sendResponse(exchange, 201);
        } catch (JsonIOException | JsonSyntaxException e) {
            sendResponse(exchange, 400);
        }
    }

    private void deleteTask(HttpExchange exchange, String id) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(id);
        if (taskIdOpt.isEmpty()) {
            sendResponse(exchange, 404);
            return;
        }
        int taskId = taskIdOpt.get();
        taskManager.removeTask(taskId);
        sendResponse(exchange, 200);
    }

    private Endpoint getEndpoint(String method, String[] pathParts) {
        if (pathParts.length == 2) {
            switch (method) {
                case "GET" -> {
                    return Endpoint.GET_TASKS;
                }
                case "POST" -> {
                    return Endpoint.POST_TASK;
                }
            }
        } else if (pathParts.length == 3) {
            switch (method) {
                case "GET" -> {
                    return Endpoint.GET_TASK_BY_ID;
                }
                case "DELETE" -> {
                    return Endpoint.DELETE_TASK;
                }
            }
        }
        return Endpoint.UNKNOWN;
    }

    private enum Endpoint {GET_TASKS, GET_TASK_BY_ID, POST_TASK, DELETE_TASK, UNKNOWN}
}
