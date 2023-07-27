package service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.*;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HttpTaskManager extends FileBackedTasksManager {
    public static final String URL = "http://localhost:" + KVServer.PORT + "/register";
    private KVTaskClient client;
    private static List<String> keyList;

    public HttpTaskManager(String url) {
        super(Path.of(url));
        keyList = new ArrayList<>();
        try {
            client = new KVTaskClient(URI.create(url));
        } catch (InterruptedException | IOException exception) {
            System.out.println("Ошибка коннекта к серверу.\n" +
                    "Проверьте, пожалуйста, адрес сервера и повторите попытку.");

        }
    }

    private void loadTaskFromServer(HttpTaskManager httpTaskManager, JsonObject jsonObj) {
        Task task;
        if (!jsonObj.isJsonNull()) {
            int id = jsonObj.get("id").getAsInt();
            task = new Task(jsonObj.get("name").getAsString()
                    , jsonObj.get("dsc").getAsString()
                    , TaskStatus.valueOf(jsonObj.get("taskStatus").getAsString())
                    , jsonObj.get("duration").getAsInt()
                    , LocalDateTime.parse(jsonObj.get("startTime").getAsString(), Task.FORMATTER));
            task.setId(id);
            addNewTask(task);
        }
    }

    private void loadSubtaskFromServer(HttpTaskManager httpTaskManager, JsonObject jsonObj) {
        Subtask task;
        if (!jsonObj.isJsonNull()) {
            int id = jsonObj.get("id").getAsInt();
            task = new Subtask(jsonObj.get("name").getAsString()
                    , jsonObj.get("dsc").getAsString()
                    , TaskStatus.valueOf(jsonObj.get("taskStatus").getAsString())
                    , jsonObj.get("epicId").getAsInt());
            task.setId(id);
            task.setDuration(jsonObj.get("duration").getAsInt());
            task.setStartTime(LocalDateTime.parse(jsonObj.get("startTime").getAsString(), Task.FORMATTER));
            addNewSubtask(task);
        }
    }

    private void loadEpicFromServer(HttpTaskManager httpTaskManager, JsonObject jsonObj) {
        Epic task;
        if (!jsonObj.isJsonNull()) {
            int id = jsonObj.get("id").getAsInt();
            task = new Epic(jsonObj.get("name").getAsString(), jsonObj.get("dsc").getAsString());
            task.setStatus(TaskStatus.valueOf(jsonObj.get("taskStatus").getAsString()));
            task.setDuration(jsonObj.get("duration").getAsInt());
            task.setStartTime(LocalDateTime.parse(jsonObj.get("startTime").getAsString(), Task.FORMATTER));
            task.setId(id);
            addNewEpic(task);
        }
    }

    public HttpTaskManager loadFromServer() {
        HttpTaskManager httpTaskManager = new HttpTaskManager(URL);
        keyList.stream().filter(keyStringId -> !keyStringId.equals("0")).forEach(keyStringId -> {
            try {
                JsonElement jsonElement = JsonParser.parseString(client.load(keyStringId));
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    if (keyStringId.contains("TASK")) {
                        loadTaskFromServer(httpTaskManager, jsonObject);
                    } else if (keyStringId.contains("EPIC")) {
                        loadEpicFromServer(httpTaskManager, jsonObject);
                    } else if (keyStringId.contains("SUBTASK")) {
                        loadSubtaskFromServer(httpTaskManager, jsonObject);
                    }
                }
            }  catch (InterruptedException | IOException exception) {
                System.out.println("Во время выполнения запроса на сохранение задач возникла ошибка.\n" +
                        "Проверьте, пожалуйста, адрес сервера и повторите попытку.");
            }
        });
        try {
            JsonElement jsonHistoryList = JsonParser.parseString(client.load("0"));
            if (jsonHistoryList.isJsonArray()) {
                for (int i = 0; i < jsonHistoryList.getAsJsonArray().size(); i++) {
                    JsonObject jsonObject = jsonHistoryList.getAsJsonArray().get(i).getAsJsonObject();
                    int taskIdFromHistory = jsonObject.get("id").getAsInt();
                    Task taskFromHistory = new Task();
                    if (httpTaskManager.getEpics().contains(httpTaskManager.getEpic(taskIdFromHistory))) {
                        taskFromHistory = httpTaskManager.getEpic(taskIdFromHistory);
                    } else if (httpTaskManager.getSubtasks().contains(httpTaskManager.getSubtask(taskIdFromHistory))) {
                        taskFromHistory = httpTaskManager.getSubtask(taskIdFromHistory);
                    } else if (httpTaskManager.getTasks().contains(httpTaskManager.getTask(taskIdFromHistory))) {
                        taskFromHistory = httpTaskManager.getTask(taskIdFromHistory);
                    }
                    if (taskFromHistory.getId() > 0) {
                        httpTaskManager.getHistoryManager().add(taskFromHistory);
                    }
                }
            }
        }  catch (InterruptedException | IOException exception) {
            System.out.println("Во время выполнения запроса на сохранение задач возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес сервера и повторите попытку.");
        }
        return httpTaskManager;
    }

    private void putJsonTasks(Task task) {
        Gson gson = new Gson();
        String className = task.getClass().toString().toUpperCase()
                .replace("CLASS","").replace(" MODEL.", "");
        try {
            String jsonTasks = gson.toJson(task);
            client.put(className + task.getId(), jsonTasks);
            keyList.add(String.valueOf(task.getId()));
        } catch (InterruptedException | IOException exception) {
            System.out.println("Во время выполнения запроса на сохранение задач возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес сервера и повторите попытку.");
        }
    }

    @Override
    protected void save() {
        Gson gson = new Gson();
        super.getTasks().forEach(this::putJsonTasks);
        super.getEpics().forEach(this::putJsonTasks);
        super.getSubtasks().forEach(this::putJsonTasks);
        try {
            String jsonHistoryList = gson.toJson(super.getHistoryManager().getHistory());
            if (!jsonHistoryList.isBlank()) {
                client.put("0", jsonHistoryList);
                keyList.add("0");
            }
        } catch (InterruptedException | IOException exception) {
            System.out.println("Во время выполнения запроса на сохранение задач возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес сервера и повторите попытку.");
        }
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        int subtaskId = super.addNewSubtask(subtask);
        save();
        return subtaskId;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int epicId = super.addNewEpic(epic);
        save();
        return epicId;
    }

    @Override
    public int addNewTask(Task task) {
        int taskId = super.addNewTask(task);
        save();
        return taskId;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int taskId) {
        super.deleteTask(taskId);
        save();
    }

    @Override
    public void deleteEpic(int epicId) {
        super.deleteEpic(epicId);
        save();
    }

    @Override
    public void deleteSubtask(int subtaskId) {
        super.deleteSubtask(subtaskId);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public Task getTask(int taskId) {
        Task task = super.getTask(taskId);
        save();
        return task;
    }

    @Override
    public Subtask getSubtask(int subtaskId) {
        Subtask subtask = super.getSubtask(subtaskId);
        save();
        return subtask;
    }

    @Override
    public Epic getEpic(int epicId) {
        Epic epic = super.getEpic(epicId);
        save();
        return epic;
    }
}
