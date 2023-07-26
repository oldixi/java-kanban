package service;
import com.google.gson.*;
import model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HttpTaskServer {
    TaskManager taskManager;
    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final HttpServer httpServer;

    public HttpTaskServer()  throws IOException {
        taskManager = new FileBackedTasksManager(FileBackedTasksManager.FILEPATH);
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks/", new TasksHandler());
        startHttpTaskServer();
    }

    public void startHttpTaskServer() {
        httpServer.start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту.");
    }

    public void stopHttpTaskServer() {
        httpServer.stop(1);
    }

    private void writeResponse(HttpExchange exchange,
                               String responseString,
                               int responseCode) throws IOException {
        if (responseString.isBlank()) {
            exchange.sendResponseHeaders(responseCode, 0);
        } else {
            byte[] bytes = responseString.getBytes(DEFAULT_CHARSET);
            exchange.sendResponseHeaders(responseCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
        exchange.close();
    }

    private Optional<Integer> getId(String idString) {
        try {
            return Optional.of(Integer.parseInt(idString));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    public class TasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Gson gson = new Gson();
            int responseCode = 200;
            RequestMethod requestMethod = RequestMethod.valueOf(exchange.getRequestMethod());
            String[] endPointSpecialArray = exchange.getRequestURI().getPath().split("/");
            int endPointSpecialArraySize = endPointSpecialArray.length;
            String endPointSpecial2 = "";
            String endPointSpecial3 = "";
            if (endPointSpecialArraySize >= 3) {
                endPointSpecial2 = exchange.getRequestURI().getPath().split("/")[2];
            }
            if (endPointSpecialArraySize >= 4) {
                endPointSpecial3 = exchange.getRequestURI().getPath().split("/")[3];
            }
            String query = exchange.getRequestURI().getRawQuery();
            String endPointPar = "";
            if (query != null && query.contains("id")) {
                endPointPar = query.substring(query.indexOf("id=") + 3);
            }
            int id = 0;
            String responseStr = "Операция не определена";

            if (!endPointPar.isBlank()) {
                Optional<Integer> idOpt = getId(endPointPar);
                if (idOpt.isEmpty()) {
                    writeResponse(exchange, "Некорректный идентификатор задачи", 400);
                    return;
                }
                id = idOpt.get();
            }
            switch (requestMethod) {
                case GET:
                    try {
                        if (endPointSpecial2.isBlank()) {
                            writeResponse(exchange, gson.toJson(taskManager.getPrioritizedTasks()), responseCode);
                        } else if (endPointSpecial2.equals("history")) {
                            writeResponse(exchange, gson.toJson(taskManager.getHistoryManager().getHistory()), responseCode);
                        } else if (endPointSpecial2.equals("subtask") && endPointSpecial3.equals("epic") && id != 0) {
                            writeResponse(exchange, gson.toJson(taskManager.getEpicSubtasks(id)), responseCode);
                        }else if (endPointSpecial2.equals("task") && id != 0) {
                            writeResponse(exchange, gson.toJson(taskManager.getTask(id)), responseCode);
                        } else if (endPointSpecial2.equals("subtask") && id != 0) {
                            writeResponse(exchange, gson.toJson(taskManager.getSubtask(id)), responseCode);
                        } else if (endPointSpecial2.equals("epic") && id != 0) {
                            writeResponse(exchange, gson.toJson(taskManager.getEpic(id)), responseCode);
                        } else if (endPointSpecial2.equals("task")) {
                            writeResponse(exchange, gson.toJson(taskManager.getTasks()), responseCode);
                        } else if (endPointSpecial2.equals("subtask")) {
                            writeResponse(exchange, gson.toJson(taskManager.getSubtasks()), responseCode);
                        } else if (endPointSpecial2.equals("epic")) {
                            writeResponse(exchange, gson.toJson(taskManager.getEpics()), responseCode);
                        }
                    } catch (JsonSyntaxException jse) {
                        responseCode = 400;
                        writeResponse(exchange, "При запросе информации по задаче получен некорректный JSON"
                                , responseCode);
                        return;
                    }
                    break;
                case DELETE:
                    if (endPointSpecial2.equals("task") && id != 0) {
                        taskManager.deleteTask(id);
                        responseStr = "Задача №" + id + " удалена";
                    } else if (endPointSpecial2.equals("subtask") && id != 0) {
                        taskManager.deleteSubtask(id);
                        responseStr = "Подзадача №" + id + " удалена";
                    } else if (endPointSpecial2.equals("epic") && id != 0) {
                        taskManager.deleteEpic(id);
                        responseStr = "Эпик-задача №" + id + " удалена";
                    } else if (endPointSpecial2.equals("task")) {
                        taskManager.deleteTasks();
                        responseStr = "Задачи удалены";
                    } else if (endPointSpecial2.equals("subtask")) {
                        taskManager.deleteSubtasks();
                        responseStr = "Подзадачи удалены";
                    } else if (endPointSpecial2.equals("epic")) {
                        taskManager.deleteEpics();
                        responseStr = "Эпик-задачи удалены";
                    } else {
                        responseStr = "Некорректно заданы параметры добавления или изменения задачи";
                        responseCode = 404;
                    }
                    writeResponse(exchange, responseStr, responseCode);
                    break;
                case POST:
                    int taskId;
                    InputStream inputStream = exchange.getRequestBody();
                    String taskJson = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                    if (endPointSpecial2.equals("task")) {
                        Task task = gson.fromJson(taskJson, Task.class);
                        if (taskManager.getTask(task.getId()) != null) {
                            taskManager.updateTask(task);
                            responseStr = "Изменена задача №" + task.getId();
                        } else {
                            taskId = taskManager.addNewTask(task);
                            responseStr = "Добавлена задача №" + taskId;
                        }
                    } else if (endPointSpecial2.equals("subtask")) {
                        Subtask task = gson.fromJson(taskJson, Subtask.class);
                        if (taskManager.getSubtask(task.getId()) != null) {
                            taskManager.updateSubtask(task);
                            responseStr = "Изменена подзадача №" + task.getId();
                        } else {
                            taskId = taskManager.addNewSubtask(task);
                            responseStr = "Добавлена подзадача №" + taskId;
                        }
                    } else if (endPointSpecial2.equals("epic")) {
                        Epic task = gson.fromJson(taskJson, Epic.class);
                        if (taskManager.getEpic(task.getId()) != null) {
                            taskManager.updateEpic(task);
                            responseStr = "Изменена эпик-задача №" + task.getId();
                        } else {
                            taskId = taskManager.addNewEpic(task);
                            responseStr = "Добавлена эпик-задача №" + taskId;
                        }
                    } else {
                        responseStr = "Некорректно заданы параметры добавления или изменения задачи";
                        responseCode = 404;
                    }
                    writeResponse(exchange, responseStr, responseCode);
                    break;
                default:
                    responseCode = 404;
                    writeResponse(exchange, responseStr, responseCode);
            }
        }
    }

    public enum RequestMethod {GET, POST, DELETE};
}
