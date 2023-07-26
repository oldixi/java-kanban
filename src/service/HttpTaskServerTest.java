package service;

import com.google.gson.*;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskServerTest<T extends TaskManager> {
    private static final int PORT = 8080;
    private static final String ENDPOINT_URL_COMMON = "http://localhost:" + PORT + "/tasks/";

    static HttpTaskServer httpTaskServer;
    static HttpClient client;

    Task task1 = new Task("Задача1", "Выполнить задачу 1", TaskStatus.NEW, 10, LocalDateTime.now().minusDays(60));
    Task task2 = new Task("Задача2", "Выполнить задачу 2");

    Subtask subtask1 = new Subtask("Подзадача1", "Выполнить подзадачу 1");
    Subtask subtask2 = new Subtask("Подзадача2", "Выполнить подзадачу 2");
    ArrayList<Subtask> subtasks = new ArrayList<>(List.of(subtask1, subtask2));
    Epic epic1 = new Epic("Эпик1", "Выполнить Эпик1", subtasks);

    Epic epic2 = new Epic("Эпик2", "Выполнить Эпик2");

    @BeforeAll
    public static void start() {
        try {
            httpTaskServer = new HttpTaskServer();
        } catch (IOException exception) {
            System.out.println("HTTP-сервер на порту " + PORT + " запустить не удалось.");
        }
        client = HttpClient.newHttpClient();
    }

    private HttpResponse<String> getHttpTaskClientResponse(
            String endPointSpacial
            , HttpTaskServer.RequestMethod requestMethod
            , Integer id
            , Task task) throws IOException, InterruptedException {
        String urlStr = ENDPOINT_URL_COMMON;
        if (endPointSpacial != null && !endPointSpacial.isBlank()) {
            urlStr += endPointSpacial;
        }
        if (id != null) {
            urlStr = urlStr + "?id=" + id;
        }
        URI url = URI.create(urlStr);

        HttpRequest request;
        switch (requestMethod) {
            case GET:
                request = HttpRequest.newBuilder().uri(url).GET().build();
                break;
            case POST:
                Gson gson = new Gson();
                String json = gson.toJson(task);
                final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
                request = HttpRequest.newBuilder().uri(url).POST(body).build();
                break;
            case DELETE:
                request = HttpRequest.newBuilder().uri(url).DELETE().build();
                break;
            default:
                request = HttpRequest.newBuilder().uri(url).build();
        }
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    public void shouldAddNewTask() {
        HttpResponse<String> responsePostTask1 = null;
        try {
            responsePostTask1 = getHttpTaskClientResponse("task/", HttpTaskServer.RequestMethod.POST, null, task1);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responsePostTask1, "Пустое тело ответа от сервера при создании задачи.");
        assertEquals(200, responsePostTask1.statusCode(), "Ошибка при создании задачи.");
        assertNotNull(responsePostTask1.body(), "API вернуло неверный результат добавления задачи.");
    }

    @Test
    public void shouldGetTasks() {
        HttpResponse<String> responseGetTasks = null;
        try {
            responseGetTasks = getHttpTaskClientResponse("task/", HttpTaskServer.RequestMethod.GET, null, null);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responseGetTasks, "Пустое тело ответа от сервера при просмотре задач.");
        assertEquals(200, responseGetTasks.statusCode(), "Ошибка при просмотре задач.");
        assertTrue(JsonParser.parseString(responseGetTasks.body()).isJsonArray()
                , "API вернуло неверный результат просмотра задач.");
    }

    @Test
    public void shouldGetTask() {
        HttpResponse<String> responseGetTask1 = null;
        try {
            HttpResponse<String> responsePostTask1 = getHttpTaskClientResponse("task/", HttpTaskServer.RequestMethod.POST, null, task1);
            responseGetTask1 = getHttpTaskClientResponse("task/", HttpTaskServer.RequestMethod.GET, Integer.valueOf(responsePostTask1.body().split("№")[1]), null);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responseGetTask1, "Пустое тело ответа от сервера при посмотре задачи.");
        assertEquals(responseGetTask1.statusCode(), 200, "Ошибка при присмотре задачи.");
        assertTrue(JsonParser.parseString(responseGetTask1.body()).isJsonObject(),
                "API вернуло неверный результат просмотра задачи.");
    }

    @Test
    public void shouldUpdateTask() {
        HttpResponse<String> responsePostNewVerTask1 = null;
        try {
            HttpResponse<String> responseAddTask = getHttpTaskClientResponse("task/", HttpTaskServer.RequestMethod.POST, null, task1);
            task1.setDuration(15);
            responsePostNewVerTask1 = getHttpTaskClientResponse("task/"
                    , HttpTaskServer.RequestMethod.POST, null, task1);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responsePostNewVerTask1, "Пустое тело ответа от сервера при изменении задачи.");
        assertEquals(200, responsePostNewVerTask1.statusCode(), "Ошибка при изменении задачи.");
        assertNotNull(responsePostNewVerTask1.body(),
                "API вернуло неверный результат при изменении задачи.");
    }

    @Test
    public void shouldDeleteTask() {
        HttpResponse<String> responseDeleteTask = null;
        try {
            HttpResponse<String> responseAddTask = getHttpTaskClientResponse("task/", HttpTaskServer.RequestMethod.POST, null, task1);
            responseDeleteTask = getHttpTaskClientResponse("task/"
                    , HttpTaskServer.RequestMethod.DELETE
                    , Integer.valueOf(responseAddTask.body().split("№")[1]), null);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responseDeleteTask, "Пустое тело ответа от сервера при удалении задачи.");
        assertEquals(200, responseDeleteTask.statusCode(), "Ошибка при удалении задачи.");
        assertNotNull(responseDeleteTask.body()
                , "API вернуло неверный результат удаления задачи.");
    }

    @Test
    public void shouldDeleteTasks() {
        HttpResponse<String> responseDeleteTasks = null;
        try {
            getHttpTaskClientResponse("task/", HttpTaskServer.RequestMethod.POST, null, task1);
            getHttpTaskClientResponse("task/", HttpTaskServer.RequestMethod.POST, null, task2);
            responseDeleteTasks = getHttpTaskClientResponse("task/", HttpTaskServer.RequestMethod.DELETE, null, null);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responseDeleteTasks, "Пустое тело ответа от сервера при создании задачи");
        assertEquals(200, responseDeleteTasks.statusCode(), "Ошибка при создании задачи");
        assertNotNull(responseDeleteTasks.body()
                , "API вернуло неверный результат просмотра задач.");
    }

    @Test
    public void shouldAddNewSubtask() {
        HttpResponse<String> responsePostSubtask1 = null;
        try {
            HttpResponse<String> responsePostEpic1 = getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.POST, null, epic1);
            subtask1.setEpicId(Integer.valueOf(responsePostEpic1.body().split("№")[1]));
            responsePostSubtask1 = getHttpTaskClientResponse("subtask/", HttpTaskServer.RequestMethod.POST, null, subtask1);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responsePostSubtask1, "Пустое тело ответа от сервера при создании подзадачи.");
        assertEquals(200, responsePostSubtask1.statusCode(), "Ошибка при создании подзадачи.");
        assertNotNull(responsePostSubtask1.body(), "API вернуло неверный результат добавления подзадачи.");
    }

    @Test
    public void shouldGetSubtasks() {
        HttpResponse<String> responseGetSubtasks = null;
        try {
            HttpResponse<String> responsePostEpic1 = getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.POST, null, epic1);
            subtask1.setEpicId(Integer.valueOf(responsePostEpic1.body().split("№")[1]));
            subtask2.setEpicId(Integer.valueOf(responsePostEpic1.body().split("№")[1]));
            getHttpTaskClientResponse("subtask/", HttpTaskServer.RequestMethod.POST, null, subtask1);
            getHttpTaskClientResponse("subtask/", HttpTaskServer.RequestMethod.POST, null, subtask2);
            responseGetSubtasks = getHttpTaskClientResponse("subtask/" +
                    "", HttpTaskServer.RequestMethod.GET, null, null);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responseGetSubtasks, "Пустое тело ответа от сервера при просмотре подзадач.");
        assertEquals(200, responseGetSubtasks.statusCode(), "Ошибка при просмотре подзадач.");
        assertTrue(JsonParser.parseString(responseGetSubtasks.body()).isJsonArray()
                , "API вернуло неверный результат просмотра подзадач.");
    }

    @Test
    public void shouldGetSubtask() {
        HttpResponse<String> responseGetSubtask1 = null;
        try {
            HttpResponse<String> responsePostEpic1 = getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.POST, null, epic1);
            subtask1.setEpicId(Integer.valueOf(responsePostEpic1.body().split("№")[1]));
            HttpResponse<String> responsePostSubtask1 = getHttpTaskClientResponse("subtask/", HttpTaskServer.RequestMethod.POST, null, subtask1);
            responseGetSubtask1 = getHttpTaskClientResponse("subtask/", HttpTaskServer.RequestMethod.GET, Integer.valueOf(responsePostSubtask1.body().split("№")[1]), null);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        assertNotNull(responseGetSubtask1, "Пустое тело ответа от сервера при посмотре подзадачи.");
        assertEquals(200, responseGetSubtask1.statusCode(), "Ошибка при присмотре подзадачи.");
        assertTrue(JsonParser.parseString(responseGetSubtask1.body()).isJsonObject(),
                "API вернуло неверный результат просмотра подзадачи.");
    }

    @Test
    public void shouldGetSubtasksByEpic() {
        HttpResponse<String> responseGetSubtasksByEpic = null;
        try {
            HttpResponse<String> responsePostEpic1 = getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.POST, null, epic1);
            subtask1.setEpicId(Integer.valueOf(responsePostEpic1.body().split("№")[1]));
            subtask2.setEpicId(Integer.valueOf(responsePostEpic1.body().split("№")[1]));
            getHttpTaskClientResponse("subtask/", HttpTaskServer.RequestMethod.POST, null, subtask1);
            getHttpTaskClientResponse("subtask/", HttpTaskServer.RequestMethod.POST, null, subtask2);
            responseGetSubtasksByEpic = getHttpTaskClientResponse("subtask/epic/", HttpTaskServer.RequestMethod.GET, Integer.valueOf(responsePostEpic1.body().split("№")[1]), null);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responseGetSubtasksByEpic, "Пустое тело ответа от сервера при посмотре подзадачи.");
        assertEquals(200, responseGetSubtasksByEpic.statusCode(), "Ошибка при присмотре подзадачи.");
        assertTrue(JsonParser.parseString(responseGetSubtasksByEpic.body()).isJsonArray(),
                "API вернуло неверный результат просмотра подзадачи.");
    }

    @Test
    public void shouldUpdateSubtask() {
        HttpResponse<String> responsePostNewVerSubtask1 = null;
        try {
            HttpResponse<String> responsePostEpic1 = getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.POST, null, epic1);
            subtask1.setEpicId(Integer.valueOf(responsePostEpic1.body().split("№")[1]));
            getHttpTaskClientResponse("subtask/", HttpTaskServer.RequestMethod.POST, null, subtask1);
            subtask1.setDsc("For POST");
            responsePostNewVerSubtask1 = getHttpTaskClientResponse("subtask/"
                    , HttpTaskServer.RequestMethod.POST, null, subtask1);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responsePostNewVerSubtask1, "Пустое тело ответа от сервера при изменении задачи.");
        assertEquals(200, responsePostNewVerSubtask1.statusCode(), "Ошибка при изменении задачи.");
        assertNotNull(responsePostNewVerSubtask1.body(),
                "API вернуло неверный результат при изменении задачи.");
    }

    @Test
    public void shouldDeleteSubtask() {
        HttpResponse<String> responseDeleteSubtask = null;
        try {
            HttpResponse<String> responsePostEpic1 = getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.POST, null, epic1);
            subtask1.setEpicId(Integer.valueOf(responsePostEpic1.body().split("№")[1]));
            HttpResponse<String> responseAddSubtask = getHttpTaskClientResponse("subtask/", HttpTaskServer.RequestMethod.POST, null, subtask1);
            responseDeleteSubtask = getHttpTaskClientResponse("subtask/", HttpTaskServer.RequestMethod.DELETE, Integer.valueOf(responseAddSubtask.body().split("№")[1]), null);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responseDeleteSubtask, "Пустое тело ответа от сервера при удалении задачи.");
        assertEquals(200, responseDeleteSubtask.statusCode(), "Ошибка при удалении задачи.");
        assertNotNull(responseDeleteSubtask.body()
                , "API вернуло неверный результат удаления задачи.");
    }

    @Test
    public void shouldDeleteSubtasks() {
        HttpResponse<String> responseDeleteSubtasks = null;
        try {
            HttpResponse<String> responsePostEpic1 = getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.POST, null, epic1);
            subtask1.setEpicId(Integer.valueOf(responsePostEpic1.body().split("№")[1]));
            subtask2.setEpicId(Integer.valueOf(responsePostEpic1.body().split("№")[1]));
            getHttpTaskClientResponse("subtask/", HttpTaskServer.RequestMethod.POST, null, subtask1);
            getHttpTaskClientResponse("subtask/", HttpTaskServer.RequestMethod.POST, null, subtask2);
            responseDeleteSubtasks = getHttpTaskClientResponse("subtask/", HttpTaskServer.RequestMethod.DELETE, null, null);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responseDeleteSubtasks, "Пустое тело ответа от сервера при удалении подзадач.");
        assertEquals(200, responseDeleteSubtasks.statusCode(), "Ошибка при удалении подзадач.");
        assertNotNull(responseDeleteSubtasks.body()
                , "API вернуло неверный результат удаления подзадач.");
    }

    @Test
    public void shouldAddNewEpic() {
        HttpResponse<String> responsePostEpic1 = null;
        try {
            responsePostEpic1 = getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.POST, null, epic1);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responsePostEpic1, "Пустое тело ответа от сервера при создании эпик-задачи.");
        assertEquals(200, responsePostEpic1.statusCode(), "Ошибка при создании эпик-задачи.");
        assertNotNull(responsePostEpic1.body(), "API вернуло неверный результат добавления эпик-задачи.");
    }

    @Test
    public void shouldGetEpics() {
        HttpResponse<String> responseGetEpics = null;
        try {
            getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.POST, null, epic1);
            getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.POST, null, epic2);
            responseGetEpics = getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.GET, null, null);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responseGetEpics, "Пустое тело ответа от сервера при просмотре эпик-задач.");
        assertEquals(200, responseGetEpics.statusCode(), "Ошибка при просмотре эпик-задач.");
        assertTrue(JsonParser.parseString(responseGetEpics.body()).isJsonArray()
                , "API вернуло неверный результат просмотра эпик-задач.");
    }

    @Test
    public void shouldGetEpic() {
        HttpResponse<String> responseGetEpic1 = null;
        try {
            HttpResponse<String> responsePostEpic1 = getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.POST, null, epic1);
            responseGetEpic1 = getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.GET, Integer.valueOf(responsePostEpic1.body().split("№")[1]), null);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responseGetEpic1, "Пустое тело ответа от сервера при посмотре эпик-задачи.");
        assertEquals(200, responseGetEpic1.statusCode(), "Ошибка при присмотре эпик-задачи.");
        assertTrue(JsonParser.parseString(responseGetEpic1.body()).isJsonObject(),
                "API вернуло неверный результат просмотра эпик-задачи.");
    }

    @Test
    public void shouldUpdateEpic() {
        HttpResponse<String> responsePostNewVerEpic1 = null;
        try {
            getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.POST, null, epic1);
            epic1.setDsc("For POST");
            responsePostNewVerEpic1 = getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.POST, null, epic1);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responsePostNewVerEpic1, "Пустое тело ответа от сервера при изменении эпик-задачи.");
        assertEquals(200, responsePostNewVerEpic1.statusCode(), "Ошибка при изменении эпик-задачи.");
        assertNotNull(responsePostNewVerEpic1.body(),
                "API вернуло неверный результат при изменении эпик-задачи.");
    }

    @Test
    public void shouldDeleteEpic() {
        HttpResponse<String> responseDeleteEpic1 = null;
        try {
            HttpResponse<String> responsePostEpic1 = getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.POST, null, epic1);
            responseDeleteEpic1 = getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.DELETE, Integer.valueOf(responsePostEpic1.body().split("№")[1]), null);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responseDeleteEpic1, "Пустое тело ответа от сервера при удалении эпик-задачи.");
        assertEquals(200, responseDeleteEpic1.statusCode(), "Ошибка при удалении эпик-задачи.");
        assertNotNull(responseDeleteEpic1.body()
                , "API вернуло неверный результат удаления эпик-задачи.");
    }

    @Test
    public void shouldDeleteEpics() {
        HttpResponse<String> responseDeleteEpics = null;
        try {
            getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.POST, null, epic1);
            getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.POST, null, epic2);
            responseDeleteEpics = getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.DELETE, null, null);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responseDeleteEpics, "Пустое тело ответа от сервера при удалении эпик-задач");
        assertEquals(200, responseDeleteEpics.statusCode(), "Ошибка при удалении эпик-задач");
        assertNotNull(responseDeleteEpics.body()
                , "API вернуло неверный результат удаления эпик-задач.");
    }

    @Test
    public void shouldGetHistory() {
        HttpResponse<String> responseGetHistory = null;
        try {
            HttpResponse<String> responsePostEpic1 = getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.POST, null, epic1);
            subtask1.setEpicId(Integer.valueOf(responsePostEpic1.body().split("№")[1]));
            subtask2.setEpicId(Integer.valueOf(responsePostEpic1.body().split("№")[1]));
            getHttpTaskClientResponse("subtask/", HttpTaskServer.RequestMethod.POST, null, subtask1);
            getHttpTaskClientResponse("subtask/", HttpTaskServer.RequestMethod.POST, null, subtask2);
            HttpResponse<String> responsePostTask1 = getHttpTaskClientResponse("task/", HttpTaskServer.RequestMethod.POST, null, task1);
            getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.GET, Integer.valueOf(responsePostEpic1.body().split("№")[1]), null);
            getHttpTaskClientResponse("task/", HttpTaskServer.RequestMethod.GET, Integer.valueOf(responsePostTask1.body().split("№")[1]), null);
            responseGetHistory = getHttpTaskClientResponse("history/", HttpTaskServer.RequestMethod.GET, null, null);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        assertNotNull(responseGetHistory, "Пустое тело ответа от сервера при посмотре подзадачи.");
        assertEquals(200, responseGetHistory.statusCode(), "Ошибка при присмотре подзадачи.");
        assertTrue(JsonParser.parseString(responseGetHistory.body()).isJsonArray(),
                "API вернуло неверный результат просмотра подзадачи.");
    }

    @Test
    public void shouldGetPrioritizedTasks() {
        HttpResponse<String> responseGetPrioritizedTasks = null;
        try {
            HttpResponse<String> responsePostEpic1 = getHttpTaskClientResponse("epic/", HttpTaskServer.RequestMethod.POST, null, epic1);
            subtask1.setEpicId(Integer.valueOf(responsePostEpic1.body().split("№")[1]));
            subtask2.setEpicId(Integer.valueOf(responsePostEpic1.body().split("№")[1]));
            getHttpTaskClientResponse("subtask/", HttpTaskServer.RequestMethod.POST, null, subtask1);
            getHttpTaskClientResponse("subtask/", HttpTaskServer.RequestMethod.POST, null, subtask2);
            getHttpTaskClientResponse("task/", HttpTaskServer.RequestMethod.POST, null, task1);
            responseGetPrioritizedTasks = getHttpTaskClientResponse("", HttpTaskServer.RequestMethod.GET, null, null);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertNotNull(responseGetPrioritizedTasks, "Пустое тело ответа от сервера при посмотре подзадачи.");
        assertEquals(200, responseGetPrioritizedTasks.statusCode(), "Ошибка при присмотре подзадачи.");
        assertTrue(JsonParser.parseString(responseGetPrioritizedTasks.body()).isJsonArray(),
                "API вернуло неверный результат просмотра подзадачи.");
    }
}