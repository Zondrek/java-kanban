package server.handler;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import manager.task.TaskManager;
import model.Epic;
import model.SubTask;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Optional;

public class EpicHandler extends BaseHttpHandler {

    public EpicHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");
            switch (getEndpoint(method, pathParts)) {
                case GET_EPICS -> getEpics(exchange);
                case GET_EPIC_BY_ID -> getEpicById(exchange, pathParts[2]);
                case GET_SUBTASKS_BY_EPIC_ID -> getSubTasksByEpicId(exchange, pathParts[2]);
                case POST_EPIC -> postEpic(exchange);
                case DELETE_EPIC -> deleteEpic(exchange, pathParts[2]);
                default -> sendResponse(exchange, 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, e.getMessage(), 500);
        }
    }

    private Optional<Integer> getEpicId(String id) {
        return Optional.of(Integer.parseInt(id));
    }

    private void getEpics(HttpExchange exchange) throws IOException {
        sendResponse(exchange, gson.toJson(taskManager.getEpics()), 200);
    }

    private void getEpicById(HttpExchange exchange, String id) throws IOException {
        Optional<Integer> epicIdOpt = getEpicId(id);
        if (epicIdOpt.isEmpty()) {
            sendResponse(exchange, 404);
            return;
        }
        int epicId = epicIdOpt.get();

        Optional<Epic> epicOpt = Optional.ofNullable(taskManager.getEpic(epicId));
        if (epicOpt.isEmpty()) {
            sendResponse(exchange, 404);
            return;
        }

        Epic epic = epicOpt.get();
        sendResponse(exchange, gson.toJson(epic), 200);
    }

    private void getSubTasksByEpicId(HttpExchange exchange, String id) throws IOException {
        Optional<Integer> epicIdOpt = getEpicId(id);
        if (epicIdOpt.isEmpty()) {
            sendResponse(exchange, 404);
            return;
        }
        int epicId = epicIdOpt.get();
        Collection<SubTask> subTasks = taskManager.getSubTasks(epicId);
        sendResponse(exchange, gson.toJson(subTasks), 200);
    }

    private void postEpic(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
        try {
            Epic epicFromRequest = gson.fromJson(JsonParser.parseReader(isr), Epic.class);
            Epic epic = taskManager.upsertEpic(epicFromRequest);
            if (epic == null) {
                sendResponse(exchange, 406);
                return;
            }
            sendResponse(exchange, 201);
        } catch (JsonIOException | JsonSyntaxException e) {
            sendResponse(exchange, 400);
        }
    }

    private void deleteEpic(HttpExchange exchange, String id) throws IOException {
        Optional<Integer> epicIdOpt = getEpicId(id);
        if (epicIdOpt.isEmpty()) {
            sendResponse(exchange, 404);
            return;
        }
        int epicId = epicIdOpt.get();
        taskManager.removeEpic(epicId);
        sendResponse(exchange, 200);
    }

    private Endpoint getEndpoint(String method, String[] pathParts) {
        if (pathParts.length == 2) {
            switch (method) {
                case "GET" -> {
                    return Endpoint.GET_EPICS;
                }
                case "POST" -> {
                    return Endpoint.POST_EPIC;
                }
            }
        } else if (pathParts.length == 3) {
            switch (method) {
                case "GET" -> {
                    return Endpoint.GET_EPIC_BY_ID;
                }
                case "DELETE" -> {
                    return Endpoint.DELETE_EPIC;
                }
            }
        } else if (pathParts.length == 4 && pathParts[3].equals("subtasks")) {
            return Endpoint.GET_SUBTASKS_BY_EPIC_ID;
        }
        return Endpoint.UNKNOWN;
    }

    private enum Endpoint {
        GET_EPICS, GET_EPIC_BY_ID, GET_SUBTASKS_BY_EPIC_ID, POST_EPIC, DELETE_EPIC, UNKNOWN
    }
}
