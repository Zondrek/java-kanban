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
            switch (method + path) {
                case "GET/tasks" -> getTasks(exchange);
                case "GET/tasks/{id}" -> getTaskById(exchange, path);
                case "POST/tasks" -> createTask(exchange);
                case "DELETE/tasks/{id}" -> deleteTask(exchange, path);
                default -> sendResponse(exchange, 404);
            }
        } catch (Exception e) {
            sendResponse(exchange, e.getMessage(), 500);
        }
    }

    private Optional<Integer> getTaskId(String path) {
        String[] parts = path.split("/");
        return parts.length > 1 ? Optional.of(Integer.parseInt(parts[1])) : Optional.empty();
    }

    private void getTasks(HttpExchange exchange) throws IOException {
        sendResponse(exchange, gson.toJson(taskManager.getTasks()), 200);
    }

    private void getTaskById(HttpExchange exchange, String path) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(path);
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
            }
            sendResponse(exchange, 201);
        } catch (JsonIOException | JsonSyntaxException e) {
            sendResponse(exchange, 400);
        }
    }

    private void deleteTask(HttpExchange exchange, String path) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(path);
        if (taskIdOpt.isEmpty()) {
            sendResponse(exchange, 404);
            return;
        }
        int taskId = taskIdOpt.get();
        taskManager.removeTask(taskId);
        sendResponse(exchange, 200);
    }

//    private Endpoint getEndpoint(String requestPath, String requestMethod) {
//        final String lastLocation = requestPath.substring(requestPath.lastIndexOf('/'));
//        switch (requestMethod + lastLocation) {
//            case "GET/posts":
//                return Endpoint.GET_POSTS;
//            case "GET/comments":
//                return Endpoint.GET_COMMENTS;
//            case "POST/comments":
//                return Endpoint.POST_COMMENT;
//            default:
//                return Endpoint.UNKNOWN;
//        }
//    }

//    private enum Endpoint {GET_TASKS, GET_TASK_BY_ID, POST_TASK, DELETE_TASK, UNKNOWN}
}
