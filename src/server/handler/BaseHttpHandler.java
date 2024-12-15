package server.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.task.TaskManager;
import server.adapter.DateTimeAdapter;
import server.adapter.DurationAdapter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

abstract class BaseHttpHandler implements HttpHandler {

    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected static final String DEFAULT_CONTENT_TYPE = "application/json;charset=utf-8";

    protected final TaskManager taskManager;
    protected final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    protected void sendResponse(HttpExchange exchange, String text, int code) throws IOException {
        byte[] resp = text.getBytes(DEFAULT_CHARSET);
        exchange.getResponseHeaders().add("Content-Type", DEFAULT_CONTENT_TYPE);
        exchange.sendResponseHeaders(code, resp.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(resp);
        }
        exchange.close();
    }

    protected void sendResponse(HttpExchange exchange, int code) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", DEFAULT_CONTENT_TYPE);
        exchange.sendResponseHeaders(code, 0);
        exchange.close();
    }
}
